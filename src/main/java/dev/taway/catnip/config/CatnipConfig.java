package dev.taway.catnip.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "catnip")
public class CatnipConfig {
    private Cache cache;
    private Permission permission;

    @Data
    public static class Cache {
        /**
         * Folders that will be created if they do not exist on application start. Paths are relative to the current working directory.
         */
        private String[] directories;
    }

    @Data
    public static class Permission {
        private Music music;
        private DeathCount deathCount;

        @Data
        public static class Music {
            private UserAction userAction;

            private PermissionLevel request;
            private PermissionLevel removeLastSelf;
            private PermissionLevel voteSkip;
            private PermissionLevel forceSkip;
            private PermissionLevel blacklist;

            @Data
            public static class UserAction {
                private PermissionLevel blacklistUser;
            }
        }

        @Data
        public static class DeathCount {
            private PermissionLevel add;
            private PermissionLevel subtract;
        }
    }
}