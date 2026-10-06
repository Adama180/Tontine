package com.tontine.ui;

import java.util.List;
import java.util.Map;

import com.tontine.model.Tontine;
import com.tontine.model.Transaction;
import com.tontine.model.Utilisateur;
import com.tontine.service.CotisationService;
import com.tontine.service.Format;
import com.tontine.service.Libelles;
import com.tontine.service.MembreService;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Historique des transactions : le mien, ou celui de toute la tontine (lecture seule, regle de transparence). */
public class HistoriquePane extends VBox {

    private final Utilisateur u;
    private final Tontine t;
    private final CotisationService cotisations = new CotisationService();
    private final Map<Integer, String> noms;
    private final VBox zoneTable = new VBox();
    private final ToggleButton mon = new ToggleButton("Mon historique");
    private final ToggleButton tous = new ToggleButton("Historique de la tontine");
    private TableView<Transaction> table;

    public HistoriquePane(Utilisateur u, Tontine t) {
        super(12);
        this.u = u;
        this.t = t;
        this.noms = new MembreService().nomsMembres(u.getId(), t.getId());

        ToggleGroup groupe = new ToggleGroup();
        mon.setToggleGroup(groupe);
        tous.setToggleGroup(groupe);
        mon.getStyleClass().add("puce");
        tous.getStyleClass().add("puce");
        mon.setSelected(true);
        groupe.selectedToggleProperty().addListener((o, a, b) -> {
            if (b == null) {
                a.setSelected(true);
            } else {
                afficher(b == tous);
            }
        });
        Label note = Composants.sousTitre("L'historique de la tontine est visible par tous ses membres, en lecture seule.");
        HBox choix = new HBox(8, mon, tous);
        choix.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(choix, note, zoneTable);
        afficher(false);
    }

    private void afficher(boolean toute) {
        List<Transaction> liste = toute ? cotisations.historiqueTontine(u.getId(), t.getId())
                : cotisations.historiquePersonnel(u.getId(), t.getId());
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Aucune transaction pour le moment."));
        table.setPrefHeight(440);
        table.getColumns().add(colonne("Date", tx -> Aff.date(tx.getDate())));
        table.getColumns().add(colonne("Type", tx -> Aff.typeTransaction(tx.getType())));
        if (toute) {
            table.getColumns().add(colonne("Membre", tx -> noms.getOrDefault(tx.getUserId(), "?")));
        }
        table.getColumns().add(colonne("Montant", tx -> Format.montant(tx.getMontant())));
        table.getColumns().add(colonne("Mode", tx -> Libelles.mode(tx.getModePaiement())));
        table.getColumns().add(colonne("Statut", tx -> Aff.statutTransaction(tx.getStatut())));
        TableColumn<Transaction, String> reference = colonne("R\u00e9f\u00e9rence", tx -> tx.getReference() == null ? "" : tx.getReference());
        reference.setMinWidth(210);
        table.getColumns().add(reference);
        table.getItems().addAll(liste);
        zoneTable.getChildren().setAll(table);
    }

    private TableColumn<Transaction, String> colonne(String titre, java.util.function.Function<Transaction, String> valeur) {
        TableColumn<Transaction, String> c = new TableColumn<>(titre);
        c.setCellValueFactory(cell -> new SimpleStringProperty(valeur.apply(cell.getValue())));
        c.setSortable(false);
        return c;
    }

    // acces pour les tests automatiques
    TableView<Transaction> getTable() {
        return table;
    }

    ToggleButton getBoutonTous() {
        return tous;
    }
}
