module nose.buscaminas {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;

    opens nose.buscaminas to javafx.fxml;
    exports nose.buscaminas;
}