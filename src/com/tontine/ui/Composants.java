package com.tontine.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/** Petits elements d'interface reutilises par tous les ecrans. */
public final class Composants {

    private Composants() {
    }

    public static Label titre(String texte) {
        Label l = new Label(texte);
        l.getStyleClass().add("titre");
        l.setWrapText(true);
        return l;
    }

    public static Label sousTitre(String texte) {
        Label l = new Label(texte);
        l.getStyleClass().add("sous-titre");
        l.setWrapText(true);
        return l;
    }

    public static Label etiquette(String texte) {
        Label l = new Label(texte);
        l.getStyleClass().add("champ-label");
        return l;
    }

    public static Button primaire(String texte) {
        Button b = new Button(texte);
        b.getStyleClass().add("btn-primaire");
        return b;
    }

    public static Button or(String texte) {
        Button b = new Button(texte);
        b.getStyleClass().add("btn-or");
        return b;
    }

    public static Button contour(String texte) {
        Button b = new Button(texte);
        b.getStyleClass().add("btn-contour");
        return b;
    }

    public static Button contourClair(String texte) {
        Button b = new Button(texte);
        b.getStyleClass().add("btn-contour-clair");
        return b;
    }

    /** Carte blanche arrondie avec ombre. */
    public static VBox carte(Node... enfants) {
        VBox v = new VBox(14, enfants);
        v.getStyleClass().add("carte");
        return v;
    }

    /** Espace extensible (pousse les elements suivants vers la droite d'un HBox). */
    public static Region espaceur() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    /** Message d'erreur ou de succes (cache tant qu'il n'a pas de texte). */
    public static Label message() {
        Label l = new Label();
        l.setWrapText(true);
        l.setVisible(false);
        l.setManaged(false);
        return l;
    }

    public static void montrer(Label l, String texte, boolean erreur) {
        l.getStyleClass().removeAll("message-erreur", "message-succes");
        l.getStyleClass().add(erreur ? "message-erreur" : "message-succes");
        l.setText(texte);
        l.setVisible(true);
        l.setManaged(true);
    }

    public static void cacher(Label l) {
        l.setVisible(false);
        l.setManaged(false);
    }

    /** Logo : pastille doree + nom de l'application. */
    public static Node logo(boolean clair) {
        Circle rond = new Circle(16);
        rond.getStyleClass().add("logo-rond");
        Label lettre = new Label("N");
        lettre.getStyleClass().add("logo-lettre");
        StackPane pastille = new StackPane(rond, lettre);
        Label nom = new Label("NAT Tontines");
        nom.getStyleClass().add(clair ? "logo-texte-clair" : "logo-texte");
        HBox h = new HBox(10, pastille, nom);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    /** Champ avec son etiquette au-dessus. */
    public static VBox champ(String libelle, Node saisie) {
        VBox v = new VBox(5, etiquette(libelle), saisie);
        return v;
    }
}
