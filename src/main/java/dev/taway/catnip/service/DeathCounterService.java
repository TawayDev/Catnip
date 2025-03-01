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
import java.util.Optional;

@Service
@Getter
public class DeathCounterService {
    private static final Logger log = LogManager.getLogger(DeathCounterService.class);
    private static final String PATH = "/cache/death-counter.json";

    ArrayList<DeathCountEntry> deathCounter = new ArrayList<>();

    @PostConstruct
    private void init() {
        File deathCounterFile = new File(System.getProperty("user.dir") + PATH);
        if (!deathCounterFile.exists()) {
            log.warn("Death counter json was not loaded as it does not exist!");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            DeathCountEntry[] deathCountEntries = objectMapper.readValue(new File(System.getProperty("user.dir") + PATH), DeathCountEntry[].class);

            deathCounter = new ArrayList<>(Arrays.asList(deathCountEntries));

            log.info("Successfully loaded {} death counter entries", deathCounter.size());
        } catch (IOException e) {
            log.error("Error while reading death counter JSON! {}", e.getMessage());
            deathCounter = new ArrayList<>();
        }
    }

    private void validateEntryExistence(String gameName) {
        for (DeathCountEntry deathCountEntry : deathCounter) {
            if (deathCountEntry.getGameName().equals(gameName)) return;
        }

        log.debug("Added new death counter for game \"{}\"", gameName);
        deathCounter.add(new DeathCountEntry(gameName, 0));
    }

    /**
     * @param gameName Game name to get its counter value
     * @return Returns counter value OR null if it could not be found.
     */
    public Optional<Integer> getCounterValue(String gameName) {
        Optional<Integer> value = Optional.empty();

        gameName = gameName.toLowerCase();
        for (DeathCountEntry deathCountEntry : deathCounter) {
            if (deathCountEntry.getGameName().toLowerCase().equals(gameName)) {
                value = Optional.of(deathCountEntry.getDeaths());
                break;
            }
        }
        return value;
    }

    /**
     * @param request Request
     * @param change  Change to the death counter. Will add or subtract this amount.
     */
    public int changeEntryValue(DeathCounterRequest request, int change) {
        String gameName = request.getGameName();
        validateEntryExistence(gameName);

        for (DeathCountEntry deathCountEntry : deathCounter) {
            if (deathCountEntry.getGameName().equals(gameName)) {
                deathCountEntry.setDeaths(deathCountEntry.getDeaths() + change);
                log.trace("Death counter: {}, Current: {}, Change: {}", deathCountEntry.getGameName(), deathCountEntry.getDeaths(), change);
                return deathCountEntry.getDeaths();
            }
        }

        throw new RuntimeException(
                String.format("Could not add \"%d\" to \"%s\" counter entry as it does not exist! ", change, gameName)
        );
    }

    @PreDestroy
    private void destroy() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writeValue(new File(System.getProperty("user.dir") + PATH), deathCounter);
            log.info("Death counter saved successfully!");
        } catch (IOException e) {
            log.error("An error occurred while trying to save death counter to file! {}", e.getMessage());
        }
    }
}
