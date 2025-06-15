package com.example.typeshelper;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

public class WindowManager {
    private final XmlManager xmlManager;
    private final ListView<String> typeListView;
    private final ComboBox<String> categoryDropdown = new ComboBox<>();
    private final ComboBox<String> usageDropdown = new ComboBox<>();
    private final ComboBox<String> valueDropdown = new ComboBox<>();
    private final Button saveButton = new Button("Save");
    private final Map<Element, TextField> elementTextFields = new HashMap<>();
    private final Map<Attr, TextField> attributeTextFields = new HashMap<>();
    private final ObservableList<String> masterData;

    public WindowManager(XmlManager xmlManager, ListView<String> typeListView, ObservableList<String> masterData) throws IOException {
        this.xmlManager = xmlManager;
        this.typeListView = typeListView;
        this.masterData = masterData;
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

    public void openNewTypeWindow(Stage stage) {
        Stage newTypeStage = new Stage();
        newTypeStage.setTitle("Add New Type");

        GridPane root = new GridPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.setVgap(5);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);

        int[] rowIndex = {0};

        TextField nameField = new TextField();
        root.addRow(rowIndex[0]++, new Label("Type name:"), nameField);
        TextField nominalField = new TextField();
        root.addRow(rowIndex[0]++, new Label("Nominal:"), nominalField);
        TextField lifetimeField = new TextField();
        root.addRow(rowIndex[0]++, new Label("Lifetime:"), lifetimeField);
        TextField restockField = new TextField();
        root.addRow(rowIndex[0]++, new Label("Restock:"), restockField);
        TextField quantMinField = new TextField();
        root.addRow(rowIndex[0]++, new Label("QuantMin:"), quantMinField);
        TextField quantMaxField = new TextField();
        root.addRow(rowIndex[0]++, new Label("QuantMax:"), quantMaxField);
        TextField costField = new TextField();
        root.addRow(rowIndex[0]++, new Label("Cost:"), costField);

        CheckBox countInCargo = new CheckBox("count_in_cargo");
        CheckBox countInHoarder = new CheckBox("count_in_hoarder");
        CheckBox countInMap = new CheckBox("count_in_map");
        CheckBox countInPlayer = new CheckBox("count_in_player");
        CheckBox crafted = new CheckBox("crafted");
        CheckBox deloot = new CheckBox("deloot");

        VBox flagsBox = new VBox(5,
                new Label("Flags:"),
                countInCargo,
                countInHoarder,
                countInMap,
                countInPlayer,
                crafted,
                deloot
        );

        root.add(flagsBox, 0, rowIndex[0]++, 2, 1);

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(categoryDropdown.getItems());
        categoryBox.setEditable(true);
        root.addRow(rowIndex[0]++, new Label("Category:"), categoryBox);

        List<ComboBox<String>> usageFields = new ArrayList<>();
        VBox usageBox = new VBox(5);
        Button addUsageBtn = new Button("Add Usage");
        addUsageBtn.setOnAction(e -> {
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll(usageDropdown.getItems());
            combo.setEditable(true);
            usageFields.add(combo);
            usageBox.getChildren().add(combo);
        });
        root.add(new Label("Usages:"), 0, rowIndex[0]);
        root.add(new VBox(5, usageBox, addUsageBtn), 1, rowIndex[0]++);

        List<ComboBox<String>> valueFields = new ArrayList<>();
        VBox valueBox = new VBox(5);
        Button addValueBtn = new Button("Add Value");
        addValueBtn.setOnAction(e -> {
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll(valueDropdown.getItems());
            combo.setEditable(true);
            valueFields.add(combo);
            valueBox.getChildren().add(combo);
        });
        root.add(new Label("Values:"), 0, rowIndex[0]);
        root.add(new VBox(5, valueBox, addValueBtn), 1, rowIndex[0]++);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> {
            String typeName = nameField.getText().trim();
            if (typeName.isEmpty()) return;

            Document doc = xmlManager.getXmlDocument();
            Element newType = doc.createElement("type");
            newType.setAttribute("name", typeName);

            addElementWithText(doc, newType, "nominal", nominalField.getText().trim());
            addElementWithText(doc, newType, "lifetime", lifetimeField.getText().trim());
            addElementWithText(doc, newType, "restock", restockField.getText().trim());
            addElementWithText(doc, newType, "min", quantMinField.getText().trim());
            addElementWithText(doc, newType, "quantmin", quantMinField.getText().trim());
            addElementWithText(doc, newType, "quantmax", quantMaxField.getText().trim());
            addElementWithText(doc, newType, "cost", costField.getText().trim());
            Element flagsEl = doc.createElement("flags");
            flagsEl.setAttribute("count_in_cargo", countInCargo.isSelected() ? "1" : "0");
            flagsEl.setAttribute("count_in_hoarder", countInHoarder.isSelected() ? "1" : "0");
            flagsEl.setAttribute("count_in_map", countInMap.isSelected() ? "1" : "0");
            flagsEl.setAttribute("count_in_player", countInPlayer.isSelected() ? "1" : "0");
            flagsEl.setAttribute("crafted", crafted.isSelected() ? "1" : "0");
            flagsEl.setAttribute("deloot", deloot.isSelected() ? "1" : "0");
            newType.appendChild(flagsEl);
            String category = categoryBox.getEditor().getText().trim();
            if (!category.isEmpty()) {
                Element catEl = doc.createElement("category");
                catEl.setAttribute("name", category);
                newType.appendChild(catEl);
            }

            for (ComboBox<String> usageField : usageFields) {
                String usage = usageField.getEditor().getText().trim();
                if (!usage.isEmpty()) {
                    Element usageEl = doc.createElement("usage");
                    usageEl.setAttribute("name", usage);
                    newType.appendChild(usageEl);
                }
            }

            for (ComboBox<String> valueField : valueFields) {
                String value = valueField.getEditor().getText().trim();
                if (!value.isEmpty()) {
                    Element valEl = doc.createElement("value");
                    valEl.setAttribute("name", value);
                    newType.appendChild(valEl);
                }
            }

            Node typesNode = doc.getElementsByTagName("types").item(0);
            typesNode.appendChild(newType);

            xmlManager.saveToXmlFile();
            xmlManager.refreshTypeElements();

            masterData.add(typeName);
            newTypeStage.close();
        });

        root.add(saveBtn, 0, rowIndex[0]++, 2, 1);
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        newTypeStage.setScene(new Scene(scrollPane, 400, 600));
        newTypeStage.show();
    }

    private void addElementWithText(Document doc, Element parent, String tagName, String textContent) {
        if (textContent != null && !textContent.isEmpty()) {
            Element el = doc.createElement(tagName);
            el.setTextContent(textContent);
            parent.appendChild(el);
        }
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

