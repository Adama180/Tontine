package com.tontine.ui;

import com.tontine.model.Cycle;
import com.tontine.model.ModePaiement;
import com.tontine.model.Tontine;
import com.tontine.model.Transaction;
import com.tontine.model.Utilisateur;
import com.tontine.service.ConsultationService;
import com.tontine.service.CotisationService;
import com.tontine.service.Format;
import com.tontine.service.Libelles;
import com.tontine.service.MembreService;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Effectuer une cotisation : montant, mode de paiement (Wave, Orange Money, especes), confirmation. */
public class CotiserPane extends VBox {

    private final TextField montant = new TextField();
    private final ChoixPaiement paiement = new ChoixPaiement();
    private final Button payer = Composants.primaire("Payer ma cotisation");

    public CotiserPane(Utilisateur u, Tontine tontine) {
        super(14);
        MembreService membre = new MembreService();
        Tontine t = new ConsultationService().detail(tontine.getId());
        Cycle cycle = membre.cycleEnCours(t.getId());

        if (cycle == null) {
            Label titre = new Label("Aucun cycle en cours");
            titre.getStyleClass().add("carte-titre");
            getChildren().add(Composants.carte(titre, Composants.sousTitre(
                    "Vous pourrez cotiser d\u00e8s que le g\u00e9rant aura d\u00e9marr\u00e9 un nouveau cycle.")));
            return;
        }
        boolean fixe = !t.isFondsCommun();
        if (fixe && membre.aDejaCotiseDansCycle(u.getId(), t.getId())) {
            Label titre = new Label("Cotisation du cycle " + cycle.getNumero() + " d\u00e9j\u00e0 effectu\u00e9e");
            titre.getStyleClass().add("carte-titre");
            getChildren().add(Composants.carte(titre, Composants.sousTitre(
                    "Dans une tontine \u00e0 montant fixe, on cotise une seule fois par cycle. "
                            + "Retrouvez votre paiement dans l'onglet Historique.")));
            return;
        }

        Label titre = new Label("Cotiser - cycle " + cycle.getNumero());
        titre.getStyleClass().add("carte-titre");
        Label regle = Composants.sousTitre(fixe
                ? "Montant fixe : " + Format.montant(t.getMontantCotisation()) + " (une seule cotisation par cycle)."
                : "Montant minimum : " + Format.montant(t.getMontantCotisation())
                        + ". Vous pouvez cotiser plusieurs fois, et plus que le minimum.");
        montant.setText(Aff.nombre(t.getMontantCotisation()));
        montant.setEditable(!fixe);

        Label message = Composants.message();
        payer.setOnAction(e -> {
            Composants.cacher(message);
            try {
                double m = Saisie.montant(montant.getText(), "Montant");
                ModePaiement mode = paiement.getMode();
                if (!Nav.confirmer("Confirmer le paiement de " + Format.montant(m) + " par " + Libelles.mode(mode) + " ?")) {
                    return;
                }
                Transaction tx = new CotisationService().payerCotisation(u.getId(), t.getId(), m, mode, paiement.getNumero());
                if (mode == ModePaiement.ESPECE) {
                    Composants.montrer(message, "Paiement de " + Format.montant(m) + " enregistr\u00e9 en esp\u00e8ces. "
                            + "Il sera confirm\u00e9 par le g\u00e9rant quand il recevra l'argent.", false);
                } else {
                    Composants.montrer(message, "Cotisation de " + Format.montant(m) + " enregistr\u00e9e par " + Libelles.mode(mode)
                            + " (r\u00e9f\u00e9rence " + tx.getReference() + ").", false);
                }
                if (fixe) {
                    payer.setDisable(true);
                }
            } catch (RuntimeException ex) {
                Composants.montrer(message, Nav.messageErreur(ex), true);
            }
        });

        getChildren().add(Composants.carte(titre, regle, Composants.champ("Montant (FCFA)", montant), paiement, message, payer));
    }

    // acces pour les tests automatiques
    ChoixPaiement getPaiement() {
        return paiement;
    }

    Button getBoutonPayer() {
        return payer;
    }
}
