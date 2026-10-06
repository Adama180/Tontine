package com.tontine.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tontine.dao.CycleDao;
import com.tontine.dao.PretDao;
import com.tontine.dao.TontineDao;
import com.tontine.dao.TransactionDao;
import com.tontine.model.Adhesion;
import com.tontine.model.Cycle;
import com.tontine.model.ModePaiement;
import com.tontine.model.Pret;
import com.tontine.model.StatutPret;
import com.tontine.model.StatutTransaction;
import com.tontine.model.Tontine;
import com.tontine.model.Transaction;
import com.tontine.model.TypeTransaction;
import com.tontine.model.Utilisateur;

/**
 * Prets (fonds commun uniquement).
 * Interet simple : interet = montant initial x taux / 100 (taux fixe pour toute la duree du pret).
 * Le gerant valide les prets des membres ; l'administrateur valide ceux du gerant (jamais lui-meme).
 */
public class PretService {

    private final PretDao pretDao = new PretDao();
    private final TontineDao tontineDao = new TontineDao();
    private final CycleDao cycleDao = new CycleDao();
    private final TransactionDao transactionDao = new TransactionDao();

    public Pret demanderPret(int userId, int tontineId, double montant, int nbJours, String motif) {
        Controles.utilisateur(userId);
        Tontine t = Controles.tontine(tontineId);
        Controles.exiger(t.isFondsCommun(), "Les prets ne sont proposes que dans les tontines de type fonds commun.");
        Controles.adhesionActive(tontineId, userId);
        Controles.cycleEnCours(t);
        Controles.exiger(t.getTauxInteret() != null && t.getDureeMaxPret() != null,
                "Le gerant n'a pas encore defini les conditions de pret (taux et duree maximale).");
        Controles.exiger(montant > 0, "Le montant du pret doit etre positif.");
        Controles.exiger(nbJours >= 1 && nbJours <= t.getDureeMaxPret(),
                "La duree doit etre comprise entre 1 et " + t.getDureeMaxPret() + " jours.");
        Controles.exiger(montant <= fondsDisponible(tontineId) + 0.005,
                "Fonds insuffisants : " + Format.montant(fondsDisponible(tontineId)) + " disponibles.");
        for (Pret p : pretDao.findByUser(userId)) {
            Controles.exiger(p.getTontineId() != tontineId
                    || (p.getStatut() != StatutPret.EN_ATTENTE && p.getStatut() != StatutPret.ACCORDE),
                    "Vous avez deja un pret en attente ou non rembourse dans cette tontine.");
        }

        Pret p = new Pret();
        p.setTontineId(tontineId);
        p.setUserId(userId);
        p.setMontant(Controles.arrondir(montant));
        p.setNbJours(nbJours);
        p.setMotif(motif == null ? "" : motif.trim());
        pretDao.insert(p);

        Utilisateur emprunteur = Controles.utilisateur(userId);
        String msg = "Nouvelle demande de pret de " + Format.montant(montant) + " par " + emprunteur.getNomComplet()
                + " (tontine " + t.getNom() + ").";
        if (t.getGerantId() == userId) {
            NotificationService.notifierAdmins(msg, "PRET");
        } else {
            NotificationService.notifier(t.getGerantId(), msg, "PRET");
        }
        return p;
    }

