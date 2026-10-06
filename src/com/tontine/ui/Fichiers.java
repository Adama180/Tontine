package com.tontine.ui;

import java.awt.Desktop;
import java.io.File;
import java.util.function.Consumer;

import javafx.application.Platform;

/** Ouverture d'un fichier (piece d'identite) avec le programme par defaut de l'ordinateur. */
public final class Fichiers {

    private Fichiers() {
    }

    /** Ouvre le fichier ; en cas d'impossibilite, transmet un message clair a 'erreur'. */
    public static void ouvrir(String chemin, Consumer<String> erreur) {
        File f = chemin == null ? null : new File(chemin);
        if (f == null || !f.isFile()) {
            erreur.accept("Fichier introuvable : " + chemin);
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            erreur.accept("Impossible d'ouvrir le fichier automatiquement. Il se trouve ici : " + f.getAbsolutePath());
            return;
        }
        Thread th = new Thread(() -> {
            try {
                Desktop.getDesktop().open(f);
            } catch (Exception ex) {
                Platform.runLater(() -> erreur.accept("Impossible d'ouvrir la pi\u00e8ce : " + ex.getMessage()));
            }
        });
        th.setDaemon(true);
        th.start();
    }
}
