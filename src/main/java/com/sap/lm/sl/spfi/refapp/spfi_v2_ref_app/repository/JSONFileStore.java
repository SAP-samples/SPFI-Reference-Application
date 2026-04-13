package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository;


import lombok.Getter;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class JSONFileStore implements FileStore<JSONObject> {
    private final String parentDirectory;

    public JSONFileStore(String parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public void store(JSONObject jsonObject, String fileName) throws IOException {
        new File(this.parentDirectory).mkdirs();
        File directory = new File(this.parentDirectory);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + directory);
        }
        File file = new File(directory, fileName);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonObject.toString(4)); // Pretty print with an indent of 4 spaces
        } catch (IOException e) {
            throw new IOException("Failed to write JSON to file: " + file, e);
        }
    }

    public JSONObject fetch(String fileName) throws IOException {
        File file = new File(this.parentDirectory, fileName);
        if (!file.exists()) {
            throw new IOException("File not found: " + file);
        }

        // Use BufferedReader for efficient reading
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return new JSONObject(content.toString());
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File not found during reading: " + file);
        } catch (IOException e) {
            throw new IOException("Failed to read JSON from file: " + file, e);
        }
    }

    public void delete(String fileName) throws IOException {
        File file = new File(this.parentDirectory, fileName);
        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file);
        }
    }

    public boolean exists(String fileName) {
        File file = new File(this.parentDirectory, fileName);
        return file.exists();
    }

    // get list of files in directory
    public File[] listFiles() {
        File directory = new File(this.parentDirectory);
        File[] files = directory.listFiles();
        return Objects.requireNonNullElseGet(files, () -> new File[0]);
    }

    // get all json files in directory and return as array of JSONObjects
    public List<JSONObject> fetchAll() throws IOException {
        File[] files = listFiles();
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (File file : files) {
            jsonObjects.add(fetch(file.getName()));
        }
        return jsonObjects;
    }


    public void deleteDirectory() throws IOException {
        File directory = new File(this.parentDirectory);
        if (!directory.exists()) {
            return;
        }
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (!file.delete()) {
                throw new IOException("Failed to delete file: " + file);
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete directory: " + directory);
        }
    }

    public String extendWithFileFormat(String fileName) {
        return fileName + ".json";
    }
}
