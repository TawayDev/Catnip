package dev.taway.catnip.controller;

import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.dto.request.BasicRequest;
import dev.taway.catnip.dto.response.BasicResponse;
import dev.taway.catnip.service.PermissionService;
import dev.taway.catnip.service.music.queue.MusicQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/song")
public class PlaybackController {
    private final MusicQueueService musicQueueService;
    private final PermissionService permissionService;
    private final CatnipConfig catnipConfig;

    @Autowired
    public PlaybackController(MusicQueueService musicQueueService, PermissionService permissionService, CatnipConfig catnipConfig) {
        this.musicQueueService = musicQueueService;
        this.permissionService = permissionService;
        this.catnipConfig = catnipConfig;
    }

    @PostMapping("/play")
    public ResponseEntity<BasicResponse> playSong(@RequestBody BasicRequest request) {
        ResponseEntity<BasicResponse> r = permissionService.validateUserRequest(request, catnipConfig.getPermission().getMusic().getMusicControls(), true);
        if (r.getStatusCode().is4xxClientError()) {
            return r;
        }

        musicQueueService.play();

        return ResponseEntity.ok(new BasicResponse());
    }

    @PostMapping("/pause")
    public ResponseEntity<BasicResponse> pauseSong(@RequestBody BasicRequest request) {
        ResponseEntity<BasicResponse> r = permissionService.validateUserRequest(request, catnipConfig.getPermission().getMusic().getMusicControls(), true);
        if (r.getStatusCode().is4xxClientError()) {
            return r;
        }

        musicQueueService.pause();

        return ResponseEntity.ok(new BasicResponse());
    }

    @PostMapping("/skip")
    public ResponseEntity<BasicResponse> skipSong(@RequestBody BasicRequest request) {
        ResponseEntity<BasicResponse> r = permissionService.validateUserRequest(request, catnipConfig.getPermission().getMusic().getMusicControls(), true);
        if (r.getStatusCode().is4xxClientError()) {
            return r;
        }

        musicQueueService.skip();

        return ResponseEntity.ok(new BasicResponse());
    }
}
