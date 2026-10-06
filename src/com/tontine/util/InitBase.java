package com.tontine.util;

import com.tontine.dao.UtilisateurDao;
import com.tontine.model.RoleSysteme;
import com.tontine.model.StatutCompte;
import com.tontine.model.Utilisateur;

/** Initialisation de la base : cree le compte administrateur s'il n'existe pas encore. */
public final class InitBase {

    public static final String ADMIN_EMAIL = "admin@tontine.sn";
    public static final String ADMIN_TELEPHONE = "770000000";
    public static final String ADMIN_MOT_DE_PASSE = "Admin@123";

    private InitBase() {
    }

    /** @return true si le compte a ete cree, false s'il existait deja. */
    public static boolean creerAdminParDefaut() {
        UtilisateurDao dao = new UtilisateurDao();
        if (dao.adminExiste()) {
            return false;
        }
        Utilisateur admin = new Utilisateur();
        admin.setNom("Administrateur");
        admin.setPrenom("Plateforme");
        admin.setEmail(ADMIN_EMAIL);
        admin.setTelephone(ADMIN_TELEPHONE);
        admin.setMotDePasse(PasswordUtil.hasher(ADMIN_MOT_DE_PASSE));
        admin.setRoleSysteme(RoleSysteme.ADMIN);
        admin.setStatut(StatutCompte.ACTIF);
        dao.insert(admin);
        return true;
    }
}
