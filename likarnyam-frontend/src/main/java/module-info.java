module com.example.likarnyam {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.example.likarnyam to javafx.fxml;
    opens com.example.likarnyam.controller to javafx.fxml;

    exports com.example.likarnyam;
    exports com.example.likarnyam.controller;
    exports com.example.likarnyam.client;
    exports com.example.likarnyam.session;
    exports com.example.likarnyam.dto;
    exports com.example.likarnyam.util;
}