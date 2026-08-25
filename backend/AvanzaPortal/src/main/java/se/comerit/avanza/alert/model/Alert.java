package se.comerit.avanza.alert.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //ska få relation till user senare
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean dismissed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Alert() {}

    public Alert(Integer userId, String alertType, String message, boolean dismissed, LocalDateTime createdAt) {
        this.userId = userId;
        this.alertType = alertType;
        this.message = message;
        this.dismissed = dismissed;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
