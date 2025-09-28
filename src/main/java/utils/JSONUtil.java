package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class JSONUtil {

    private JSONUtil() {}

    public static JsonNode readJsonFromFile(String filePath) throws IOException {
        String jsonString = Files.readString(Paths.get(filePath));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(jsonString);
    }
}