package dev.taway.catnip.controller;

import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.dto.request.death.DeathCounterRequest;
import dev.taway.catnip.dto.response.BasicResponse;
import dev.taway.catnip.service.DeathCounterService;
import dev.taway.catnip.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/death")
public class DeathCounterController {

    private final PermissionService permissionService;
    private final DeathCounterService deathCounterService;
    private final CatnipConfig catnipConfig;

    @Autowired
    public DeathCounterController(PermissionService permissionService, DeathCounterService deathCounterService, CatnipConfig catnipConfig) {
        this.permissionService = permissionService;
        this.deathCounterService = deathCounterService;
        this.catnipConfig = catnipConfig;
    }

    @Operation(summary = "Adds 1 to death counter", description = "Adds 1 to death counter and returns the standard response JSON")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "401", description = "User requesting this does not have the necessary permissions to add to counter")
    @PostMapping("/add")
    public ResponseEntity<BasicResponse> addPost(@RequestBody DeathCounterRequest request) {
        if(!permissionService.canRequest(request, catnipConfig.getPermission().getDeathCount().getAdd())) {
            return ResponseEntity.status(401).body(new BasicResponse(true,"You do not have the necessary permissions to perform this action."));
        }

        deathCounterService.add(request);

        return  ResponseEntity.ok(new BasicResponse(
                false,
                String.format("Added 1 death to %s death counter", request.getGameName())
                ));
    }

    @Operation(summary = "Adds 1 to death counter", description = "Adds 1 to death counter and returns only the message.")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "401", description = "User requesting this does not have the necessary permissions to add to counter")
    @PutMapping("/add")
    public ResponseEntity<String> addPut(@RequestBody DeathCounterRequest request) {
        if(!permissionService.canRequest(request, catnipConfig.getPermission().getDeathCount().getAdd())) {
            return ResponseEntity.status(401).body("You do not have the necessary permissions to perform this action.");
        }

        deathCounterService.add(request);

        return  ResponseEntity.ok(String.format("Added 1 death to %s death counter", request.getGameName()));
    }

    @Operation(summary = "Subtracts 1 from death counter", description = "Subtracts 1 from death counter and returns only the message.")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "401", description = "User requesting this does not have the necessary permissions to subtract 1 from counter")
    @PostMapping("/subtract")
    public ResponseEntity<BasicResponse> subtractPost(@RequestBody DeathCounterRequest request) {
        if(!permissionService.canRequest(request, catnipConfig.getPermission().getDeathCount().getAdd())) {
            return ResponseEntity.status(401).body(new BasicResponse(true,"You do not have the necessary permissions to perform this action."));
        }

        deathCounterService.add(request);

        return  ResponseEntity.ok(new BasicResponse(
                false,
                String.format("Subtracted 1 death from %s counter", request.getGameName())
        ));
    }

    @Operation(summary = "Subtracts 1 from death counter", description = "Subtracts 1 from death counter and returns only the message.")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "401", description = "User requesting this does not have the necessary permissions to subtract 1 from counter")
    @PutMapping("/subtract")
    public ResponseEntity<String> subtractPut(@RequestBody DeathCounterRequest request) {
        if(!permissionService.canRequest(request, catnipConfig.getPermission().getDeathCount().getAdd())) {
            return ResponseEntity.status(401).body("You do not have the necessary permissions to perform this action.");
        }

        deathCounterService.subtract(request);

        return  ResponseEntity.ok(String.format("Subtracted 1 death from %s counter", request.getGameName()));
    }
}
