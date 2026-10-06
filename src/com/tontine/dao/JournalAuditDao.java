package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `journal_audit` : insertion, lecture, modification, suppression et requetes utiles. */
public class JournalAuditDao extends BaseDao {

    private JournalAudit map(java.sql.ResultSet rs) throws java.sql.SQLException {
        JournalAudit o = new JournalAudit();
        o.setId(rs.getInt("id"));
        o.setUserId(getInteger(rs, "user_id"));
        o.setAction(rs.getString("action"));
        o.setDetails(rs.getString("details"));
        o.setDate(rs.getObject("date", LocalDateTime.class));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(JournalAudit o) {
        if (o.getDate() == null) {
            o.setDate(LocalDateTime.now());
        }

        int id = insertReturnId(
                "INSERT INTO `journal_audit` (`user_id`,`action`,`details`,`date`) VALUES (?,?,?,?)",
                o.getUserId(),
                o.getAction(),
                o.getDetails(),
                o.getDate());
        o.setId(id);
        return id;
    }

    public boolean update(JournalAudit o) {
        return executeUpdate("UPDATE `journal_audit` SET `user_id`=?,`action`=?,`details`=?,`date`=? WHERE `id`=?",
                o.getUserId(),
                o.getAction(),
                o.getDetails(),
                o.getDate(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `journal_audit` WHERE `id`=?", id) > 0;
    }

    public JournalAudit findById(int id) {
        return queryOne("SELECT * FROM `journal_audit` WHERE `id`=?", this::map, id);
    }

    public List<JournalAudit> findAll() {
        return queryList("SELECT * FROM `journal_audit` ORDER BY `id`", this::map);
    }

    public List<JournalAudit> findRecents(int limite) {
        return queryList("SELECT * FROM `journal_audit` ORDER BY `date` DESC, `id` DESC LIMIT ?", this::map, limite);
    }

    public List<JournalAudit> findByUser(int userId) {
        return queryList("SELECT * FROM `journal_audit` WHERE `user_id`=? ORDER BY `date` DESC, `id` DESC",
                this::map, userId);
    }

    /** Raccourci : enregistre une action dans le journal d'audit. */
    public void log(Integer userId, String action, String details) {
        JournalAudit j = new JournalAudit();
        j.setUserId(userId);
        j.setAction(action);
        j.setDetails(details);
        insert(j);
    }
}
