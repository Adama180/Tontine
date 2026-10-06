package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `demande_creation` : insertion, lecture, modification, suppression et requetes utiles. */
public class DemandeCreationDao extends BaseDao {

    private DemandeCreation map(java.sql.ResultSet rs) throws java.sql.SQLException {
        DemandeCreation o = new DemandeCreation();
        o.setId(rs.getInt("id"));
        o.setDemandeurId(rs.getInt("demandeur_id"));
        o.setNomSouhaite(rs.getString("nom_souhaite"));
        o.setTypeSouhaite(enumOf(TypeTontine.class, rs.getString("type_souhaite")));
        o.setMontant(rs.getDouble("montant"));
        o.setFrequence(enumOf(Frequence.class, rs.getString("frequence")));
        o.setNbMembre(rs.getInt("nb_membre"));
        o.setDateEnvoi(rs.getObject("date_envoi", LocalDateTime.class));
        o.setStatut(enumOf(StatutDemande.class, rs.getString("statut")));
        o.setAdminId(getInteger(rs, "admin_id"));
        o.setPieceHash(rs.getString("piece_hash"));
        o.setPieceChemin(rs.getString("piece_chemin"));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(DemandeCreation o) {
        if (o.getDateEnvoi() == null) {
            o.setDateEnvoi(LocalDateTime.now());
        }
        if (o.getStatut() == null) {
            o.setStatut(StatutDemande.EN_ATTENTE);
        }

        int id = insertReturnId(
                "INSERT INTO `demande_creation` (`demandeur_id`,`nom_souhaite`,`type_souhaite`,`montant`,`frequence`,`nb_membre`,`date_envoi`,`statut`,`admin_id`,`piece_hash`,`piece_chemin`) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                o.getDemandeurId(),
                o.getNomSouhaite(),
                o.getTypeSouhaite(),
                o.getMontant(),
                o.getFrequence(),
                o.getNbMembre(),
                o.getDateEnvoi(),
                o.getStatut(),
                o.getAdminId(),
                o.getPieceHash(),
                o.getPieceChemin());
        o.setId(id);
        return id;
    }

    public boolean update(DemandeCreation o) {
        return executeUpdate("UPDATE `demande_creation` SET `demandeur_id`=?,`nom_souhaite`=?,`type_souhaite`=?,`montant`=?,`frequence`=?,`nb_membre`=?,`date_envoi`=?,`statut`=?,`admin_id`=?,`piece_hash`=?,`piece_chemin`=? WHERE `id`=?",
                o.getDemandeurId(),
                o.getNomSouhaite(),
                o.getTypeSouhaite(),
                o.getMontant(),
                o.getFrequence(),
                o.getNbMembre(),
                o.getDateEnvoi(),
                o.getStatut(),
                o.getAdminId(),
                o.getPieceHash(),
                o.getPieceChemin(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `demande_creation` WHERE `id`=?", id) > 0;
    }

    public DemandeCreation findById(int id) {
        return queryOne("SELECT * FROM `demande_creation` WHERE `id`=?", this::map, id);
    }

    public List<DemandeCreation> findAll() {
        return queryList("SELECT * FROM `demande_creation` ORDER BY `id`", this::map);
    }

    public List<DemandeCreation> findByStatut(StatutDemande statut) {
        return queryList("SELECT * FROM `demande_creation` WHERE `statut`=? ORDER BY `date_envoi`", this::map, statut);
    }

    public List<DemandeCreation> findByDemandeur(int demandeurId) {
        return queryList("SELECT * FROM `demande_creation` WHERE `demandeur_id`=? ORDER BY `date_envoi` DESC",
                this::map, demandeurId);
    }

    /** Premiere demande de creation deposee avec cette piece d'identite (null si aucune). */
    public DemandeCreation findByPieceHash(String pieceHash) {
        return queryOne("SELECT * FROM `demande_creation` WHERE `piece_hash`=?", this::map, pieceHash);
    }

    /** Accepte ou refuse la demande (par l'administrateur). */
    public boolean traiter(int id, StatutDemande statut, int adminId) {
        return executeUpdate("UPDATE `demande_creation` SET `statut`=?, `admin_id`=? WHERE `id`=?",
                statut, adminId, id) > 0;
    }
}
