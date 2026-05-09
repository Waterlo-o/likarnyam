package com.example.likarnyam.session;

// TODO Sprint 1 — синглтон: хранит залогиненного врача и JWT токен
// Используется всеми контроллерами для знания "кто сейчас в системе"
public class UserSession {
    private static UserSession instance;
    private String jwtToken;
    private Object currentDoctor; // заменить на DoctorDto

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public String getJwtToken() { return jwtToken; }
    public void setJwtToken(String token) { this.jwtToken = token; }
}
