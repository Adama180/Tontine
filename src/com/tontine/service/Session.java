package com.tontine.service;

import com.tontine.model.Utilisateur;

/** Utilisateur actuellement connecte a l'application (un seul a la fois). */
public final class Session {

    private static Utilisateur courant;

    private Session() {
    }

    public static Utilisateur getUtilisateur() {
        return courant;
    }

    public static void setUtilisateur(Utilisateur u) {
        courant = u;
    }

    public static boolean estConnecte() {
        return courant != null;
    }

    public static void deconnecter() {
        courant = null;
    }
}
