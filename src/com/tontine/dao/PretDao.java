package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `pret` : insertion, lecture, modification, suppression et requetes utiles. */
public class PretDao extends BaseDao {

    private Pret map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Pret o = new Pret();
        o.setId(rs.getInt("id"));
        o.setTontineId(rs.getInt("tontine_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setMontant(rs.getDouble("montant"));
        o.setNbJours(rs.getInt("nb_jours"));
        o.setMotif(rs.getString("motif"));
        o.setDateDemande(rs.getObject("date_demande", LocalDateTime.class));
        o.setStatut(enumOf(StatutPret.class, rs.getString("statut")));
        o.setValideurId(getInteger(rs, "valideur_id"));
        o.setMontantRembourse(rs.getDouble("montant_rembourse"));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(Pret o) {
        if (o.getDateDemande() == null) {
            o.setDateDemande(LocalDateTime.now());
        }
        if (o.getStatut() == null) {
            o.setStatut(StatutPret.EN_ATTENTE);
        }

        int id = insertReturnId(
                "INSERT INTO `pret` (`tontine_id`,`user_id`,`montant`,`nb_jours`,`motif`,`date_demande`,`statut`,`valideur_id`,`montant_rembourse`) VALUES (?,?,?,?,?,?,?,?,?)",
                o.getTontineId(),
                o.getUserId(),
                o.getMontant(),
                o.getNbJours(),
                o.getMotif(),
                o.getDateDemande(),
                o.getStatut(),
                o.getValideurId(),
                o.getMontantRembourse());
        o.setId(id);
        return id;
    }

    public boolean update(Pret o) {
        return executeUpdate("UPDATE `pret` SET `tontine_id`=?,`user_id`=?,`montant`=?,`nb_jours`=?,`motif`=?,`date_demande`=?,`statut`=?,`valideur_id`=?,`montant_rembourse`=? WHERE `id`=?",
                o.getTontineId(),
                o.getUserId(),
                o.getMontant(),
                o.getNbJours(),
                o.getMotif(),
                o.getDateDemande(),
                o.getStatut(),
                o.getValideurId(),
                o.getMontantRembourse(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `pret` WHERE `id`=?", id) > 0;
    }

    public Pret findById(int id) {
        return queryOne("SELECT * FROM `pret` WHERE `id`=?", this::map, id);
    }

    public List<Pret> findAll() {
        return queryList("SELECT * FROM `pret` ORDER BY `id`", this::map);
    }

    public List<Pret> findByTontine(int tontineId) {
        return queryList("SELECT * FROM `pret` WHERE `tontine_id`=? ORDER BY `date_demande` DESC", this::map, tontineId);
    }

    public List<Pret> findByTontineEtStatut(int tontineId, StatutPret statut) {
        return queryList("SELECT * FROM `pret` WHERE `tontine_id`=? AND `statut`=? ORDER BY `date_demande`",
                this::map, tontineId, statut);
    }

    public List<Pret> findByUser(int userId) {
        return queryList("SELECT * FROM `pret` WHERE `user_id`=? ORDER BY `date_demande` DESC", this::map, userId);
    }

    public List<Pret> findByStatut(StatutPret statut) {
        return queryList("SELECT * FROM `pret` WHERE `statut`=? ORDER BY `date_demande`", this::map, statut);
    }

    /** Prets en attente demandes par un gerant de sa propre tontine (valides par l'administrateur). */
    public List<Pret> findEnAttentePourGerants() {
        return queryList("SELECT p.* FROM `pret` p JOIN `tontine` t ON t.`id`=p.`tontine_id` "
                + "WHERE p.`statut`='EN_ATTENTE' AND p.`user_id`=t.`gerant_id` ORDER BY p.`date_demande`", this::map);
    }

    /** Prets accordes non encore rembourses d'un membre dans une tontine. */
    public List<Pret> findEnCours(int tontineId, int userId) {
        return queryList("SELECT * FROM `pret` WHERE `tontine_id`=? AND `user_id`=? AND `statut`='ACCORDE'",
                this::map, tontineId, userId);
    }

    public boolean traiter(int id, StatutPret statut, int valideurId) {
        return executeUpdate("UPDATE `pret` SET `statut`=?, `valideur_id`=? WHERE `id`=?", statut, valideurId, id) > 0;
    }

    public boolean ajouterRemboursement(int id, double montant) {
        return executeUpdate("UPDATE `pret` SET `montant_rembourse`=`montant_rembourse`+? WHERE `id`=?",
                montant, id) > 0;
    }

    public boolean changerStatut(int id, StatutPret statut) {
        return executeUpdate("UPDATE `pret` SET `statut`=? WHERE `id`=?", statut, id) > 0;
    }
}
