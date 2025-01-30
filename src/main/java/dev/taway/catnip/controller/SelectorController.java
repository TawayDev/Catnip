package dev.taway.catnip.controller;

import dev.taway.catnip.dto.request.selector.SelectorRequest;
import dev.taway.catnip.dto.response.BasicResponse;
import dev.taway.catnip.service.SelectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/selector")
public class SelectorController {

    private final SelectorService selectorService;

    @Autowired
    public SelectorController(SelectorService selectorService) {
        this.selectorService = selectorService;
    }

    @Operation(summary = "Chooses one of n inputted strings", description = "Chooses one of n inputted strings. Reply is JSON with error boolean and message string.")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Options in request are empty")
    @PostMapping("/either")
    public ResponseEntity<BasicResponse> eitherPost(@RequestBody SelectorRequest request) {
        String selection = selectorService.selectOne(request.getOptions());

        if (selection == null) {
            return ResponseEntity.badRequest().body(new BasicResponse(true, "No selection found"));
        }

        return ResponseEntity.ok(new BasicResponse(false, selection));
    }

    @Operation(summary = "Chooses one of n inputted strings", description = "Chooses one of n inputted strings. Reply is string.")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Options in request are empty")
    @ApiResponse()
    @PutMapping("/either")
    public ResponseEntity<String> eitherPut(@RequestBody SelectorRequest request) {
        String selection = selectorService.selectOne(request.getOptions());

        if (selection == null) {
            return ResponseEntity.badRequest().body("No selection found");
        }

        return ResponseEntity.ok(selection);
    }
}
