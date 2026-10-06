package com.tontine.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Base des onglets du gerant : message de succes en haut (banniere), zone d'erreur, reconstruction
 * de l'ecran apres chaque action pour toujours afficher des donnees a jour.
 */
abstract class PaneGestion extends VBox {

    private String banniere;
    protected final Label zoneErreur = Composants.message();

    PaneGestion() {
        super(16);
    }

    /** Construit le contenu (appele par rafraichir). */
    protected abstract void construire();

    /** A appeler a la fin du constructeur de chaque sous-classe. */
    protected final void rafraichir() {
        getChildren().clear();
        if (banniere != null) {
            Label b = Composants.message();
            Composants.montrer(b, banniere, false);
            getChildren().add(b);
            banniere = null;
        }
        Composants.cacher(zoneErreur);
        getChildren().add(zoneErreur);
        try {
            construire();
        } catch (RuntimeException e) {
            erreur(e);
        }
    }

    /** Affiche un message de succes en haut puis reconstruit l'ecran. */
    protected final void succes(String message) {
        banniere = message;
        rafraichir();
    }

    protected final void erreur(RuntimeException e) {
        Composants.montrer(zoneErreur, Nav.messageErreur(e), true);
    }

    protected final void erreur(String message) {
        Composants.montrer(zoneErreur, message, true);
    }

    /** Execute une action et affiche l'erreur eventuelle (regle metier refusee). */
    protected final void action(Runnable r) {
        try {
            r.run();
        } catch (RuntimeException e) {
            erreur(e);
        }
    }

    protected final Label titreSection(String texte) {
        Label l = new Label(texte);
        l.getStyleClass().add("carte-titre");
        return l;
    }

    protected final VBox texte(String titre, String detail) {
        Label t = new Label(titre);
        t.getStyleClass().add("carte-titre");
        t.setWrapText(true);
        VBox v = new VBox(4, t);
        if (detail != null && !detail.isEmpty()) {
            Label d = Composants.sousTitre(detail);
            v.getChildren().add(d);
        }
        return v;
    }

    /** Une ligne de liste : texte a gauche, actions a droite. */
    protected final HBox ligne(Node gauche, Node... droite) {
        HBox h = new HBox(12, gauche, Composants.espaceur());
        for (Node n : droite) {
            if (n instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) n).setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE); // jamais de bouton tronque
            }
        }
        h.getChildren().addAll(droite);
        h.setAlignment(Pos.CENTER_LEFT);
        h.getStyleClass().add("ligne-liste");
        return h;
    }

    protected final Label vide(String texte) {
        Label l = new Label(texte);
        l.getStyleClass().add("sous-titre");
        l.setWrapText(true);
        return l;
    }
}
