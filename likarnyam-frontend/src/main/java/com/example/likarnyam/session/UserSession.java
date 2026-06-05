package com.example.likarnyam.session;

import java.io.*;
import java.nio.file.*;

public class UserSession {
    private static UserSession instance;
    private String jwtToken;
    private String theme = "LIGHT";
    private String timeFormat = "24h";
    private boolean animationsEnabled = true;

    private static final String TOKEN_FILE =
            System.getProperty("user.home") + "/.likarnyam_token";

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getTimeFormat() { return timeFormat; }
    public void setTimeFormat(String timeFormat) { this.timeFormat = timeFormat; }

    public boolean isAnimationsEnabled() { return animationsEnabled; }
    public void setAnimationsEnabled(boolean animationsEnabled) { this.animationsEnabled = animationsEnabled; }

    public String getJwtToken() { return jwtToken; }

    public void setJwtToken(String token) {
        this.jwtToken = token;
    }


    public void saveToken(String token) {
        this.jwtToken = token;
        try {
            Files.writeString(Path.of(TOKEN_FILE), token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean loadSavedToken() {
        try {
            File file = new File(TOKEN_FILE);
            System.out.println("Token file path: " + TOKEN_FILE);
            System.out.println("Token file exists: " + file.exists());

            if (!file.exists()) return false;

            long ageInDays = (System.currentTimeMillis() - file.lastModified())
                    / (1000 * 60 * 60 * 24);
            System.out.println("Token age in days: " + ageInDays);

            if (ageInDays > 30) {
                file.delete();
                return false;
            }

            this.jwtToken = Files.readString(Path.of(TOKEN_FILE)).trim();
            System.out.println("Token loaded: " + (this.jwtToken != null ? "YES" : "NO"));
            return true;

        } catch (IOException e) {
            System.out.println("Error loading token: " + e.getMessage());
            return false;
        }
    }


    public void logout() {
        this.jwtToken = null;
        try {
            Files.deleteIfExists(Path.of(TOKEN_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String role = "DOCTOR";

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin() { return "ADMIN".equals(role); }

    
}