package com.example.project_ee297;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;
    public String getUsername() {
        return username;
    }

    public String getPhotoPath() {
        return "C:\\Users\\Lenovo\\Pictures\\Screenshots\\" + username + ".jpg";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
// getters and setters
}
