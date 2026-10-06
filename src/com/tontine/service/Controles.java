package com.tontine.service;

import com.tontine.dao.AdhesionDao;
import com.tontine.dao.CycleDao;
import com.tontine.dao.JournalAuditDao;
import com.tontine.dao.TontineDao;
import com.tontine.dao.UtilisateurDao;
import com.tontine.model.Adhesion;
import com.tontine.model.Cycle;
import com.tontine.model.StatutAdhesion;
import com.tontine.model.StatutCompte;
import com.tontine.model.Tontine;
import com.tontine.model.Utilisateur;

/** Verifications communes a tous les services (droits, existence, formats). */
final class Controles {

    private static final UtilisateurDao UTILISATEURS = new UtilisateurDao();
    private static final TontineDao TONTINES = new TontineDao();
    private static final AdhesionDao ADHESIONS = new AdhesionDao();
    private static final CycleDao CYCLES = new CycleDao();

    private Controles() {
    }

    static void exiger(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message);
        }
    }

    static String texte(String valeur, String champ) {
        exiger(valeur != null && !valeur.trim().isEmpty(), "Le champ '" + champ + "' est obligatoire.");
        return valeur.trim();
    }

    static String email(String valeur) {
        String v = texte(valeur, "email").toLowerCase();
        exiger(v.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"), "Adresse email invalide.");
        return v;
    }

    static String telephone(String valeur) {
        String v = texte(valeur, "telephone").replaceAll("[\\s.-]", "");
        exiger(v.matches("^\\+?[0-9]{8,15}$"), "Numero de telephone invalide (8 a 15 chiffres).");
        return v;
    }

    static void motDePasse(String valeur) {
        exiger(valeur != null && valeur.length() >= 6, "Le mot de passe doit contenir au moins 6 caracteres.");
    }

    static double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }

    /** Charge un utilisateur existant dont le compte n'est pas suspendu. */
    static Utilisateur utilisateur(int id) {
        Utilisateur u = UTILISATEURS.findById(id);
        exiger(u != null, "Utilisateur introuvable.");
        exiger(u.getStatut() == StatutCompte.ACTIF, "Ce compte est suspendu.");
        return u;
    }

    static Tontine tontine(int id) {
        Tontine t = TONTINES.findById(id);
        exiger(t != null, "Tontine introuvable.");
        return t;
    }

    static Adhesion adhesionActive(int tontineId, int userId) {
        Adhesion a = ADHESIONS.findByTontineEtUser(tontineId, userId);
        exiger(a != null && a.getStatut() == StatutAdhesion.ACTIF, "Vous n'etes pas membre de cette tontine.");
        return a;
    }

    static void exigerGerant(Tontine t, int userId) {
        exiger(t.getGerantId() == userId, "Action reservee au gerant de cette tontine.");
    }

    static void exigerAdmin(int userId) {
        Utilisateur u = utilisateur(userId);
        exiger(u.isAdmin(), "Action reservee a l'administrateur.");
    }

    /** Membre actif, gerant ou administrateur : peut consulter la tontine. */
    static void exigerParticipant(Tontine t, int userId) {
        Utilisateur u = utilisateur(userId);
        if (u.isAdmin()) {
            return;
        }
        adhesionActive(t.getId(), userId);
    }

    static Cycle cycleEnCours(Tontine t) {
        Cycle c = CYCLES.findEnCours(t.getId());
        exiger(c != null, "Aucun cycle en cours pour cette tontine. Le gerant doit d'abord demarrer un cycle.");
        return c;
    }

    static void audit(Integer userId, String action, String details) {
        try {
            new JournalAuditDao().log(userId, action, details);
        } catch (RuntimeException e) {
            // l'audit ne doit jamais bloquer l'operation principale
        }
    }
}
