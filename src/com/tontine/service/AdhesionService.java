package com.tontine.service;

import java.util.ArrayList;
import java.util.List;

import com.tontine.dao.AdhesionDao;
import com.tontine.dao.CycleDao;
import com.tontine.dao.DemandeAdhesionDao;
import com.tontine.dao.PretDao;
import com.tontine.dao.TontineDao;
import com.tontine.dao.UtilisateurDao;
import com.tontine.model.Adhesion;
import com.tontine.model.DemandeAdhesion;
import com.tontine.model.StatutAdhesion;
import com.tontine.model.StatutDemande;
import com.tontine.model.StatutPret;
import com.tontine.model.StatutTontine;
import com.tontine.model.Tontine;
import com.tontine.model.Utilisateur;

/** Actions du gerant sur sa tontine : demandes d'adhesion, membres, parametres. */
public class AdhesionService {

    private final AdhesionDao adhesionDao = new AdhesionDao();
    private final DemandeAdhesionDao demandeDao = new DemandeAdhesionDao();
    private final TontineDao tontineDao = new TontineDao();
    private final CycleDao cycleDao = new CycleDao();
    private final PretDao pretDao = new PretDao();
    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    public List<DemandeAdhesion> listerDemandes(int gerantId, int tontineId, StatutDemande statut) {
        Tontine t = Controles.tontine(tontineId);
        Controles.exigerGerant(t, gerantId);
        return demandeDao.findByTontineEtStatut(tontineId, statut);
    }

    /** Accepte ou refuse une demande. Retourne la nouvelle adhesion (null si refusee). */
    public Adhesion validerDemandeAdhesion(int gerantId, int demandeId, boolean accepter) {
        DemandeAdhesion d = demandeDao.findById(demandeId);
        Controles.exiger(d != null, "Demande introuvable.");
        Controles.exiger(d.getStatut() == StatutDemande.EN_ATTENTE, "Cette demande a deja ete traitee.");
        Tontine t = Controles.tontine(d.getTontineId());
        Controles.exigerGerant(t, gerantId);

        if (!accepter) {
            demandeDao.changerStatut(d.getId(), StatutDemande.REFUSEE);
            NotificationService.notifier(d.getUserId(),
                    "Votre demande pour la tontine " + t.getNom() + " a ete refusee.", "ADHESION");
            Controles.audit(gerantId, "ADHESION_REFUSEE", "Demande " + d.getId() + " - tontine " + t.getNom());
            return null;
        }

        Controles.exiger(t.getStatut() == StatutTontine.ACTIVE, "Cette tontine est cloturee.");
        if (!t.isFondsCommun()) {
            Controles.exiger(cycleDao.findEnCours(t.getId()) == null,
                    "Dans une tontine a tour de role ou a tirage au sort, un membre ne peut etre accepte "
                            + "qu'entre deux cycles. Attendez la cloture du cycle en cours.");
        }
        Controles.exiger(adhesionDao.compterActifs(t.getId()) < t.getNbMembreMax(),
                "La tontine est complete (" + t.getNbMembreMax() + " membres).");
        Utilisateur u = Controles.utilisateur(d.getUserId());

        int ordre = prochainOrdre(t.getId());
        Adhesion a = adhesionDao.findByTontineEtUser(t.getId(), u.getId());
        if (a != null) {
            Controles.exiger(a.getStatut() == StatutAdhesion.INACTIF, "Cette personne est deja membre.");
            adhesionDao.changerStatut(a.getId(), StatutAdhesion.ACTIF);
            adhesionDao.definirOrdreTour(a.getId(), ordre);
            a = adhesionDao.findById(a.getId());
        } else {
            a = new Adhesion();
            a.setTontineId(t.getId());
            a.setUserId(u.getId());
            a.setOrdreTour(ordre);
            adhesionDao.insert(a);
        }
        demandeDao.changerStatut(d.getId(), StatutDemande.ACCEPTEE);
        NotificationService.notifier(u.getId(),
                "Bienvenue ! Vous etes maintenant membre de la tontine " + t.getNom() + ".", "ADHESION");
        Controles.audit(gerantId, "ADHESION_ACCEPTEE", u.getNomComplet() + " - tontine " + t.getNom());
        return a;
    }

    /** Membres de la tontine (visibles par les membres, le gerant et l'administrateur). */
    public List<LigneMembre> listerMembres(int userId, int tontineId) {
        Tontine t = Controles.tontine(tontineId);
        Controles.exigerParticipant(t, userId);
        List<LigneMembre> lignes = new ArrayList<>();
        for (Adhesion a : adhesionDao.findByTontine(tontineId)) {
            lignes.add(new LigneMembre(a, utilisateurDao.findById(a.getUserId())));
        }
        return lignes;
    }

