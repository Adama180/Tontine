package com.tontine.ui;

import java.io.File;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/** Selection de la piece d'identite (PDF, JPG ou PNG). */
public class ChoixPiece extends VBox {

    private File fichier;
    private final Label nomFichier = new Label("Aucun fichier choisi");

    public ChoixPiece() {
        super(6);
        Button choisir = Composants.contour("Choisir un fichier...");
        choisir.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Pi\u00e8ce d'identit\u00e9");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "Pi\u00e8ces d'identit\u00e9 (PDF, JPG, PNG)", "*.pdf", "*.jpg", "*.jpeg", "*.png"));
            File f = fc.showOpenDialog(Nav.getStage());
            if (f != null) {
                setFichier(f);
            }
        });
        nomFichier.getStyleClass().add("sous-titre");
        Label aide = new Label("Copie de votre carte d'identit\u00e9 nationale - PDF, JPG ou PNG, 5 Mo maximum.");
        aide.getStyleClass().add("pied");
        aide.setWrapText(true);
        HBox ligne = new HBox(12, choisir, nomFichier);
        ligne.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        getChildren().addAll(Composants.etiquette("Pi\u00e8ce d'identit\u00e9"), ligne, aide);
    }

    public File getFichier() {
        return fichier;
    }

    public void setFichier(File f) {
        this.fichier = f;
        nomFichier.setText(f == null ? "Aucun fichier choisi" : f.getName());
    }
}
