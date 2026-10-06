package com.tontine.model;

import java.time.LocalDateTime;

/** Table demande_creation : demande pour devenir g\u00e9rant (valid\u00e9e par l'administrateur). */
public class DemandeCreation {
    private int id;
    private int demandeurId;
    private String nomSouhaite;
    private TypeTontine typeSouhaite;
    private double montant;
    private Frequence frequence;
    private int nbMembre;
    private LocalDateTime dateEnvoi;
    private StatutDemande statut;
    private Integer adminId;
    private String pieceHash;
    private String pieceChemin;

    public DemandeCreation() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDemandeurId() {
        return demandeurId;
    }

    public void setDemandeurId(int demandeurId) {
        this.demandeurId = demandeurId;
    }

    public String getNomSouhaite() {
        return nomSouhaite;
    }

    public void setNomSouhaite(String nomSouhaite) {
        this.nomSouhaite = nomSouhaite;
    }

    public TypeTontine getTypeSouhaite() {
        return typeSouhaite;
    }

    public void setTypeSouhaite(TypeTontine typeSouhaite) {
        this.typeSouhaite = typeSouhaite;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Frequence getFrequence() {
        return frequence;
    }

    public void setFrequence(Frequence frequence) {
        this.frequence = frequence;
    }

    public int getNbMembre() {
        return nbMembre;
    }

    public void setNbMembre(int nbMembre) {
        this.nbMembre = nbMembre;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public StatutDemande getStatut() {
        return statut;
    }

    public void setStatut(StatutDemande statut) {
        this.statut = statut;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getPieceHash() {
        return pieceHash;
    }

    public void setPieceHash(String pieceHash) {
        this.pieceHash = pieceHash;
    }

    public String getPieceChemin() {
        return pieceChemin;
    }

    public void setPieceChemin(String pieceChemin) {
        this.pieceChemin = pieceChemin;
    }
}
