package com.tontine.ui;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tontine.model.Tontine;
import com.tontine.model.Transaction;
import com.tontine.model.TypeTransaction;
import com.tontine.model.Utilisateur;
import com.tontine.service.AdminService;
import com.tontine.service.ConsultationService;
import com.tontine.service.Format;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/** Paiements en especes d'un gerant : il ne peut pas les confirmer lui-meme, l'administrateur s'en charge. */
public class PaiementsGerantsAdminPane extends PaneGestion {

    private final Utilisateur admin;
    private final AdminService service = new AdminService();

    public PaiementsGerantsAdminPane(Utilisateur admin) {
        this.admin = admin;
        rafraichir();
    }

    @Override
    protected void construire() {
        Map<Integer, Utilisateur> users = service.listerUtilisateurs(admin.getId()).stream()
                .collect(Collectors.toMap(Utilisateur::getId, u -> u));
        ConsultationService consultation = new ConsultationService();
        List<Transaction> attente = service.paiementsGerantsEnAttente(admin.getId());

        getChildren().add(vide("Les paiements en esp\u00e8ces des membres sont confirm\u00e9s par leur g\u00e9rant. "
                + "Ceux d'un g\u00e9rant (il cotise aussi dans sa tontine) sont confirm\u00e9s ici, par vous."));
        getChildren().add(titreSection("Paiements en esp\u00e8ces de g\u00e9rants \u00e0 confirmer (" + attente.size() + ")"));
        if (attente.isEmpty()) {
            getChildren().add(vide("Aucun paiement en attente."));
        }
        for (Transaction tx : attente) {
            Tontine t = consultation.detail(tx.getTontineId());
            Utilisateur g = users.get(tx.getUserId());
            String type = tx.getType() == TypeTransaction.REMBOURSEMENT ? "Remboursement de pr\u00eat" : "Cotisation";
            VBox infos = texte((g == null ? "?" : g.getNomComplet()) + " : " + type + " de " + Format.montant(tx.getMontant()),
                    "Tontine \u00ab " + t.getNom() + " \u00bb - " + Aff.date(tx.getDate()));
            Button confirmer = Composants.primaire("Confirmer la r\u00e9ception");
            confirmer.setOnAction(e -> action(() -> {
                if (Nav.confirmer("Confirmer que " + (g == null ? "le g\u00e9rant" : g.getNomComplet()) + " a remis " + Format.montant(tx.getMontant()) + " en esp\u00e8ces ?")) {
                    service.confirmerPaiementGerant(admin.getId(), tx.getId(), true);
                    succes("Paiement de " + Format.montant(tx.getMontant()) + " confirm\u00e9.");
                }
            }));
            Button refuser = Composants.contour("Refuser");
            refuser.setOnAction(e -> action(() -> {
                if (Nav.confirmer("Refuser ce paiement ? Il ne sera pas pris en compte.")) {
                    service.confirmerPaiementGerant(admin.getId(), tx.getId(), false);
                    succes("Paiement refus\u00e9.");
                }
            }));
            getChildren().add(ligne(infos, refuser, confirmer));
        }
    }
}
