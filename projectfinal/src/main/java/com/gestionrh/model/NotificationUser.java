package com.gestionrh.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "Notification_user",
    uniqueConstraints = @UniqueConstraint(columnNames = {"notification_id", "utilisateur_id"})
)
public class NotificationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "est_lu", nullable = false)
    private boolean estLu = false;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;

    // ===== GETTERS / SETTERS =====

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public boolean isEstLu() { return estLu; }
    public void setEstLu(boolean estLu) { this.estLu = estLu; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateLecture() { return dateLecture; }
    public void setDateLecture(LocalDateTime dateLecture) { this.dateLecture = dateLecture; }

    public void marquerLu() {
        this.estLu = true;
        this.dateLecture = LocalDateTime.now();
    }
    
    @Transient
    private String dateCreationFormatee;

    public String getDateCreationFormatee() {
        return dateCreationFormatee;
    }

    public void setDateCreationFormatee(String dateCreationFormatee) {
        this.dateCreationFormatee = dateCreationFormatee;
    }
}
