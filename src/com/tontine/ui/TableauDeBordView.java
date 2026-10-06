package com.tontine.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.tontine.model.Adhesion;
import com.tontine.model.Tontine;
import com.tontine.model.Utilisateur;
import com.tontine.service.AdminService;
import com.tontine.service.AuthService;
import com.tontine.service.ConsultationService;
import com.tontine.service.Format;
import com.tontine.service.Indicateurs;
import com.tontine.service.Libelles;
import com.tontine.service.NotificationService;
import com.tontine.service.StatistiquesService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Espace connecte : barre laterale (menu selon le role) + zone de contenu.
 * Les sections des etapes suivantes s'ajoutent avec ajouterMenu(...).
 */
public class TableauDeBordView extends BorderPane implements Fenetre {

    private final Utilisateur utilisateur;
    private final VBox menu = new VBox(4);
    private final List<Button> boutons = new ArrayList<>();
    private final Map<String, Button> parLibelle = new LinkedHashMap<>();
    private final StackPane contenu = new StackPane();
    private final Label titrePage = new Label();
    private final Hyperlink lienNotifications = new Hyperlink();

    public TableauDeBordView(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        getStyleClass().add("page");
        setLeft(barreLaterale());
        setCenter(zoneCentrale());

        if (utilisateur.isAdmin()) {
            ajouterMenu("Accueil", this::accueilAdmin);
            ajouterMenu("Demandes de cr\u00e9ation", () -> new DemandesCreationAdminPane(utilisateur));
            ajouterMenu("Utilisateurs", () -> new UtilisateursAdminPane(utilisateur));
            ajouterMenu("Tontines", () -> new TontinesAdminPane(utilisateur));
            ajouterMenu("Pr\u00eats des g\u00e9rants", () -> new PretsGerantsAdminPane(utilisateur));
            ajouterMenu("Paiements des g\u00e9rants", () -> new PaiementsGerantsAdminPane(utilisateur));
            ajouterMenu("Tirages au sort", () -> new TiragesAdminPane(utilisateur));
            ajouterMenu("Versements", () -> new VersementsAdminPane(utilisateur));
            ajouterMenu("Journal d'audit", () -> new JournalAuditPane(utilisateur));
        } else {
            ajouterMenu("Accueil", this::accueilUtilisateur);
            ajouterMenu("Explorer les tontines", () -> new ExplorerPane(utilisateur, this));
            ajouterMenu("Cr\u00e9er ma tontine", () -> new CreerTontineView(utilisateur, this));
            ajouterMenu("Mes demandes", () -> new MesDemandesView(utilisateur));
            ajouterMenu("Mes tontines", () -> new MesTontinesView(utilisateur, this));
        }
        ajouterMenu("Notifications", () -> new NotificationsView(utilisateur, this::rafraichirNotifications));
        ajouterMenu("Mon profil", () -> new ProfilView(utilisateur));
        boutons.get(0).fire();
    }

    // ------------------------------------------------------------------ structure

    private Node barreLaterale() {
        Label nom = new Label(utilisateur.getNomComplet());
        nom.getStyleClass().add("sidebar-nom");
        nom.setWrapText(true);
        Label email = new Label(utilisateur.getEmail());
        email.getStyleClass().add("sidebar-info");
        email.setWrapText(true);
        Label badge = new Label(utilisateur.isAdmin() ? "Administrateur" : "Utilisateur");
        badge.getStyleClass().add("badge");
        VBox profil = new VBox(4, nom, email, badge);
        profil.setPadding(new Insets(14, 6, 14, 6));

        Button deconnexion = new Button("D\u00e9connexion");
        deconnexion.getStyleClass().add("menu-btn");
        deconnexion.setMaxWidth(Double.MAX_VALUE);
        deconnexion.setOnAction(e -> {
            new AuthService().deconnecter();
            Nav.aller(new AccueilView());
        });

        Region espace = new Region();
        VBox.setVgrow(espace, Priority.ALWAYS);
        VBox barre = new VBox(10, Composants.logo(true), profil, menu, espace, deconnexion);
        barre.getStyleClass().add("sidebar");
        barre.setPrefWidth(250);
        barre.setMinWidth(250);
        return barre;
    }

