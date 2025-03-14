package dev.taway.catnip.service.music.queue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MusicQueueLifecycle {
    private final MusicQueueCacheService cacheService;
    private final PlaybackControlService playbackControl;

    @Autowired
    public MusicQueueLifecycle(MusicQueueCacheService cacheService, PlaybackControlService playbackControl) {
        this.cacheService = cacheService;
        this.playbackControl = playbackControl;
    }

    @PostConstruct
    public void init() {
        cacheService.loadCache();
    }

    @PreDestroy
    public void destroy() {
        playbackControl.pause();
        cacheService.saveCache();
    }
}
