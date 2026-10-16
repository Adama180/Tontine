package com.tontine.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/** Page accessible sans compte : barre du haut + contenu qui change (explorer, rejoindre, creer). */
public class PagePublique extends BorderPane implements Fenetre {

    private final StackPane zone = new StackPane();

    public PagePublique() {
        getStyleClass().add("page");
        setTop(new BarreNavigation());

        zone.setAlignment(Pos.TOP_CENTER);
        zone.setPadding(new Insets(30, 40, 40, 40));
        ScrollPane defilement = new ScrollPane(zone);
        defilement.setFitToWidth(true);
        defilement.getStyleClass().add("defilement");
        setCenter(defilement);
        Adaptatif.surLargeur(this, 760, etroit -> {
            Adaptatif.classeCompact(this, etroit);
            zone.setPadding(etroit ? new Insets(16, 14, 24, 14) : new Insets(30, 40, 40, 40));
        });
    }

    @Override
    public void afficher(String titre, Node contenu) {
        if (contenu instanceof Region) {
            ((Region) contenu).setMaxWidth(1000);
        }
        zone.getChildren().setAll(contenu);
        Effets.fondu(contenu);
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
