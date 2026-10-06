package com.tontine.ui;

import com.tontine.model.ModePaiement;
import com.tontine.service.Libelles;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Choix du mode de paiement (Wave, Orange Money, especes) + numero pour le paiement mobile. */
public class ChoixPaiement extends VBox {

    private final ToggleGroup groupe = new ToggleGroup();
    private final ToggleButton wave = bouton(ModePaiement.WAVE);
    private final ToggleButton orange = bouton(ModePaiement.ORANGE_MONEY);
    private final ToggleButton especes = bouton(ModePaiement.ESPECE);
    private final TextField numero = new TextField();
    private final Label etiquetteNumero = Composants.etiquette("Num\u00e9ro Wave");
    private final VBox zoneNumero = new VBox(5, etiquetteNumero, numero);
    private final Label aideEspeces = Composants.sousTitre(
            "Le paiement en esp\u00e8ces est enregistr\u00e9 \u00ab en attente \u00bb puis confirm\u00e9 par le g\u00e9rant quand il re\u00e7oit l'argent.");

    public ChoixPaiement() {
        super(10);
        numero.setPromptText("77 000 00 00");
        HBox puces = new HBox(8, wave, orange, especes);
        wave.setSelected(true);
        groupe.selectedToggleProperty().addListener((o, ancien, nouveau) -> {
            if (nouveau == null) {
                ancien.setSelected(true); // un mode doit toujours etre choisi
            } else {
                miseAJour();
            }
        });
        getChildren().addAll(Composants.etiquette("Mode de paiement"), puces, zoneNumero, aideEspeces);
        miseAJour();
    }

    private ToggleButton bouton(ModePaiement m) {
        ToggleButton b = new ToggleButton(Libelles.mode(m));
        b.setToggleGroup(groupe);
        b.getStyleClass().add("puce");
        b.setUserData(m);
        return b;
    }

    private void miseAJour() {
        ModePaiement m = getMode();
        boolean mobile = m != ModePaiement.ESPECE;
        zoneNumero.setVisible(mobile);
        zoneNumero.setManaged(mobile);
        aideEspeces.setVisible(!mobile);
        aideEspeces.setManaged(!mobile);
        etiquetteNumero.setText("Num\u00e9ro " + Libelles.mode(m));
    }

    public ModePaiement getMode() {
        return (ModePaiement) groupe.getSelectedToggle().getUserData();
    }

    public String getNumero() {
        return numero.getText();
    }

    public void setNumero(String n) {
        numero.setText(n);
    }

    public void selectionner(ModePaiement m) {
        (m == ModePaiement.WAVE ? wave : m == ModePaiement.ORANGE_MONEY ? orange : especes).setSelected(true);
    }
}
