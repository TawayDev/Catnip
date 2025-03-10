package dev.taway.catnip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.taway.catnip.config.PermissionLevel;
import dev.taway.catnip.data.UserBlacklistEntry;
import dev.taway.catnip.dto.request.BasicRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class PermissionService {
    private static final Logger log = LogManager.getLogger(PermissionService.class);
    private static final String PATH = "/cache/user-blacklist.json";
    ArrayList<UserBlacklistEntry> userBlacklist = new ArrayList<>();

    @PostConstruct
    private void init() {
        File blacklistFile = new File(System.getProperty("user.dir") + PATH);
        if (!blacklistFile.exists()) {
            log.warn("User blacklist json was not loaded as it does not exist!");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            UserBlacklistEntry[] blacklistEntries = objectMapper.readValue(new File(System.getProperty("user.dir") + PATH), UserBlacklistEntry[].class);

            userBlacklist = new ArrayList<>(Arrays.asList(blacklistEntries));

            log.info("Successfully loaded {} user blacklist entries", userBlacklist.size());
        } catch (IOException e) {
            log.error("Error while reading user blacklist JSON! {}", e.getMessage());
            userBlacklist = new ArrayList<>();
        }
    }

    /**
     * @param username Username to check
     * @return True if user is blacklisted
     */
    public boolean isBlacklisted(String username) {
        for (UserBlacklistEntry userBlacklistEntry : userBlacklist) {
            if (userBlacklistEntry.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds user to blacklist if they are not blacklisted already.
     *
     * @param username username to be blacklisted
     * @return Message
     */
    public String blacklistUser(String username, String bannedBy, int expiresInDays) {
        if (isBlacklisted(username)) {
            return String.format("User %s is already blacklisted!", username);
        }

        UserBlacklistEntry userBlacklistEntry = new UserBlacklistEntry(
                username,
                bannedBy,
                Instant.now().toEpochMilli(),
                expiresInDays > 0 ?
//                        Multiply expiresInDays by seconds in a day and convert that to epoch milli
                        Instant.now().plusSeconds((long) expiresInDays * 86_400).toEpochMilli() :
                        Long.MAX_VALUE
        );

        userBlacklist.add(userBlacklistEntry);

        log.info("Successfully blacklisted user \"{}\" [Banned by: {}, Expires in: {}]",
                username,
                bannedBy,
                expiresInDays > 0 ?
                        (expiresInDays + " days") : "never"
        );

        return String.format("Added user %s to blacklist!", username);
    }

    /**
     * Removes user from blacklist if they are blacklisted.
     *
     * @param username Username to be removed from blacklist
     * @return Message
     */
    public String unblacklistUser(String username) {
        UserBlacklistEntry entry = null;

        for (UserBlacklistEntry userBlacklistEntry : userBlacklist) {
            if (userBlacklistEntry.getUsername().equals(username)) {
//                Modifying an arraylist over which we are currently iterating is NOT a good idea. Copy and then later remove.
                entry = userBlacklistEntry;
            }
        }

        if (entry != null) {
            userBlacklist.remove(entry);

            log.info("Successfully removed user \"{}\" from blacklist!",
                    username
            );

            return String.format("Removed user %s from blacklist!", username);
        } else {
            return String.format("User %s is not blacklisted!", username);
        }
    }

    /**
     * Checks if the request has permissions to do a certain action.
     *
     * @param request            UserAction request
     * @param requiredPermission Minimum required permission level for the action
     */
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

    @PreDestroy
    private void destroy() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writeValue(new File(System.getProperty("user.dir") + PATH), userBlacklist);
            log.info("User blacklist saved successfully!");
        } catch (IOException e) {
            log.error("An error occurred while trying to save user blacklist to file! {}", e.getMessage());
        }
    }
}
