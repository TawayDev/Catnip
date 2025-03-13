package dev.taway.catnip.service;

import dev.taway.catnip.data.DeathCountEntry;
import dev.taway.catnip.dto.request.death.DeathCounterRequest;
import dev.taway.catnip.util.CacheDataHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Getter
public class DeathCounterService {
    private static final Logger log = LogManager.getLogger(DeathCounterService.class);
    private static final String PATH = "/cache/death-counter.json";

    ArrayList<DeathCountEntry> deathCounter = new ArrayList<>();

    @PostConstruct
    private void init() {
        CacheDataHandler<DeathCountEntry> cacheDataHandler = new CacheDataHandler<>(DeathCountEntry.class);
        deathCounter = cacheDataHandler.load(PATH);
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
        CacheDataHandler<DeathCountEntry> cacheDataHandler = new CacheDataHandler<>(DeathCountEntry.class);
        cacheDataHandler.save(PATH, deathCounter);
    }
}
