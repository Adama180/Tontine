package com.tontine.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.tontine.dao.AdhesionDao;
import com.tontine.dao.CycleDao;
import com.tontine.dao.TransactionDao;
import com.tontine.dao.UtilisateurDao;
import com.tontine.model.Adhesion;
import com.tontine.model.Cycle;
import com.tontine.model.Frequence;
import com.tontine.model.ModePaiement;
import com.tontine.model.StatutAdhesion;
import com.tontine.model.StatutCycle;
import com.tontine.model.StatutTransaction;
import com.tontine.model.Tontine;
import com.tontine.model.Transaction;
import com.tontine.model.TypeTransaction;
import com.tontine.model.Utilisateur;

/**
 * Cotisations (Wave, Orange Money, especes), confirmation des paiements en especes et historique.
 * Fonds commun : plusieurs cotisations possibles, chacune au moins egale au montant fixe.
 * Tour de role / tirage au sort : une seule cotisation, exactement du montant fixe, par cycle.
 */
public class CotisationService {

    private final AdhesionDao adhesionDao = new AdhesionDao();
    private final CycleDao cycleDao = new CycleDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    /**
     * Le membre cotise. Wave / Orange Money : confirme tout de suite (simulation).
     * Especes : reste "en attente" jusqu'a la confirmation du gerant.
     */
    public Transaction payerCotisation(int userId, int tontineId, double montant, ModePaiement mode,
            String numeroMobile) {
        Controles.utilisateur(userId);
        Tontine t = Controles.tontine(tontineId);
        Adhesion a = Controles.adhesionActive(tontineId, userId);
        Cycle c = Controles.cycleEnCours(t);
        Controles.exiger(mode != null, "Choisissez un mode de paiement.");
        controlerMontant(t, c, userId, montant);

        Transaction tx = nouvelleTransaction(t, c, userId, montant, mode);
        if (mode == ModePaiement.ESPECE) {
            tx.setStatut(StatutTransaction.EN_ATTENTE);
            tx.setReference("ESP-" + System.currentTimeMillis());
            transactionDao.insert(tx);
            NotificationService.notifier(t.getGerantId(), "Un paiement en especes de "
                    + Format.montant(montant) + " attend votre confirmation (tontine " + t.getNom() + ").",
                    "COTISATION");
        } else {
            tx.setReference(PasserelleSimulee.initierPaiement(mode, montant, numeroMobile));
            tx.setStatut(StatutTransaction.CONFIRMEE);
            transactionDao.insert(tx);
            appliquer(tx, t, c, a);
        }
        return tx;
    }

    /** Le gerant enregistre directement une cotisation recue en especes d'un membre. */
    public Transaction enregistrerCotisationEspece(int gerantId, int tontineId, int membreUserId, double montant) {
        Tontine t = Controles.tontine(tontineId);
        Controles.exigerGerant(t, gerantId);
        Controles.exiger(membreUserId != gerantId,
                "Votre propre paiement en especes doit etre confirme par l'administrateur : "
                        + "utilisez 'Payer ma cotisation' puis demandez-lui la confirmation.");
        Adhesion a = Controles.adhesionActive(tontineId, membreUserId);
        Cycle c = Controles.cycleEnCours(t);
        controlerMontant(t, c, membreUserId, montant);

        Transaction tx = nouvelleTransaction(t, c, membreUserId, montant, ModePaiement.ESPECE);
        tx.setReference("ESP-" + System.currentTimeMillis());
        tx.setStatut(StatutTransaction.CONFIRMEE);
        transactionDao.insert(tx);
        appliquer(tx, t, c, a);
        NotificationService.notifier(membreUserId, "Votre cotisation de " + Format.montant(montant)
                + " (especes) a ete enregistree pour la tontine " + t.getNom() + ".", "COTISATION");
        return tx;
    }

    /**
     * Confirme (ou refuse) un paiement en especes en attente : cotisation ou remboursement de pret.
     * Fait par le gerant (jamais pour son propre paiement) ou par l'administrateur.
     */
    public Transaction confirmerPaiementEspece(int acteurId, int transactionId, boolean accepter) {
        Transaction tx = transactionDao.findById(transactionId);
        Controles.exiger(tx != null, "Transaction introuvable.");
        Controles.exiger(tx.getStatut() == StatutTransaction.EN_ATTENTE && tx.getModePaiement() == ModePaiement.ESPECE,
                "Ce paiement n'est pas en attente de confirmation.");
        Tontine t = Controles.tontine(tx.getTontineId());
        Utilisateur acteur = Controles.utilisateur(acteurId);
        if (!acteur.isAdmin()) {
            Controles.exigerGerant(t, acteurId);
            Controles.exiger(tx.getUserId() != acteurId,
                    "Vous ne pouvez pas confirmer votre propre paiement : l'administrateur doit le faire.");
        }

        if (!accepter) {
            transactionDao.changerStatut(tx.getId(), StatutTransaction.ECHOUEE);
            tx.setStatut(StatutTransaction.ECHOUEE);
            NotificationService.notifier(tx.getUserId(), "Votre paiement en especes de "
                    + Format.montant(tx.getMontant()) + " n'a pas ete confirme (tontine " + t.getNom() + ").",
                    "COTISATION");
            return tx;
        }

        Cycle c = tx.getCycleId() == null ? null : cycleDao.findById(tx.getCycleId());
        Controles.exiger(c != null && c.getStatut() == StatutCycle.EN_COURS, "Le cycle de ce paiement est deja cloture.");
        transactionDao.changerStatut(tx.getId(), StatutTransaction.CONFIRMEE);
        tx.setStatut(StatutTransaction.CONFIRMEE);
        if (tx.getType() == TypeTransaction.COTISATION) {
            appliquer(tx, t, c, Controles.adhesionActive(t.getId(), tx.getUserId()));
        } else if (tx.getType() == TypeTransaction.REMBOURSEMENT) {
            new PretService().appliquerRemboursement(tx);
        }
        NotificationService.notifier(tx.getUserId(), "Votre paiement en especes de "
                + Format.montant(tx.getMontant()) + " a ete confirme (tontine " + t.getNom() + ").", "COTISATION");
        return tx;
    }

