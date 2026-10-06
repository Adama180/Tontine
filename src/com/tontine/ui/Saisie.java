package com.tontine.ui;

import com.tontine.service.ServiceException;

/** Lecture des champs saisis : transforme un texte en nombre, avec un message clair en cas d'erreur. */
public final class Saisie {

    private Saisie() {
    }

    public static double montant(String texte, String champ) {
        try {
            return Double.parseDouble(texte.trim().replace(" ", "").replace(",", "."));
        } catch (RuntimeException e) {
            throw new ServiceException("Valeur invalide pour \u00ab " + champ + " \u00bb : saisissez un nombre.");
        }
    }

    public static int entier(String texte, String champ) {
        try {
            return Integer.parseInt(texte.trim());
        } catch (RuntimeException e) {
            throw new ServiceException("Valeur invalide pour \u00ab " + champ + " \u00bb : saisissez un nombre entier.");
        }
    }
}
