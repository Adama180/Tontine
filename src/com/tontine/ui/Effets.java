package com.tontine.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.util.Duration;

/** Petites animations (apparition, survol, flottement). Desactivables d'un seul appel (utile pour les tests). */
public final class Effets {

    private static boolean actif = true;

    private Effets() {
    }

    public static void setActif(boolean valeur) {
        actif = valeur;
    }

    public static boolean isActif() {
        return actif;
    }

    /** Fondu + leger glissement vers le haut, des que le noeud est affiche. */
    public static void apparition(Node n, int retardMs) {
        if (!actif) {
            return;
        }
        n.setOpacity(0);
        n.setTranslateY(18);
        Runnable lancer = () -> Platform.runLater(() -> {
            FadeTransition f = new FadeTransition(Duration.millis(420), n);
            f.setToValue(1);
            TranslateTransition t = new TranslateTransition(Duration.millis(420), n);
            t.setToY(0);
            t.setInterpolator(Interpolator.EASE_OUT);
            ParallelTransition p = new ParallelTransition(f, t);
            p.setDelay(Duration.millis(retardMs));
            p.play();
        });
        if (n.getScene() != null) {
            lancer.run();
        } else {
            n.sceneProperty().addListener((o, a, s) -> {
                if (s != null && a == null) {
                    lancer.run();
                }
            });
        }
    }

    /** Fondu simple (changement d'ecran). */
    public static void fondu(Node n) {
        if (!actif) {
            return;
        }
        n.setOpacity(0);
        FadeTransition f = new FadeTransition(Duration.millis(260), n);
        f.setToValue(1);
        Platform.runLater(f::play);
    }

    /** L'element se souleve legerement quand la souris passe dessus. */
    public static void survol(Node n) {
        if (!actif) {
            return;
        }
        n.setOnMouseEntered(e -> deplacer(n, -3, 1.012));
        n.setOnMouseExited(e -> deplacer(n, 0, 1.0));
    }

    private static void deplacer(Node n, double y, double echelle) {
        TranslateTransition t = new TranslateTransition(Duration.millis(140), n);
        t.setToY(y);
        ScaleTransition s = new ScaleTransition(Duration.millis(140), n);
        s.setToX(echelle);
        s.setToY(echelle);
        new ParallelTransition(t, s).play();
    }

    /** Mouvement lent de haut en bas, en boucle (decor de la page d'accueil). */
    public static void flotter(Node n, double amplitude, int dureeMs, int retardMs) {
        if (!actif) {
            return;
        }
        TranslateTransition t = new TranslateTransition(Duration.millis(dureeMs), n);
        t.setFromY(0);
        t.setToY(-amplitude);
        t.setAutoReverse(true);
        t.setCycleCount(Animation.INDEFINITE);
        t.setInterpolator(Interpolator.EASE_BOTH);
        SequentialTransition s = new SequentialTransition(new PauseTransition(Duration.millis(retardMs)), t);
        s.play();
    }
}