    /** Paiements en especes a confirmer dans une tontine (vue du gerant). */
    public List<Transaction> paiementsEnAttente(int gerantId, int tontineId) {
        Tontine t = Controles.tontine(tontineId);
        Controles.exigerGerant(t, gerantId);
        List<Transaction> liste = new ArrayList<>();
        for (Transaction tx : transactionDao.findByTontine(tontineId)) {
            if (tx.getStatut() == StatutTransaction.EN_ATTENTE) {
                liste.add(tx);
            }
        }
        return liste;
    }

    /** Historique personnel d'un membre dans une tontine. */
    public List<Transaction> historiquePersonnel(int userId, int tontineId) {
        Controles.adhesionActive(tontineId, userId);
        return transactionDao.findByTontineEtUser(tontineId, userId);
    }

    /** Historique complet de la tontine, en lecture seule (regle de transparence). */
    public List<Transaction> historiqueTontine(int userId, int tontineId) {
        Tontine t = Controles.tontine(tontineId);
        Controles.exigerParticipant(t, userId);
        return transactionDao.findByTontine(tontineId);
    }

    /** Membres qui n'ont pas encore cotise dans le cycle en cours. */
    public List<Utilisateur> membresSansCotisation(int tontineId) {
        Tontine t = Controles.tontine(tontineId);
        Cycle c = Controles.cycleEnCours(t);
        List<Utilisateur> liste = new ArrayList<>();
        for (Adhesion a : adhesionDao.findByTontine(tontineId)) {
            if (a.getStatut() == StatutAdhesion.ACTIF && cotisationDuCycle(c, a.getUserId()) <= 0) {
                liste.add(utilisateurDao.findById(a.getUserId()));
            }
        }
        return liste;
    }

    // ---------------------------------------------------------------- interne

    private void controlerMontant(Tontine t, Cycle c, int userId, double montant) {
        Controles.exiger(montant > 0, "Le montant doit etre positif.");
        if (t.isFondsCommun()) {
            Controles.exiger(montant + 0.005 >= t.getMontantCotisation(),
                    "Le montant minimum d'une cotisation est " + Format.montant(t.getMontantCotisation()) + ".");
        } else {
            Controles.exiger(Math.abs(montant - t.getMontantCotisation()) < 0.005,
                    "Le montant de la cotisation est fixe : " + Format.montant(t.getMontantCotisation()) + ".");
            Controles.exiger(cotisationDuCycle(c, userId) <= 0,
                    "Vous avez deja cotise (ou un paiement est en attente) pour ce cycle.");
        }
    }

    /** Somme des cotisations de l'utilisateur dans le cycle, hors paiements refuses. */
    private double cotisationDuCycle(Cycle c, int userId) {
        double somme = 0;
        for (Transaction tx : transactionDao.findByCycle(c.getId())) {
            if (tx.getUserId() == userId && tx.getType() == TypeTransaction.COTISATION
                    && tx.getStatut() != StatutTransaction.ECHOUEE) {
                somme += tx.getMontant();
            }
        }
        return somme;
    }

    private Transaction nouvelleTransaction(Tontine t, Cycle c, int userId, double montant, ModePaiement mode) {
        Transaction tx = new Transaction();
        tx.setTontineId(t.getId());
        tx.setUserId(userId);
        tx.setCycleId(c.getId());
        tx.setType(TypeTransaction.COTISATION);
        tx.setMontant(Controles.arrondir(montant));
        tx.setModePaiement(mode);
        return tx;
    }

    /** Une cotisation confirmee : met a jour le solde et applique la penalite si elle est en retard. */
    private void appliquer(Transaction tx, Tontine t, Cycle c, Adhesion a) {
        adhesionDao.enregistrerCotisation(a.getId(), tx.getMontant());
        if (!t.isFondsCommun() && t.getPenaliteRetard() > 0
                && tx.getDate().toLocalDate().isAfter(echeance(c, t.getFrequence()))) {
            Transaction p = new Transaction();
            p.setTontineId(t.getId());
            p.setUserId(tx.getUserId());
            p.setCycleId(c.getId());
            p.setType(TypeTransaction.PENALITE);
            p.setMontant(t.getPenaliteRetard());
            p.setModePaiement(tx.getModePaiement());
            p.setStatut(StatutTransaction.CONFIRMEE);
            p.setReference("PEN-" + tx.getId());
            transactionDao.insert(p);
            NotificationService.notifier(tx.getUserId(), "Penalite de retard de "
                    + Format.montant(t.getPenaliteRetard()) + " appliquee (tontine " + t.getNom() + ").", "PENALITE");
        }
    }

    /** Date limite de cotisation d'un cycle (tour de role / tirage au sort) : debut + une periode. */
    static LocalDate echeance(Cycle c, Frequence f) {
        switch (f) {
        case HEBDOMADAIRE:
            return c.getDateDebut().plusWeeks(1);
        case MENSUELLE:
            return c.getDateDebut().plusMonths(1);
        default:
            return c.getDateDebut().plusYears(1);
        }
    }
}
