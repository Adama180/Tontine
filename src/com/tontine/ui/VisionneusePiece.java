package com.tontine.ui;

import java.io.File;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Affichage de la piece d'identite DANS la plateforme : une petite vignette cliquable dans la liste des
 * demandes, et une fenetre d'agrandissement (zoom, ajustement a la fenetre) pour comparer la photo avec
 * l'identite declaree avant d'accepter ou de refuser. Les PDF (non affichables par JavaFX) s'ouvrent avec
 * le lecteur de l'ordinateur.
 */
public final class VisionneusePiece {

    private VisionneusePiece() {
    }

    private static boolean estImage(String chemin) {
        if (chemin == null) {
            return false;
        }
        String n = chemin.toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png");
    }

    /** Vignette cliquable (clic = agrandir). 'infos' : texte d'identite a afficher a cote de la grande image. */
    public static Node vignette(String chemin, String titre, String infos, Consumer<String> erreur) {
        StackPane cadre = new StackPane();
        cadre.getStyleClass().add("vignette-piece");
        cadre.setMinSize(96, 66);
        cadre.setPrefSize(96, 66);
        cadre.setMaxSize(96, 66);
        File f = chemin == null ? null : new File(chemin);
        if (f != null && f.isFile() && estImage(chemin)) {
            Image im = new Image(f.toURI().toString(), 192, 132, true, true, true);
            ImageView iv = new ImageView(im);
            iv.setFitWidth(94);
            iv.setFitHeight(64);
            iv.setPreserveRatio(true);
            cadre.getChildren().add(iv);
        } else {
            String txt = f != null && f.isFile() ? "PDF" : "Aucune\npi\u00e8ce";
            Label l = new Label(txt);
            l.getStyleClass().add("vignette-texte");
            l.setAlignment(Pos.CENTER);
            cadre.getChildren().add(l);
        }
        if (f != null && f.isFile()) {
            cadre.setStyle("-fx-cursor: hand;");
            cadre.setOnMouseClicked(e -> voir(chemin, titre, infos, erreur));
        }
        return cadre;
    }

    /** Ouvre la piece : fenetre integree pour une image, programme par defaut pour un PDF. */
    public static void voir(String chemin, String titre, String infos, Consumer<String> erreur) {
        File f = chemin == null ? null : new File(chemin);
        if (f == null || !f.isFile()) {
            erreur.accept("Fichier introuvable : " + chemin);
            return;
        }
        if (!estImage(chemin)) {
            Fichiers.ouvrir(chemin, erreur);
            return;
        }
        Image image = new Image(f.toURI().toString());
        if (image.isError()) {
            erreur.accept("Impossible d'afficher l'image de la pi\u00e8ce.");
            return;
        }
        Stage fen = new Stage();
        fen.initOwner(Nav.getStage());
        fen.initModality(Modality.WINDOW_MODAL);
        fen.setTitle("Pi\u00e8ce d'identit\u00e9 - " + titre);

        ImageView iv = new ImageView(image);
        iv.setPreserveRatio(true);
        ScrollPane defil = new ScrollPane(new StackPane(iv));
        defil.getStyleClass().add("visionneuse-fond");
        double[] largeurDeBase = { 720 };
        double[] zoom = { 1.0 };
        Runnable appliquer = () -> iv.setFitWidth(Math.max(120, largeurDeBase[0] * zoom[0]));
        appliquer.run();

        Label niveau = new Label("100 %");
        Button plus = Composants.contour("Zoom +");
        Button moins = Composants.contour("Zoom -");
        Button ajuster = Composants.contour("Ajuster");
        Runnable maj = () -> {
            appliquer.run();
            niveau.setText(Math.round(zoom[0] * 100) + " %");
        };
        plus.setOnAction(e -> {
            zoom[0] = Math.min(4.0, zoom[0] * 1.25);
            maj.run();
        });
        moins.setOnAction(e -> {
            zoom[0] = Math.max(0.25, zoom[0] / 1.25);
            maj.run();
        });
        ajuster.setOnAction(e -> {
            zoom[0] = 1.0;
            maj.run();
        });
        defil.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                zoom[0] = e.getDeltaY() > 0 ? Math.min(4.0, zoom[0] * 1.1) : Math.max(0.25, zoom[0] / 1.1);
                maj.run();
                e.consume();
            }
        });

        Button fermer = Composants.primaire("Fermer");
        fermer.setOnAction(e -> fen.close());
        HBox barre = new HBox(10, moins, niveau, plus, ajuster, Composants.espaceur(), fermer);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.setPadding(new Insets(12, 16, 12, 16));

        Label entete = new Label(infos == null || infos.isEmpty() ? titre : infos);
        entete.setWrapText(true);
        entete.getStyleClass().add("visionneuse-info");
        Label aide = new Label("Comparez la photo avec le nom et les informations ci-dessus avant d'accepter. "
                + "Astuce : Ctrl + molette pour zoomer.");
        aide.setWrapText(true);
        aide.getStyleClass().add("sous-titre");
        VBox haut = new VBox(4, entete, aide);
        haut.setPadding(new Insets(14, 16, 10, 16));

        BorderPane racine = new BorderPane();
        racine.setTop(haut);
        racine.setCenter(defil);
        racine.setBottom(barre);
        Scene sc = new Scene(racine, 860, 640);
        sc.getStylesheets().addAll(Nav.getStage().getScene().getStylesheets());
        fen.setScene(sc);
        // l'image s'ajuste a la largeur de la fenetre au depart
        defil.viewportBoundsProperty().addListener((o, a, b) -> {
            if (zoom[0] == 1.0) {
                largeurDeBase[0] = Math.max(200, b.getWidth() - 24);
                appliquer.run();
            } else {
                largeurDeBase[0] = Math.max(200, b.getWidth() - 24);
            }
        });
        fen.show();
    }
}
