import util.LogCombiner;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        LogCombiner.combineLogs(Path.of("."));
    }
}