package com.tontine.ui;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tontine.model.JournalAudit;
import com.tontine.model.Utilisateur;
import com.tontine.service.AdminService;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/** Journal d'audit : toutes les actions sensibles, avec recherche. */
public class JournalAuditPane extends PaneGestion {

    private static final int LIMITE = 500;

    private final Utilisateur admin;
    private final TextField recherche = new TextField();
    private final Label resume = new Label();
    private final TableView<JournalAudit> table = new TableView<>();
    private List<JournalAudit> toutes;
    private Map<Integer, String> noms;

    public JournalAuditPane(Utilisateur admin) {
        this.admin = admin;
        recherche.setPromptText("Filtrer : action, utilisateur ou d\u00e9tail");
        recherche.setPrefWidth(420);
        resume.getStyleClass().add("sous-titre");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Aucune action enregistr\u00e9e."));
        table.setPrefHeight(500);
        table.getColumns().add(colonne("Date", j -> Aff.date(j.getDate()), 130));
        table.getColumns().add(colonne("Utilisateur", j -> nom(j), 160));
        table.getColumns().add(colonne("Action", j -> Aff.actionAudit(j.getAction()), 200));
        table.getColumns().add(colonne("D\u00e9tails", j -> j.getDetails() == null ? "" : j.getDetails(), 380));
        recherche.textProperty().addListener((o, a, b) -> filtrer());
        rafraichir();
    }

    @Override
    protected void construire() {
        AdminService service = new AdminService();
        toutes = service.consulterJournalAudit(admin.getId(), LIMITE);
        noms = service.listerUtilisateurs(admin.getId()).stream()
                .collect(Collectors.toMap(Utilisateur::getId, Utilisateur::getNomComplet));
        Button actualiser = Composants.contour("Actualiser");
        actualiser.setOnAction(e -> rafraichir());
        HBox haut = new HBox(12, recherche, Composants.espaceur(), actualiser);
        haut.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        getChildren().addAll(haut, resume, table);
        filtrer();
    }

    private void filtrer() {
        if (toutes == null) {
            return;
        }
        String q = recherche.getText() == null ? "" : recherche.getText().trim().toLowerCase();
        table.getItems().clear();
        for (JournalAudit j : toutes) {
            String texte = (Aff.actionAudit(j.getAction()) + " " + nom(j) + " " + (j.getDetails() == null ? "" : j.getDetails())).toLowerCase();
            if (q.isEmpty() || texte.contains(q)) {
                table.getItems().add(j);
            }
        }
        resume.setText(table.getItems().size() + " action(s) affich\u00e9e(s) sur " + toutes.size()
                + " (les " + LIMITE + " plus r\u00e9centes au maximum).");
    }

    private String nom(JournalAudit j) {
        return j.getUserId() == null ? "Syst\u00e8me" : noms.getOrDefault(j.getUserId(), "?");
    }

    private TableColumn<JournalAudit, String> colonne(String titre, java.util.function.Function<JournalAudit, String> valeur, double largeur) {
        TableColumn<JournalAudit, String> c = new TableColumn<>(titre);
        c.setCellValueFactory(cell -> new SimpleStringProperty(valeur.apply(cell.getValue())));
        c.setSortable(false);
        c.setMinWidth(largeur / 2);
        c.setPrefWidth(largeur);
        return c;
    }

    // acces pour les tests automatiques
    TableView<JournalAudit> getTable() { return table; }
    TextField getRecherche() { return recherche; }
}
