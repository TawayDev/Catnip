package dev.taway.catnip.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CacheDataHandler<T> {
    private final Class<T> clazz;
    private static final Logger log = LogManager.getLogger(CacheDataHandler.class);

    public CacheDataHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public ArrayList<T> load(String path) {
        ArrayList<T> data = new ArrayList<>();

        File file = new File(System.getProperty("user.dir") + path);

        if (!file.exists()) {
            log.warn("\"{}\" does not exist! No data loaded.", path);
            return data;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JavaType arrayType = objectMapper.getTypeFactory().constructArrayType(clazz);
            T[] entriesList = objectMapper.readValue(new File(System.getProperty("user.dir") + path), arrayType);

            data = new ArrayList<>(Arrays.asList(entriesList));

            log.info("Loaded {} entries from \"{}\"", data.size(), path);
        } catch (IOException e) {
            log.error("Error while reading \"{}\"! {}", path, e.getMessage());
            data = new ArrayList<>();
        }
        return data;
    }

    public void save(String path, ArrayList<T> data) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writeValue(new File(System.getProperty("user.dir") + path), data);
            log.info("Successfully saved {} entries to \"{}\"", data.size(), path);
        } catch (IOException e) {
            log.error("An error occurred while trying to save {} entries to \"{}\"! {}", data.size(), path, e.getMessage());
        }
    }
}
