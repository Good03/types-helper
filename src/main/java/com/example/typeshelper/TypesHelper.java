package com.example.typeshelper;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    private final TextField searchField = new TextField();
    private final Button addNewTypeButton = new Button("Add New Type");
    private final XmlManager xmlManager = new XmlManager();
    private final EditorWindow editorWindow = new EditorWindow(xmlManager, typeListView);



    public TypesHelper() throws IOException {
    }

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10, loadFileButton, searchField, typeListView);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #36454F;");

        Scene scene = new Scene(root, 600, 400);

        ObservableList<String> masterData = FXCollections.observableArrayList();
        FilteredList<String> filteredData = new FilteredList<>(masterData, p -> true);
        typeListView.setItems(filteredData);

        loadFileButton.setOnAction(event -> openFileChooser(stage, masterData));

        searchField.setPromptText("Search by classname...");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            final String filter = newValue.toLowerCase();
            filteredData.setPredicate(item -> {
                if (filter.isEmpty()) return true;
                return item.toLowerCase().contains(filter);
            });
        });

        typeListView.setCellFactory(param -> new ListCell<>() {
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
                        masterData.remove(item);
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

    private void openFileChooser(Stage stage, ObservableList<String> masterData) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            xmlManager.loadXmlData(file);
            masterData.setAll(xmlManager.getTypeElements().keySet());
            VBox root = (VBox) stage.getScene().getRoot();
            if (!root.getChildren().contains(addNewTypeButton)) {
                addNewTypeButton.setOnAction(e -> editorWindow.open(stage, xmlManager.getTypeElements().get(masterData.get(0))));
                root.getChildren().add(addNewTypeButton);
            }
        }
    }


    public static void main(String[] args) {
        Application.launch(TypesHelper.class, args);
    }
}
