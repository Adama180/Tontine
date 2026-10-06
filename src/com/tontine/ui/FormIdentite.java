package com.tontine.ui;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

/** Formulaire d'identite d'un visiteur : nom, prenom, email, telephone, mot de passe. */
public class FormIdentite extends GridPane {

    private final TextField nom = new TextField();
    private final TextField prenom = new TextField();
    private final TextField email = new TextField();
    private final TextField telephone = new TextField();
    private final PasswordField motDePasse = new PasswordField();
    private final PasswordField confirmation = new PasswordField();

    public FormIdentite() {
        setHgap(16);
        setVgap(14);
        email.setPromptText("exemple@mail.com");
        telephone.setPromptText("77 000 00 00");
        motDePasse.setPromptText("6 caract\u00e8res minimum");
        ColumnConstraints moitie = new ColumnConstraints();
        moitie.setPercentWidth(50);
        getColumnConstraints().addAll(moitie, moitie);
        add(Composants.champ("Nom", nom), 0, 0);
        add(Composants.champ("Pr\u00e9nom", prenom), 1, 0);
        add(Composants.champ("Email", email), 0, 1);
        add(Composants.champ("T\u00e9l\u00e9phone", telephone), 1, 1);
        add(Composants.champ("Mot de passe", motDePasse), 0, 2);
        add(Composants.champ("Confirmer le mot de passe", confirmation), 1, 2);
    }

    public String getNom() { return nom.getText(); }
    public String getPrenom() { return prenom.getText(); }
    public String getEmail() { return email.getText(); }
    public String getTelephone() { return telephone.getText(); }
    public String getMotDePasse() { return motDePasse.getText(); }

    /** Retourne un message d'erreur si les deux mots de passe different, sinon null. */
    public String erreurConfirmation() {
        return motDePasse.getText().equals(confirmation.getText()) ? null
                : "Les deux mots de passe ne sont pas identiques.";
    }

    /** Pre-remplit le formulaire (utile pour les tests automatiques). */
    public void remplir(String n, String p, String e, String t, String mdp) {
        nom.setText(n);
        prenom.setText(p);
        email.setText(e);
        telephone.setText(t);
        motDePasse.setText(mdp);
        confirmation.setText(mdp);
    }
}
