package dev.taway.catnip.controller;

import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.dto.request.permission.BlacklistRequest;
import dev.taway.catnip.dto.response.BasicResponse;
import dev.taway.catnip.service.PermissionService;
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

@RestController
@RequestMapping("/api/permission")
public class PermissionController {
    private static final Logger log = LogManager.getLogger(PermissionController.class);
    private final PermissionService permissionService;
    private final CatnipConfig catnipConfig;

    @Autowired
    public PermissionController(PermissionService permissionService, CatnipConfig catnipConfig) {
        this.permissionService = permissionService;
        this.catnipConfig = catnipConfig;
    }

    @Operation(summary = "Adds user to blacklist", description = "Blacklisted user will not be able to request media to be played in mediashare. If expiresInDays is provided then the request acts as a timeout otherwise it is a ban.")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "401", description = "User requesting this does not have the necessary permissions")
    @PostMapping("/blacklist/add")
    public ResponseEntity<BasicResponse> addToBlacklist(@RequestBody BlacklistRequest request) {
        if(!permissionService.canRequest(request, catnipConfig.getPermission().getMusic().getUserAction().getBlacklistUser())) {
            log.info(
                    "Blacklist user request rejected. Insufficient permissions. <{}> Required: {} but has [Streamer: {}, Mod: {}, VIP: {}, Subscriber: {}]",
                    request.getUsername(),
                    catnipConfig.getPermission().getMusic().getUserAction().getBlacklistUser(),
                    request.isStreamer(),
                    request.isMod(),
                    request.isVIP(),
                    request.isSubscriber()
            );
            return ResponseEntity.status(401).body(
                    new BasicResponse(true, "You do not have the necessary permissions to perform this action.")
            );
        }

        String message = permissionService.blacklistUser(request.getBlacklistUsername(), request.getUsername(), request.getExpiresInDays());

        return ResponseEntity.ok(new BasicResponse(
                false,
                message
        ));
    }

    @Operation(summary = "Removes user from blacklist", description = "User will be able to request media to be played again.")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "401", description = "User requesting this does not have the necessary permissions")
    @PostMapping("/blacklist/remove")
    public ResponseEntity<BasicResponse> removeFromBlacklist(@RequestBody BlacklistRequest request) {
        if(!permissionService.canRequest(request, catnipConfig.getPermission().getMusic().getUserAction().getBlacklistUser())) {
            log.info(
                    "Unblacklist user request rejected. Insufficient permissions. <{}> Required: {} but has [Streamer: {}, Mod: {}, VIP: {}, Subscriber: {}]",
                    request.getUsername(),
                    catnipConfig.getPermission().getMusic().getUserAction().getBlacklistUser(),
                    request.isStreamer(),
                    request.isMod(),
                    request.isVIP(),
                    request.isSubscriber()
            );
            return ResponseEntity.status(401).body(
                    new BasicResponse(true, "You do not have the necessary permissions to perform this action.")
            );
        }

        String message = permissionService.unblacklistUser(request.getBlacklistUsername());

        return ResponseEntity.ok(new BasicResponse(
                false,
                message
        ));
    }
}
