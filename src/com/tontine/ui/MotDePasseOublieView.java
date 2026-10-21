package com.tontine.ui;

import com.tontine.service.AuthService;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Mot de passe oublie : l'utilisateur s'authentifie avec les informations de son compte (email, telephone, nom),
 * puis choisit un nouveau mot de passe et revient a l'ecran de connexion.
 */
public class MotDePasseOublieView extends StackPane {

    private final AuthService auth = new AuthService();
    private final TextField email = new TextField();
    private final TextField telephone = new TextField();
    private final TextField nom = new TextField();
    private final PasswordField nouveau = new PasswordField();
    private final PasswordField confirmation = new PasswordField();
    private final Button valider = Composants.primaire("Red\u00e9finir mon mot de passe");

    public MotDePasseOublieView(String emailInitial) {
        email.setPromptText("L'email de votre compte");
        if (emailInitial != null) {
            email.setText(emailInitial.contains("@") ? emailInitial.trim() : "");
        }
        telephone.setPromptText("Le t\u00e9l\u00e9phone de votre compte");
        nom.setPromptText("Votre nom de famille");
        nouveau.setPromptText("6 caract\u00e8res minimum");
        confirmation.setPromptText("Retapez le nouveau mot de passe");

        Label message = Composants.message();
        valider.setMaxWidth(Double.MAX_VALUE);
        valider.setDefaultButton(true);
        valider.setOnAction(e -> {
            Composants.cacher(message);
            try {
                auth.reinitialiserMotDePasse(email.getText(), telephone.getText(), nom.getText(),
                        nouveau.getText(), confirmation.getText());
                Nav.aller(new ConnexionView("Mot de passe modifi\u00e9. Connectez-vous avec votre nouveau mot de passe."));
            } catch (RuntimeException ex) {
                Composants.montrer(message, Nav.messageErreur(ex), true);
                nouveau.clear();
                confirmation.clear();
            }
        });

        Hyperlink retour = new Hyperlink("\u2190 Retour \u00e0 la connexion");
        retour.setOnAction(e -> Nav.aller(new ConnexionView(null)));

        Node logo = Composants.logo(false);
        VBox carte = new VBox(14, logo, Composants.titre("Mot de passe oubli\u00e9"),
                Composants.sousTitre("Prouvez que ce compte est le v\u00f4tre avec les informations saisies \u00e0 l'inscription, "
                        + "puis choisissez un nouveau mot de passe."),
                message,
                Composants.champ("Email", email),
                Composants.champ("T\u00e9l\u00e9phone", telephone),
                Composants.champ("Nom", nom),
                Composants.champ("Nouveau mot de passe", nouveau),
                Composants.champ("Confirmer le mot de passe", confirmation),
                valider, retour);
        carte.getStyleClass().add("auth-carte");
        carte.setMaxWidth(440);
        carte.setMaxHeight(VBox.USE_PREF_SIZE);

        VBox marque = PanneauMarque.creer("Pas de panique.",
                "Retrouvez l'acc\u00e8s \u00e0 votre compte en quelques secondes.",
                "V\u00e9rification de votre identit\u00e9", "Un nouveau mot de passe de votre choix",
                "Vos tontines et votre historique restent intacts");
        ConnexionView.assembler(this, marque, carte, logo);
        Effets.apparition(carte, 0);
        javafx.application.Platform.runLater(email::requestFocus);
    }

    // acces pour les tests automatiques
    TextField getEmail() { return email; }
    TextField getTelephone() { return telephone; }
    TextField getNom() { return nom; }
    PasswordField getNouveau() { return nouveau; }
    PasswordField getConfirmation() { return confirmation; }
    Button getBoutonValider() { return valider; }
}
