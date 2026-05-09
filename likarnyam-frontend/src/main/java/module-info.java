module com.example.likarnyam {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;

    opens com.example.likarnyam to javafx.fxml;
    exports com.example.likarnyam;
}