    private Node zoneCentrale() {
        titrePage.getStyleClass().add("titre");
        lienNotifications.getStyleClass().add("lien-notif");
        lienNotifications.setOnAction(e -> {
            Button b = parLibelle.get("Notifications");
            if (b != null) {
                b.fire();
            }
        });
        rafraichirNotifications();
        HBox entete = new HBox(12, titrePage, Composants.espaceur(), lienNotifications);
        entete.setAlignment(Pos.CENTER_LEFT);
        entete.getStyleClass().add("entete-page");

        ScrollPane defilement = new ScrollPane(contenu);
        defilement.setFitToWidth(true);
        defilement.getStyleClass().add("defilement");
        contenu.setPadding(new Insets(24, 30, 30, 30));
        contenu.setAlignment(Pos.TOP_LEFT);

        BorderPane zone = new BorderPane();
        zone.setTop(entete);
        zone.setCenter(defilement);
        return zone;
    }

    /** Met a jour le compteur de notifications non lues affiche en haut a droite. */
    public void rafraichirNotifications() {
        try {
            int n = NotificationService.compterNonLues(utilisateur.getId());
            lienNotifications.setText(n == 0 ? "Aucune nouvelle notification" : n + " notification(s) non lue(s)");
            lienNotifications.getStyleClass().remove("lien-notif-actif");
            if (n > 0) {
                lienNotifications.getStyleClass().add("lien-notif-actif");
            }
        } catch (RuntimeException e) {
            lienNotifications.setText("");
        }
    }

