package com.tontine.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tontine.util.ConnexionBD;
import com.tontine.util.DaoException;

/** Outils communs a tous les DAO : execution des requetes SQL et conversions. */
public abstract class BaseDao {

    /** Transforme une ligne de resultat SQL en objet Java. */
    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    // ---------- execution ----------

    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection c = ConnexionBD.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> liste = new ArrayList<>();
                while (rs.next()) {
                    liste.add(mapper.map(rs));
                }
                return liste;
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur SQL : " + e.getMessage(), e);
        }
    }

    protected <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> liste = queryList(sql, mapper, params);
        return liste.isEmpty() ? null : liste.get(0);
    }

    protected int executeUpdate(String sql, Object... params) {
        try (Connection c = ConnexionBD.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur SQL : " + e.getMessage(), e);
        }
    }

    protected int insertReturnId(String sql, Object... params) {
        try (Connection c = ConnexionBD.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur SQL : " + e.getMessage(), e);
        }
    }

    protected int scalarInt(String sql, Object... params) {
        Integer v = queryOne(sql, rs -> rs.getInt(1), params);
        return v == null ? 0 : v;
    }

    protected double scalarDouble(String sql, Object... params) {
        Double v = queryOne(sql, rs -> rs.getDouble(1), params);
        return v == null ? 0 : v;
    }

    // ---------- conversions ----------

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p instanceof Enum) {
                p = ((Enum<?>) p).name();
            }
            ps.setObject(i + 1, p);
        }
    }

    protected static Integer getInteger(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    protected static Double getDoubleObj(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }

    protected static <E extends Enum<E>> E enumOf(Class<E> type, String valeur) {
        return valeur == null ? null : Enum.valueOf(type, valeur);
    }
}
