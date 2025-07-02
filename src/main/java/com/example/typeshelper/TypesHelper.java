package com.example.typeshelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class TypesHelper extends JFrame {

    private final JTextField searchField = new JTextField();
    private final JButton addNewTypeButton = new JButton("Add New Type");

    private List<String> currentExclusions = List.of(
            "_ColorBase", "_Base", "_SoundShader", "_SoundSet", "_Buttstock_ (приклад)", "_Hndgrd_ (рукоять)",
            "_Bipod_ (сошки)", "_Bttstck_ (приклад)", "_Buttstock (приклад)", "_Hndgrd (рукоять)", "_Bipod (сошки)", "_Bttstck (приклад)",
            "_Bttstk", "_Flashlight", "_Grip", "_Optic", "_PistolGrip", "_Supp",
            "_Suppressor (глушитель)", "_VerticalGrip (вертикальная рукоять)", "_RailAK (рукоять АК)", "_Muzzle_AK_", "_ReceiverCover_",
            "_base", "_Adapter", "_Ammo", "Bttstck"
    );

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> typeList = new JList<>(listModel);

    private final XmlManager xmlManager = new XmlManager();
    private final WindowManager windowManager;

    private List<String> fullDataList = List.of();

    public TypesHelper() throws IOException {
        super("Types Helper");

        windowManager = new WindowManager(xmlManager, typeList, listModel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton loadFileButton = new JButton("Load XML File");
        topPanel.add(loadFileButton, BorderLayout.WEST);
        PromptSupport.setPrompt("Search...", searchField);
        topPanel.add(searchField, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(typeList);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(addNewTypeButton);
        JButton extractAllClassnamesButton = new JButton("Extract All Classnames");
        bottomPanel.add(extractAllClassnamesButton);
        JButton exclusionSettingsButton = new JButton("Filters");
        bottomPanel.add(exclusionSettingsButton);

        exclusionSettingsButton.setVisible(true);
        addNewTypeButton.setVisible(false);
        extractAllClassnamesButton.setVisible(true);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadFileButton.addActionListener(e -> openFileChooser());
        extractAllClassnamesButton.addActionListener(e -> openDirChooserAndExtract());

        exclusionSettingsButton.addActionListener(e -> {
            ExclusionSettingsDialog dialog = new ExclusionSettingsDialog(this, currentExclusions);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                currentExclusions = dialog.getSelectedExclusions();
                System.out.println("Selected exclusions: " + currentExclusions);
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterList();
            }

            public void removeUpdate(DocumentEvent e) {
                filterList();
            }

            public void changedUpdate(DocumentEvent e) {
                filterList();
            }
        });

        typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        typeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = typeList.getSelectedValue();
                if (selected != null) {
                    windowManager.openEditor(selected);
                }
            }
        });

        addNewTypeButton.addActionListener(e -> windowManager.openNewTypeDialog(this));
    }

    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("XML files", "xml"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            xmlManager.loadXmlData(file);
            fullDataList = List.copyOf(xmlManager.getTypeElements().keySet());
            updateList(fullDataList);
            addNewTypeButton.setVisible(true);
        }
    }

    private void openDirChooserAndExtract() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = dirChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = dirChooser.getSelectedFile();
            System.out.println("Dir selected: " + selectedDir.getAbsolutePath());

            extractClassNamesFromDir(selectedDir);
        }
    }

    private void extractClassNamesFromDir(File rootDir) {
        List<File> cppFiles = new ArrayList<>();
        findFilesWithExtension(rootDir, ".cpp", cppFiles);

        String[] prefixes = {"IMP_", "KOD_", "Vp_", "SC_", "sk_", "SM_", "VP_", "SR_", "MOD_", "CTS_", "TG_", "FZ_", "kr_", "Fruck_"};
        Set<String> allClassNames = new LinkedHashSet<>();

        for (File cppFile : cppFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(cppFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("class ") && line.contains(":")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2) {
                            String className = parts[1];
                            int colonIndex = className.indexOf(':');
                            if (colonIndex != -1) {
                                className = className.substring(0, colonIndex).trim();
                            }

                            boolean matchesPrefix = false;
                            for (String prefix : prefixes) {
                                if (className.startsWith(prefix)) {
                                    matchesPrefix = true;
                                    break;
                                }
                            }
                            if (!matchesPrefix) continue;

                            boolean excluded = false;
                            for (String excl : currentExclusions) {
                                if (className.endsWith(excl)) {
                                    excluded = true;
                                    break;
                                }
                            }
                            if (excluded) continue;

                            System.out.println("Added: " + className);
                            allClassNames.add(className);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File outFile = new File("classnames.txt");
        try (PrintWriter writer = new PrintWriter(outFile)) {
            for (String className : allClassNames) {
                writer.println(className);
            }
            JOptionPane.showMessageDialog(this, "Classnames were saved in:\n" + outFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errer while saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void findFilesWithExtension(File dir, String extension, List<File> result) {
        if (dir == null || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                findFilesWithExtension(f, extension, result);
            } else if (f.isFile() && f.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                result.add(f);
            }
        }
    }

    private void updateList(List<String> items) {
        listModel.clear();
        for (String item : items) {
            listModel.addElement(item);
        }
    }

    private void filterList() {
        String filter = searchField.getText().toLowerCase();
        List<String> filtered = fullDataList.stream()
                .filter(name -> name.toLowerCase().contains(filter))
                .collect(Collectors.toList());
        updateList(filtered);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new TypesHelper().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
