package com.tontine.service;

/** Chiffres agreges affichables par un visiteur (aucune donnee personnelle). */
public class ResumePublic {

    private final int tontinesOuvertes;
    private final int membresActifs;
    private final double totalVerse;

    public ResumePublic(int tontinesOuvertes, int membresActifs, double totalVerse) {
        this.tontinesOuvertes = tontinesOuvertes;
        this.membresActifs = membresActifs;
        this.totalVerse = totalVerse;
    }

    public int getTontinesOuvertes() { return tontinesOuvertes; }
    public int getMembresActifs() { return membresActifs; }
    public double getTotalVerse() { return totalVerse; }
}
