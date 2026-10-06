package com.tontine.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connexion a la base MySQL de XAMPP.
 * Si vous avez change le mot de passe root ou le port MySQL, modifiez les 3 constantes ci-dessous.
 */
public final class ConnexionBD {

    private static final String URL = "jdbc:mysql://localhost:3306/tontine"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String UTILISATEUR = "root";
    private static final String MOT_DE_PASSE = ""; // vide par defaut dans XAMPP

    private ConnexionBD() {
    }

    /** Ouvre une nouvelle connexion (a fermer par l'appelant, idealement avec try-with-resources). */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE);
        } catch (SQLException e) {
            throw new DaoException("Impossible de se connecter a MySQL. Verifiez que MySQL est demarre "
                    + "dans XAMPP et que la base 'tontine' existe. Detail : " + e.getMessage(), e);
        }
    }

    /** Retourne true si la connexion fonctionne. */
    public static boolean tester() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException | DaoException e) {
            return false;
        }
    }
}
