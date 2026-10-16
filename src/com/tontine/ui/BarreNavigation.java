package com.tontine.ui;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/** Barre du haut des pages publiques : logo, explorer, connexion, inscription. S'allege sur une fenetre etroite. */
public class BarreNavigation extends HBox {

    public BarreNavigation() {
        super(12);
        getStyleClass().add("navbar");
        setAlignment(Pos.CENTER_LEFT);

        Node logo = Composants.logo(false);
        logo.setCursor(Cursor.HAND);
        logo.setOnMouseClicked(e -> Nav.aller(new AccueilView()));
        Button explorer = Composants.contour("Explorer les tontines");
        explorer.setOnAction(e -> Nav.aller(PagePublique.explorer()));
        Button connexion = Composants.contour("Se connecter");
        connexion.setOnAction(e -> Nav.aller(new ConnexionView(null)));
        Button inscription = Composants.primaire("Cr\u00e9er un compte");
        inscription.setOnAction(e -> Nav.aller(new InscriptionView()));

        getChildren().addAll(logo, Composants.espaceur(), explorer, connexion, inscription);
        Adaptatif.surLargeur(this, 780, etroit -> {
            explorer.setVisible(!etroit);
            explorer.setManaged(!etroit);
        });
    }
}
