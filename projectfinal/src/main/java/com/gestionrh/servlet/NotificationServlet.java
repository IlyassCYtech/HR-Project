package com.gestionrh.servlet;

import com.gestionrh.dao.NotificationDAO;
import com.gestionrh.dao.impl.NotificationDAOImpl;
import com.gestionrh.model.NotificationUser;
import com.gestionrh.model.Utilisateur;
import com.gestionrh.model.Utilisateur.Role;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/app/notifications")
public class NotificationServlet extends HttpServlet {

    private NotificationDAO notificationDAO;


    @Override
    public void init() throws ServletException {
        super.init();
        this.notificationDAO = new NotificationDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ⚠️ utiliser la bonne clé de session : "utilisateur"
        Utilisateur utilisateurConnecte =
                (Utilisateur) request.getSession().getAttribute("utilisateur");

        if (utilisateurConnecte == null) {
            // si ton login est sur /app/login, mets /app/login ici
            response.sendRedirect(request.getContextPath() + "/app/login");
            return;
        }

        boolean seulementNonLues = "1".equals(request.getParameter("nonLues"));
        boolean isAdmin = utilisateurConnecte.getRole() == Role.ADMIN;

        List<NotificationUser> notifications;
        notifications = notificationDAO.getNotificationsUtilisateur( utilisateurConnecte.getId(), seulementNonLues );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (NotificationUser nu : notifications) {
            if (nu.getDateCreation() != null) {
                nu.setDateCreationFormatee(nu.getDateCreation().format(formatter));
            }
        }
        
        
        request.setAttribute("notifications", notifications);
        request.setAttribute("seulementNonLues", seulementNonLues);
        request.setAttribute("isAdmin", isAdmin);

        request.getRequestDispatcher("/WEB-INF/jsp/notifcations.jsp")
        .forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if ("marquerLu".equals(action)) {

            try {
                int notifUserId = Integer.parseInt(request.getParameter("notificationUserId"));
                notificationDAO.marquerCommeLue(notifUserId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // ⚠️ bien remettre /app/notifications
            if ("1".equals(request.getParameter("nonLues"))) {
                response.sendRedirect(request.getContextPath() + "/app/notifications?nonLues=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/app/notifications");
            }
            return;
        }

        doGet(request, response);
    }
}
