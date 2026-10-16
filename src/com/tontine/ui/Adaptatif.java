package com.tontine.ui;

import java.util.function.Consumer;

import javafx.scene.layout.Region;

/** Aide a l'adaptation de l'affichage a la largeur de la fenetre. */
public final class Adaptatif {

    private Adaptatif() {
    }

    /**
     * Appelle 'action' avec true quand la largeur passe sous le seuil, false quand elle le depasse.
     * L'action n'est appelee qu'aux changements d'etat, jamais a chaque pixel.
     */
    public static void surLargeur(Region region, double seuil, Consumer<Boolean> action) {
        Boolean[] etat = { null };
        region.widthProperty().addListener((o, a, w) -> {
            if (w.doubleValue() <= 0) {
                return;
            }
            boolean etroit = w.doubleValue() < seuil;
            if (etat[0] == null || etat[0] != etroit) {
                etat[0] = etroit;
                action.accept(etroit);
            }
        });
    }

    /** Ajoute ou retire la classe "compact" (le CSS reduit alors les tailles de texte et les marges). */
    public static void classeCompact(Region region, boolean compact) {
        region.getStyleClass().remove("compact");
        if (compact) {
            region.getStyleClass().add("compact");
        }
    }
}
