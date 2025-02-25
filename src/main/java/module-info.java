module com.example.typeshelper {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires javafx.graphics;
    requires java.xml.bind;

    opens com.example.typeshelper to javafx.fxml, java.xml.bind;
    exports com.example.typeshelper;
}