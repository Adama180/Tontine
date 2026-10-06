package com.tontine.ui;

import java.util.List;

import com.tontine.model.Notification;
import com.tontine.model.Utilisateur;
import com.tontine.service.NotificationService;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Liste des notifications d'un utilisateur (non lues mises en evidence). */
public class NotificationsView extends VBox {

    private final Utilisateur utilisateur;
    private final Runnable apresChangement;

    /** @param apresChangement appele quand le nombre de notifications non lues change (met a jour l'en-tete) */
    public NotificationsView(Utilisateur utilisateur, Runnable apresChangement) {
        super(12);
        this.utilisateur = utilisateur;
        this.apresChangement = apresChangement;
        construire();
    }

    private void construire() {
        getChildren().clear();
        List<Notification> liste = NotificationService.lister(utilisateur.getId());
        int nonLues = 0;
        for (Notification n : liste) {
            if (!n.isLue()) {
                nonLues++;
            }
        }

        Button toutLire = Composants.contour("Tout marquer comme lu");
        toutLire.setDisable(nonLues == 0);
        toutLire.setOnAction(e -> {
            NotificationService.marquerToutesLues(utilisateur.getId());
            changement();
        });
        Label resume = Composants.sousTitre(liste.isEmpty() ? "Aucune notification pour le moment."
                : nonLues + " non lue(s) sur " + liste.size());
        HBox haut = new HBox(12, resume, Composants.espaceur(), toutLire);
        haut.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(haut);

        for (Notification n : liste) {
            Label message = new Label(n.getMessage());
            message.setWrapText(true);
            message.getStyleClass().add(n.isLue() ? "notif-texte-lue" : "notif-texte");
            Label date = new Label(Aff.date(n.getDateEnvoi()));
            date.getStyleClass().add("pied");
            VBox texte = new VBox(4, message, date);
            HBox.setHgrow(texte, javafx.scene.layout.Priority.ALWAYS);
            HBox ligne = new HBox(12, texte);
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.getStyleClass().addAll("ligne-liste", n.isLue() ? "notif-lue" : "notif-nonlue");
            if (!n.isLue()) {
                Button lire = Composants.contour("Marquer comme lue");
                lire.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
                lire.setOnAction(e -> {
                    NotificationService.marquerLue(utilisateur.getId(), n.getId());
                    changement();
                });
                ligne.getChildren().add(lire);
            }
            getChildren().add(ligne);
        }
    }

    private void changement() {
        construire();
        if (apresChangement != null) {
            apresChangement.run();
        }
    }
}
