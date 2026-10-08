package com.example.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadPathUtils {
    private UploadPathUtils() {
    }

    public static Path resolve(String configuredPath) {
        Path configured = Paths.get(configuredPath == null ? "upload/files" : configuredPath);
        if (configured.isAbsolute()) {
            return configured.normalize();
        }
        Path workDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path configuredCandidate = workDir.resolve(configured).normalize();
        if (Files.exists(configuredCandidate)) {
            return configuredCandidate;
        }
        Path currentProjectCandidate = workDir.resolve("upload").resolve("files").normalize();
        if (Files.exists(currentProjectCandidate)) {
            return currentProjectCandidate;
        }
        Path parentProjectCandidate = workDir.resolve("..").resolve("upload").resolve("files").normalize();
        if (Files.exists(parentProjectCandidate)) {
            return parentProjectCandidate;
        }
        return configuredCandidate;
    }
}
