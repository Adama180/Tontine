package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `cycle` : insertion, lecture, modification, suppression et requetes utiles. */
public class CycleDao extends BaseDao {

    private Cycle map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Cycle o = new Cycle();
        o.setId(rs.getInt("id"));
        o.setTontineId(rs.getInt("tontine_id"));
        o.setNumero(rs.getInt("numero"));
        o.setDateDebut(rs.getObject("date_debut", LocalDate.class));
        o.setDateFin(rs.getObject("date_fin", LocalDate.class));
        o.setStatut(enumOf(StatutCycle.class, rs.getString("statut")));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(Cycle o) {
        if (o.getStatut() == null) {
            o.setStatut(StatutCycle.EN_COURS);
        }

        int id = insertReturnId(
                "INSERT INTO `cycle` (`tontine_id`,`numero`,`date_debut`,`date_fin`,`statut`) VALUES (?,?,?,?,?)",
                o.getTontineId(),
                o.getNumero(),
                o.getDateDebut(),
                o.getDateFin(),
                o.getStatut());
        o.setId(id);
        return id;
    }

    public boolean update(Cycle o) {
        return executeUpdate("UPDATE `cycle` SET `tontine_id`=?,`numero`=?,`date_debut`=?,`date_fin`=?,`statut`=? WHERE `id`=?",
                o.getTontineId(),
                o.getNumero(),
                o.getDateDebut(),
                o.getDateFin(),
                o.getStatut(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `cycle` WHERE `id`=?", id) > 0;
    }

    public Cycle findById(int id) {
        return queryOne("SELECT * FROM `cycle` WHERE `id`=?", this::map, id);
    }

    public List<Cycle> findAll() {
        return queryList("SELECT * FROM `cycle` ORDER BY `id`", this::map);
    }

    public List<Cycle> findByTontine(int tontineId) {
        return queryList("SELECT * FROM `cycle` WHERE `tontine_id`=? ORDER BY `numero`", this::map, tontineId);
    }

    public Cycle findEnCours(int tontineId) {
        return queryOne("SELECT * FROM `cycle` WHERE `tontine_id`=? AND `statut`='EN_COURS' "
                + "ORDER BY `numero` DESC", this::map, tontineId);
    }

    public int dernierNumero(int tontineId) {
        return scalarInt("SELECT COALESCE(MAX(`numero`),0) FROM `cycle` WHERE `tontine_id`=?", tontineId);
    }

    public boolean cloturer(int cycleId, LocalDate dateFin) {
        return executeUpdate("UPDATE `cycle` SET `statut`='CLOTURE', `date_fin`=? WHERE `id`=?", dateFin, cycleId) > 0;
    }
}
