package com.tontine.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/** Page accessible sans compte : barre du haut + contenu qui change (explorer, rejoindre, creer). */
public class PagePublique extends BorderPane implements Fenetre {

    private final StackPane zone = new StackPane();

    public PagePublique() {
        getStyleClass().add("page");

        Node logo = Composants.logo(false);
        logo.setCursor(Cursor.HAND);
        logo.setOnMouseClicked(e -> Nav.aller(new AccueilView()));
        Button explorer = Composants.contour("Explorer les tontines");
        explorer.setOnAction(e -> Nav.aller(PagePublique.explorer()));
        Button connexion = Composants.contour("Se connecter");
        connexion.setOnAction(e -> Nav.aller(new ConnexionView(null)));
        Button inscription = Composants.primaire("Cr\u00e9er un compte");
        inscription.setOnAction(e -> Nav.aller(new InscriptionView()));
        HBox barre = new HBox(12, logo, Composants.espaceur(), explorer, connexion, inscription);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.getStyleClass().add("navbar");
        setTop(barre);

        zone.setAlignment(Pos.TOP_CENTER);
        zone.setPadding(new Insets(30, 40, 40, 40));
        ScrollPane defilement = new ScrollPane(zone);
        defilement.setFitToWidth(true);
        defilement.getStyleClass().add("defilement");
        setCenter(defilement);
    }

    @Override
    public void afficher(String titre, Node contenu) {
        if (contenu instanceof Region) {
            ((Region) contenu).setMaxWidth(1000);
        }
        zone.getChildren().setAll(contenu);
    }

    public static PagePublique explorer() {
        PagePublique p = new PagePublique();
        p.afficher("Explorer les tontines", new ExplorerPane(null, p));
        return p;
    }

    public static PagePublique creerTontine() {
        PagePublique p = new PagePublique();
        p.afficher("Cr\u00e9er ma tontine", new CreerTontineView(null, p));
        return p;
    }
}
