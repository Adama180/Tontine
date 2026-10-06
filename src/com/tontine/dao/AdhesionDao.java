package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `adhesion` : insertion, lecture, modification, suppression et requetes utiles. */
public class AdhesionDao extends BaseDao {

    private Adhesion map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Adhesion o = new Adhesion();
        o.setId(rs.getInt("id"));
        o.setTontineId(rs.getInt("tontine_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setRole(enumOf(RoleAdhesion.class, rs.getString("role")));
        o.setDateAdhesion(rs.getObject("date_adhesion", LocalDateTime.class));
        o.setSolde(rs.getDouble("solde"));
        o.setTotalCotise(rs.getDouble("total_cotise"));
        o.setCotisationsFaites(rs.getInt("cotisations_faites"));
        o.setOrdreTour(getInteger(rs, "ordre_tour"));
        o.setStatut(enumOf(StatutAdhesion.class, rs.getString("statut")));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(Adhesion o) {
        if (o.getRole() == null) {
            o.setRole(RoleAdhesion.MEMBRE);
        }
        if (o.getDateAdhesion() == null) {
            o.setDateAdhesion(LocalDateTime.now());
        }
        if (o.getStatut() == null) {
            o.setStatut(StatutAdhesion.ACTIF);
        }

        int id = insertReturnId(
                "INSERT INTO `adhesion` (`tontine_id`,`user_id`,`role`,`date_adhesion`,`solde`,`total_cotise`,`cotisations_faites`,`ordre_tour`,`statut`) VALUES (?,?,?,?,?,?,?,?,?)",
                o.getTontineId(),
                o.getUserId(),
                o.getRole(),
                o.getDateAdhesion(),
                o.getSolde(),
                o.getTotalCotise(),
                o.getCotisationsFaites(),
                o.getOrdreTour(),
                o.getStatut());
        o.setId(id);
        return id;
    }

    public boolean update(Adhesion o) {
        return executeUpdate("UPDATE `adhesion` SET `tontine_id`=?,`user_id`=?,`role`=?,`date_adhesion`=?,`solde`=?,`total_cotise`=?,`cotisations_faites`=?,`ordre_tour`=?,`statut`=? WHERE `id`=?",
                o.getTontineId(),
                o.getUserId(),
                o.getRole(),
                o.getDateAdhesion(),
                o.getSolde(),
                o.getTotalCotise(),
                o.getCotisationsFaites(),
                o.getOrdreTour(),
                o.getStatut(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `adhesion` WHERE `id`=?", id) > 0;
    }

    public Adhesion findById(int id) {
        return queryOne("SELECT * FROM `adhesion` WHERE `id`=?", this::map, id);
    }

    public List<Adhesion> findAll() {
        return queryList("SELECT * FROM `adhesion` ORDER BY `id`", this::map);
    }

    public List<Adhesion> findByTontine(int tontineId) {
        return queryList("SELECT * FROM `adhesion` WHERE `tontine_id`=? ORDER BY `date_adhesion`", this::map, tontineId);
    }

    public List<Adhesion> findByTontineOrdreTour(int tontineId) {
        return queryList("SELECT * FROM `adhesion` WHERE `tontine_id`=? AND `statut`='ACTIF' "
                + "ORDER BY `ordre_tour`", this::map, tontineId);
    }

    public List<Adhesion> findByUser(int userId) {
        return queryList("SELECT * FROM `adhesion` WHERE `user_id`=? ORDER BY `date_adhesion`", this::map, userId);
    }

    public Adhesion findByTontineEtUser(int tontineId, int userId) {
        return queryOne("SELECT * FROM `adhesion` WHERE `tontine_id`=? AND `user_id`=?", this::map, tontineId, userId);
    }

    public boolean estMembre(int tontineId, int userId) {
        return scalarInt("SELECT COUNT(*) FROM `adhesion` WHERE `tontine_id`=? AND `user_id`=? AND `statut`='ACTIF'",
                tontineId, userId) > 0;
    }

    public int compterActifs(int tontineId) {
        return scalarInt("SELECT COUNT(*) FROM `adhesion` WHERE `tontine_id`=? AND `statut`='ACTIF'", tontineId);
    }

    /** Ajoute une cotisation : solde et total cotise augmentent, le compteur de cotisations passe a +1. */
    public boolean enregistrerCotisation(int adhesionId, double montant) {
        return executeUpdate("UPDATE `adhesion` SET `solde`=`solde`+?, `total_cotise`=`total_cotise`+?, "
                + "`cotisations_faites`=`cotisations_faites`+1 WHERE `id`=?", montant, montant, adhesionId) > 0;
    }

    /** Ajoute (ou retire si negatif) un montant au solde. */
    public boolean ajouterAuSolde(int adhesionId, double montant) {
        return executeUpdate("UPDATE `adhesion` SET `solde`=`solde`+? WHERE `id`=?", montant, adhesionId) > 0;
    }

    public boolean changerStatut(int adhesionId, StatutAdhesion statut) {
        return executeUpdate("UPDATE `adhesion` SET `statut`=? WHERE `id`=?", statut, adhesionId) > 0;
    }

    public boolean definirOrdreTour(int adhesionId, Integer ordre) {
        return executeUpdate("UPDATE `adhesion` SET `ordre_tour`=? WHERE `id`=?", ordre, adhesionId) > 0;
    }
}
