package com.tontine.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tontine.model.*;

/** DAO de la table `utilisateur` : insertion, lecture, modification, suppression et requetes utiles. */
public class UtilisateurDao extends BaseDao {

    private Utilisateur map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Utilisateur o = new Utilisateur();
        o.setId(rs.getInt("id"));
        o.setNom(rs.getString("nom"));
        o.setPrenom(rs.getString("prenom"));
        o.setEmail(rs.getString("email"));
        o.setTelephone(rs.getString("telephone"));
        o.setMotDePasse(rs.getString("mot_de_passe"));
        o.setRoleSysteme(enumOf(RoleSysteme.class, rs.getString("role_systeme")));
        o.setStatut(enumOf(StatutCompte.class, rs.getString("statut")));
        o.setDateCreation(rs.getObject("date_creation", LocalDateTime.class));
        return o;
    }

    /** Insere l'objet, renseigne son id et retourne cet id. */
    public int insert(Utilisateur o) {
        if (o.getRoleSysteme() == null) {
            o.setRoleSysteme(RoleSysteme.UTILISATEUR);
        }
        if (o.getStatut() == null) {
            o.setStatut(StatutCompte.ACTIF);
        }
        if (o.getDateCreation() == null) {
            o.setDateCreation(LocalDateTime.now());
        }

        int id = insertReturnId(
                "INSERT INTO `utilisateur` (`nom`,`prenom`,`email`,`telephone`,`mot_de_passe`,`role_systeme`,`statut`,`date_creation`) VALUES (?,?,?,?,?,?,?,?)",
                o.getNom(),
                o.getPrenom(),
                o.getEmail(),
                o.getTelephone(),
                o.getMotDePasse(),
                o.getRoleSysteme(),
                o.getStatut(),
                o.getDateCreation());
        o.setId(id);
        return id;
    }

    public boolean update(Utilisateur o) {
        return executeUpdate("UPDATE `utilisateur` SET `nom`=?,`prenom`=?,`email`=?,`telephone`=?,`mot_de_passe`=?,`role_systeme`=?,`statut`=?,`date_creation`=? WHERE `id`=?",
                o.getNom(),
                o.getPrenom(),
                o.getEmail(),
                o.getTelephone(),
                o.getMotDePasse(),
                o.getRoleSysteme(),
                o.getStatut(),
                o.getDateCreation(), o.getId()) > 0;
    }

    public boolean delete(int id) {
        return executeUpdate("DELETE FROM `utilisateur` WHERE `id`=?", id) > 0;
    }

    public Utilisateur findById(int id) {
        return queryOne("SELECT * FROM `utilisateur` WHERE `id`=?", this::map, id);
    }

    public List<Utilisateur> findAll() {
        return queryList("SELECT * FROM `utilisateur` ORDER BY `id`", this::map);
    }

    public Utilisateur findByEmail(String email) {
        return queryOne("SELECT * FROM `utilisateur` WHERE `email`=?", this::map, email);
    }

    public Utilisateur findByTelephone(String telephone) {
        return queryOne("SELECT * FROM `utilisateur` WHERE `telephone`=?", this::map, telephone);
    }

    /** Connexion : le login peut etre l'email ou le numero de telephone. */
    public Utilisateur findByLogin(String login) {
        return queryOne("SELECT * FROM `utilisateur` WHERE `email`=? OR `telephone`=?", this::map, login, login);
    }

    public boolean emailExiste(String email) {
        return scalarInt("SELECT COUNT(*) FROM `utilisateur` WHERE `email`=?", email) > 0;
    }

    public boolean telephoneExiste(String telephone) {
        return scalarInt("SELECT COUNT(*) FROM `utilisateur` WHERE `telephone`=?", telephone) > 0;
    }

    public boolean adminExiste() {
        return scalarInt("SELECT COUNT(*) FROM `utilisateur` WHERE `role_systeme`='ADMIN'") > 0;
    }

    public boolean changerMotDePasse(int id, String motDePasseHache) {
        return executeUpdate("UPDATE `utilisateur` SET `mot_de_passe`=? WHERE `id`=?", motDePasseHache, id) > 0;
    }

    public boolean changerStatut(int id, StatutCompte statut) {
        return executeUpdate("UPDATE `utilisateur` SET `statut`=? WHERE `id`=?", statut, id) > 0;
    }

    public boolean changerCoordonnees(int id, String email, String telephone) {
        return executeUpdate("UPDATE `utilisateur` SET `email`=?, `telephone`=? WHERE `id`=?", email, telephone, id) > 0;
    }
}
