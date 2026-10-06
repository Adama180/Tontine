package com.tontine.service;

import java.time.LocalDateTime;

/** Un versement avec les noms lisibles (tontine, beneficiaire) : pour le suivi de l'administrateur. */
public class LigneVersement {

    private final LocalDateTime date;
    private final String tontine;
    private final int numeroCycle;
    private final String beneficiaire;
    private final double montant;

    public LigneVersement(LocalDateTime date, String tontine, int numeroCycle, String beneficiaire, double montant) {
        this.date = date;
        this.tontine = tontine;
        this.numeroCycle = numeroCycle;
        this.beneficiaire = beneficiaire;
        this.montant = montant;
    }

    public LocalDateTime getDate() { return date; }
    public String getTontine() { return tontine; }
    public int getNumeroCycle() { return numeroCycle; }
    public String getBeneficiaire() { return beneficiaire; }
    public double getMontant() { return montant; }
}
