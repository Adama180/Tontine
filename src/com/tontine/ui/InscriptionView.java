package com.tontine.ui;

import com.tontine.service.AuthService;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Creation d'un compte Utilisateur (sans tontine pour le moment). */
public class InscriptionView extends StackPane {

    private final AuthService auth = new AuthService();

    public InscriptionView() {
        TextField nom = new TextField();
        TextField prenom = new TextField();
        TextField email = new TextField();
        email.setPromptText("exemple@mail.com");
        TextField telephone = new TextField();
        telephone.setPromptText("77 000 00 00");
        PasswordField mdp = new PasswordField();
        mdp.setPromptText("6 caract\u00e8res minimum");
        PasswordField confirmation = new PasswordField();

        FlowPane grille = new FlowPane(16, 14,
                colonne("Nom", nom), colonne("Pr\u00e9nom", prenom),
                colonne("Email", email), colonne("T\u00e9l\u00e9phone", telephone),
                colonne("Mot de passe", mdp), colonne("Confirmer le mot de passe", confirmation));

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

        Node logo = Composants.logo(false);
        VBox carte = new VBox(14, logo, Composants.titre("Cr\u00e9er un compte"),
                Composants.sousTitre("Votre compte est cr\u00e9\u00e9 une seule fois : vous pourrez ensuite demander \u00e0 rejoindre "
                        + "une ou plusieurs tontines, ou en cr\u00e9er une."),
                message, grille, valider, connexion, retour);
        carte.getStyleClass().add("auth-carte");
        carte.setMaxWidth(680);
        carte.setMaxHeight(VBox.USE_PREF_SIZE);

        VBox marque = PanneauMarque.creer("Rejoignez la plateforme.",
                "Un seul compte pour toutes vos tontines : membre dans l'une, g\u00e9rant dans une autre.",
                "Inscription en une minute", "Explorer les tontines sans engagement",
                "Vos donn\u00e9es prot\u00e9g\u00e9es : mot de passe chiffr\u00e9");
        ConnexionView.assembler(this, marque, carte, logo);
        Effets.apparition(carte, 0);
        javafx.application.Platform.runLater(nom::requestFocus);
    }

    private VBox colonne(String libelle, Node saisie) {
        VBox v = Composants.champ(libelle, saisie);
        v.setPrefWidth(300);
        v.setMinWidth(200);
        return v;
    }
}
