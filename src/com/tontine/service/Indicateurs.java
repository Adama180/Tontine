package com.tontine.service;

import java.util.LinkedHashMap;
import java.util.Map;

/** Liste ordonnee de couples libelle / valeur (statistiques et tableaux de bord). */
public class Indicateurs {

    private final Map<String, String> valeurs = new LinkedHashMap<>();

    public Indicateurs ajouter(String libelle, Object valeur) {
        valeurs.put(libelle, String.valueOf(valeur));
        return this;
    }

    public Indicateurs ajouterMontant(String libelle, double montant) {
        valeurs.put(libelle, Format.montant(montant));
        return this;
    }

    public Map<String, String> getValeurs() {
        return valeurs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        valeurs.forEach((k, v) -> sb.append(k).append(" : ").append(v).append('\n'));
        return sb.toString();
    }
}
