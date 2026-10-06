package com.tontine.util;

import java.sql.SQLException;

/** Erreur d'acces a la base de donnees (non controlee : pas besoin de try/catch partout). */
public class DaoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(String message) {
        super(message);
    }

    /** Vrai si l'erreur vient d'une valeur deja existante (email, telephone, nom de tontine unique...). */
    public boolean estDoublon() {
        Throwable t = getCause();
        return (t instanceof SQLException) && "23000".equals(((SQLException) t).getSQLState());
    }
}
