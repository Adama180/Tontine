package com.tontine;

import com.tontine.ui.MainApp;

/**
 * Point d'entree de l'application (a lancer avec Run As > Java Application).
 * Cette classe n'herite pas de javafx.application.Application : cela evite l'erreur
 * "JavaFX runtime components are missing" au demarrage.
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
