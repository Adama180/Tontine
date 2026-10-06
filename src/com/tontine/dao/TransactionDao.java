package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `transaction` : insertion, lecture, modification, suppression et requetes utiles. */
public class TransactionDao extends BaseDao {

    private Transaction map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Transaction o = new Transaction();
        o.setId(rs.getInt("id"));
        o.setTontineId(rs.getInt("tontine_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setCycleId(getInteger(rs, "cycle_id"));
        o.setType(enumOf(TypeTransaction.class, rs.getString("type")));
        o.setMontant(rs.getDouble("montant"));
        o.setDate(rs.getObject("date", LocalDateTime.class));
        o.setStatut(enumOf(StatutTransaction.class, rs.getString("statut")));
        o.setModePaiement(enumOf(ModePaiement.class, rs.getString("mode_paiement")));
        o.setReference(rs.getString("reference"));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(Transaction o) {
        if (o.getDate() == null) {
            o.setDate(LocalDateTime.now());
        }
        if (o.getStatut() == null) {
            o.setStatut(StatutTransaction.CONFIRMEE);
        }

        int id = insertReturnId(
                "INSERT INTO `transaction` (`tontine_id`,`user_id`,`cycle_id`,`type`,`montant`,`date`,`statut`,`mode_paiement`,`reference`) VALUES (?,?,?,?,?,?,?,?,?)",
                o.getTontineId(),
                o.getUserId(),
                o.getCycleId(),
                o.getType(),
                o.getMontant(),
                o.getDate(),
                o.getStatut(),
                o.getModePaiement(),
                o.getReference());
        o.setId(id);
        return id;
    }

    public boolean update(Transaction o) {
        return executeUpdate("UPDATE `transaction` SET `tontine_id`=?,`user_id`=?,`cycle_id`=?,`type`=?,`montant`=?,`date`=?,`statut`=?,`mode_paiement`=?,`reference`=? WHERE `id`=?",
                o.getTontineId(),
                o.getUserId(),
                o.getCycleId(),
                o.getType(),
                o.getMontant(),
                o.getDate(),
                o.getStatut(),
                o.getModePaiement(),
                o.getReference(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `transaction` WHERE `id`=?", id) > 0;
    }

    public Transaction findById(int id) {
        return queryOne("SELECT * FROM `transaction` WHERE `id`=?", this::map, id);
    }

    public List<Transaction> findAll() {
        return queryList("SELECT * FROM `transaction` ORDER BY `id`", this::map);
    }

    public List<Transaction> findByTontine(int tontineId) {
        return queryList("SELECT * FROM `transaction` WHERE `tontine_id`=? ORDER BY `date` DESC, `id` DESC",
                this::map, tontineId);
    }

    public List<Transaction> findByUser(int userId) {
        return queryList("SELECT * FROM `transaction` WHERE `user_id`=? ORDER BY `date` DESC, `id` DESC",
                this::map, userId);
    }

    public List<Transaction> findByTontineEtUser(int tontineId, int userId) {
        return queryList("SELECT * FROM `transaction` WHERE `tontine_id`=? AND `user_id`=? "
                + "ORDER BY `date` DESC, `id` DESC", this::map, tontineId, userId);
    }

    public List<Transaction> findByCycle(int cycleId) {
        return queryList("SELECT * FROM `transaction` WHERE `cycle_id`=? ORDER BY `date` DESC", this::map, cycleId);
    }

    /** Somme des transactions confirmees d'un type donne pour une tontine. */
    public double totalParType(int tontineId, TypeTransaction type) {
        return scalarDouble("SELECT COALESCE(SUM(`montant`),0) FROM `transaction` WHERE `tontine_id`=? "
                + "AND `type`=? AND `statut`='CONFIRMEE'", tontineId, type);
    }

    /** Somme des transactions confirmees d'un type donne pour un cycle. */
    public double totalParTypeEtCycle(int cycleId, TypeTransaction type) {
        return scalarDouble("SELECT COALESCE(SUM(`montant`),0) FROM `transaction` WHERE `cycle_id`=? "
                + "AND `type`=? AND `statut`='CONFIRMEE'", cycleId, type);
    }

    public boolean changerStatut(int id, StatutTransaction statut) {
        return executeUpdate("UPDATE `transaction` SET `statut`=? WHERE `id`=?", statut, id) > 0;
    }
}
