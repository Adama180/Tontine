package com.tontine.ui;

import com.tontine.util.ConnexionBD;
import com.tontine.util.InitBase;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Fenetre principale de l'application. */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("NAT Tontines - Gestion de tontines");
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        Nav.init(stage);

        if (!ConnexionBD.tester()) {
            Nav.aller(vueBaseInaccessible());
        } else {
            try {
                InitBase.creerAdminParDefaut(); // garantit qu'un compte administrateur existe
            } catch (RuntimeException e) {
                System.err.println("Initialisation du compte administrateur impossible : " + e.getMessage());
            }
            Nav.aller(new AccueilView());
        }
        stage.show();
    }

    /** Ecran affiche si MySQL n'est pas joignable (XAMPP non demarre, base absente...). */
    private VBox vueBaseInaccessible() {
        Label titre = new Label("Base de donn\u00e9es inaccessible");
        titre.getStyleClass().add("titre");
        Label texte = new Label("Ouvrez le panneau XAMPP, d\u00e9marrez MySQL (bouton Start), puis cliquez sur R\u00e9essayer.\n"
                + "V\u00e9rifiez aussi que la base 'tontine' existe dans phpMyAdmin.");
        texte.setWrapText(true);
        Button reessayer = Composants.primaire("R\u00e9essayer");
        reessayer.setOnAction(e -> {
            if (ConnexionBD.tester()) {
                try {
                    InitBase.creerAdminParDefaut();
                } catch (RuntimeException ex) {
                    System.err.println(ex.getMessage());
                }
                Nav.aller(new AccueilView());
            }
        });
        VBox carte = Composants.carte(titre, texte, reessayer);
        carte.setMaxWidth(480);
        VBox fond = new VBox(carte);
        fond.getStyleClass().add("fond");
        fond.setAlignment(javafx.geometry.Pos.CENTER);
        return fond;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