    /**
     * Accepte ou refuse un pret. Pret d'un membre : le gerant de la tontine. Pret du gerant : l'administrateur.
     */
    public Pret validerPret(int validateurId, int pretId, boolean accepter) {
        Pret p = pretDao.findById(pretId);
        Controles.exiger(p != null, "Pret introuvable.");
        Controles.exiger(p.getStatut() == StatutPret.EN_ATTENTE, "Ce pret a deja ete traite.");
        Tontine t = Controles.tontine(p.getTontineId());
        Utilisateur validateur = Controles.utilisateur(validateurId);

        if (p.getUserId() == t.getGerantId()) {
            Controles.exiger(validateur.isAdmin(),
                    "Ce pret est demande par le gerant : seul l'administrateur peut le valider.");
        } else {
            Controles.exiger(t.getGerantId() == validateurId, "Seul le gerant de la tontine peut valider ce pret.");
        }

        if (!accepter) {
            pretDao.traiter(p.getId(), StatutPret.REFUSE, validateurId);
            p.setStatut(StatutPret.REFUSE);
            NotificationService.notifier(p.getUserId(), "Votre demande de pret de " + Format.montant(p.getMontant())
                    + " a ete refusee.", "PRET");
            Controles.audit(validateurId, "PRET_REFUSE", "Pret " + p.getId() + " - tontine " + t.getNom());
            return p;
        }

        Cycle c = Controles.cycleEnCours(t);
        Controles.exiger(p.getMontant() <= fondsDisponible(t.getId()) + 0.005,
                "Fonds insuffisants : " + Format.montant(fondsDisponible(t.getId())) + " disponibles.");
        Transaction tx = new Transaction();
        tx.setTontineId(t.getId());
        tx.setUserId(p.getUserId());
        tx.setCycleId(c.getId());
        tx.setType(TypeTransaction.PRET);
        tx.setMontant(p.getMontant());
        tx.setStatut(StatutTransaction.CONFIRMEE);
        tx.setReference(refPret(p));
        transactionDao.insert(tx);
        pretDao.traiter(p.getId(), StatutPret.ACCORDE, validateurId);
        p.setStatut(StatutPret.ACCORDE);
        p.setValideurId(validateurId);

        NotificationService.notifier(p.getUserId(), "Votre pret de " + Format.montant(p.getMontant())
                + " est accorde. A rembourser : " + Format.montant(totalDu(p, t)) + " en " + p.getNbJours()
                + " jours maximum.", "PRET");
        // transparence : tous les membres voient les prets accordes, y compris ceux du gerant
        NotificationService.notifierMembres(t.getId(), "Un pret de " + Format.montant(p.getMontant())
                + " a ete accorde dans la tontine " + t.getNom() + ".", "PRET", p.getUserId());
        Controles.audit(validateurId, "PRET_ACCORDE", "Pret " + p.getId() + " - tontine " + t.getNom()
                + " - " + Format.montant(p.getMontant()));
        return p;
    }

    /** Rembourse tout ou partie d'un pret (jamais plus que le reste du). */
    public Transaction rembourser(int userId, int pretId, double montant, ModePaiement mode, String numeroMobile) {
        Pret p = pretDao.findById(pretId);
        Controles.exiger(p != null && p.getUserId() == userId, "Pret introuvable.");
        Controles.exiger(p.getStatut() == StatutPret.ACCORDE, "Ce pret n'est pas a rembourser.");
        Tontine t = Controles.tontine(p.getTontineId());
        Cycle c = Controles.cycleEnCours(t);
        Controles.exiger(mode != null, "Choisissez un mode de paiement.");
        Controles.exiger(montant > 0, "Le montant doit etre positif.");
        double reste = resteAPayer(p, t);
        Controles.exiger(montant <= reste + 0.005,
                "Le remboursement ne peut pas depasser le reste du : " + Format.montant(reste) + ".");

        Transaction tx = new Transaction();
        tx.setTontineId(t.getId());
        tx.setUserId(userId);
        tx.setCycleId(c.getId());
        tx.setType(TypeTransaction.REMBOURSEMENT);
        tx.setMontant(Controles.arrondir(montant));
        tx.setModePaiement(mode);
        if (mode == ModePaiement.ESPECE) {
            tx.setStatut(StatutTransaction.EN_ATTENTE);
            tx.setReference(refPret(p) + "|ESP");
            transactionDao.insert(tx);
            NotificationService.notifier(t.getGerantId(), "Un remboursement de pret en especes de "
                    + Format.montant(montant) + " attend votre confirmation (tontine " + t.getNom() + ").", "PRET");
        } else {
            tx.setStatut(StatutTransaction.CONFIRMEE);
            tx.setReference(refPret(p) + "|" + PasserelleSimulee.initierPaiement(mode, montant, numeroMobile));
            transactionDao.insert(tx);
            appliquerRemboursement(tx);
        }
        return tx;
    }

