package com.tontine.ui;

import com.tontine.model.TypeTontine;
import com.tontine.service.ConsultationService;
import com.tontine.service.Libelles;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Page d'accueil publique : presentation de la plateforme (aucun compte necessaire). */
public class AccueilView extends BorderPane {

    public AccueilView() {
        getStyleClass().add("page");

        Button connexion = Composants.contour("Se connecter");
        connexion.setOnAction(e -> Nav.aller(new ConnexionView(null)));
        Button inscription = Composants.primaire("Cr\u00e9er un compte");
        inscription.setOnAction(e -> Nav.aller(new InscriptionView()));
        Button explorerBarre = Composants.contour("Explorer les tontines");
        explorerBarre.setOnAction(e -> Nav.aller(PagePublique.explorer()));
        HBox barre = new HBox(12, Composants.logo(false), Composants.espaceur(), explorerBarre, connexion, inscription);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.getStyleClass().add("navbar");
        setTop(barre);

        // ---- bandeau principal
        Label titre = new Label("Vos tontines, en toute transparence.");
        titre.getStyleClass().add("hero-titre");
        titre.setWrapText(true);
        Label texte = new Label("Fini les cahiers perdus et les erreurs de calcul : suivez les cotisations, les pr\u00eats "
                + "et les tours de chaque tontine, avec un historique que tous les membres peuvent consulter.");
        texte.getStyleClass().add("hero-texte");
        texte.setWrapText(true);
        texte.setMaxWidth(640);
        Button commencer = Composants.or("Explorer les tontines");
        commencer.setOnAction(e -> Nav.aller(PagePublique.explorer()));
        Button seConnecter = Composants.contourClair("Cr\u00e9er ma propre tontine");
        seConnecter.setOnAction(e -> Nav.aller(PagePublique.creerTontine()));
        VBox hero = new VBox(18, titre, texte, new HBox(12, commencer, seConnecter));
        hero.getStyleClass().add("hero");

        // ---- les trois types de tontines (texte issu du cahier des charges)
        Label t1 = new Label("Trois types de tontines");
        t1.getStyleClass().add("titre");
        HBox types = new HBox(18);
        for (TypeTontine type : new ConsultationService().listerTypes()) {
            Label nom = new Label(Libelles.type(type));
            nom.getStyleClass().add("carte-titre");
            Label desc = new Label(Libelles.descriptionType(type));
            desc.setWrapText(true);
            desc.getStyleClass().add("sous-titre");
            VBox c = Composants.carte(nom, desc);
            c.setPrefWidth(300);
            HBox.setHgrow(c, Priority.ALWAYS);
            types.getChildren().add(c);
        }

        // ---- les atouts
        Label t2 = new Label("Pourquoi NAT Tontines ?");
        t2.getStyleClass().add("titre");
        HBox atouts = new HBox(18,
                atout("Transparence", "Chaque membre consulte l'historique complet de sa tontine : cotisations, pr\u00eats, versements."),
                atout("S\u00e9curit\u00e9", "Pi\u00e8ce d'identit\u00e9 v\u00e9rifi\u00e9e, adh\u00e9sion valid\u00e9e par le g\u00e9rant, journal d'audit pour l'administrateur."),
                atout("Paiement simple", "Cotisez par Wave, Orange Money ou en esp\u00e8ces, confirm\u00e9es par le g\u00e9rant."));

        Label pied = new Label("Application de gestion de tontines - projet NAT");
        pied.getStyleClass().add("pied");

        VBox section = new VBox(18, t1, types, spacer(10), t2, atouts, spacer(10), pied);
        section.setPadding(new Insets(36, 40, 30, 40));

        ScrollPane defilement = new ScrollPane(new VBox(hero, section));
        defilement.setFitToWidth(true);
        defilement.getStyleClass().add("defilement");
        setCenter(defilement);
    }

    private VBox atout(String titre, String texte) {
        Label t = new Label(titre);
        t.getStyleClass().add("carte-titre-or");
        Label d = new Label(texte);
        d.setWrapText(true);
        d.getStyleClass().add("sous-titre");
        VBox c = Composants.carte(t, d);
        c.setPrefWidth(300);
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    private javafx.scene.layout.Region spacer(double h) {
        javafx.scene.layout.Region r = new javafx.scene.layout.Region();
        r.setPrefHeight(h);
        return r;
    }
}
