package com.tontine.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tontine.dao.AdhesionDao;
import com.tontine.dao.CycleDao;
import com.tontine.dao.DemandeCreationDao;
import com.tontine.dao.DistributionDao;
import com.tontine.dao.JournalAuditDao;
import com.tontine.dao.PretDao;
import com.tontine.dao.TirageDao;
import com.tontine.dao.TontineDao;
import com.tontine.dao.TransactionDao;
import com.tontine.dao.UtilisateurDao;
import com.tontine.model.Adhesion;
import com.tontine.model.Cycle;
import com.tontine.model.DemandeCreation;
import com.tontine.model.Distribution;
import com.tontine.model.JournalAudit;
import com.tontine.model.Pret;
import com.tontine.model.RoleAdhesion;
import com.tontine.model.StatutCompte;
import com.tontine.model.StatutCycle;
import com.tontine.model.StatutDemande;
import com.tontine.model.StatutTransaction;
import com.tontine.model.Tirage;
import com.tontine.model.Tontine;
import com.tontine.model.Transaction;
import com.tontine.model.TypeTontine;
import com.tontine.model.ModePaiement;
import com.tontine.model.Utilisateur;
import com.tontine.util.DaoException;

/** Actions de l'administrateur : comptes, demandes de creation, prets des gerants, audit, versements. */
public class AdminService {

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final TontineDao tontineDao = new TontineDao();
    private final AdhesionDao adhesionDao = new AdhesionDao();
    private final DemandeCreationDao demandeDao = new DemandeCreationDao();
    private final PretDao pretDao = new PretDao();
    private final CycleDao cycleDao = new CycleDao();
    private final TirageDao tirageDao = new TirageDao();
    private final DistributionDao distributionDao = new DistributionDao();
    private final JournalAuditDao auditDao = new JournalAuditDao();
    private final TransactionDao transactionDao = new TransactionDao();

    // ---------------------------------------------------------------- comptes

    public List<Utilisateur> listerUtilisateurs(int adminId) {
        Controles.exigerAdmin(adminId);
        return utilisateurDao.findAll();
    }

    public void suspendreCompte(int adminId, int userId) {
        Controles.exigerAdmin(adminId);
        Utilisateur u = utilisateurDao.findById(userId);
        Controles.exiger(u != null, "Utilisateur introuvable.");
        Controles.exiger(u.getId() != adminId && !u.isAdmin(), "Un compte administrateur ne peut pas etre suspendu.");
        utilisateurDao.changerStatut(userId, StatutCompte.SUSPENDU);
        Controles.audit(adminId, "COMPTE_SUSPENDU", u.getNomComplet() + " (" + u.getEmail() + ")");
    }

    public void reactiverCompte(int adminId, int userId) {
        Controles.exigerAdmin(adminId);
        Utilisateur u = utilisateurDao.findById(userId);
        Controles.exiger(u != null, "Utilisateur introuvable.");
        utilisateurDao.changerStatut(userId, StatutCompte.ACTIF);
        NotificationService.notifier(userId, "Votre compte a ete reactive.", "COMPTE");
        Controles.audit(adminId, "COMPTE_REACTIVE", u.getNomComplet() + " (" + u.getEmail() + ")");
    }

    // ---------------------------------------------------------------- demandes de creation

    public List<DemandeCreation> demandesCreationEnAttente(int adminId) {
        Controles.exigerAdmin(adminId);
        return demandeDao.findByStatut(StatutDemande.EN_ATTENTE);
    }

