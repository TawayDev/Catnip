package dev.taway.catnip.controller;

import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.data.MusicCacheEntry;
import dev.taway.catnip.dto.request.music.MusicQueueRequest;
import dev.taway.catnip.dto.response.BasicResponse;
import dev.taway.catnip.service.PermissionService;
import dev.taway.catnip.service.music.MusicCacheService;
import dev.taway.catnip.service.music.UrlShortenerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/music/queue")
public class MusicQueueController {
    private static final Logger log = LogManager.getLogger(MusicQueueController.class);
    private MusicCacheService musicCacheService;
    private PermissionService permissionService;
    private CatnipConfig catnipConfig;

    @Autowired
    public MusicQueueController(MusicCacheService musicCacheService, PermissionService permissionService, CatnipConfig catnipConfig) {
        this.musicCacheService = musicCacheService;
        this.permissionService = permissionService;
        this.catnipConfig = catnipConfig;
    }

    @Operation(summary = "Adds song to queue", description = "Adds song to queue to be played. If it does not exist in cache it will be downloaded")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "401", description = "User requesting this does not have the necessary permissions")
    @ApiResponse(responseCode = "500", description = "An error has occurred somewhere during song download. CHECK LOGS!")
    @PostMapping("/add")
    public ResponseEntity<BasicResponse> addToQueue(@RequestBody MusicQueueRequest request) {
//        Check if user can request
        ResponseEntity<BasicResponse> r = permissionService.validateUserRequest(request, catnipConfig.getPermission().getMusic().getRequest(), true);
        if (r.getStatusCode().is4xxClientError()) {
            return r;
        }

//        TODO: check if link is playlist. remove playlist part from it.
//        TODO: remove ?si= from URL!!!!

        BasicResponse response = new BasicResponse();
        String url_shortened = UrlShortenerUtil.shortenURL(request.getURL());
        log.trace(request.toString());
//        Find in cache
        Optional<MusicCacheEntry> entry = musicCacheService.getMusicCacheEntry(url_shortened);
//        If not found in cache then cache it
        if (entry.isEmpty()) {
            log.info("[{}] Song not found in cache. Downloading!", url_shortened);
            entry = musicCacheService.cacheSong(request.getURL());
        }

//        Here the song should be downloaded
        if (entry.isPresent()) {
            MusicCacheEntry cacheEntry = entry.get();
            if (cacheEntry.isBlocked()) {
                log.info("[{}] Song is blocked and will not be played!", url_shortened);

                response.setMessage("Song exceeded allowed play time and will not be added to queue!");
            } else {
                if (cacheEntry.getLocalData() == null) {
                    log.error("[{}] Song is not blocked but does not contain any local data!", url_shortened);

                    response.setError(true);
                    response.setMessage("Internal error occurred!");
                } else {
//                    TODO: add to queue
                    double playedInXMinutes = 0; // TODO: Calculate "played in x minutes" time
                    response.setMessage(
                            String.format(
                                    "Added %s - %s to queue! Playing in ~%s minutes.",
                                    cacheEntry.getArtist(),
                                    cacheEntry.getTitle(),
                                    playedInXMinutes
                            )
                    );
                    log.info("[{}] Added {} - {} to queue. Will be played in ~{} minutes.",
                            url_shortened,
                            cacheEntry.getArtist(),
                            cacheEntry.getTitle(),
                            playedInXMinutes
                    );
                }
            }
        } else {
            response.setError(true);
            response.setMessage("Internal error occurred!");
        }

        if (response.isError()) {
            musicCacheService.cleanupCache();
            return ResponseEntity.status(500).body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<BasicResponse> removeFromQueue(@RequestBody MusicQueueRequest request) {
//        Check if user can request
        ResponseEntity<BasicResponse> response = permissionService.validateUserRequest(request, catnipConfig.getPermission().getMusic().getRemove(), true);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

//        TODO: remove from queue

        return ResponseEntity.ok(new BasicResponse());
    }

    @PostMapping("/remove/last-self")
    public ResponseEntity<BasicResponse> removeFromQueueLastSongFromUser(@RequestBody MusicQueueRequest request) {
//        Check if user can request
        ResponseEntity<BasicResponse> response = permissionService.validateUserRequest(request, catnipConfig.getPermission().getMusic().getRemoveLastSelf(), true);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

//        TODO: remove last song submitted by user (sort by timestamp)

        return ResponseEntity.ok(new BasicResponse());
    }
}