    /** Ajoute une entree au menu : la vue est construite a chaque clic (donnees toujours a jour). */
    public void ajouterMenu(String libelle, Supplier<Node> vue) {
        Button b = new Button(libelle);
        b.getStyleClass().add("menu-btn");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> {
            for (Button autre : boutons) {
                autre.getStyleClass().remove("menu-btn-actif");
            }
            b.getStyleClass().add("menu-btn-actif");
            Node noeud;
            try {
                noeud = vue.get();
            } catch (RuntimeException ex) {
                Label erreur = new Label(Nav.messageErreur(ex));
                erreur.setWrapText(true);
                erreur.getStyleClass().add("message-erreur");
                noeud = erreur;
            }
            afficher(libelle, noeud);
            rafraichirNotifications();
        });
        boutons.add(b);
        parLibelle.put(libelle, b);
        menu.getChildren().add(b);
    }

    /** Affiche un ecran dans la zone de contenu (utilise par les ecrans qui en ouvrent un autre). */
    @Override
    public void afficher(String titre, Node noeud) {
        titrePage.setText(titre);
        contenu.getChildren().setAll(noeud);
    }

    // ------------------------------------------------------------------ pages

    private Node accueilUtilisateur() {
        ConsultationService consultation = new ConsultationService();
        Label bonjour = Composants.titre("Bonjour, " + utilisateur.getPrenom() + " !");
        VBox page = new VBox(18, bonjour);

        List<Tontine> mesTontines = consultation.mesTontines(utilisateur.getId());
        if (mesTontines.isEmpty()) {
            // etat normal et prevu (cahier des charges, section 3.2) : ni erreur ni ecran vide
            Label titre = new Label("Vous n'\u00eates membre d'aucune tontine pour le moment");
            titre.getStyleClass().add("carte-titre");
            Label texte = Composants.sousTitre("Explorez les tontines disponibles et envoyez une demande d'adh\u00e9sion, "
                    + "ou demandez \u00e0 devenir g\u00e9rant pour cr\u00e9er la v\u00f4tre. Vous pourrez cotiser "
                    + "d\u00e8s que le g\u00e9rant aura accept\u00e9 votre demande.");
            Button explorer = Composants.primaire("Explorer les tontines");
            explorer.setOnAction(e -> parLibelle.get("Explorer les tontines").fire());
            Button creer = Composants.contour("Cr\u00e9er ma tontine");
            creer.setOnAction(e -> parLibelle.get("Cr\u00e9er ma tontine").fire());
            page.getChildren().add(Composants.carte(titre, texte, new HBox(12, explorer, creer)));
        } else {
            Label titre = new Label("Mes tontines");
            titre.getStyleClass().add("carte-titre");
            page.getChildren().add(titre);
            for (Tontine t : mesTontines) {
                Adhesion a = consultation.monAdhesion(utilisateur.getId(), t.getId());
                Label nom = new Label(t.getNom());
                nom.getStyleClass().add("carte-titre");
                Label detail = Composants.sousTitre(Libelles.type(t.getType()) + " - cotisation "
                        + Format.montant(t.getMontantCotisation()) + " (" + Libelles.frequence(t.getFrequence()) + ")");
                Label role = new Label(a == null ? "" : Libelles.role(a.getRole()));
                role.getStyleClass().add("badge");
                Button ouvrir = Composants.primaire("Ouvrir");
                ouvrir.setOnAction(e -> afficher(t.getNom(), new TontineMembreView(utilisateur, t, this)));
                HBox ligne = new HBox(12, new VBox(4, nom, detail), Composants.espaceur(), role, ouvrir);
                ligne.setAlignment(Pos.CENTER_LEFT);
                ligne.getStyleClass().add("ligne-liste");
                page.getChildren().add(ligne);
            }
        }
        return page;
    }

    private Node accueilAdmin() {
        Indicateurs stats = new StatistiquesService().globales(utilisateur.getId());
        FlowPane cartes = new FlowPane(16, 16);
        for (Map.Entry<String, String> e : stats.getValeurs().entrySet()) {
            Label valeur = new Label(e.getValue());
            valeur.getStyleClass().add("stat-valeur");
            valeur.setWrapText(true);
            String cle = e.getKey().trim();
            if (cle.startsWith("- ")) {
                cle = "Tontines de type " + cle.substring(2).toLowerCase();
            }
            Label libelle = new Label(cle);
            libelle.getStyleClass().add("stat-libelle");
            libelle.setWrapText(true);
            VBox c = new VBox(6, valeur, libelle);
            c.getStyleClass().add("stat-card");
            c.setPrefWidth(250);
            cartes.getChildren().add(c);
        }
        // ---- a traiter : acces direct aux files d'attente de l'administrateur
        AdminService admin = new AdminService();
        int demandes = admin.demandesCreationEnAttente(utilisateur.getId()).size();
        int prets = admin.pretsGerantsEnAttente(utilisateur.getId()).size();
        int paiements = admin.paiementsGerantsEnAttente(utilisateur.getId()).size();
        Label aTraiter = new Label("\u00c0 traiter");
        aTraiter.getStyleClass().add("carte-titre");
        HBox raccourcis = new HBox(12,
                raccourci("Demandes de cr\u00e9ation", demandes),
                raccourci("Pr\u00eats des g\u00e9rants", prets),
                raccourci("Paiements des g\u00e9rants", paiements));
        return new VBox(18, Composants.titre("Activit\u00e9 globale de la plateforme"), cartes, aTraiter, raccourcis);
    }

    /** Bouton d'acces direct a une file d'attente, mis en avant s'il y a quelque chose a traiter. */
    private Button raccourci(String menuLibelle, int nombre) {
        Button b = nombre > 0 ? Composants.or(menuLibelle + " (" + nombre + ")") : Composants.contour(menuLibelle + " (0)");
        b.setOnAction(e -> parLibelle.get(menuLibelle).fire());
        return b;
    }

    private Node bientot(String titre, String etape) {
        Label t = new Label(titre);
        t.getStyleClass().add("carte-titre");
        Label texte = Composants.sousTitre("Cette section sera ajout\u00e9e \u00e0 l'\u00e9tape " + etape + ".");
        return Composants.carte(t, texte);
    }
}
