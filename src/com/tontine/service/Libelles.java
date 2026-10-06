package com.tontine.service;

import com.tontine.model.Frequence;
import com.tontine.model.ModePaiement;
import com.tontine.model.RoleAdhesion;
import com.tontine.model.StatutDemande;
import com.tontine.model.TypeTontine;

/** Textes lisibles pour l'interface (listes deroulantes, tableaux...). */
public final class Libelles {

    private Libelles() {
    }

    public static String type(TypeTontine t) {
        if (t == null) {
            return "";
        }
        switch (t) {
        case FONDS_COMMUN:
            return "Fonds commun";
        case TOUR_DE_ROLE:
            return "Tour de r\u00f4le";
        default:
            return "Tirage au sort";
        }
    }

    public static String descriptionType(TypeTontine t) {
        switch (t) {
        case FONDS_COMMUN:
            return "Les cotisations s'accumulent pendant le cycle et les membres peuvent emprunter "
                    + "sur ce fonds avec un int\u00e9r\u00eat. En fin de cycle, chacun re\u00e7oit sa cotisation "
                    + "cumul\u00e9e plus une part \u00e9gale des int\u00e9r\u00eats et p\u00e9nalit\u00e9s.";
        case TOUR_DE_ROLE:
            return "Montant fixe identique pour tous. \u00c0 chaque p\u00e9riode, un membre re\u00e7oit le total "
                    + "collect\u00e9, selon un ordre qui peut \u00eatre anticip\u00e9 sur demande au g\u00e9rant.";
        default:
            return "M\u00eame principe que le tour de r\u00f4le, mais le b\u00e9n\u00e9ficiaire de chaque p\u00e9riode est "
                    + "d\u00e9sign\u00e9 par tirage au sort parmi les membres pas encore tir\u00e9s.";
        }
    }

    public static String frequence(Frequence f) {
        if (f == null) {
            return "";
        }
        switch (f) {
        case HEBDOMADAIRE:
            return "Hebdomadaire";
        case MENSUELLE:
            return "Mensuelle";
        default:
            return "Annuelle";
        }
    }

    public static String mode(ModePaiement m) {
        if (m == null) {
            return "";
        }
        switch (m) {
        case ESPECE:
            return "Esp\u00e8ces";
        case WAVE:
            return "Wave";
        default:
            return "Orange Money";
        }
    }

    public static String role(RoleAdhesion r) {
        return r == RoleAdhesion.GERANT ? "G\u00e9rant" : "Membre";
    }

    public static String statutDemande(StatutDemande s) {
        if (s == null) {
            return "";
        }
        switch (s) {
        case EN_ATTENTE:
            return "En attente";
        case ACCEPTEE:
            return "Accept\u00e9e";
        default:
            return "Refus\u00e9e";
        }
    }
}
