package com.tontine.ui;

import com.tontine.model.Utilisateur;
import com.tontine.service.AuthService;
import com.tontine.service.Session;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Gestion du profil : coordonnees (email, telephone) et mot de passe. */
public class ProfilView extends VBox {

    private final AuthService auth = new AuthService();

    public ProfilView(Utilisateur utilisateur) {
        super(18);
        Utilisateur u = Session.getUtilisateur() != null && Session.getUtilisateur().getId() == utilisateur.getId()
                ? Session.getUtilisateur() : utilisateur;

        // ---- coordonnees
        TextField email = new TextField(u.getEmail());
        TextField telephone = new TextField(u.getTelephone());
        Label msgProfil = Composants.message();
        Button enregistrer = Composants.primaire("Enregistrer");
        enregistrer.setOnAction(e -> {
            Composants.cacher(msgProfil);
            try {
                auth.modifierProfil(u.getId(), email.getText(), telephone.getText());
                Composants.montrer(msgProfil, "Vos coordonn\u00e9es ont \u00e9t\u00e9 mises \u00e0 jour.", false);
            } catch (RuntimeException ex) {
                Composants.montrer(msgProfil, Nav.messageErreur(ex), true);
            }
        });
        Label identite = new Label(u.getNomComplet());
        identite.getStyleClass().add("carte-titre");
        VBox carteProfil = Composants.carte(Composants.titre("Mes informations"),
                Composants.etiquette("Nom"), identite,
                Composants.champ("Email", email), Composants.champ("T\u00e9l\u00e9phone", telephone),
                msgProfil, enregistrer);
        carteProfil.setMaxWidth(520);

        // ---- mot de passe
        PasswordField ancien = new PasswordField();
        PasswordField nouveau = new PasswordField();
        nouveau.setPromptText("6 caract\u00e8res minimum");
        PasswordField confirmation = new PasswordField();
        Label msgMdp = Composants.message();
        Button changer = Composants.primaire("Changer le mot de passe");
        changer.setOnAction(e -> {
            Composants.cacher(msgMdp);
            if (!nouveau.getText().equals(confirmation.getText())) {
                Composants.montrer(msgMdp, "Les deux nouveaux mots de passe ne sont pas identiques.", true);
                return;
            }
            try {
                auth.changerMotDePasse(u.getId(), ancien.getText(), nouveau.getText());
                ancien.clear();
                nouveau.clear();
                confirmation.clear();
                Composants.montrer(msgMdp, "Votre mot de passe a \u00e9t\u00e9 modifi\u00e9.", false);
            } catch (RuntimeException ex) {
                Composants.montrer(msgMdp, Nav.messageErreur(ex), true);
            }
        });
        VBox carteMdp = Composants.carte(Composants.titre("Mot de passe"),
                Composants.champ("Ancien mot de passe", ancien), Composants.champ("Nouveau mot de passe", nouveau),
                Composants.champ("Confirmer le nouveau mot de passe", confirmation), msgMdp, changer);
        carteMdp.setMaxWidth(520);

        getChildren().addAll(carteProfil, carteMdp);
    }
}
