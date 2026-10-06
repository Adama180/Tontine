package com.tontine.ui;

import com.tontine.service.AuthService;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Creation d'un compte Utilisateur (sans tontine pour le moment). */
public class InscriptionView extends StackPane {

    private final AuthService auth = new AuthService();

    public InscriptionView() {
        getStyleClass().add("fond");

        TextField nom = new TextField();
        TextField prenom = new TextField();
        TextField email = new TextField();
        email.setPromptText("exemple@mail.com");
        TextField telephone = new TextField();
        telephone.setPromptText("77 000 00 00");
        PasswordField mdp = new PasswordField();
        mdp.setPromptText("6 caract\u00e8res minimum");
        PasswordField confirmation = new PasswordField();

        GridPane grille = new GridPane();
        grille.setHgap(16);
        grille.setVgap(14);
        ColumnConstraints moitie = new ColumnConstraints();
        moitie.setPercentWidth(50);
        grille.getColumnConstraints().addAll(moitie, moitie);
        grille.add(Composants.champ("Nom", nom), 0, 0);
        grille.add(Composants.champ("Pr\u00e9nom", prenom), 1, 0);
        grille.add(Composants.champ("Email", email), 0, 1);
        grille.add(Composants.champ("T\u00e9l\u00e9phone", telephone), 1, 1);
        grille.add(Composants.champ("Mot de passe", mdp), 0, 2);
        grille.add(Composants.champ("Confirmer le mot de passe", confirmation), 1, 2);

        Label message = Composants.message();
        Button valider = Composants.primaire("Cr\u00e9er mon compte");
        valider.setMaxWidth(Double.MAX_VALUE);
        valider.setDefaultButton(true);
        valider.setOnAction(e -> {
            Composants.cacher(message);
            if (!mdp.getText().equals(confirmation.getText())) {
                Composants.montrer(message, "Les deux mots de passe ne sont pas identiques.", true);
                return;
            }
            try {
                auth.inscrire(nom.getText(), prenom.getText(), email.getText(), telephone.getText(), mdp.getText());
                Nav.aller(new ConnexionView("Compte cr\u00e9\u00e9 avec succ\u00e8s ! Connectez-vous pour continuer."));
            } catch (RuntimeException ex) {
                Composants.montrer(message, Nav.messageErreur(ex), true);
            }
        });

        Hyperlink connexion = new Hyperlink("D\u00e9j\u00e0 un compte ? Se connecter");
        connexion.setOnAction(e -> Nav.aller(new ConnexionView(null)));
        Hyperlink retour = new Hyperlink("\u2190 Retour \u00e0 l'accueil");
        retour.setOnAction(e -> Nav.aller(new AccueilView()));

        VBox carte = Composants.carte(
                Composants.logo(false),
                Composants.titre("Cr\u00e9er un compte"),
                Composants.sousTitre("Votre compte est g\u00e9r\u00e9 une seule fois : vous pourrez ensuite demander \u00e0 rejoindre "
                        + "une ou plusieurs tontines, ou en cr\u00e9er une."),
                message,
                grille,
                valider,
                connexion,
                retour);
        carte.setMaxWidth(660);
        carte.setMaxHeight(VBox.USE_PREF_SIZE);
        getChildren().add(carte);
        StackPane.setAlignment(carte, Pos.CENTER);
        javafx.application.Platform.runLater(nom::requestFocus);
    }
}
