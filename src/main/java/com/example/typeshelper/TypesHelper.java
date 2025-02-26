package com.example.typeshelper;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class TypesHelper extends Application {
    private final ListView<String> typeListView = new ListView<>();
    private final Button loadFileButton = new Button("Load XML File");
    private final XmlManager xmlManager = new XmlManager();
    private final EditorWindow editorWindow = new EditorWindow(xmlManager, typeListView);

    public TypesHelper() throws IOException {
    }

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10, loadFileButton, typeListView);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #36454F;");

        Scene scene = new Scene(root, 600, 400);
        loadFileButton.setOnAction(event -> openFileChooser(stage));

        typeListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                editorWindow.open(stage, xmlManager.getTypeElements().get(newValue));
            }
        });

        stage.setScene(scene);
        stage.setTitle("Types Helper");
        stage.show();
    }

    private void openFileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            xmlManager.loadXmlData(file);
            typeListView.getItems().setAll(xmlManager.getTypeElements().keySet());
        }
    }
    public static void main(String[] args) {
        Application.launch(TypesHelper.class, args);
    }
}

