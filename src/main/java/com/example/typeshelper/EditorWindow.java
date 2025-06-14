package com.example.typeshelper;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.*;

import org.w3c.dom.*;


import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EditorWindow {
    private final XmlManager xmlManager;
    private final ListView<String> typeListView;
    private final ComboBox<String> categoryDropdown = new ComboBox<>();
    private final ComboBox<String> usageDropdown = new ComboBox<>();
    private final ComboBox<String> valueDropdown = new ComboBox<>();
    private final Button saveButton = new Button("Save");
    private final Map<Element, TextField> elementTextFields = new HashMap<>();
    private final Map<Attr, TextField> attributeTextFields = new HashMap<>();

    public EditorWindow(XmlManager xmlManager, ListView<String> typeListView) throws IOException {
        this.xmlManager = xmlManager;
        this.typeListView = typeListView;
        FilesManager filesManager = new FilesManager();
        valueDropdown.getItems().addAll(filesManager.loadValues());
        categoryDropdown.getItems().addAll(filesManager.loadCategories());
        usageDropdown.getItems().addAll(filesManager.loadUsage());

        saveButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
    }

    public void open(Stage stage, Element selectedElement) {
        Stage editStage = new Stage();
        editStage.setOnCloseRequest(e -> typeListView.getSelectionModel().clearSelection());

        GridPane editRoot = new GridPane();
        editRoot.setPadding(new Insets(10));
        editRoot.setHgap(10);
        editRoot.setVgap(5);

        ScrollPane scrollPane = new ScrollPane(editRoot);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);

        populateEditorFields(editRoot, selectedElement, editStage);

        Scene editScene = new Scene(scrollPane, 350, 750);
        editStage.setScene(editScene);
        editStage.setTitle(selectedElement.getAttribute("name"));
        editStage.show();
    }

    private void populateEditorFields(GridPane editRoot, Element selectedElement, Stage editStage) {
        editRoot.getChildren().clear();
        elementTextFields.clear();
        attributeTextFields.clear();
        int[] rowIndex = {0};

        elementStream(selectedElement.getChildNodes()).forEach(element -> {
            String key = element.getTagName();

            switch (key) {
                case "usage", "value" -> renderRemovableElement(editRoot, selectedElement, editStage, element, key, rowIndex);
                case "category" -> renderCategoryField(editRoot, selectedElement, editStage, element, rowIndex);
                default -> renderElementWithAttributesOrText(editRoot, element, key, rowIndex);
            }
        });


        renderUsageAdditionUI(editRoot, selectedElement, editStage, rowIndex);
        renderValueAdditionUI(editRoot, selectedElement, editStage, rowIndex);
        renderSaveAndCloseButtons(editRoot, selectedElement, editStage, rowIndex);
    }
    private void renderRemovableElement(GridPane grid, Element parent, Stage stage, Element child, String key, int[] rowIndex) {
        String name = child.getAttribute("name");
        Label label = new Label(key + ": " + name);
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            parent.removeChild(child);
            xmlManager.saveToXmlFile();
            xmlManager.refreshTypeElements();
            populateEditorFields(grid, parent, stage);
        });

        HBox box = new HBox(10, label, deleteBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        grid.add(box, 0, rowIndex[0]++, 2, 1);
    }
    private void renderCategoryField(GridPane grid, Element parent, Stage stage, Element categoryElement, int[] rowIndex) {
        if (!categoryElement.hasAttribute("name")) return;

        String categoryName = categoryElement.getAttribute("name");
        Label label = new Label("category: " + categoryName);
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: orange; -fx-text-fill: black;");
        int currentRow = rowIndex[0];

        editBtn.setOnAction(e -> {
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll(categoryDropdown.getItems());
            combo.setEditable(true);
            combo.setValue(categoryName);

            Button applyBtn = new Button("Apply");
            applyBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            applyBtn.setOnAction(ev -> {
                String newName = combo.getEditor().getText().trim();
                if (!newName.isEmpty()) {
                    categoryElement.setAttribute("name", newName);
                    xmlManager.saveToXmlFile();
                    xmlManager.refreshTypeElements();
                    populateEditorFields(grid, parent, stage);
                }
            });

            HBox editBox = new HBox(10, new Label("Edit category:"), combo, applyBtn);
            editBox.setAlignment(Pos.CENTER_LEFT);

            grid.getChildren().removeIf(node -> {
                Integer row = GridPane.getRowIndex(node);
                return row != null && row == currentRow;
            });

            grid.add(editBox, 0, currentRow, 2, 1);
        });

        HBox box = new HBox(10, label, editBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        grid.add(box, 0, rowIndex[0]++, 2, 1);
    }
    private void renderElementWithAttributesOrText(GridPane grid, Element element, String key, int[] rowIndex) {
        if (element.hasAttributes()) {
            NamedNodeMap attrs = element.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                Node attr = attrs.item(j);
                TextField field = new TextField(attr.getNodeValue());
                attributeTextFields.put((Attr) attr, field);
                grid.addRow(rowIndex[0]++, new Label(key + "@" + attr.getNodeName() + ":"), field);
            }
        } else {
            TextField field = new TextField(element.getTextContent().trim());
            elementTextFields.put(element, field);
            grid.addRow(rowIndex[0]++, new Label(key + ":"), field);
        }
    }
    private void renderUsageAdditionUI(GridPane grid, Element parent, Stage stage, int[] rowIndex) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(usageDropdown.getItems());
        combo.setEditable(true);

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> {
            String name = combo.getEditor().getText().trim();
            if (!name.isEmpty() && elementStream(parent.getElementsByTagName("usage"))
                    .noneMatch(el -> el.getAttribute("name").equals(name))) {
                Element newUsage = xmlManager.getXmlDocument().createElement("usage");
                newUsage.setAttribute("name", name);
                parent.appendChild(newUsage);
                xmlManager.saveToXmlFile();
                xmlManager.refreshTypeElements();
                populateEditorFields(grid, parent, stage);
            }
        });


        HBox box = new HBox(10, new Label("Add Usage:"), combo, addBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        grid.add(box, 0, rowIndex[0]++, 2, 1);
    }
    private void renderValueAdditionUI(GridPane grid, Element parent, Stage stage, int[] rowIndex) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(valueDropdown.getItems());
        combo.setEditable(true);

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> {
            String name = combo.getEditor().getText().trim();
            if (!name.isEmpty() && elementStream(parent.getElementsByTagName("value"))
                    .noneMatch(el -> el.getAttribute("name").equals(name))) {
                Element newValue = xmlManager.getXmlDocument().createElement("value");
                newValue.setAttribute("name", name);
                parent.appendChild(newValue);
                xmlManager.saveToXmlFile();
                xmlManager.refreshTypeElements();
                populateEditorFields(grid, parent, stage);
            }
        });


        HBox box = new HBox(10, new Label("Add Value:"), combo, addBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        grid.add(box, 0, rowIndex[0]++, 2, 1);
    }
    private void renderSaveAndCloseButtons(GridPane grid, Element parent, Stage stage, int[] rowIndex) {
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: gray; -fx-text-fill: white;");

        saveButton.setOnAction(e -> {
            elementTextFields.forEach((element, field) -> {
                String newText = field.getText().trim();
                element.setTextContent(newText);
            });

            attributeTextFields.forEach((attr, field) -> {
                String newVal = field.getText().trim();
                attr.setValue(newVal);
            });

            xmlManager.saveToXmlFile();
            xmlManager.refreshTypeElements();
            Element fresh = xmlManager.getTypeElements().get(parent.getAttribute("name"));
            populateEditorFields(grid, fresh != null ? fresh : parent, stage);
        });

        closeButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10, saveButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(buttonBox, 0, rowIndex[0]++, 2, 1);
    }



    private Stream<Node> stream(NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item);
    }

    private Stream<Element> elementStream(NodeList nodeList) {
        return stream(nodeList)
                .filter(node -> node instanceof Element)
                .map(node -> (Element) node);
    }

}

