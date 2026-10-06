package com.tontine.ui;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tontine.model.Pret;
import com.tontine.model.Tontine;
import com.tontine.model.Utilisateur;
import com.tontine.service.AdminService;
import com.tontine.service.ConsultationService;
import com.tontine.service.Format;
import com.tontine.service.PretService;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/** Prets demandes par un gerant : seul l'administrateur peut les valider (regle anti-conflit d'interet). */
public class PretsGerantsAdminPane extends PaneGestion {

    private final Utilisateur admin;
    private final AdminService service = new AdminService();
    private final PretService prets = new PretService();

    public PretsGerantsAdminPane(Utilisateur admin) {
        this.admin = admin;
        rafraichir();
    }

    @Override
    protected void construire() {
        Map<Integer, Utilisateur> users = service.listerUtilisateurs(admin.getId()).stream()
                .collect(Collectors.toMap(Utilisateur::getId, u -> u));
        ConsultationService consultation = new ConsultationService();
        List<Pret> attente = service.pretsGerantsEnAttente(admin.getId());

        getChildren().add(vide("Un gerant ne peut jamais valider son propre pr\u00eat : c'est vous qui d\u00e9cidez. "
                + "Tout pr\u00eat accord\u00e9 devient visible par tous les membres de la tontine."));
        getChildren().add(titreSection("Pr\u00eats de g\u00e9rants \u00e0 valider (" + attente.size() + ")"));
        if (attente.isEmpty()) {
            getChildren().add(vide("Aucun pr\u00eat de g\u00e9rant en attente."));
        }
        for (Pret p : attente) {
            Tontine t = consultation.detail(p.getTontineId());
            Utilisateur g = users.get(p.getUserId());
            VBox infos = texte((g == null ? "?" : g.getNomComplet()) + " demande " + Format.montant(p.getMontant()) + " sur " + p.getNbJours() + " jours",
                    "Tontine \u00ab " + t.getNom() + " \u00bb - fonds disponible " + Format.montant(prets.fondsDisponible(t.getId()))
                            + "\nInt\u00e9r\u00eat " + Format.montant(prets.interets(p, t)) + " - total \u00e0 rembourser " + Format.montant(prets.totalDu(p, t))
                            + (p.getMotif() == null || p.getMotif().isEmpty() ? "" : "\nMotif : " + p.getMotif())
                            + "\nDemand\u00e9 le " + Aff.date(p.getDateDemande()));
            Button accorder = Composants.primaire("Accorder");
            accorder.setOnAction(e -> action(() -> {
                if (Nav.confirmer("Accorder ce pr\u00eat de " + Format.montant(p.getMontant()) + " au g\u00e9rant ?")) {
                    service.validerPretGerant(admin.getId(), p.getId(), true);
                    succes("Pr\u00eat de " + Format.montant(p.getMontant()) + " accord\u00e9. Les membres de la tontine en sont inform\u00e9s.");
                }
            }));
            Button refuser = Composants.contour("Refuser");
            refuser.setOnAction(e -> action(() -> {
                if (Nav.confirmer("Refuser ce pr\u00eat ?")) {
                    service.validerPretGerant(admin.getId(), p.getId(), false);
                    succes("La demande de pr\u00eat du g\u00e9rant a \u00e9t\u00e9 refus\u00e9e.");
                }
            }));
            getChildren().add(ligne(infos, refuser, accorder));
        }
    }
}
