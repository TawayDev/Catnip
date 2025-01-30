package dev.taway.catnip.config;

import lombok.Getter;

@Getter
public enum PermissionLevel {
    ALL(0, "Anyone"),
    SUB(1, "Subscriber"),
    VIP(2, "VIP"),
    MOD(3, "Moderator"),
    STREAMER(4, "Streamer");

    final int level;
    final String name;

    PermissionLevel(int level, String name) {
        this.level = level;
        this.name = name;
    }
}
