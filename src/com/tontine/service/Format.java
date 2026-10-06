package com.tontine.service;

import java.util.Locale;

/** Mise en forme des montants pour l'affichage. */
public final class Format {

    public static final String DEVISE = "FCFA";

    private Format() {
    }

    /** Exemple : 5266.67 -> "5 266,67 FCFA" ; 5000 -> "5 000 FCFA". */
    public static String montant(double valeur) {
        boolean entier = Math.abs(valeur - Math.rint(valeur)) < 0.005;
        String s = String.format(Locale.FRANCE, entier ? "%,.0f" : "%,.2f", valeur);
        return s.replace('\u202f', ' ').replace('\u00a0', ' ') + " " + DEVISE;
    }
}
