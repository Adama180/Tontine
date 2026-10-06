package com.tontine.ui;

import com.tontine.model.Utilisateur;
import com.tontine.service.AuthService;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Ecran de connexion (email ou telephone + mot de passe). */
public class ConnexionView extends StackPane {

    private final AuthService auth = new AuthService();

    /** @param messageSucces message a afficher en vert (ex. apres une inscription), ou null */
    public ConnexionView(String messageSucces) {
        getStyleClass().add("fond");

        TextField identifiant = new TextField();
        identifiant.setPromptText("Email ou num\u00e9ro de t\u00e9l\u00e9phone");
        PasswordField motDePasse = new PasswordField();
        motDePasse.setPromptText("Mot de passe");

        Label message = Composants.message();
        if (messageSucces != null) {
            Composants.montrer(message, messageSucces, false);
        }

        Button valider = Composants.primaire("Se connecter");
        valider.setMaxWidth(Double.MAX_VALUE);
        valider.setDefaultButton(true);
        valider.setOnAction(e -> {
            Composants.cacher(message);
            try {
                Utilisateur u = auth.seConnecter(identifiant.getText(), motDePasse.getText());
                Nav.aller(new TableauDeBordView(u));
            } catch (RuntimeException ex) {
                Composants.montrer(message, Nav.messageErreur(ex), true);
                motDePasse.clear();
            }
        });

        Hyperlink inscription = new Hyperlink("Pas encore de compte ? Cr\u00e9er un compte");
        inscription.setOnAction(e -> Nav.aller(new InscriptionView()));
        Hyperlink retour = new Hyperlink("\u2190 Retour \u00e0 l'accueil");
        retour.setOnAction(e -> Nav.aller(new AccueilView()));

        VBox carte = Composants.carte(
                Composants.logo(false),
                Composants.titre("Connexion"),
                Composants.sousTitre("Acc\u00e9dez \u00e0 votre espace avec votre email ou votre num\u00e9ro de t\u00e9l\u00e9phone."),
                message,
                Composants.champ("Identifiant", identifiant),
                Composants.champ("Mot de passe", motDePasse),
                valider,
                inscription,
                retour);
        carte.setMaxWidth(430);
        carte.setMaxHeight(VBox.USE_PREF_SIZE);
        getChildren().add(carte);
        StackPane.setAlignment(carte, Pos.CENTER);
        javafx.application.Platform.runLater(identifiant::requestFocus);
    }
}
