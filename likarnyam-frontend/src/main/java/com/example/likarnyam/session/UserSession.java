package com.example.likarnyam.session;

import java.io.*;
import java.nio.file.*;

public class UserSession {
    private static UserSession instance;
    private String jwtToken;

    private static final String TOKEN_FILE =
            System.getProperty("user.home") + "/.likarnyam_token";

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

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
            if (!file.exists()) return false;

            // Проверяем что файл не старше 30 дней
            long ageInDays = (System.currentTimeMillis() - file.lastModified())
                    / (1000 * 60 * 60 * 24);
            if (ageInDays > 30) {
                file.delete();
                return false;
            }

            this.jwtToken = Files.readString(Path.of(TOKEN_FILE));
            return true;

        } catch (IOException e) {
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
}