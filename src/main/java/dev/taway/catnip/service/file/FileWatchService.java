package dev.taway.catnip.service.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
public class FileWatchService {

    private static final Logger log = LogManager.getLogger(FileWatchService.class);

    public CompletableFuture<Void> waitForFile(String filePath) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        File file = new File(filePath);

//        Check if the file already exists
        if (file.exists()) {
            log.debug("file already exists at path: {}", filePath);
            future.complete(null);
            return future;
        }

        log.debug("Starting file watch service for path: {}", filePath);

//        Start watching the directory in a background thread
        Executors.newSingleThreadExecutor().submit(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path dir = file.getParentFile().toPath();
                dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                log.debug("Watching directory: {} for file creation", dir);

                while (!future.isDone()) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path createdFile = dir.resolve((Path) event.context());
                            log.trace("Detected new file: {}", createdFile);

                            if (createdFile.endsWith(file.getName())) {
                                log.info("Target file detected: {}", createdFile);
                                future.complete(null);
                                return;
                            }
                        }
                    }
                    if (!key.reset()) {
                        log.warn("Watch key invalid, terminating watch service");
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error occurred while watching for file", e);
                future.completeExceptionally(e);
            }
        });

        log.debug("file watch service initialized for path: {}", filePath);
        return future;
    }
}