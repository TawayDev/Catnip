package dev.taway.catnip.controller;

import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.dto.request.death.DeathCounterRequest;
import dev.taway.catnip.dto.response.DeathCounterResponse;
import dev.taway.catnip.service.DeathCounterService;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/death")
public class DeathCounterController {
    private static final Logger log = LogManager.getLogger(DeathCounterController.class);

    private final PermissionService permissionService;
    private final DeathCounterService deathCounterService;
    private final CatnipConfig catnipConfig;

    @Autowired
    public DeathCounterController(PermissionService permissionService, DeathCounterService deathCounterService, CatnipConfig catnipConfig) {
        this.permissionService = permissionService;
        this.deathCounterService = deathCounterService;
        this.catnipConfig = catnipConfig;
    }

    @Operation(summary = "Adds 1 to death counter", description = "Adds 1 to death counter")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "401", description = "UserAction requesting this does not have the necessary permissions")
    @PostMapping("/add")
    public ResponseEntity<DeathCounterResponse> add(@RequestBody DeathCounterRequest request) {
        if (!permissionService.canRequest(request, catnipConfig.getPermission().getDeathCount().getAdd())) {
            log.info(
                    "Death counter add request rejected. Insufficient permissions. <{}> Required: {} but has [Streamer: {}, Mod: {}, VIP: {}, Subscriber: {}]",
                    request.getUsername(),
                    catnipConfig.getPermission().getDeathCount().getAdd(),
                    request.isStreamer(),
                    request.isMod(),
                    request.isVIP(),
                    request.isSubscriber()
            );
            return ResponseEntity.status(401).body(new DeathCounterResponse(
                    true,
                    "You do not have the necessary permissions to perform this action.",
                    -1
            ));
        }

        int value = deathCounterService.changeEntryValue(request, 1);

        return ResponseEntity.ok(new DeathCounterResponse(
                false,
                String.format("%s counter is now at %d!", request.getGameName(), value),
                value
        ));
    }

    @Operation(summary = "Subtracts 1 from death counter", description = "Subtracts 1 from death counter")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "401", description = "User requesting this does not have the necessary permissions")
    @PostMapping("/subtract")
    public ResponseEntity<DeathCounterResponse> subtract(@RequestBody DeathCounterRequest request) {
        if (!permissionService.canRequest(request, catnipConfig.getPermission().getDeathCount().getSubtract())) {
            log.info(
                    "Death counter subtract request rejected. Insufficient permissions. <{}> Required: {} but has [Streamer: {}, Mod: {}, VIP: {}, Subscriber: {}]",
                    request.getUsername(),
                    catnipConfig.getPermission().getDeathCount().getSubtract(),
                    request.isStreamer(),
                    request.isMod(),
                    request.isVIP(),
                    request.isSubscriber()
            );
            return ResponseEntity.status(401).body(new DeathCounterResponse(
                    true,
                    "You do not have the necessary permissions to perform this action.",
                    -1
            ));
        }

        int value = deathCounterService.changeEntryValue(request, -1);

        return ResponseEntity.ok(new DeathCounterResponse(
                false,
                String.format("%s counter is now at %d!", request.getGameName(), value),
                value
        ));
    }

    @Operation(summary = "Returns counter value", description = "Returns counter value for the specified counter.")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "404", description = "The requested game death counter has not been found on this server.")
    @PostMapping("/info")
    public ResponseEntity<DeathCounterResponse> info(@RequestBody DeathCounterRequest request) {

        Optional<Integer> value = deathCounterService.getCounterValue(request.getGameName());

        return value.map(integer -> ResponseEntity.ok(new DeathCounterResponse(
                false,
                String.format("%s counter is at %d!", request.getGameName(), integer),
                integer
        ))).orElseGet(() -> ResponseEntity.status(404).body(new DeathCounterResponse(
                true,
                String.format("Death counter '%s' could not be found!", request.getGameName()),
                -1
        )));
    }
}
