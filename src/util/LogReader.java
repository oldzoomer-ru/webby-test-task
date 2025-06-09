package util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LogReader {
    public static List<Path> readLogs(Path logDir) throws Exception {
        if (!Files.exists(logDir)) {
            throw new Exception("Директория логов не существует");
        }

        List<Path> logs = new ArrayList<>();

        try (Stream<Path> files = Files.list(logDir)) {
            for (Path file : files.toList()) {
                if (file.getFileName().endsWith(".log")) {
                    logs.add(file);
                }
            }
        }

        return logs;
    }
}
