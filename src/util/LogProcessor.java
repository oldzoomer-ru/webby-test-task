package util;

import model.Transaction;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.io.BufferedWriter;

public class LogProcessor {
    private static final List<String> operations = Arrays.asList("balance inquiry", "transferred", "withdrew");

    public static void processLog(Path logFile, Path resultFolder) throws Exception {
        Map<String, Transaction> transactionsByUser = new HashMap<>();

        if (!Files.exists(resultFolder)) {
            Files.createDirectory(resultFolder);
        }

        if (!Files.isDirectory(resultFolder) && !Files.isWritable(resultFolder)) {
            throw new IllegalArgumentException("Result folder is not writable");
        }

        if (!Files.isRegularFile(logFile) && !Files.exists(logFile) &&
            !Files.isReadable(logFile)) {
            throw new IllegalArgumentException("Log file is not readable");
        }

        try (BufferedReader reader = Files.newBufferedReader(logFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("] ");
                String operation = parts[1].substring(parts[2].indexOf(" ") + 1);

                if (operations.contains(operation) && operation.contains("transferred")) {
                    Transaction transaction = new Transaction(line);
                    transactionsByUser.put(transaction.getUsername(), transaction);
                }
            }
        }

        for (Map.Entry<String, Transaction> entry : transactionsByUser.entrySet()) {
            String user = entry.getKey();
            Transaction transaction = entry.getValue();
            String targetUser = transaction.getTargetUser();

            // Объединение записей в рамках каждого пользователя
            if (!user.equals(transaction.getUsername())) {
                Transaction reverseTransaction = new Transaction(targetUser, transaction.getTimestamp(), transaction.getAmount(), user);
                transactionsByUser.put(targetUser, reverseTransaction);
            }

            // Сортировка записей по дате лога
            List<Transaction> sortedTransactions = new ArrayList<>(transactionsByUser.values());
            sortedTransactions.sort(null);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFolder + "/" + user + ".log", true))) {
                for (Transaction t : sortedTransactions) {
                    writer.write("[" + t.getTimestamp() + "] " + targetUser + " " + t.getOperation());
                    writer.newLine();
                }
            }

            // Добавление строки с финальным балансом
            double balance = 0;
            for (Transaction t : sortedTransactions) {
                if (t.getOperation().equals("balance inquiry")) {
                    balance += t.getAmount();
                } else if (t.getOperation().equals("transferred") || t.getOperation().equals("received")) {
                    balance -= t.getAmount();
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFolder + "/" + targetUser + ".log", true))) {
                writer.write("[" + LocalDateTime.now() + "] " + targetUser + " final balance " + String.format("%.2f", balance) + " ");
            }
        }
    }
}
