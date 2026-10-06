package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `demande_adhesion` : insertion, lecture, modification, suppression et requetes utiles. */
public class DemandeAdhesionDao extends BaseDao {

    private DemandeAdhesion map(java.sql.ResultSet rs) throws java.sql.SQLException {
        DemandeAdhesion o = new DemandeAdhesion();
        o.setId(rs.getInt("id"));
        o.setTontineId(rs.getInt("tontine_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setNom(rs.getString("nom"));
        o.setTelephone(rs.getString("telephone"));
        o.setEmail(rs.getString("email"));
        o.setPieceHash(rs.getString("piece_hash"));
        o.setPieceChemin(rs.getString("piece_chemin"));
        o.setDateEnvoi(rs.getObject("date_envoi", LocalDateTime.class));
        o.setStatut(enumOf(StatutDemande.class, rs.getString("statut")));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(DemandeAdhesion o) {
        if (o.getDateEnvoi() == null) {
            o.setDateEnvoi(LocalDateTime.now());
        }
        if (o.getStatut() == null) {
            o.setStatut(StatutDemande.EN_ATTENTE);
        }

        int id = insertReturnId(
                "INSERT INTO `demande_adhesion` (`tontine_id`,`user_id`,`nom`,`telephone`,`email`,`piece_hash`,`piece_chemin`,`date_envoi`,`statut`) VALUES (?,?,?,?,?,?,?,?,?)",
                o.getTontineId(),
                o.getUserId(),
                o.getNom(),
                o.getTelephone(),
                o.getEmail(),
                o.getPieceHash(),
                o.getPieceChemin(),
                o.getDateEnvoi(),
                o.getStatut());
        o.setId(id);
        return id;
    }

    public boolean update(DemandeAdhesion o) {
        return executeUpdate("UPDATE `demande_adhesion` SET `tontine_id`=?,`user_id`=?,`nom`=?,`telephone`=?,`email`=?,`piece_hash`=?,`piece_chemin`=?,`date_envoi`=?,`statut`=? WHERE `id`=?",
                o.getTontineId(),
                o.getUserId(),
                o.getNom(),
                o.getTelephone(),
                o.getEmail(),
                o.getPieceHash(),
                o.getPieceChemin(),
                o.getDateEnvoi(),
                o.getStatut(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `demande_adhesion` WHERE `id`=?", id) > 0;
    }

    public DemandeAdhesion findById(int id) {
        return queryOne("SELECT * FROM `demande_adhesion` WHERE `id`=?", this::map, id);
    }

    public List<DemandeAdhesion> findAll() {
        return queryList("SELECT * FROM `demande_adhesion` ORDER BY `id`", this::map);
    }

    public List<DemandeAdhesion> findByTontineEtStatut(int tontineId, StatutDemande statut) {
        return queryList("SELECT * FROM `demande_adhesion` WHERE `tontine_id`=? AND `statut`=? "
                + "ORDER BY `date_envoi`", this::map, tontineId, statut);
    }

    public List<DemandeAdhesion> findByTontine(int tontineId) {
        return queryList("SELECT * FROM `demande_adhesion` WHERE `tontine_id`=? ORDER BY `date_envoi` DESC",
                this::map, tontineId);
    }

    public List<DemandeAdhesion> findByUser(int userId) {
        return queryList("SELECT * FROM `demande_adhesion` WHERE `user_id`=? ORDER BY `date_envoi` DESC",
                this::map, userId);
    }

    /** Vrai si cette piece d'identite (empreinte) a deja ete utilisee pour une demande. */
    public boolean pieceHashExiste(String pieceHash) {
        return scalarInt("SELECT COUNT(*) FROM `demande_adhesion` WHERE `piece_hash`=?", pieceHash) > 0;
    }

    public DemandeAdhesion findByPieceHash(String pieceHash) {
        return queryOne("SELECT * FROM `demande_adhesion` WHERE `piece_hash`=?", this::map, pieceHash);
    }

    public boolean demandeEnAttenteExiste(int tontineId, int userId) {
        return scalarInt("SELECT COUNT(*) FROM `demande_adhesion` WHERE `tontine_id`=? AND `user_id`=? "
                + "AND `statut`='EN_ATTENTE'", tontineId, userId) > 0;
    }

    public boolean changerStatut(int id, StatutDemande statut) {
        return executeUpdate("UPDATE `demande_adhesion` SET `statut`=? WHERE `id`=?", statut, id) > 0;
    }
}
