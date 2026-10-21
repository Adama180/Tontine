package com.tontine.ui;

import com.tontine.model.Utilisateur;
import com.tontine.service.AuthService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Ecran de connexion (email ou telephone + mot de passe). Panneau de marque a gauche, masque sur fenetre etroite. */
public class ConnexionView extends StackPane {

    private final AuthService auth = new AuthService();

    /** @param messageSucces message a afficher en vert (ex. apres une inscription), ou null */
    public ConnexionView(String messageSucces) {
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

        Hyperlink oublie = new Hyperlink("Mot de passe oubli\u00e9 ?");
        oublie.setOnAction(e -> Nav.aller(new MotDePasseOublieView(identifiant.getText())));
        HBox ligneOubli = new HBox(oublie);
        ligneOubli.setAlignment(Pos.CENTER_RIGHT);

        Hyperlink inscription = new Hyperlink("Pas encore de compte ? Cr\u00e9er un compte");
        inscription.setOnAction(e -> Nav.aller(new InscriptionView()));
        Hyperlink retour = new Hyperlink("\u2190 Retour \u00e0 l'accueil");
        retour.setOnAction(e -> Nav.aller(new AccueilView()));

        Node logo = Composants.logo(false);
        Label titre = Composants.titre("Connexion");
        VBox carte = new VBox(14, logo, titre,
                Composants.sousTitre("Acc\u00e9dez \u00e0 votre espace avec votre email ou votre num\u00e9ro de t\u00e9l\u00e9phone."),
                message,
                Composants.champ("Identifiant", identifiant),
                Composants.champ("Mot de passe", motDePasse),
                ligneOubli,
                valider, inscription, retour);
        carte.getStyleClass().add("auth-carte");
        carte.setMaxWidth(440);
        carte.setMaxHeight(VBox.USE_PREF_SIZE);

        VBox marque = PanneauMarque.creer("Heureux de vous revoir.",
                "Retrouvez vos tontines, vos cotisations et votre historique au m\u00eame endroit.",
                "Suivi en temps r\u00e9el de chaque cotisation", "Pr\u00eats et remboursements clairs pour tous",
                "Notifications \u00e0 chaque \u00e9tape importante");
        assembler(this, marque, carte, logo);
        Effets.apparition(carte, 0);
        javafx.application.Platform.runLater(identifiant::requestFocus);
    }

    /** Disposition commune : marque a gauche (42 %), formulaire centre a droite, adaptation a la largeur. */
    static void assembler(StackPane racine, VBox marque, VBox carte, Node logoFormulaire) {
        StackPane centre = new StackPane(carte);
        centre.setPadding(new Insets(32));
        centre.setAlignment(Pos.CENTER);
        ScrollPane defilement = new ScrollPane(centre);
        defilement.setFitToWidth(true);
        defilement.setFitToHeight(true);
        defilement.getStyleClass().addAll("defilement", "auth-fond");
        HBox ligne = new HBox(marque, defilement);
        HBox.setHgrow(defilement, Priority.ALWAYS);
        marque.prefWidthProperty().bind(ligne.widthProperty().multiply(0.42));
        marque.setMinWidth(0);
        racine.getChildren().add(ligne);
        racine.getStyleClass().add("auth-fond");
        logoFormulaire.setVisible(false);
        logoFormulaire.setManaged(false);
        Adaptatif.surLargeur(racine, 880, etroit -> {
            marque.setVisible(!etroit);
            marque.setManaged(!etroit);
            logoFormulaire.setVisible(etroit);
            logoFormulaire.setManaged(etroit);
            Adaptatif.classeCompact(racine, etroit);
        });
    }
}
