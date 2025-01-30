package dev.taway.catnip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.taway.catnip.data.DeathCountEntry;
import dev.taway.catnip.dto.request.death.DeathCounterRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@Service
@Getter
public class DeathCounterService {
    private static final Logger log = LogManager.getLogger(DeathCounterService.class);

    ArrayList<DeathCountEntry> deathCounter = new ArrayList<>();

    @PostConstruct
    private void init() {
        File deathCounterFile = new File(System.getProperty("user.dir") + "/cache/death-counter.json");
        if (!deathCounterFile.exists()) {
            log.warn("Death counter json was not loaded as it does not exist!");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            DeathCountEntry[] deathCountEntries = objectMapper.readValue(new File(System.getProperty("user.dir") + "/cache/death-counter.json"), DeathCountEntry[].class);

            deathCounter = new ArrayList<>(Arrays.asList(deathCountEntries));

            log.info("Successfully loaded {} death counter entries", deathCounter.size());
        } catch (IOException e) {
            log.error("Error while reading death counter JSON! {}", e.getMessage());
            deathCounter = new ArrayList<>();
        }
    }

    /**
     * Subtracts one to death counter for the game
     * @param request Request from client
     */
    public void add(DeathCounterRequest request) {
        validateEntryExistence(request.getGameName());
        changeEntryValue(request.getGameName(), 1);
    }

    /**
     * Adds one to death counter for the game
     * @param request Request from client
     */
    public void subtract(DeathCounterRequest request) {
        validateEntryExistence(request.getGameName());
        changeEntryValue(request.getGameName(), -1);
    }

    private void validateEntryExistence(String gameName) {
        for (DeathCountEntry deathCountEntry : deathCounter) {
            if (deathCountEntry.getGameName().equals(gameName)) return;
        }

        deathCounter.add(new DeathCountEntry(gameName, 0));
    }

    /**
     * @param gameName Game entry to be modified
     * @param change Change to the death counter. Will add or subtract this amount.
     */
    private void changeEntryValue(String gameName, int change) {
        for (DeathCountEntry deathCountEntry : deathCounter) {
            if (deathCountEntry.getGameName().equals(gameName)) {
                deathCountEntry.setDeaths(deathCountEntry.getDeaths() + change);
                log.debug("Death counter {} now has value {}", deathCountEntry.getGameName(), deathCountEntry.getDeaths());
                return;
            }
        }

        throw new RuntimeException(
                String.format("Could not add %d to death counter entry as it does not exist!", change)
        );
    }

    @PreDestroy
    private void destroy() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writeValue(new File(System.getProperty("user.dir") + "/cache/death-counter.json"), deathCounter);
            log.info("Death counter saved successfully!");
        } catch (IOException e) {
            log.error("An error occurred while trying to save death counter to file! {}", e.getMessage());
        }
    }
}
