package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `distribution` : insertion, lecture, modification, suppression et requetes utiles. */
public class DistributionDao extends BaseDao {

    private Distribution map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Distribution o = new Distribution();
        o.setId(rs.getInt("id"));
        o.setCycleId(rs.getInt("cycle_id"));
        o.setBeneficiaireId(getInteger(rs, "beneficiaire_id"));
        o.setDate(rs.getObject("date", LocalDateTime.class));
        o.setMontantTotal(rs.getDouble("montant_total"));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(Distribution o) {
        if (o.getDate() == null) {
            o.setDate(LocalDateTime.now());
        }

        int id = insertReturnId(
                "INSERT INTO `distribution` (`cycle_id`,`beneficiaire_id`,`date`,`montant_total`) VALUES (?,?,?,?)",
                o.getCycleId(),
                o.getBeneficiaireId(),
                o.getDate(),
                o.getMontantTotal());
        o.setId(id);
        return id;
    }

    public boolean update(Distribution o) {
        return executeUpdate("UPDATE `distribution` SET `cycle_id`=?,`beneficiaire_id`=?,`date`=?,`montant_total`=? WHERE `id`=?",
                o.getCycleId(),
                o.getBeneficiaireId(),
                o.getDate(),
                o.getMontantTotal(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `distribution` WHERE `id`=?", id) > 0;
    }

    public Distribution findById(int id) {
        return queryOne("SELECT * FROM `distribution` WHERE `id`=?", this::map, id);
    }

    public List<Distribution> findAll() {
        return queryList("SELECT * FROM `distribution` ORDER BY `id`", this::map);
    }

    public List<Distribution> findByCycle(int cycleId) {
        return queryList("SELECT * FROM `distribution` WHERE `cycle_id`=? ORDER BY `date`", this::map, cycleId);
    }

    public List<Distribution> findByTontine(int tontineId) {
        return queryList("SELECT d.* FROM `distribution` d JOIN `cycle` c ON c.`id`=d.`cycle_id` "
                + "WHERE c.`tontine_id`=? ORDER BY d.`date` DESC", this::map, tontineId);
    }
}
