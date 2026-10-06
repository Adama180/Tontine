package com.tontine.service;

import java.util.List;

import com.tontine.dao.AdhesionDao;
import com.tontine.dao.NotificationDao;
import com.tontine.dao.UtilisateurDao;
import com.tontine.model.Adhesion;
import com.tontine.model.Notification;
import com.tontine.model.StatutAdhesion;
import com.tontine.model.Utilisateur;

/** Notifications : consultation par l'utilisateur et envoi par les autres services. */
public final class NotificationService {

    private static final NotificationDao DAO = new NotificationDao();

    private NotificationService() {
    }

    public static List<Notification> lister(int userId) {
        return DAO.findByUser(userId);
    }

    public static int compterNonLues(int userId) {
        return DAO.compterNonLues(userId);
    }

    public static void marquerLue(int userId, int notificationId) {
        Notification n = DAO.findById(notificationId);
        Controles.exiger(n != null && n.getUserId() == userId, "Notification introuvable.");
        DAO.marquerLue(notificationId);
    }

    public static void marquerToutesLues(int userId) {
        DAO.marquerToutesLues(userId);
    }

    static void notifier(int userId, String message, String type) {
        try {
            String m = message.length() > 250 ? message.substring(0, 250) : message;
            DAO.envoyer(userId, m, type);
        } catch (RuntimeException e) {
            // une notification perdue ne doit pas annuler l'operation principale
        }
    }

    static void notifierAdmins(String message, String type) {
        for (Utilisateur u : new UtilisateurDao().findAll()) {
            if (u.isAdmin()) {
                notifier(u.getId(), message, type);
            }
        }
    }

    static void notifierMembres(int tontineId, String message, String type, Integer exclure) {
        for (Adhesion a : new AdhesionDao().findByTontine(tontineId)) {
            if (a.getStatut() == StatutAdhesion.ACTIF && (exclure == null || a.getUserId() != exclure)) {
                notifier(a.getUserId(), message, type);
            }
        }
    }
}
