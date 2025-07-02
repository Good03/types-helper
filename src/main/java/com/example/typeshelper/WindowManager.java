package com.example.typeshelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import org.w3c.dom.*;
import java.io.IOException;

public class WindowManager {
    private final XmlManager xmlManager;
    private final DefaultListModel<String> listModel;
    private final List<String> categoryOptions;
    private final List<String> usageOptions;
    private final List<String> valueOptions;

    public WindowManager(XmlManager xmlManager, JList<String> typeList, DefaultListModel<String> listModel) throws IOException {
        this.xmlManager = xmlManager;
        this.listModel = listModel;
        FilesManager filesManager = new FilesManager();
        this.categoryOptions = filesManager.loadCategories();
        this.usageOptions = filesManager.loadUsage();
        this.valueOptions = filesManager.loadValues();
    }

    public void openEditor(String typeName) {
        Element element = xmlManager.getTypeElements().get(typeName);
        if (element == null) return;

        JDialog dialog = new JDialog((Frame) null, "Edit: " + typeName, true);
        dialog.setSize(450, 700);
        dialog.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        Map<Node, JTextField> textFields = new HashMap<>();

        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel usagePanel = new JPanel();
        usagePanel.setLayout(new BoxLayout(usagePanel, BoxLayout.Y_AXIS));
        JPanel valuePanel = new JPanel();
        valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.Y_AXIS));


        Runnable refreshCategoryUI = () -> refreshCategoryUI(element, categoryPanel, dialog);
        Runnable refreshUsageUI = () -> refreshUsageUI(element, usagePanel, dialog);
        Runnable refreshValueUI = () -> refreshValueUI(element, valuePanel, dialog);

        NodeList children = element.getChildNodes();
        int row = 0;
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element child) {
                String tag = child.getTagName();
                if (tag.equals("category") || tag.equals("usage") || tag.equals("value")) {
                    continue;
                }
                JLabel label = new JLabel(tag);
                JTextField field = new JTextField(child.getTextContent());
                textFields.put(child, field);

                gbc.gridx = 0;
                gbc.gridy = row;
                mainPanel.add(label, gbc);
                gbc.gridx = 1;
                mainPanel.add(field, gbc);
                row++;
            }
        }

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        mainPanel.add(categoryPanel, gbc);

        gbc.gridy = row++;
        mainPanel.add(new JLabel("Usages:"), gbc);
        gbc.gridy = row++;
        mainPanel.add(new JScrollPane(usagePanel), gbc);

        gbc.gridy = row++;
        mainPanel.add(new JLabel("Values:"), gbc);
        gbc.gridy = row++;
        mainPanel.add(new JScrollPane(valuePanel), gbc);

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(Color.GREEN);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            for (Map.Entry<Node, JTextField> entry : textFields.entrySet()) {
                entry.getKey().setTextContent(entry.getValue().getText().trim());
            }
            xmlManager.saveToXmlFile();
            xmlManager.refreshTypeElements();
            dialog.dispose();
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(saveButton);
        buttonsPanel.add(closeButton);

        gbc.gridy = row++;
        gbc.gridwidth = 2;
        mainPanel.add(buttonsPanel, gbc);

        dialog.setContentPane(new JScrollPane(mainPanel));

        refreshCategoryUI.run();
        refreshUsageUI.run();
        refreshValueUI.run();

        dialog.setVisible(true);
    }

    private void refreshCategoryUI(Element element, JPanel categoryPanel, JDialog dialog) {
        categoryPanel.removeAll();
        NodeList categories = element.getElementsByTagName("category");
        if (categories.getLength() > 0) {
            Element catEl = (Element) categories.item(0);
            JLabel catLabel = new JLabel("Category: " + catEl.getAttribute("name"));
            JButton editBtn = new JButton("Edit");
            editBtn.addActionListener(e -> {
                categoryPanel.removeAll();

                JComboBox<String> combo = new JComboBox<>(categoryOptions.toArray(new String[0]));
                combo.setEditable(true);
                combo.setSelectedItem(catEl.getAttribute("name"));

                JButton applyBtn = new JButton("Apply");
                applyBtn.addActionListener(ev -> {
                    String newCat = ((String) combo.getEditor().getItem()).trim();
                    if (!newCat.isEmpty()) {
                        catEl.setAttribute("name", newCat);
                        xmlManager.saveToXmlFile();
                        xmlManager.refreshTypeElements();
                        refreshCategoryUI(element, categoryPanel, dialog);
                        dialog.revalidate();
                        dialog.repaint();
                    }
                });

                categoryPanel.add(new JLabel("Category: "));
                categoryPanel.add(combo);
                categoryPanel.add(applyBtn);
                categoryPanel.revalidate();
                categoryPanel.repaint();
            });

            categoryPanel.add(catLabel);
            categoryPanel.add(editBtn);
        } else {
            categoryPanel.add(new JLabel("Category: (none)"));
        }
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    private void refreshUsageUI(Element element, JPanel usagePanel, JDialog dialog) {
        usagePanel.removeAll();

        NodeList usageNodes = element.getElementsByTagName("usage");
        for (int i = 0; i < usageNodes.getLength(); i++) {
            Element usageEl = (Element) usageNodes.item(i);
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

            JLabel usageLabel = new JLabel(usageEl.getAttribute("name"));
            JButton editBtn = new JButton("Edit");
            JButton delBtn = new JButton("Delete");

            editBtn.addActionListener(e -> {
                rowPanel.removeAll();
                JComboBox<String> combo = new JComboBox<>(usageOptions.toArray(new String[0]));
                combo.setEditable(true);
                combo.setSelectedItem(usageEl.getAttribute("name"));

                JButton applyBtn = new JButton("Apply");
                applyBtn.addActionListener(ev -> {
                    String newUsage = ((String) combo.getEditor().getItem()).trim();
                    if (!newUsage.isEmpty()) {
                        usageEl.setAttribute("name", newUsage);
                        xmlManager.saveToXmlFile();
                        xmlManager.refreshTypeElements();
                        refreshUsageUI(element, usagePanel, dialog);
                        dialog.revalidate();
                        dialog.repaint();
                    }
                });

                rowPanel.add(combo);
                rowPanel.add(applyBtn);
                rowPanel.revalidate();
                rowPanel.repaint();
            });

            delBtn.addActionListener(e -> {
                element.removeChild(usageEl);
                xmlManager.saveToXmlFile();
                xmlManager.refreshTypeElements();
                refreshUsageUI(element, usagePanel, dialog);
                dialog.revalidate();
                dialog.repaint();
            });

            rowPanel.add(usageLabel);
            rowPanel.add(editBtn);
            rowPanel.add(delBtn);

            usagePanel.add(rowPanel);
        }

        JButton addUsageBtn = new JButton("Add Usage");
        addUsageBtn.addActionListener(e -> {
            JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            JComboBox<String> combo = new JComboBox<>(usageOptions.toArray(new String[0]));
            combo.setEditable(true);
            JButton applyBtn = new JButton("Add");
            applyBtn.addActionListener(ev -> {
                String newUsage = ((String) combo.getEditor().getItem()).trim();
                if (!newUsage.isEmpty()) {
                    Element newUsageEl = xmlManager.getXmlDocument().createElement("usage");
                    newUsageEl.setAttribute("name", newUsage);
                    element.appendChild(newUsageEl);
                    xmlManager.saveToXmlFile();
                    xmlManager.refreshTypeElements();
                    refreshUsageUI(element, usagePanel, dialog);
                    dialog.revalidate();
                    dialog.repaint();
                }
            });
            addPanel.add(combo);
            addPanel.add(applyBtn);

            usagePanel.add(addPanel);
            usagePanel.revalidate();
            usagePanel.repaint();
            addUsageBtn.setEnabled(false);
        });

        usagePanel.add(addUsageBtn);
        usagePanel.revalidate();
        usagePanel.repaint();
    }

    private void refreshValueUI(Element element, JPanel valuePanel, JDialog dialog) {
        valuePanel.removeAll();

        NodeList valueNodes = element.getElementsByTagName("value");
        for (int i = 0; i < valueNodes.getLength(); i++) {
            Element valueEl = (Element) valueNodes.item(i);
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

            JLabel valueLabel = new JLabel(valueEl.getAttribute("name"));
            JButton editBtn = new JButton("Edit");
            JButton delBtn = new JButton("Delete");

            editBtn.addActionListener(e -> {
                rowPanel.removeAll();
                JComboBox<String> combo = new JComboBox<>(valueOptions.toArray(new String[0]));
                combo.setEditable(true);
                combo.setSelectedItem(valueEl.getAttribute("name"));

                JButton applyBtn = new JButton("Apply");
                applyBtn.addActionListener(ev -> {
                    String newValue = ((String) combo.getEditor().getItem()).trim();
                    if (!newValue.isEmpty()) {
                        valueEl.setAttribute("name", newValue);
                        xmlManager.saveToXmlFile();
                        xmlManager.refreshTypeElements();
                        refreshValueUI(element, valuePanel, dialog);
                        dialog.revalidate();
                        dialog.repaint();
                    }
                });

                rowPanel.add(combo);
                rowPanel.add(applyBtn);
                rowPanel.revalidate();
                rowPanel.repaint();
            });

            delBtn.addActionListener(e -> {
                element.removeChild(valueEl);
                xmlManager.saveToXmlFile();
                xmlManager.refreshTypeElements();
                refreshValueUI(element, valuePanel, dialog);
                dialog.revalidate();
                dialog.repaint();
            });

            rowPanel.add(valueLabel);
            rowPanel.add(editBtn);
            rowPanel.add(delBtn);

            valuePanel.add(rowPanel);
        }

        JButton addValueBtn = new JButton("Add Value");
        addValueBtn.addActionListener(e -> {
            JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            JComboBox<String> combo = new JComboBox<>(valueOptions.toArray(new String[0]));
            combo.setEditable(true);
            JButton applyBtn = new JButton("Add");
            applyBtn.addActionListener(ev -> {
                String newValue = ((String) combo.getEditor().getItem()).trim();
                if (!newValue.isEmpty()) {
                    Element newValueEl = xmlManager.getXmlDocument().createElement("value");
                    newValueEl.setAttribute("name", newValue);
                    element.appendChild(newValueEl);
                    xmlManager.saveToXmlFile();
                    xmlManager.refreshTypeElements();
                    refreshValueUI(element, valuePanel, dialog);
                    dialog.revalidate();
                    dialog.repaint();
                }
            });
            addPanel.add(combo);
            addPanel.add(applyBtn);

            valuePanel.add(addPanel);
            valuePanel.revalidate();
            valuePanel.repaint();
            addValueBtn.setEnabled(false);
        });

        valuePanel.add(addValueBtn);
        valuePanel.revalidate();
        valuePanel.repaint();
    }

    public void openNewTypeDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Add New Type", true);
        dialog.setSize(450, 650);
        dialog.setLocationRelativeTo(parent);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        JTextField nameField = new JTextField();
        JTextField nominalField = new JTextField();
        JTextField lifetimeField = new JTextField();
        JTextField restockField = new JTextField();
        JTextField quantMinField = new JTextField();
        JTextField quantMaxField = new JTextField();
        JTextField costField = new JTextField();

        root.add(new JLabel("Type name:"), gridPos(0, row));
        root.add(nameField, gridPos(1, row++));
        root.add(new JLabel("Nominal:"), gridPos(0, row));
        root.add(nominalField, gridPos(1, row++));
        root.add(new JLabel("Lifetime:"), gridPos(0, row));
        root.add(lifetimeField, gridPos(1, row++));
        root.add(new JLabel("Restock:"), gridPos(0, row));
        root.add(restockField, gridPos(1, row++));
        root.add(new JLabel("QuantMin:"), gridPos(0, row));
        root.add(quantMinField, gridPos(1, row++));
        root.add(new JLabel("QuantMax:"), gridPos(0, row));
        root.add(quantMaxField, gridPos(1, row++));
        root.add(new JLabel("Cost:"), gridPos(0, row));
        root.add(costField, gridPos(1, row++));

        JPanel flagsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        JCheckBox countInCargo = new JCheckBox("count_in_cargo");
        JCheckBox countInHoarder = new JCheckBox("count_in_hoarder");
        JCheckBox countInMap = new JCheckBox("count_in_map");
        JCheckBox countInPlayer = new JCheckBox("count_in_player");
        JCheckBox crafted = new JCheckBox("crafted");
        JCheckBox deloot = new JCheckBox("deloot");

        flagsPanel.add(countInCargo);
        flagsPanel.add(countInHoarder);
        flagsPanel.add(countInMap);
        flagsPanel.add(countInPlayer);
        flagsPanel.add(crafted);
        flagsPanel.add(deloot);

        gbc.gridwidth = 2;
        root.add(new JLabel("Flags:"), gridPos(0, row++));
        root.add(flagsPanel, gridPos(0, row++));
        gbc.gridwidth = 1;

        JComboBox<String> categoryBox = new JComboBox<>(categoryOptions.toArray(new String[0]));
        categoryBox.setEditable(true);
        root.add(new JLabel("Category:"), gridPos(0, row));
        root.add(categoryBox, gridPos(1, row++));

        JPanel usageBox = new JPanel();
        usageBox.setLayout(new BoxLayout(usageBox, BoxLayout.Y_AXIS));
        List<JComboBox<String>> usageFields = new ArrayList<>();

        JButton addUsageBtn = new JButton("Add Usage");
        actionListener(root, usageBox, usageFields, addUsageBtn, usageOptions);

        root.add(new JLabel("Usages:"), gridPos(0, row));
        JPanel usagePanel = new JPanel(new BorderLayout());
        usagePanel.add(new JScrollPane(usageBox), BorderLayout.CENTER);
        usagePanel.add(addUsageBtn, BorderLayout.SOUTH);
        root.add(usagePanel, gridPos(1, row++));

        JPanel valueBox = new JPanel();
        valueBox.setLayout(new BoxLayout(valueBox, BoxLayout.Y_AXIS));
        List<JComboBox<String>> valueFields = new ArrayList<>();
        JButton addValueBtn = new JButton("Add Value");
        actionListener(root, valueBox, valueFields, addValueBtn, valueOptions);

        root.add(new JLabel("Values:"), gridPos(0, row));
        JPanel valuePanel = new JPanel(new BorderLayout());
        valuePanel.add(new JScrollPane(valueBox), BorderLayout.CENTER);
        valuePanel.add(addValueBtn, BorderLayout.SOUTH);
        root.add(valuePanel, gridPos(1, row++));

        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(Color.GREEN);
        saveBtn.setForeground(Color.WHITE);

        saveBtn.addActionListener(e -> {
            String typeName = nameField.getText().trim();
            if (typeName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Type name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

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

            String category = ((String) categoryBox.getEditor().getItem()).trim();
            if (!category.isEmpty()) {
                Element catEl = doc.createElement("category");
                catEl.setAttribute("name", category);
                newType.appendChild(catEl);
            }

            for (JComboBox<String> combo : usageFields) {
                String usage = ((String) combo.getEditor().getItem()).trim();
                if (!usage.isEmpty()) {
                    Element usageEl = doc.createElement("usage");
                    usageEl.setAttribute("name", usage);
                    newType.appendChild(usageEl);
                }
            }

            for (JComboBox<String> combo : valueFields) {
                String value = ((String) combo.getEditor().getItem()).trim();
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

            listModel.addElement(typeName);

            dialog.dispose();
        });

        gbc.gridwidth = 2;
        root.add(saveBtn, gridPos(0, row++));

        dialog.setContentPane(new JScrollPane(root));
        dialog.setVisible(true);
    }

    private void actionListener(JPanel root, JPanel usageBox, List<JComboBox<String>> usageFields, JButton addUsageBtn, List<String> usageOptions) {
        addUsageBtn.addActionListener(e -> {
            JComboBox<String> combo = new JComboBox<>(usageOptions.toArray(new String[0]));
            combo.setEditable(true);
            usageFields.add(combo);
            usageBox.add(combo);
            usageBox.revalidate();
            usageBox.repaint();

            root.revalidate();
            root.repaint();
        });
    }

    private GridBagConstraints gridPos(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }

    private void addElementWithText(Document doc, Element parent, String tagName, String textContent) {
        if (textContent != null && !textContent.isEmpty()) {
            Element el = doc.createElement(tagName);
            el.setTextContent(textContent);
            parent.appendChild(el);
        }
    }
}