    /** Remboursement confirme : met a jour le pret, le solde du et la penalite eventuelle. */
    void appliquerRemboursement(Transaction tx) {
        Pret p = pretDao.findById(idPret(tx.getReference()));
        Controles.exiger(p != null, "Pret introuvable pour ce remboursement.");
        Tontine t = Controles.tontine(p.getTontineId());
        pretDao.ajouterRemboursement(p.getId(), tx.getMontant());
        p = pretDao.findById(p.getId());
        if (resteAPayer(p, t) <= 0.004) {
            pretDao.changerStatut(p.getId(), StatutPret.REMBOURSE);
            NotificationService.notifier(p.getUserId(), "Votre pret de " + Format.montant(p.getMontant())
                    + " est entierement rembourse.", "PRET");
        }
        // penalite de retard (une seule fois par pret)
        if (t.getPenaliteRetard() > 0 && tx.getDate().isAfter(dateDecaissement(p).plusDays(p.getNbJours()))
                && !penaliteDejaAppliquee(p)) {
            Transaction pen = new Transaction();
            pen.setTontineId(t.getId());
            pen.setUserId(p.getUserId());
            pen.setCycleId(tx.getCycleId());
            pen.setType(TypeTransaction.PENALITE);
            pen.setMontant(t.getPenaliteRetard());
            pen.setModePaiement(tx.getModePaiement());
            pen.setStatut(StatutTransaction.CONFIRMEE);
            pen.setReference(refPret(p));
            transactionDao.insert(pen);
            NotificationService.notifier(p.getUserId(), "Penalite de retard de "
                    + Format.montant(t.getPenaliteRetard()) + " appliquee a votre pret.", "PENALITE");
        }
    }

    // ---------------------------------------------------------------- consultation et calculs

    /**
     * Argent reellement disponible dans le fonds : tout ce qui est entre (cotisations, remboursements,
     * penalites) moins ce qui est sorti (prets, distributions). Seuls les mouvements confirmes comptent.
     */
    public double fondsDisponible(int tontineId) {
        double entrees = transactionDao.totalParType(tontineId, TypeTransaction.COTISATION)
                + transactionDao.totalParType(tontineId, TypeTransaction.REMBOURSEMENT)
                + transactionDao.totalParType(tontineId, TypeTransaction.PENALITE);
        double sorties = transactionDao.totalParType(tontineId, TypeTransaction.PRET)
                + transactionDao.totalParType(tontineId, TypeTransaction.DISTRIBUTION);
        return Controles.arrondir(entrees - sorties);
    }

    public double interets(Pret p, Tontine t) {
        double taux = t.getTauxInteret() == null ? 0 : t.getTauxInteret();
        return Controles.arrondir(p.getMontant() * taux / 100.0);
    }

    public double totalDu(Pret p, Tontine t) {
        return Controles.arrondir(p.getMontant() + interets(p, t));
    }

    public double resteAPayer(Pret p, Tontine t) {
        return Controles.arrondir(totalDu(p, t) - p.getMontantRembourse());
    }

    /** Tous les prets d'une tontine, visibles par ses membres (transparence collective). */
    public List<Pret> listerPrets(int userId, int tontineId) {
        Tontine t = Controles.tontine(tontineId);
        Controles.exigerParticipant(t, userId);
        return pretDao.findByTontine(tontineId);
    }

    public List<Pret> mesPrets(int userId) {
        return pretDao.findByUser(userId);
    }

    /** Demandes en attente a valider par le gerant (prets des membres simples). */
    public List<Pret> pretsAValider(int gerantId, int tontineId) {
        Tontine t = Controles.tontine(tontineId);
        Controles.exigerGerant(t, gerantId);
        List<Pret> liste = new ArrayList<>();
        for (Pret p : pretDao.findByTontineEtStatut(tontineId, StatutPret.EN_ATTENTE)) {
            if (p.getUserId() != t.getGerantId()) {
                liste.add(p);
            }
        }
        return liste;
    }

    // ---------------------------------------------------------------- interne

    private static String refPret(Pret p) {
        return "PRET-" + p.getId();
    }

    private static int idPret(String reference) {
        Controles.exiger(reference != null && reference.startsWith("PRET-"), "Reference de pret invalide.");
        int barre = reference.indexOf('|');
        String cle = barre > 0 ? reference.substring(5, barre) : reference.substring(5);
        return Integer.parseInt(cle);
    }

    private LocalDateTime dateDecaissement(Pret p) {
        for (Transaction tx : transactionDao.findByTontineEtUser(p.getTontineId(), p.getUserId())) {
            if (tx.getType() == TypeTransaction.PRET && refPret(p).equals(tx.getReference())) {
                return tx.getDate();
            }
        }
        return p.getDateDemande();
    }

    private boolean penaliteDejaAppliquee(Pret p) {
        for (Transaction tx : transactionDao.findByTontineEtUser(p.getTontineId(), p.getUserId())) {
            if (tx.getType() == TypeTransaction.PENALITE && refPret(p).equals(tx.getReference())) {
                return true;
            }
        }
        return false;
    }
}
