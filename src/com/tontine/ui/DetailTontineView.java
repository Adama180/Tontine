package com.tontine.ui;

import com.tontine.model.Adhesion;
import com.tontine.model.DemandeAdhesion;
import com.tontine.model.StatutAdhesion;
import com.tontine.model.StatutDemande;
import com.tontine.model.Tontine;
import com.tontine.model.Utilisateur;
import com.tontine.service.ConsultationService;
import com.tontine.service.DemandeService;
import com.tontine.service.Libelles;

import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Regles generales d'une tontine + bouton "Rejoindre cette tontine". */
public class DetailTontineView extends VBox {

    public DetailTontineView(Utilisateur utilisateur, Fenetre fenetre, Tontine t) {
        super(14);
        ConsultationService consultation = new ConsultationService();

        Hyperlink retour = new Hyperlink("\u2190 Retour \u00e0 la liste des tontines");
        retour.setOnAction(e -> fenetre.afficher("Explorer les tontines", new ExplorerPane(utilisateur, fenetre)));

        Label nom = Composants.titre(t.getNom());
        VBox contenu = Composants.carte(nom, Aff.badge(Libelles.type(t.getType())));
        if (t.getDescription() != null && !t.getDescription().trim().isEmpty()) {
            contenu.getChildren().add(Composants.sousTitre(t.getDescription()));
        }
        contenu.getChildren().add(Composants.sousTitre(Libelles.descriptionType(t.getType())));

        Label regles = new Label("R\u00e8gles de la tontine");
        regles.getStyleClass().add("carte-titre");
        contenu.getChildren().add(regles);
        for (String ligne : consultation.reglesGenerales(t)) {
            Label l = new Label("\u2022  " + ligne);
            l.setWrapText(true);
            contenu.getChildren().add(l);
        }
        Label confidentialite = new Label("Les soldes individuels et l'historique des transactions sont r\u00e9serv\u00e9s "
                + "aux membres accept\u00e9s.");
        confidentialite.getStyleClass().add("pied");
        confidentialite.setWrapText(true);
        contenu.getChildren().add(confidentialite);

        // ---- action possible selon la situation de la personne
        boolean complet = consultation.nombreMembres(t.getId()) >= t.getNbMembreMax();
        Label etat = Composants.message();
        Button rejoindre = Composants.primaire("Rejoindre cette tontine");
        rejoindre.setOnAction(e -> fenetre.afficher("Rejoindre " + t.getNom(), new RejoindreView(utilisateur, fenetre, t)));

        if (utilisateur != null) {
            Adhesion a = consultation.monAdhesion(utilisateur.getId(), t.getId());
            boolean enAttente = false;
            for (DemandeAdhesion d : new DemandeService().suivreDemandesAdhesion(utilisateur.getId())) {
                if (d.getTontineId() == t.getId() && d.getStatut() == StatutDemande.EN_ATTENTE) {
                    enAttente = true;
                }
            }
            if (a != null && a.getStatut() == StatutAdhesion.ACTIF) {
                Composants.montrer(etat, "Vous \u00eates d\u00e9j\u00e0 membre de cette tontine.", false);
                rejoindre.setDisable(true);
            } else if (enAttente) {
                Composants.montrer(etat, "Votre demande est en attente de validation par le g\u00e9rant.", false);
                rejoindre.setDisable(true);
            }
        }
        if (complet && !rejoindre.isDisabled()) {
            Composants.montrer(etat, "Cette tontine est compl\u00e8te pour le moment.", true);
            rejoindre.setDisable(true);
        }
        contenu.getChildren().addAll(etat, rejoindre);
        getChildren().addAll(retour, contenu);
    }
}
