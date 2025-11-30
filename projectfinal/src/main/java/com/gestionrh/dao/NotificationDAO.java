package com.gestionrh.dao;

import com.gestionrh.model.NotificationUser;

import java.util.List;

public interface NotificationDAO {

    void creerNotificationPourDepartement(int i, String message, String type);

    void creerNotificationPourProjet(int projetId, String message, String type);

    List<NotificationUser> getNotificationsUtilisateur(int utilisateurId, boolean seulementNonLues);

    void marquerCommeLue(int notificationUserId);
}

