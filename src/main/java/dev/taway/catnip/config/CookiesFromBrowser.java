package dev.taway.catnip.config;

import lombok.Getter;

@Getter
public enum CookiesFromBrowser {
    NONE(""),
    BRAVE("brave"),
    CHROME("chrome"),
    CHROMIUM("chromium"),
    EDGE("edge"),
    FIREFOX("firefox"),
    OPERA("opera"),
    SAFARI("safari"),
    VIVALDI("vivaldi"),
    WHALE("whale");

    private final String name;

    CookiesFromBrowser(String name) {
        this.name = name;
    }
}
