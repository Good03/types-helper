package com.example.typeshelper;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.Priority;

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

        // Настроим ListView с кастомными ячейками
        typeListView.setCellFactory(param -> new ListCell<String>() {
            private final Button deleteButton = new Button("Delete");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    Label label = new Label(item);
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    hbox.getChildren().addAll(label, spacer, deleteButton);
                    deleteButton.setOnAction(e -> {
                        xmlManager.deleteElement(item);
                        typeListView.getItems().remove(item);
                    });

                    setText(null);
                    setGraphic(hbox);
                }
            }
        });

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
