package com.tontine.ui;

import java.util.Map;

import com.tontine.model.Adhesion;
import com.tontine.model.StatutAdhesion;
import com.tontine.model.Tontine;
import com.tontine.model.Utilisateur;
import com.tontine.service.AdhesionService;
import com.tontine.service.CycleService;
import com.tontine.service.Format;
import com.tontine.service.Indicateurs;
import com.tontine.service.LigneMembre;
import com.tontine.service.Libelles;
import com.tontine.service.StatistiquesService;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Situation personnelle, indicateurs de la tontine et liste des membres (transparence). */
public class VueEnsemblePane extends VBox {

    public VueEnsemblePane(Utilisateur u, Tontine t) {
        super(18);
        StatistiquesService stats = new StatistiquesService();

        // ---- ma situation
        Label t1 = new Label("Ma situation");
        t1.getStyleClass().add("carte-titre");
        getChildren().addAll(t1, cartes(stats.activiteMembre(u.getId(), t.getId()), "Tontine", "R\u00f4le"));

        // ---- la tontine
        Label t2 = new Label("La tontine");
        t2.getStyleClass().add("carte-titre");
        getChildren().addAll(t2, cartes(stats.tontine(u.getId(), t.getId()), "Tontine"));

        if (t.isTourDeRole()) {
            try {
                Utilisateur prochain = new CycleService().prochainBeneficiaire(t.getId());
                boolean moi = prochain.getId() == u.getId();
                Label l = new Label(moi ? "C'est votre tour de recevoir la cagnotte au prochain versement !"
                        : "Prochain b\u00e9n\u00e9ficiaire : " + prochain.getNomComplet());
                l.getStyleClass().add("message-succes");
                l.setWrapText(true);
                getChildren().add(l);
            } catch (RuntimeException e) {
                // pas encore de membre actif : rien a afficher
            }
        }

        // ---- membres
        Label t3 = new Label("Membres de la tontine");
        t3.getStyleClass().add("carte-titre");
        GridPane grille = new GridPane();
        grille.setHgap(16);
        grille.setVgap(10);
        ColumnConstraints large = new ColumnConstraints();
        large.setPercentWidth(34);
        ColumnConstraints etroite = new ColumnConstraints();
        etroite.setPercentWidth(22);
        grille.getColumnConstraints().addAll(large, etroite, etroite, etroite);
        String[] titres = { "Membre", "R\u00f4le", "Total cotis\u00e9", "Solde" };
        for (int i = 0; i < titres.length; i++) {
            grille.add(Composants.etiquette(titres[i]), i, 0);
        }
        int ligne = 1;
        for (LigneMembre m : new AdhesionService().listerMembres(u.getId(), t.getId())) {
            Adhesion a = m.getAdhesion();
            if (a.getStatut() != StatutAdhesion.ACTIF) {
                continue;
            }
            Label nom = new Label(m.getUtilisateur().getNomComplet() + (a.getUserId() == u.getId() ? " (vous)" : ""));
            Label role = new Label(Libelles.role(a.getRole()));
            role.getStyleClass().add(a.isGerant() ? "badge" : "sous-titre");
            grille.add(nom, 0, ligne);
            grille.add(role, 1, ligne);
            grille.add(new Label(Format.montant(a.getTotalCotise())), 2, ligne);
            grille.add(new Label(Format.montant(a.getSolde())), 3, ligne);
            ligne++;
        }
        getChildren().addAll(t3, Composants.carte(grille));
    }

    private FlowPane cartes(Indicateurs ind, String... ignorer) {
        FlowPane flux = new FlowPane(14, 14);
        for (Map.Entry<String, String> e : ind.getValeurs().entrySet()) {
            boolean saute = false;
            for (String s : ignorer) {
                if (s.equals(e.getKey())) {
                    saute = true;
                }
            }
            if (saute) {
                continue;
            }
            Label valeur = new Label(e.getValue());
            valeur.getStyleClass().add("stat-valeur");
            valeur.setWrapText(true);
            Label libelle = new Label(e.getKey());
            libelle.getStyleClass().add("stat-libelle");
            libelle.setWrapText(true);
            VBox c = new VBox(6, valeur, libelle);
            c.getStyleClass().add("stat-card");
            c.setPrefWidth(230);
            flux.getChildren().add(c);
        }
        return flux;
    }
}
