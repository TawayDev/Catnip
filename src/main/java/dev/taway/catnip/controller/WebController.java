package dev.taway.catnip.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping
public class WebController {
//    This is NOT a good approach to serving static html files but spring boot has forced my hand

    private static final Logger log = LogManager.getLogger(WebController.class);

    @GetMapping("/")
    public ResponseEntity<byte[]> index() throws IOException {
        log.info("Sending index page to client.");
        ClassPathResource resource = new ClassPathResource("static/index.html");
        byte[] content = resource.getInputStream().readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    @GetMapping("/web/progressbar")
    public ResponseEntity<byte[]> progressbar() throws IOException {
        log.info("Sending progressbar page to client.");
        ClassPathResource resource = new ClassPathResource("static/progressbar.html");
        byte[] content = resource.getInputStream().readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}