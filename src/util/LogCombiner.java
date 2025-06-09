package util;

import java.nio.file.Path;
import java.util.List;

public class LogCombiner {
    public static void combineLogs(Path logPath) throws Exception {
        List<Path> logs = LogReader.readLogs(logPath);

        for (Path log : logs) {
            LogProcessor.processLog(log, logPath.resolve("users"));
        }
    }
}
