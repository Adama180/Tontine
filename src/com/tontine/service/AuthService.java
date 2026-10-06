package com.tontine.service;

import com.tontine.dao.UtilisateurDao;
import com.tontine.model.StatutCompte;
import com.tontine.model.Utilisateur;
import com.tontine.util.DaoException;
import com.tontine.util.PasswordUtil;

/** Inscription, connexion et gestion du profil. */
public class AuthService {

    private final UtilisateurDao dao = new UtilisateurDao();

    /** Cree un compte Utilisateur (sans tontine pour le moment). */
    public Utilisateur inscrire(String nom, String prenom, String email, String telephone, String motDePasse) {
        nom = Controles.texte(nom, "nom");
        prenom = Controles.texte(prenom, "prenom");
        email = Controles.email(email);
        telephone = Controles.telephone(telephone);
        Controles.motDePasse(motDePasse);
        Controles.exiger(!dao.emailExiste(email), "Cette adresse email est deja utilisee.");
        Controles.exiger(!dao.telephoneExiste(telephone), "Ce numero de telephone est deja utilise.");

        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setTelephone(telephone);
        u.setMotDePasse(PasswordUtil.hasher(motDePasse));
        try {
            dao.insert(u);
        } catch (DaoException e) {
            if (e.estDoublon()) {
                throw new ServiceException("Email ou telephone deja utilise.");
            }
            throw e;
        }
        return u;
    }

    /** Connexion par email ou telephone. Memorise l'utilisateur dans la Session. */
    public Utilisateur seConnecter(String login, String motDePasse) {
        Controles.exiger(login != null && !login.trim().isEmpty() && motDePasse != null && !motDePasse.isEmpty(),
                "Saisissez votre identifiant et votre mot de passe.");
        Utilisateur u = dao.findByLogin(login.trim().toLowerCase());
        if (u == null) {
            u = dao.findByLogin(login.trim());
        }
        Controles.exiger(u != null && PasswordUtil.verifier(motDePasse, u.getMotDePasse()),
                "Identifiant ou mot de passe incorrect.");
        Controles.exiger(u.getStatut() == StatutCompte.ACTIF,
                "Votre compte est suspendu. Contactez l'administrateur.");
        Session.setUtilisateur(u);
        return u;
    }

    public void deconnecter() {
        Session.deconnecter();
    }

    public void modifierProfil(int userId, String email, String telephone) {
        Utilisateur u = Controles.utilisateur(userId);
        email = Controles.email(email);
        telephone = Controles.telephone(telephone);
        Utilisateur autre = dao.findByEmail(email);
        Controles.exiger(autre == null || autre.getId() == userId, "Cette adresse email est deja utilisee.");
        autre = dao.findByTelephone(telephone);
        Controles.exiger(autre == null || autre.getId() == userId, "Ce numero de telephone est deja utilise.");
        dao.changerCoordonnees(userId, email, telephone);
        u.setEmail(email);
        u.setTelephone(telephone);
        if (Session.getUtilisateur() != null && Session.getUtilisateur().getId() == userId) {
            Session.setUtilisateur(u);
        }
    }

    public void changerMotDePasse(int userId, String ancien, String nouveau) {
        Utilisateur u = Controles.utilisateur(userId);
        Controles.exiger(PasswordUtil.verifier(ancien == null ? "" : ancien, u.getMotDePasse()),
                "L'ancien mot de passe est incorrect.");
        Controles.motDePasse(nouveau);
        dao.changerMotDePasse(userId, PasswordUtil.hasher(nouveau));
    }
}
