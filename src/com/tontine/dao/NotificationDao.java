package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `notification` : insertion, lecture, modification, suppression et requetes utiles. */
public class NotificationDao extends BaseDao {

    private Notification map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Notification o = new Notification();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setMessage(rs.getString("message"));
        o.setType(rs.getString("type"));
        o.setDateEnvoi(rs.getObject("date_envoi", LocalDateTime.class));
        o.setLue(rs.getBoolean("lue"));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(Notification o) {
        if (o.getDateEnvoi() == null) {
            o.setDateEnvoi(LocalDateTime.now());
        }

        int id = insertReturnId(
                "INSERT INTO `notification` (`user_id`,`message`,`type`,`date_envoi`,`lue`) VALUES (?,?,?,?,?)",
                o.getUserId(),
                o.getMessage(),
                o.getType(),
                o.getDateEnvoi(),
                o.isLue());
        o.setId(id);
        return id;
    }

    public boolean update(Notification o) {
        return executeUpdate("UPDATE `notification` SET `user_id`=?,`message`=?,`type`=?,`date_envoi`=?,`lue`=? WHERE `id`=?",
                o.getUserId(),
                o.getMessage(),
                o.getType(),
                o.getDateEnvoi(),
                o.isLue(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `notification` WHERE `id`=?", id) > 0;
    }

    public Notification findById(int id) {
        return queryOne("SELECT * FROM `notification` WHERE `id`=?", this::map, id);
    }

    public List<Notification> findAll() {
        return queryList("SELECT * FROM `notification` ORDER BY `id`", this::map);
    }

    public List<Notification> findByUser(int userId) {
        return queryList("SELECT * FROM `notification` WHERE `user_id`=? ORDER BY `date_envoi` DESC, `id` DESC",
                this::map, userId);
    }

    public int compterNonLues(int userId) {
        return scalarInt("SELECT COUNT(*) FROM `notification` WHERE `user_id`=? AND `lue`=FALSE", userId);
    }

    public boolean marquerLue(int id) {
        return executeUpdate("UPDATE `notification` SET `lue`=TRUE WHERE `id`=?", id) > 0;
    }

    public int marquerToutesLues(int userId) {
        return executeUpdate("UPDATE `notification` SET `lue`=TRUE WHERE `user_id`=?", userId);
    }

    /** Raccourci : cree et enregistre une notification pour un utilisateur. */
    public void envoyer(int userId, String message, String type) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setMessage(message);
        n.setType(type);
        insert(n);
    }
}
