package com.tontine.ui;

import com.tontine.model.Tontine;
import com.tontine.model.Utilisateur;
import com.tontine.service.DemandeService;

import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Formulaire "Rejoindre cette tontine".
 * Visiteur : cree son compte ET sa demande en une seule action. Utilisateur connecte : envoie juste la demande.
 */
public class RejoindreView extends VBox {

    private final DemandeService demandes = new DemandeService();
    private final ChoixPiece piece = new ChoixPiece();
    private final FormIdentite identite;

    public RejoindreView(Utilisateur utilisateur, Fenetre fenetre, Tontine t) {
        super(14);
        Hyperlink retour = new Hyperlink("\u2190 Retour aux r\u00e8gles de la tontine");
        retour.setOnAction(e -> fenetre.afficher(t.getNom(), new DetailTontineView(utilisateur, fenetre, t)));

        VBox carte = Composants.carte(
                Composants.titre("Rejoindre \u00ab " + t.getNom() + " \u00bb"),
                Composants.sousTitre("Votre demande sera examin\u00e9e par le g\u00e9rant de la tontine, qui consulte vos "
                        + "informations et votre pi\u00e8ce d'identit\u00e9 avant de l'accepter ou de la refuser."));

        if (utilisateur == null) {
            identite = new FormIdentite();
            carte.getChildren().addAll(Composants.sousTitre("Un compte sera cr\u00e9\u00e9 avec ces informations."), identite);
        } else {
            identite = null;
            Label resume = new Label(utilisateur.getNomComplet() + "  -  " + utilisateur.getEmail() + "  -  "
                    + utilisateur.getTelephone());
            resume.getStyleClass().add("carte-titre");
            resume.setWrapText(true);
            carte.getChildren().addAll(Composants.etiquette("Demande faite avec votre compte"), resume);
        }
        if (utilisateur != null && demandes.aPieceEnregistree(utilisateur.getId())) {
            carte.getChildren().add(Composants.sousTitre("Votre pi\u00e8ce d'identit\u00e9 d\u00e9j\u00e0 enregistr\u00e9e sera utilis\u00e9e pour "
                    + "cette demande : inutile de la renvoyer."));
        } else {
            carte.getChildren().add(piece);
        }

        Label message = Composants.message();
        Button envoyer = Composants.primaire("Envoyer ma demande");
        envoyer.setOnAction(e -> {
            Composants.cacher(message);
            try {
                if (utilisateur == null) {
                    String erreur = identite.erreurConfirmation();
                    if (erreur != null) {
                        Composants.montrer(message, erreur, true);
                        return;
                    }
                    demandes.inscrireEtDemanderAdhesion(identite.getNom(), identite.getPrenom(), identite.getEmail(),
                            identite.getTelephone(), identite.getMotDePasse(), t.getId(), piece.getFichier());
                    Nav.aller(new ConnexionView("Votre compte est cr\u00e9\u00e9 et votre demande pour \u00ab " + t.getNom()
                            + " \u00bb a \u00e9t\u00e9 envoy\u00e9e au g\u00e9rant. Connectez-vous pour suivre son \u00e9tat."));
                } else {
                    demandes.demanderAdhesion(utilisateur.getId(), t.getId(), piece.getFichier());
                    fenetre.afficher("Mes demandes", new MesDemandesView(utilisateur));
                }
            } catch (RuntimeException ex) {
                Composants.montrer(message, Nav.messageErreur(ex), true);
            }
        });
        Button annuler = Composants.contour("Annuler");
        annuler.setOnAction(e -> fenetre.afficher(t.getNom(), new DetailTontineView(utilisateur, fenetre, t)));
        carte.getChildren().addAll(message, new HBox(12, envoyer, annuler));
        getChildren().addAll(retour, carte);
    }
}
