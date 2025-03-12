package dev.taway.catnip.controller;

import dev.taway.catnip.dto.response.BasicResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/song")
public class MusicPlaybackController {
// TODO: this
    @PostMapping("/play")
    public ResponseEntity<BasicResponse> playSong() {
        return ResponseEntity.ok(new BasicResponse());
    }

    @PostMapping("/pause")
    public ResponseEntity<BasicResponse> pauseSong() {
        return ResponseEntity.ok(new BasicResponse());
    }

    @PostMapping("/skip")
    public ResponseEntity<BasicResponse> skipSong() {
        return ResponseEntity.ok(new BasicResponse());
    }
}
