package com.tontine.ui;

import java.util.List;
import java.util.Map;

import com.tontine.model.Pret;
import com.tontine.model.StatutPret;
import com.tontine.model.Tontine;
import com.tontine.model.Utilisateur;
import com.tontine.service.ConsultationService;
import com.tontine.service.Format;
import com.tontine.service.MembreService;
import com.tontine.service.PretService;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/** Le gerant valide ou refuse les prets des membres. Son propre pret est valide par l'administrateur. */
public class PretsAValiderPane extends PaneGestion {

    private final Utilisateur gerant;
    private final Tontine tontine;
    private final PretService prets = new PretService();

    public PretsAValiderPane(Utilisateur gerant, Tontine tontine) {
        this.gerant = gerant;
        this.tontine = tontine;
        rafraichir();
    }

    @Override
    protected void construire() {
        Tontine t = new ConsultationService().detail(tontine.getId());
        Map<Integer, String> noms = new MembreService().nomsMembres(gerant.getId(), t.getId());

        getChildren().add(vide("Fonds disponible pour les pr\u00eats : " + Format.montant(prets.fondsDisponible(t.getId()))
                + ". Taux d'int\u00e9r\u00eat : " + (t.getTauxInteret() == null ? "non d\u00e9fini" : Aff.nombre(t.getTauxInteret()) + " %") + "."));

        // ---- demandes a valider
        List<Pret> aValider = prets.pretsAValider(gerant.getId(), t.getId());
        getChildren().add(titreSection("Demandes de pr\u00eat \u00e0 valider (" + aValider.size() + ")"));
        if (aValider.isEmpty()) {
            getChildren().add(vide("Aucune demande de pr\u00eat en attente."));
        }
        for (Pret p : aValider) {
            VBox infos = texte(noms.getOrDefault(p.getUserId(), "?") + " demande " + Format.montant(p.getMontant()) + " sur " + p.getNbJours() + " jours",
                    "Int\u00e9r\u00eat " + Format.montant(prets.interets(p, t)) + " - total \u00e0 rembourser " + Format.montant(prets.totalDu(p, t))
                            + (p.getMotif() == null || p.getMotif().isEmpty() ? "" : "\nMotif : " + p.getMotif())
                            + "\nDemand\u00e9 le " + Aff.date(p.getDateDemande()));
            Button accepter = Composants.primaire("Accorder");
            accepter.setOnAction(e -> action(() -> {
                if (Nav.confirmer("Accorder ce pr\u00eat de " + Format.montant(p.getMontant()) + " ?")) {
                    prets.validerPret(gerant.getId(), p.getId(), true);
                    succes("Pr\u00eat de " + Format.montant(p.getMontant()) + " accord\u00e9. Tous les membres en sont inform\u00e9s.");
                }
            }));
            Button refuser = Composants.contour("Refuser");
            refuser.setOnAction(e -> action(() -> {
                if (Nav.confirmer("Refuser ce pr\u00eat ?")) {
                    prets.validerPret(gerant.getId(), p.getId(), false);
                    succes("La demande de pr\u00eat a \u00e9t\u00e9 refus\u00e9e.");
                }
            }));
            getChildren().add(ligne(infos, refuser, accepter));
        }

        // ---- son propre pret
        for (Pret p : prets.mesPrets(gerant.getId())) {
            if (p.getTontineId() == t.getId() && p.getStatut() == StatutPret.EN_ATTENTE) {
                getChildren().add(vide("Votre propre demande de pr\u00eat de " + Format.montant(p.getMontant())
                        + " est en attente : elle sera valid\u00e9e par l'administrateur (r\u00e8gle anti-conflit d'int\u00e9r\u00eat)."));
            }
        }

        // ---- prets en cours
        getChildren().add(titreSection("Pr\u00eats en cours de remboursement"));
        int n = 0;
        for (Pret p : prets.listerPrets(gerant.getId(), t.getId())) {
            if (p.getStatut() == StatutPret.ACCORDE) {
                n++;
                getChildren().add(ligne(texte(noms.getOrDefault(p.getUserId(), "?") + " : " + Format.montant(p.getMontant()) + " sur " + p.getNbJours() + " jours",
                        "Total d\u00fb " + Format.montant(prets.totalDu(p, t)) + " - rembours\u00e9 " + Format.montant(p.getMontantRembourse())
                                + " - reste " + Format.montant(prets.resteAPayer(p, t))), Aff.badgePret(p.getStatut())));
            }
        }
        if (n == 0) {
            getChildren().add(vide("Aucun pr\u00eat en cours."));
        }
    }
}
