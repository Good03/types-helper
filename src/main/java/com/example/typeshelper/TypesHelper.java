package com.example.typeshelper;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.*;

public class TypesHelper extends Application {
    private final ListView<String> typeListView = new ListView<>();
    private final Button loadFileButton = new Button("Load XML File");
    private final Button saveButton = new Button("Save");

    //TODO починить запись в файл, добавляются лишние строки

    private Document xmlDocument;
    private File currentFile;
    private final Map<String, Element> typeElements = new HashMap<>();
    private final ComboBox<String> categoryDropdown = new ComboBox<>();
    private final ComboBox<String> usageDropdown = new ComboBox<>();
    private final ComboBox<String> valueDropdown = new ComboBox<>();


    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10, loadFileButton, typeListView);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        saveButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        categoryDropdown.getItems().addAll("None", "clothes", "containers", "explosives", "food", "tools", "weapons", "vehiclesparts");
        valueDropdown.getItems().addAll("None", "Tier1", "Tier2", "Tier3", "Tier4");
        usageDropdown.getItems().addAll("None", "Coast", "Farm", "Firefighter", "Hunting", "Industrial", "Medic", "Military", "Office", "Police", "Prison", "School", "Town", "Village");

        Scene scene = new Scene(root, 600, 400);

        loadFileButton.setOnAction(event -> openFileChooser(stage));

        typeListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                openEditWindow(stage, typeElements.get(newValue));
            }
        });

        stage.setScene(scene);
        stage.setTitle("Types Helper");
        stage.show();
    }

    private void openEditWindow(Stage parentStage, Element selectedElement) {
        Stage editStage = new Stage();
        editStage.setOnCloseRequest(e -> {
            typeListView.getSelectionModel().clearSelection();
        });
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
            if (node instanceof Element) {
                Element element = (Element) node;
                String key = element.getTagName();
                String value = element.getTextContent().trim();

                if (element.hasAttributes()) {
                    NamedNodeMap attributes = element.getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node attr = attributes.item(j);
                        String attrKey = key + "@" + attr.getNodeName();
                        String attrValue = attr.getNodeValue();

                        TextField textField = new TextField(attrValue);
                        editRoot.addRow(rowIndex++, new Label(attrKey + ":"), textField);
                        inputFields.add(textField);
                    }
                } else if (!key.equals("flags") && !key.equals("category") && !key.equals("usage") && !key.equals("value")) {
                    TextField textField = new TextField(value);
                    editRoot.addRow(rowIndex++, new Label(key + ":"), textField);
                    inputFields.add(textField);
                }
            }
        }

        Element categoryElement = getElementByTagName(selectedElement, "category");
        if (categoryElement != null) {
            categoryDropdown.setValue(categoryElement.getAttribute("name"));
        }
        Element usageElement = getElementByTagName(selectedElement, "usage");
        if (usageElement != null) {
            usageDropdown.setValue(usageElement.getAttribute("name"));
        }
        Element valueElement = getElementByTagName(selectedElement, "value");
        if (valueElement != null) {
            valueDropdown.setValue(valueElement.getAttribute("name"));
        }
        editRoot.addRow(rowIndex++, new Label("Category:"), categoryDropdown);
        editRoot.addRow(rowIndex++, new Label("Usage:"), usageDropdown);
        editRoot.addRow(rowIndex++, new Label("Value:"), valueDropdown);

        // Размещение кнопки по центру
        saveButton.setOnAction(e -> {
            int fieldIndex = 0;
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if (!element.hasAttributes() && !element.getTagName().equals("category") && !element.getTagName().equals("usage") && !element.getTagName().equals("value")) {
                        element.setTextContent(inputFields.get(fieldIndex++).getText());
                    }
                }
            }

            updateCategoryElement(selectedElement, categoryDropdown.getValue());
            updateUsageElement(selectedElement, usageDropdown.getValue());
            updateValueElement(selectedElement, valueDropdown.getValue());

            saveToXmlFile();
            typeListView.getSelectionModel().clearSelection();
            editStage.close();

        });
        editRoot.add(saveButton, 0, rowIndex, 2, 1);
        Scene editScene = new Scene(scrollPane, 300, 750);
        editStage.setScene(editScene);
        editStage.setTitle(selectedElement.getAttribute("name"));
        editStage.show();
    }

    private void updateCategoryElement(Element selectedElement, String categoryName) {
        Element categoryElement = getElementByTagName(selectedElement, "category");
        if (categoryElement != null) {
            categoryElement.setAttribute("name", categoryName);
            categoryElement.setTextContent("");  // Очищаем текстовое содержимое
        } else if (!categoryName.equals("None")) {
            categoryElement = selectedElement.getOwnerDocument().createElement("category");
            categoryElement.setAttribute("name", categoryName);
            selectedElement.appendChild(categoryElement);
        }
    }

    private void updateUsageElement(Element selectedElement, String usageName) {
        Element usageElement = getElementByTagName(selectedElement, "usage");
        if (usageElement != null) {
            usageElement.setAttribute("name", usageName);
            usageElement.setTextContent("");  // Очищаем текстовое содержимое
        } else if (!usageName.equals("None")) {
            usageElement = selectedElement.getOwnerDocument().createElement("usage");
            usageElement.setAttribute("name", usageName);
            selectedElement.appendChild(usageElement);
        }
    }

    private void updateValueElement(Element selectedElement, String valueName) {
        Element valueElement = getElementByTagName(selectedElement, "value");
        if (valueElement != null) {
            valueElement.setAttribute("name", valueName);
            valueElement.setTextContent("");  // Очищаем текстовое содержимое
        } else if (!valueName.equals("None")) {
            valueElement = selectedElement.getOwnerDocument().createElement("value");
            valueElement.setAttribute("name", valueName);
            selectedElement.appendChild(valueElement);
        }
    }

    private void loadXmlData(File file) {
        try {
            currentFile = file;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            xmlDocument = builder.parse(file);
            xmlDocument.getDocumentElement().normalize();

            NodeList typeNodes = xmlDocument.getElementsByTagName("type");
            typeElements.clear();
            typeListView.getItems().clear();

            for (int i = 0; i < typeNodes.getLength(); i++) {
                Element typeElement = (Element) typeNodes.item(i);
                String name = typeElement.getAttribute("name");

                if (!name.isEmpty()) {
                    typeElements.put(name, typeElement);
                    typeListView.getItems().add(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            loadXmlData(file);
        }
    }

    private void saveToXmlFile() {
        if (currentFile == null) return;

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Устанавливаем отступы для читаемости
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            DOMSource source = new DOMSource(xmlDocument);
            StreamResult result = new StreamResult(currentFile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private Element getElementByTagName(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return (Element) nodeList.item(0);
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