    /**
     * Accepte (cree la tontine, le demandeur en devient gerant) ou refuse. Retourne la tontine creee.
     * Les autres regles (penalite, taux, duree des prets) sont ensuite definies par le gerant.
     */
    public Tontine validerDemandeCreation(int adminId, int demandeId, boolean accepter) {
        Controles.exigerAdmin(adminId);
        DemandeCreation d = demandeDao.findById(demandeId);
        Controles.exiger(d != null && d.getStatut() == StatutDemande.EN_ATTENTE, "Demande introuvable ou deja traitee.");

        if (!accepter) {
            demandeDao.traiter(d.getId(), StatutDemande.REFUSEE, adminId);
            NotificationService.notifier(d.getDemandeurId(), "Votre demande de creation de la tontine '"
                    + d.getNomSouhaite() + "' a ete refusee.", "CREATION");
            Controles.audit(adminId, "CREATION_REFUSEE", "Demande " + d.getId() + " - " + d.getNomSouhaite());
            return null;
        }

        Controles.exiger(!tontineDao.nomExiste(d.getNomSouhaite()), "Une tontine porte deja ce nom.");
        Utilisateur demandeur = Controles.utilisateur(d.getDemandeurId());

        Tontine t = new Tontine();
        t.setNom(d.getNomSouhaite());
        t.setDescription("");
        t.setType(d.getTypeSouhaite());
        t.setMontantCotisation(d.getMontant());
        t.setFrequence(d.getFrequence());
        t.setDureeEnCycle(d.getNbMembre());
        t.setPenaliteRetard(0);
        t.setNbMembreMax(d.getNbMembre());
        t.setGerantId(demandeur.getId());
        try {
            tontineDao.insert(t);
        } catch (DaoException e) {
            if (e.estDoublon()) {
                throw new ServiceException("Une tontine porte deja ce nom.");
            }
            throw e;
        }

        Adhesion a = new Adhesion(); // le gerant est aussi un membre cotisant
        a.setTontineId(t.getId());
        a.setUserId(demandeur.getId());
        a.setRole(RoleAdhesion.GERANT);
        a.setOrdreTour(1);
        adhesionDao.insert(a);

        demandeDao.traiter(d.getId(), StatutDemande.ACCEPTEE, adminId);
        NotificationService.notifier(demandeur.getId(), "Felicitations ! Votre tontine '" + t.getNom()
                + "' est creee, vous en etes le gerant. Definissez ses parametres.", "CREATION");
        Controles.audit(adminId, "CREATION_ACCEPTEE", "Tontine " + t.getNom() + " - gerant " + demandeur.getNomComplet());
        return t;
    }

    // ---------------------------------------------------------------- prets des gerants

    public List<Pret> pretsGerantsEnAttente(int adminId) {
        Controles.exigerAdmin(adminId);
        return pretDao.findEnAttentePourGerants();
    }

    public Pret validerPretGerant(int adminId, int pretId, boolean accepter) {
        Controles.exigerAdmin(adminId);
        return new PretService().validerPret(adminId, pretId, accepter);
    }

    // ---------------------------------------------------------------- audit, versements, tirages

    public List<JournalAudit> consulterJournalAudit(int adminId, int limite) {
        Controles.exigerAdmin(adminId);
        return auditDao.findRecents(limite);
    }

    /** Tous les versements effectues sur la plateforme. */
    public List<Distribution> listerVersements(int adminId) {
        Controles.exigerAdmin(adminId);
        return distributionDao.findAll();
    }

    /**
     * Correction d'un tirage au sort : possible uniquement tant que le cycle n'est pas cloture
     * (aucun versement fait), avec un motif obligatoire. Elle est toujours tracee dans le journal d'audit.
     */
    public Tirage corrigerTirage(int adminId, int tirageId, int nouveauBeneficiaireId, String motif) {
        Controles.exigerAdmin(adminId);
        Controles.exiger(motif != null && !motif.trim().isEmpty(), "Le motif de la correction est obligatoire.");
        Tirage ti = tirageDao.findById(tirageId);
        Controles.exiger(ti != null, "Tirage introuvable.");
        Tontine t = Controles.tontine(ti.getTontineId());
        Cycle cycle = null;
        for (Cycle c : cycleDao.findByTontine(t.getId())) {
            if (c.getNumero() == ti.getSemaine()) {
                cycle = c;
            }
        }
        Controles.exiger(cycle != null && cycle.getStatut() == StatutCycle.EN_COURS,
                "Le versement de ce tirage a deja ete effectue : il ne peut plus etre corrige.");
        Controles.adhesionActive(t.getId(), nouveauBeneficiaireId);
        int ancien = ti.getBeneficiaireId();
        ti.setBeneficiaireId(nouveauBeneficiaireId);
        tirageDao.update(ti);
        Controles.audit(adminId, "CORRECTION_TIRAGE", "Tontine " + t.getNom() + " - cycle " + ti.getSemaine()
                + " - beneficiaire " + ancien + " remplace par " + nouveauBeneficiaireId + " - motif : " + motif.trim());
        NotificationService.notifierMembres(t.getId(), "Le resultat du tirage du cycle " + ti.getSemaine()
                + " a ete corrige par l'administrateur (tontine " + t.getNom() + ").", "TIRAGE", null);
        return ti;
    }