    /** Desactive un membre. Impossible s'il a un solde, un pret non rembourse, ou est le gerant. */
    public void desactiverMembre(int gerantId, int adhesionId) {
        Adhesion a = adhesionDao.findById(adhesionId);
        Controles.exiger(a != null, "Adhesion introuvable.");
        Tontine t = Controles.tontine(a.getTontineId());
        Controles.exigerGerant(t, gerantId);
        Controles.exiger(a.getUserId() != gerantId, "Le gerant ne peut pas se desactiver lui-meme.");
        Controles.exiger(a.getStatut() == StatutAdhesion.ACTIF, "Ce membre est deja desactive.");
        Controles.exiger(Math.abs(a.getSolde()) < 0.005,
                "Ce membre a un solde dans la tontine : il doit etre regle avant la desactivation.");
        Controles.exiger(pretDao.findEnCours(t.getId(), a.getUserId()).isEmpty()
                && pretDao.findByTontineEtStatut(t.getId(), StatutPret.EN_ATTENTE).stream()
                        .noneMatch(p -> p.getUserId() == a.getUserId()),
                "Ce membre a un pret en cours.");
        if (!t.isFondsCommun()) {
            Controles.exiger(cycleDao.findEnCours(t.getId()) == null,
                    "Un membre ne peut etre desactive qu'entre deux cycles.");
        }
        adhesionDao.changerStatut(a.getId(), StatutAdhesion.INACTIF);
        NotificationService.notifier(a.getUserId(),
                "Vous avez ete retire de la tontine " + t.getNom() + ".", "ADHESION");
        Controles.audit(gerantId, "MEMBRE_DESACTIVE", "Adhesion " + a.getId() + " - tontine " + t.getNom());
    }

    /** Definit les regles de la tontine (entre deux cycles uniquement). */
    public Tontine definirParametres(int gerantId, int tontineId, ParametresTontine p) {
        Tontine t = Controles.tontine(tontineId);
        Controles.exigerGerant(t, gerantId);
        Controles.exiger(cycleDao.findEnCours(tontineId) == null,
                "Les parametres ne peuvent etre modifies qu'entre deux cycles.");
        Controles.exiger(p.getFrequence() != null, "Choisissez une frequence.");
        Controles.exiger(p.getMontantCotisation() > 0, "Le montant de la cotisation doit etre positif.");
        Controles.exiger(p.getPenaliteRetard() >= 0, "La penalite de retard ne peut pas etre negative.");
        Controles.exiger(p.getDureeEnCycle() >= 1, "La duree d'un cycle doit etre d'au moins 1 periode.");
        Controles.exiger(p.getNbMembreMax() >= 2 && p.getNbMembreMax() >= adhesionDao.compterActifs(tontineId),
                "Le nombre maximum de membres doit etre au moins 2 et au moins egal au nombre de membres actuels.");
        if (t.isFondsCommun()) {
            Controles.exiger(p.getTauxInteret() != null && p.getTauxInteret() >= 0 && p.getTauxInteret() <= 100,
                    "Le taux d'interet doit etre compris entre 0 et 100 %.");
            Controles.exiger(p.getDureeMaxPret() != null && p.getDureeMaxPret() >= 1,
                    "La duree maximale d'un pret doit etre d'au moins 1 jour.");
            t.setTauxInteret(p.getTauxInteret());
            t.setDureeMaxPret(p.getDureeMaxPret());
        } else {
            t.setTauxInteret(null);
            t.setDureeMaxPret(null);
        }
        t.setDescription(p.getDescription());
        t.setMontantCotisation(p.getMontantCotisation());
        t.setFrequence(p.getFrequence());
        t.setDureeEnCycle(p.getDureeEnCycle());
        t.setPenaliteRetard(p.getPenaliteRetard());
        t.setNbMembreMax(p.getNbMembreMax());
        tontineDao.update(t);
        Controles.audit(gerantId, "PARAMETRES_MODIFIES", "Tontine " + t.getNom());
        return t;
    }

    private int prochainOrdre(int tontineId) {
        int max = 0;
        for (Adhesion a : adhesionDao.findByTontine(tontineId)) {
            if (a.getOrdreTour() != null && a.getOrdreTour() > max) {
                max = a.getOrdreTour();
            }
        }
        return max + 1;
    }
}
