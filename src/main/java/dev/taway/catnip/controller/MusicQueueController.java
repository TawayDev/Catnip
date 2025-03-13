package dev.taway.catnip.controller;

import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.data.music.MusicCacheEntry;
import dev.taway.catnip.dto.request.music.MusicQueueRequest;
import dev.taway.catnip.dto.response.BasicResponse;
import dev.taway.catnip.service.PermissionService;
import dev.taway.catnip.service.music.cache.MusicCacheService;
import dev.taway.catnip.service.music.cache.UrlUtil;
import dev.taway.catnip.service.music.queue.MusicQueueService;
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
    private MusicQueueService musicQueueService;
    private PermissionService permissionService;
    private CatnipConfig catnipConfig;

    @Autowired
    public MusicQueueController(MusicCacheService musicCacheService, MusicQueueService musicQueueService, PermissionService permissionService, CatnipConfig catnipConfig) {
        this.musicCacheService = musicCacheService;
        this.musicQueueService = musicQueueService;
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

//        Sanitize URL. Remove tracking, playlist link etc.
        request.setURL(UrlUtil.sanitizeURL(request.getURL()));

        BasicResponse response = new BasicResponse();
        String url_shortened = UrlUtil.shortenURL(request.getURL());
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
                log.info("[{}] {} - {} was not added to queue. Reason: {}",
                        url_shortened,
                        cacheEntry.getArtist(),
                        cacheEntry.getTitle(),
                        cacheEntry.getBlockReason().getMessage()
                );

                response.setMessage(
                        String.format("%s - %s was not added to queue. Reason: %s",
                                cacheEntry.getArtist(),
                                cacheEntry.getTitle(),
                                cacheEntry.getBlockReason().getMessage()
                        )
                );
            } else {
                if (cacheEntry.getLocalData() == null) {
                    log.error("[{}] Song is not blocked but does not contain any local data!", url_shortened);

                    response.setError(true);
                    response.setMessage("Internal error occurred!");
                } else {
//                    Get time to play the song
                    String playingIn = musicQueueService.queueEmptyInAsString();
//                    Add to queue
                    musicQueueService.addToQueue(cacheEntry);

                    response.setMessage(
                            String.format(
                                    "Added %s - %s to queue! Playing in ~%s",
                                    cacheEntry.getArtist(),
                                    cacheEntry.getTitle(),
                                    playingIn
                            )
                    );
                    log.info("[{}] Added {} - {} to queue. Will be played in ~{}",
                            url_shortened,
                            cacheEntry.getArtist(),
                            cacheEntry.getTitle(),
                            playingIn
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
