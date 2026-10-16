package com.tontine.ui;

import java.net.URL;

import com.tontine.service.ServiceException;
import com.tontine.util.DaoException;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/** Navigation entre les ecrans : une seule fenetre, dont on change le contenu. */
public final class Nav {

    private static Stage stage;
    private static Scene scene;
    private static double largeur = 1180;
    private static double hauteur = 760;
    private static Boolean reponseAutomatique; // uniquement pour les tests automatiques

    private Nav() {
    }

    static void init(Stage s) {
        stage = s;
    }

    /** Memorise la taille initiale de la fenetre (calculee d'apres la taille de l'ecran). */
    static void init(Stage s, double l, double h) {
        stage = s;
        largeur = l;
        hauteur = h;
    }

    public static Stage getStage() {
        return stage;
    }

    /** Affiche un ecran dans la fenetre principale. */
    public static void aller(Parent racine) {
        if (scene == null) {
            scene = new Scene(racine, largeur, hauteur);
            URL css = Nav.class.getResource("style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("ATTENTION : style.css introuvable (il doit \u00eatre dans le package com.tontine.ui).");
            }
            stage.setScene(scene);
        } else {
            scene.setRoot(racine);
        }
        Effets.fondu(racine);
    }

    public static void info(String titre, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.initOwner(stage);
        a.showAndWait();
    }

    static void setReponseAutomatique(Boolean reponse) {
        reponseAutomatique = reponse;
    }

    public static boolean confirmer(String message) {
        if (reponseAutomatique != null) {
            return reponseAutomatique;
        }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirmation");
        a.setHeaderText(null);
        a.initOwner(stage);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    /** Texte d'erreur a montrer a l'utilisateur. */
    public static String messageErreur(Throwable e) {
        if (e instanceof ServiceException) {
            return e.getMessage();
        }
        if (e instanceof DaoException) {
            return "Erreur de base de donn\u00e9es : v\u00e9rifiez que MySQL est d\u00e9marr\u00e9 dans XAMPP. (" + e.getMessage() + ")";
        }
        e.printStackTrace();
        return "Erreur inattendue : " + e;
    }
}
