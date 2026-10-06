package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `tirage` : insertion, lecture, modification, suppression et requetes utiles. */
public class TirageDao extends BaseDao {

    private Tirage map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Tirage o = new Tirage();
        o.setId(rs.getInt("id"));
        o.setTontineId(rs.getInt("tontine_id"));
        o.setSemaine(rs.getInt("semaine"));
        o.setDate(rs.getObject("date", LocalDateTime.class));
        o.setBeneficiaireId(rs.getInt("beneficiaire_id"));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(Tirage o) {
        if (o.getDate() == null) {
            o.setDate(LocalDateTime.now());
        }

        int id = insertReturnId(
                "INSERT INTO `tirage` (`tontine_id`,`semaine`,`date`,`beneficiaire_id`) VALUES (?,?,?,?)",
                o.getTontineId(),
                o.getSemaine(),
                o.getDate(),
                o.getBeneficiaireId());
        o.setId(id);
        return id;
    }

    public boolean update(Tirage o) {
        return executeUpdate("UPDATE `tirage` SET `tontine_id`=?,`semaine`=?,`date`=?,`beneficiaire_id`=? WHERE `id`=?",
                o.getTontineId(),
                o.getSemaine(),
                o.getDate(),
                o.getBeneficiaireId(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `tirage` WHERE `id`=?", id) > 0;
    }

    public Tirage findById(int id) {
        return queryOne("SELECT * FROM `tirage` WHERE `id`=?", this::map, id);
    }

    public List<Tirage> findAll() {
        return queryList("SELECT * FROM `tirage` ORDER BY `id`", this::map);
    }

    public List<Tirage> findByTontine(int tontineId) {
        return queryList("SELECT * FROM `tirage` WHERE `tontine_id`=? ORDER BY `semaine`", this::map, tontineId);
    }

    /** Identifiants des utilisateurs deja tires dans cette tontine (a exclure du prochain tirage). */
    public List<Integer> beneficiairesDejaTires(int tontineId) {
        return queryList("SELECT `beneficiaire_id` FROM `tirage` WHERE `tontine_id`=?",
                rs -> rs.getInt(1), tontineId);
    }

    public int dernierTour(int tontineId) {
        return scalarInt("SELECT COALESCE(MAX(`semaine`),0) FROM `tirage` WHERE `tontine_id`=?", tontineId);
    }
}
