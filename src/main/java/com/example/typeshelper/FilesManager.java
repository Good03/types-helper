package com.example.typeshelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilesManager {

    public List<String> loadCategories() throws IOException {
        String defaultCategories = "None clothes containers explosives food tools weapons vehiclesparts";
        return loadFile("categories.txt", defaultCategories);
    }

    public List<String> loadUsage() throws IOException {
        String defaultUsage = "None Coast Farm Firefighter Hunting Industrial Medic Military Office Police Prison School Town Village Bunker_Alcatras";
        return loadFile("usage.txt", defaultUsage);
    }

    public List<String> loadValues() throws IOException {
        String defaultValues = "None Tier1 Tier2 Tier3 Tier4";
        return loadFile("values.txt", defaultValues);
    }

    private List<String> readFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                return Arrays.asList(line.trim().split("\\s+"));
            }
        }
        return new ArrayList<>();
    }

    private List<String> loadFile(String fileName, String defaultContent) throws IOException {
        List<String> items;
        File directory = new File("data");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File("data\\" + fileName);
        if (!file.exists()) {
            file.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(defaultContent);
            }
        }
        items = readFromFile(file);
        return items;
    }
}
