package dev.taway.catnip.service.music.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;

public class ProcessOutputUtil {
    private static final Logger log = LogManager.getLogger(ProcessOutputUtil.class);

    /**
     * Splits process STDOUT and STDERR output into two separate arrays returned as SimpleEntry
     *
     * @param process Process which will have its output handled.
     * @return STD as SimpleEntry key, STDERR as SimpleEntry value.
     */
    public static AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> handleProcessOutput(Process process) {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> errLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
            while ((line = errReader.readLine()) != null) errLines.add(line);
        } catch (IOException e) {
            log.error("An error has occurred while handling process output!", e);
        }

        return new AbstractMap.SimpleEntry<>(lines, errLines);
    }
}
