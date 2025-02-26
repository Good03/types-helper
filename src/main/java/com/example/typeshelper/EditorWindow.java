package com.example.typeshelper;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditorWindow {
    private final XmlManager xmlManager;
    private final ListView<String> typeListView;
    private final ComboBox<String> categoryDropdown = new ComboBox<>();
    private final ComboBox<String> usageDropdown = new ComboBox<>();
    private final ComboBox<String> valueDropdown = new ComboBox<>();
    private final Button saveButton = new Button("Save");
    private final FilesManager filesManager = new FilesManager();

    public EditorWindow(XmlManager xmlManager, ListView<String> typeListView) throws IOException {
        this.xmlManager = xmlManager;
        this.typeListView = typeListView;
        valueDropdown.getItems().addAll(filesManager.loadValues());
        categoryDropdown.getItems().addAll(filesManager.loadCategories());
        usageDropdown.getItems().addAll(filesManager.loadUsage());
        saveButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
    }

    public void open(Stage parentStage, Element selectedElement) {
        Stage editStage = new Stage();
        editStage.setOnCloseRequest(e -> typeListView.getSelectionModel().clearSelection());

        GridPane editRoot = new GridPane();
        editRoot.setPadding(new Insets(10));
        editRoot.setHgap(10);
        editRoot.setVgap(5);
        ScrollPane scrollPane = new ScrollPane(editRoot);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);

        List<TextField> inputFields = new ArrayList<>();
        NodeList childNodes = selectedElement.getChildNodes();
        int rowIndex = 0;

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element element) {
                String key = element.getTagName();

                if (element.hasAttributes()) {
                    NamedNodeMap attributes = element.getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node attr = attributes.item(j);
                        TextField textField = new TextField(attr.getNodeValue());
                        editRoot.addRow(rowIndex++, new Label(key + "@" + attr.getNodeName() + ":"), textField);
                        inputFields.add(textField);
                    }
                } else {
                    TextField textField = new TextField(element.getTextContent().trim());
                    editRoot.addRow(rowIndex++, new Label(key + ":"), textField);
                    inputFields.add(textField);
                }
            }
        }

        editRoot.addRow(rowIndex++, new Label("Category:"), categoryDropdown);
        editRoot.addRow(rowIndex++, new Label("Usage:"), usageDropdown);
        editRoot.addRow(rowIndex++, new Label("Value:"), valueDropdown);
        editRoot.add(saveButton, 0, rowIndex, 2, 1);

        saveButton.setOnAction(e -> {
            xmlManager.saveToXmlFile();
            typeListView.getSelectionModel().clearSelection();
            editStage.close();
        });

        Scene editScene = new Scene(scrollPane, 300, 750);
        editStage.setScene(editScene);
        editStage.setTitle(selectedElement.getAttribute("name"));
        editStage.show();
    }
}
