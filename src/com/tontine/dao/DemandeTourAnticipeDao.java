package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `demande_tour_anticipe` : insertion, lecture, modification, suppression et requetes utiles. */
public class DemandeTourAnticipeDao extends BaseDao {

    private DemandeTourAnticipe map(java.sql.ResultSet rs) throws java.sql.SQLException {
        DemandeTourAnticipe o = new DemandeTourAnticipe();
        o.setId(rs.getInt("id"));
        o.setTontineId(rs.getInt("tontine_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setMotif(rs.getString("motif"));
        o.setDateDemande(rs.getObject("date_demande", LocalDateTime.class));
        o.setStatut(enumOf(StatutDemande.class, rs.getString("statut")));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(DemandeTourAnticipe o) {
        if (o.getDateDemande() == null) {
            o.setDateDemande(LocalDateTime.now());
        }
        if (o.getStatut() == null) {
            o.setStatut(StatutDemande.EN_ATTENTE);
        }

        int id = insertReturnId(
                "INSERT INTO `demande_tour_anticipe` (`tontine_id`,`user_id`,`motif`,`date_demande`,`statut`) VALUES (?,?,?,?,?)",
                o.getTontineId(),
                o.getUserId(),
                o.getMotif(),
                o.getDateDemande(),
                o.getStatut());
        o.setId(id);
        return id;
    }

    public boolean update(DemandeTourAnticipe o) {
        return executeUpdate("UPDATE `demande_tour_anticipe` SET `tontine_id`=?,`user_id`=?,`motif`=?,`date_demande`=?,`statut`=? WHERE `id`=?",
                o.getTontineId(),
                o.getUserId(),
                o.getMotif(),
                o.getDateDemande(),
                o.getStatut(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `demande_tour_anticipe` WHERE `id`=?", id) > 0;
    }

    public DemandeTourAnticipe findById(int id) {
        return queryOne("SELECT * FROM `demande_tour_anticipe` WHERE `id`=?", this::map, id);
    }

    public List<DemandeTourAnticipe> findAll() {
        return queryList("SELECT * FROM `demande_tour_anticipe` ORDER BY `id`", this::map);
    }

    public List<DemandeTourAnticipe> findByTontineEtStatut(int tontineId, StatutDemande statut) {
        return queryList("SELECT * FROM `demande_tour_anticipe` WHERE `tontine_id`=? AND `statut`=? "
                + "ORDER BY `date_demande`", this::map, tontineId, statut);
    }

    public List<DemandeTourAnticipe> findByUser(int userId) {
        return queryList("SELECT * FROM `demande_tour_anticipe` WHERE `user_id`=? ORDER BY `date_demande` DESC",
                this::map, userId);
    }

    public boolean changerStatut(int id, StatutDemande statut) {
        return executeUpdate("UPDATE `demande_tour_anticipe` SET `statut`=? WHERE `id`=?", statut, id) > 0;
    }
}
