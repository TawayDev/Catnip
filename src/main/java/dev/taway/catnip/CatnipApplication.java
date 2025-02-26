package dev.taway.catnip;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CatnipApplication {

    private static final Logger log = LogManager.getLogger(CatnipApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CatnipApplication.class, args);
        System.out.println(
                """
                          ___   __  ____  __ _  __  ____\s
                         / __) / _\\(_  _)(  ( \\(  )(  _ \\
                        ( (__ /    \\ )(  /    / )(  ) __/
                         \\___)\\_/\\_/(__) \\_)__)(__)(__) \s
                               Made by Taway (with love)"""
        );
    }

    @Bean
    public OpenAPI catnipAPI() {
        return new OpenAPI()
                .info(new Info().title("Catnip API")
                        .description("API for your twitch bot!")
                        .version("in-dev"));
    }
}
