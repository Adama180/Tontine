package com.tontine.service;

import com.tontine.model.Adhesion;
import com.tontine.model.Utilisateur;

/** Un membre d'une tontine : sa participation (solde, role...) et son identite. */
public class LigneMembre {

    private final Adhesion adhesion;
    private final Utilisateur utilisateur;

    public LigneMembre(Adhesion adhesion, Utilisateur utilisateur) {
        this.adhesion = adhesion;
        this.utilisateur = utilisateur;
    }

    public Adhesion getAdhesion() {
        return adhesion;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
}
