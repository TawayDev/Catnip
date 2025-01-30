package dev.taway.catnip.service;

import dev.taway.catnip.config.PermissionLevel;
import dev.taway.catnip.dto.request.BasicRequest;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    public boolean canRequest(BasicRequest request, PermissionLevel requiredPermission) {
        if (requiredPermission == PermissionLevel.ALL) return true;

        PermissionLevel userMaxLevel = getMaxPermissionLevel(request);
        return userMaxLevel.getLevel() >= requiredPermission.getLevel();
    }

    private PermissionLevel getMaxPermissionLevel(BasicRequest request) {
        if (request.isStreamer()) return PermissionLevel.STREAMER;
        if (request.isMod()) return PermissionLevel.MOD;
        if (request.isVIP()) return PermissionLevel.VIP;
        if (request.isSubscriber()) return PermissionLevel.SUB;
        return PermissionLevel.ALL;
    }
}