    // ---------------------------------------------------------------- suivi de la plateforme (partie 5e)

    /** Toutes les tontines de la plateforme, ouvertes ou cloturees. */
    public List<Tontine> listerToutesTontines(int adminId) {
        Controles.exigerAdmin(adminId);
        return tontineDao.findAll();
    }

    /** Demandes de creation deja acceptees ou refusees, les plus recentes d'abord. */
    public List<DemandeCreation> demandesCreationTraitees(int adminId) {
        Controles.exigerAdmin(adminId);
        List<DemandeCreation> liste = new ArrayList<>();
        liste.addAll(demandeDao.findByStatut(StatutDemande.ACCEPTEE));
        liste.addAll(demandeDao.findByStatut(StatutDemande.REFUSEE));
        liste.sort(Comparator.comparing(DemandeCreation::getDateEnvoi).reversed());
        return liste;
    }

    /** Paiements en especes des GERANTS en attente : eux seuls sont confirmes par l'administrateur. */
    public List<Transaction> paiementsGerantsEnAttente(int adminId) {
        Controles.exigerAdmin(adminId);
        List<Transaction> liste = new ArrayList<>();
        for (Tontine t : tontineDao.findAll()) {
            for (Transaction tx : transactionDao.findByTontine(t.getId())) {
                if (tx.getStatut() == StatutTransaction.EN_ATTENTE && tx.getModePaiement() == ModePaiement.ESPECE
                        && tx.getUserId() == t.getGerantId()) {
                    liste.add(tx);
                }
            }
        }
        return liste;
    }

    /** Confirme ou refuse le paiement en especes d'un gerant (un gerant ne confirme jamais le sien). */
    public Transaction confirmerPaiementGerant(int adminId, int transactionId, boolean accepter) {
        Controles.exigerAdmin(adminId);
        Transaction tx = transactionDao.findById(transactionId);
        Controles.exiger(tx != null, "Transaction introuvable.");
        Tontine t = Controles.tontine(tx.getTontineId());
        Controles.exiger(tx.getUserId() == t.getGerantId(),
                "Seuls les paiements du gerant sont traites ici : les autres sont confirmes par le gerant de la tontine.");
        Transaction resultat = new CotisationService().confirmerPaiementEspece(adminId, transactionId, accepter);
        Controles.audit(adminId, accepter ? "PAIEMENT_GERANT_CONFIRME" : "PAIEMENT_GERANT_REFUSE",
                "Tontine " + t.getNom() + " - " + Format.montant(tx.getMontant()));
        return resultat;
    }

    /** Tous les tirages au sort de la plateforme, les plus recents d'abord. */
    public List<Tirage> tousLesTirages(int adminId) {
        Controles.exigerAdmin(adminId);
        List<Tirage> liste = new ArrayList<>();
        for (Tontine t : tontineDao.findAll()) {
            if (t.getType() == TypeTontine.TIRAGE_AU_SORT) {
                liste.addAll(tirageDao.findByTontine(t.getId()));
            }
        }
        liste.sort(Comparator.comparing(Tirage::getDate).reversed());
        return liste;
    }

    /** Versements avec le nom de la tontine, le numero du cycle et le nom du beneficiaire. */
    public List<LigneVersement> versementsDetailles(int adminId) {
        Controles.exigerAdmin(adminId);
        Map<Integer, Cycle> cycles = new HashMap<>();
        Map<Integer, Tontine> tontines = new HashMap<>();
        Map<Integer, String> noms = new HashMap<>();
        List<LigneVersement> lignes = new ArrayList<>();
        for (Distribution d : distributionDao.findAll()) {
            Cycle c = cycles.computeIfAbsent(d.getCycleId(), cycleDao::findById);
            Tontine t = c == null ? null : tontines.computeIfAbsent(c.getTontineId(), tontineDao::findById);
            String nom = "?";
            if (d.getBeneficiaireId() != null) {
                nom = noms.computeIfAbsent(d.getBeneficiaireId(), id -> {
                    Utilisateur u = utilisateurDao.findById(id);
                    return u == null ? "?" : u.getNomComplet();
                });
            }
            lignes.add(new LigneVersement(d.getDate(), t == null ? "?" : t.getNom(), c == null ? 0 : c.getNumero(), nom,
                    d.getMontantTotal()));
        }
        lignes.sort(Comparator.comparing(LigneVersement::getDate).reversed());
        return lignes;
    }
}
