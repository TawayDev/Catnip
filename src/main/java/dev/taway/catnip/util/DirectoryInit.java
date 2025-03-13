package dev.taway.catnip.util;

import dev.taway.catnip.config.CatnipConfig;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DirectoryInit {
    private static final Logger log = LogManager.getLogger(DirectoryInit.class);
    private CatnipConfig config;

    @Autowired
    private void init(CatnipConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void validateDirectoryStructure() {
        for (String folderPath : config.getCache().getDirectories()) {
            Path path = Paths.get(String.format("%s%s", System.getProperty("user.dir"), folderPath));
            File folder = path.toFile();
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (created) {
                    log.debug("Created directory \"{}\"", folderPath);
                } else {
                    log.debug("Failed to create directory \"{}\"", folderPath);
                }
            } else {
                log.debug("Directory already exists \"{}\"", folderPath);
            }
        }
    }
}
