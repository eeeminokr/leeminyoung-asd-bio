package com.ecoinsight.bdsp.asd.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecoinsight.bdsp.asd.repository.IEyeTrackingRepository;
import com.ecoinsight.bdsp.asd.repository.IAsdProjectSubjectRepository;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Transactional
@Service(DataSetDocumnetProcessorService.ID)
public class DataSetDocumnetProcessorService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String ID = "DataSetDocumnetProcessorService";

    @Autowired
    IEyeTrackingRepository _eyeTrackingRepository;

    @Autowired
    protected IAsdProjectSubjectRepository _projectSubjectRepository;

    @Value("${ecoinsight.video.crypto.output-dir}")
    private String _outputDir;

    @Value("${ecoinsight.download-dir}")
    private String _downloadDir;

    // Method to convert Document to JSON string
    public String convertDocumentToJson(Document document) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable pretty printing
            return objectMapper.writeValueAsString(document);
        } catch (Exception e) {
            LOGGER.error("Error converting Document to JSON", e);
            return null;
        }
    }

    public String convertJsonObjectToJsonString(JSONObject jsonObject) {
        try {
            // Convert JSONObject to a Map
            Map<String, Object> map = jsonObject.toMap();

            // Convert Map to a JSON string with indentation
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable indentation
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            LOGGER.error("Error converting JSONObject to JSON string", e);
            return null;
        }
    }

    // JSON 배열을 가독성있는 문자열로 변환하는 메소드
    public String convertListHashMapTomapper(List<LinkedHashMap<String, Object>> documentList) {
        try {
            // Convert List of LinkedHashMap to a JSON string with indentation
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable indentation
            return objectMapper.writeValueAsString(documentList);
        } catch (Exception e) {
            LOGGER.error("Error converting List of LinkedHashMap to JSON string", e);
            return null;
        }
    }

    public String convertJsonArrayToJsonString(JSONArray jsonArray) {
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                Object element = jsonArray.get(i);
                if (element instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) element;
                    Map<String, Object> map = jsonObject.toMap();
                    list.add(map);
                } else {
                    LOGGER.warn("Skipping non-JSONObject element at index: {}", i);
                }
            }

            // Convert List to a JSON string with indentation
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable indentation
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            LOGGER.error("Error converting JSONArray to JSON string", e);
            return null;
        }
    }

    // Method to convert JSON string to Document
    public Document convertVoToDocument(Object vo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable pretty printing
            String json = objectMapper.writeValueAsString(vo);
            return Document.parse(json);
        } catch (Exception e) {
            LOGGER.error("Error converting VO to Document", e);
            return null;
        }
    }

    public Document objectToDocument(Object obj) {
        Document doc = new Document();
        // Get all fields of the object's class and iterate over them
        for (Field field : obj.getClass().getDeclaredFields()) {
            // Make the field accessible
            field.setAccessible(true);
            try {
                // Add the field's name and value to the Document
                doc.append(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return doc;
    }

    public Document convertJsonStringToDocument(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable pretty printing
            return objectMapper.readValue(jsonString, Document.class);
        } catch (Exception e) {
            LOGGER.error("Error converting JSON string to Document", e);
            return null;
        }
    }

    public byte[] dataSetDownload(String systemId, long projectSeq, Map<String, List<String>> jsonDataMap,
            String directoryName, String fileName) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Create a list to store all JSON data
            List<Document> jsonDataList = new ArrayList<>();

            for (Map.Entry<String, List<String>> entry : jsonDataMap.entrySet()) {
                List<String> dataForSubject = entry.getValue();

                for (String jsonData : dataForSubject) {
                    // Convert JSON string to Document
                    Document jsonDocument = convertJsonStringToDocument(jsonData);

                    // Ensure cDate is formatted as a string
                    // formatDateField(jsonDocument, "cDate");

                    // Add the Document to the list
                    jsonDataList.add(jsonDocument);
                }
            }

            // Convert the list of Documents to JSON array string
            String jsonDataArray = convertDocumentListToJsonArray(jsonDataList);

            // Write the JSON string to the ZIP file
            String entryName = directoryName + fileName + projectSeq + ".json";
            zos.putNextEntry(new ZipEntry(entryName));

            // Write the formatted JSON string to the ZIP file
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable pretty printing
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(zos, mapper.readValue(jsonDataArray, Object.class));
            zos.closeEntry();
            zos.finish();
            return baos.toByteArray();
        }
    }

    private String convertDocumentListToJsonArray(List<Document> documentList) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable pretty printing
            return objectMapper.writeValueAsString(documentList);
        } catch (Exception e) {
            LOGGER.error("Error converting Document list to JSON array", e);
            return null;
        }
    }

    // Method to ensure that the specified field is formatted correctly as a string
    private void formatDateField(Document document, String fieldName) {
        Object fieldValue = document.get(fieldName);
        if (fieldValue instanceof List) {
            List<?> dateList = (List<?>) fieldValue;
            if (dateList.size() >= 6) { // Check if the list has at least 6 elements
                String formattedDate = String.format("%04d-%02d-%02d %02d:%02d:%02d",
                        dateList.get(0), dateList.get(1), dateList.get(2),
                        dateList.get(3), dateList.get(4), dateList.get(5));
                document.put(fieldName, formattedDate);
            } else {
                // Handle case where the list doesn't have enough elements
                // For example, throw an exception or log a warning
                LOGGER.warn("Date list doesn't have enough elements for formatting");
            }
        }
    }

}
