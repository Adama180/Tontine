package com.tontine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.tontine.dao.JournalAuditDao;
import com.tontine.dao.UtilisateurDao;
import com.tontine.model.Utilisateur;
import com.tontine.util.ConnexionBD;
import com.tontine.util.InitBase;
import com.tontine.util.PasswordUtil;

/**
 * Test de l'etape 3 : connexion MySQL, compte administrateur et DAO.
 * Lancer : clic droit > Run As > Java Application (MySQL doit etre demarre dans XAMPP).
 */
public class TestConnexion {

    public static void main(String[] args) {
        System.out.println("=== TEST ETAPE 3 ===");

        // 1. Connexion
        if (!ConnexionBD.tester()) {
            System.out.println("[ECHEC] Connexion impossible. Lancez MySQL dans XAMPP et verifiez la base 'tontine'.");
            return;
        }
        System.out.println("[OK] Connexion a MySQL reussie.");

        // 2. Tables presentes
        try (Connection c = ConnexionBD.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SHOW TABLES")) {
            int n = 0;
            while (rs.next()) {
                n++;
            }
            System.out.println((n == 13 ? "[OK] " : "[ATTENTION] ") + n + " tables trouvees (13 attendues).");
        } catch (Exception e) {
            System.out.println("[ECHEC] Lecture des tables : " + e.getMessage());
            return;
        }

        UtilisateurDao dao = new UtilisateurDao();

        // 3. Compte administrateur
        boolean cree = InitBase.creerAdminParDefaut();
        System.out.println(cree ? "[OK] Compte administrateur cree." : "[OK] Compte administrateur deja present.");
        Utilisateur admin = dao.findByLogin(InitBase.ADMIN_EMAIL);
        boolean mdpOk = admin != null && PasswordUtil.verifier(InitBase.ADMIN_MOT_DE_PASSE, admin.getMotDePasse());
        System.out.println(mdpOk ? "[OK] Connexion admin verifiee (email : " + InitBase.ADMIN_EMAIL
                + ", mot de passe : " + InitBase.ADMIN_MOT_DE_PASSE + ")" : "[ECHEC] Mot de passe admin incorrect.");

        // 4. Test insertion / lecture / suppression d'un utilisateur temporaire
        Utilisateur t = new Utilisateur();
        t.setNom("Test");
        t.setPrenom("Dao");
        t.setEmail("test.dao@tontine.sn");
        t.setTelephone("000000000");
        t.setMotDePasse(PasswordUtil.hasher("test"));
        int id = dao.insert(t);
        Utilisateur relu = dao.findById(id);
        boolean okLecture = relu != null && "Test".equals(relu.getNom()) && relu.getDateCreation() != null;
        System.out.println(okLecture ? "[OK] Insertion et lecture d'un utilisateur (id " + id + ")."
                : "[ECHEC] Insertion/lecture.");
        System.out.println(dao.delete(id) ? "[OK] Suppression de l'utilisateur de test." : "[ECHEC] Suppression.");

        // 5. Journal d'audit
        new JournalAuditDao().log(admin == null ? null : admin.getId(), "TEST_ETAPE_3", "Test de connexion reussi");
        System.out.println("[OK] Ecriture dans le journal d'audit.");

        System.out.println("=== FIN DU TEST : si tout est [OK], passez a l'etape 4 ===");
    }
}
