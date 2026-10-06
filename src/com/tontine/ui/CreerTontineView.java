package com.tontine.ui;

import com.tontine.model.Frequence;
import com.tontine.model.TypeTontine;
import com.tontine.model.Utilisateur;
import com.tontine.service.DemandeService;
import com.tontine.service.Libelles;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Demande pour devenir gerant et creer sa propre tontine (validee par l'administrateur).
 * Visiteur : cree aussi son compte. Utilisateur connecte : envoie juste la demande.
 */
public class CreerTontineView extends VBox {

    private final DemandeService demandes = new DemandeService();
    private final FormIdentite identite;
    private final TextField nom = new TextField();
    private final ComboBox<TypeTontine> type = new ComboBox<>();
    private final ComboBox<Frequence> frequence = new ComboBox<>();
    private final TextField montant = new TextField();
    private final TextField nbMembres = new TextField();
    private final ChoixPiece piece = new ChoixPiece();

    public CreerTontineView(Utilisateur utilisateur, Fenetre fenetre) {
        super(14);

        type.getItems().addAll(TypeTontine.values());
        type.setConverter(new StringConverter<TypeTontine>() {
            @Override
            public String toString(TypeTontine t) {
                return t == null ? "" : Libelles.type(t);
            }

            @Override
            public TypeTontine fromString(String s) {
                return null;
            }
        });
        type.getSelectionModel().select(TypeTontine.FONDS_COMMUN);
        frequence.getItems().addAll(Frequence.values());
        frequence.setConverter(new StringConverter<Frequence>() {
            @Override
            public String toString(Frequence f) {
                return f == null ? "" : Libelles.frequence(f);
            }

            @Override
            public Frequence fromString(String s) {
                return null;
            }
        });
        frequence.getSelectionModel().select(Frequence.MENSUELLE);
        type.setMaxWidth(Double.MAX_VALUE);
        frequence.setMaxWidth(Double.MAX_VALUE);
        montant.setPromptText("ex. 5000");
        nbMembres.setPromptText("ex. 10");
        nom.setPromptText("Nom unique dans toute l'application");

        Label descriptionType = Composants.sousTitre(Libelles.descriptionType(TypeTontine.FONDS_COMMUN));
        type.valueProperty().addListener((o, a, b) -> {
            if (b != null) {
                descriptionType.setText(Libelles.descriptionType(b));
            }
        });

        GridPane grille = new GridPane();
        grille.setHgap(16);
        grille.setVgap(14);
        ColumnConstraints moitie = new ColumnConstraints();
        moitie.setPercentWidth(50);
        grille.getColumnConstraints().addAll(moitie, moitie);
        grille.add(Composants.champ("Nom de la tontine", nom), 0, 0, 2, 1);
        grille.add(Composants.champ("Type de tontine", type), 0, 1);
        grille.add(Composants.champ("Fr\u00e9quence de cotisation", frequence), 1, 1);
        grille.add(descriptionType, 0, 2, 2, 1);
        grille.add(Composants.champ("Montant de la cotisation (FCFA)", montant), 0, 3);
        grille.add(Composants.champ("Nombre de membres", nbMembres), 1, 3);

        VBox carte = Composants.carte(
                Composants.titre("Cr\u00e9er ma propre tontine"),
                Composants.sousTitre("Votre demande est examin\u00e9e par l'administrateur. Si elle est accept\u00e9e, vous devenez "
                        + "g\u00e9rant : vous d\u00e9finirez ensuite les r\u00e8gles (p\u00e9nalit\u00e9, taux d'int\u00e9r\u00eat, dur\u00e9e des pr\u00eats...) "
                        + "et vous cotiserez vous aussi dans cette tontine."));

        if (utilisateur == null) {
            identite = new FormIdentite();
            carte.getChildren().addAll(Composants.sousTitre("Un compte sera cr\u00e9\u00e9 avec ces informations."), identite, piece);
        } else {
            identite = null;
            if (demandes.aPieceEnregistree(utilisateur.getId())) {
                carte.getChildren().add(Composants.sousTitre("Votre pi\u00e8ce d'identit\u00e9 d\u00e9j\u00e0 enregistr\u00e9e sera "
                        + "utilis\u00e9e pour cette demande : inutile de la renvoyer."));
            } else {
                carte.getChildren().add(piece);
            }
        }
        carte.getChildren().add(grille);

        Label message = Composants.message();
        Button envoyer = Composants.primaire("Envoyer ma demande");
        envoyer.setOnAction(e -> {
            Composants.cacher(message);
            double m;
            int n;
            try {
                m = Double.parseDouble(montant.getText().trim().replace(" ", "").replace(",", "."));
            } catch (NumberFormatException ex) {
                Composants.montrer(message, "Montant de cotisation invalide.", true);
                return;
            }
            try {
                n = Integer.parseInt(nbMembres.getText().trim());
            } catch (NumberFormatException ex) {
                Composants.montrer(message, "Nombre de membres invalide.", true);
                return;
            }
            try {
                if (utilisateur == null) {
                    String erreur = identite.erreurConfirmation();
                    if (erreur != null) {
                        Composants.montrer(message, erreur, true);
                        return;
                    }
                    demandes.inscrireEtDemanderCreation(identite.getNom(), identite.getPrenom(), identite.getEmail(),
                            identite.getTelephone(), identite.getMotDePasse(), nom.getText(), type.getValue(), m,
                            frequence.getValue(), n, piece.getFichier());
                    Nav.aller(new ConnexionView("Votre compte est cr\u00e9\u00e9 et votre demande de cr\u00e9ation a \u00e9t\u00e9 "
                            + "envoy\u00e9e \u00e0 l'administrateur. Connectez-vous pour suivre son \u00e9tat."));
                } else {
                    demandes.soumettreDemandeCreation(utilisateur.getId(), nom.getText(), type.getValue(), m,
                            frequence.getValue(), n, piece.getFichier());
                    fenetre.afficher("Mes demandes", new MesDemandesView(utilisateur));
                }
            } catch (RuntimeException ex) {
                Composants.montrer(message, Nav.messageErreur(ex), true);
            }
        });
        carte.getChildren().addAll(message, new HBox(12, envoyer));
        getChildren().add(carte);
    }

    // acces pour les tests automatiques
    void remplirTontine(String n, double m, int nb) {
        nom.setText(n);
        montant.setText(String.valueOf(m));
        nbMembres.setText(String.valueOf(nb));
    }

    FormIdentite getIdentite() {
        return identite;
    }

    ChoixPiece getPiece() {
        return piece;
    }
}
