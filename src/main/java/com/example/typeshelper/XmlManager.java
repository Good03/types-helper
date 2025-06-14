package com.example.typeshelper;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class XmlManager {
    private Document xmlDocument;
    private File currentFile;
    private final Map<String, Element> typeElements = new HashMap<>();

    public void loadXmlData(File file) {
        try {
            currentFile = file;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            xmlDocument = builder.parse(file);
            xmlDocument.getDocumentElement().normalize();

            NodeList typeNodes = xmlDocument.getElementsByTagName("type");
            typeElements.clear();

            for (int i = 0; i < typeNodes.getLength(); i++) {
                Element typeElement = (Element) typeNodes.item(i);
                String name = typeElement.getAttribute("name");
                if (!name.isEmpty()) {
                    typeElements.put(name, typeElement);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToXmlFile() {
        if (currentFile == null) return;

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
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

    public void deleteElement(String name) {
        try {
            NodeList typeNodes = xmlDocument.getElementsByTagName("type");
            for (int i = 0; i < typeNodes.getLength(); i++) {
                Element typeElement = (Element) typeNodes.item(i);
                String typeName = typeElement.getAttribute("name");
                if (typeName.equals(name)) {
                    Node parent = typeElement.getParentNode();
                    if (parent != null) {
                        parent.removeChild(typeElement);
                    }
                    break;
                }
            }
            saveToXmlFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshTypeElements() {
        typeElements.clear();
        NodeList typeNodes = xmlDocument.getElementsByTagName("type");
        for (int i = 0; i < typeNodes.getLength(); i++) {
            Element typeElement = (Element) typeNodes.item(i);
            String name = typeElement.getAttribute("name");
            if (!name.isEmpty()) {
                typeElements.put(name, typeElement);
            }
        }
    }
    public Document getXmlDocument() {
        return xmlDocument;
    }


    public Map<String, Element> getTypeElements() {
        return typeElements;
    }
}
