package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Transaction implements Comparable<Transaction> {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("[yyyy-MM-dd HH:mm:ss]");

    private final String username;
    private final LocalDateTime timestamp;
    private final String operation;
    private final double amount;
    private final String targetUser; // Для операций перевода

    public Transaction(String logEntry) throws IllegalArgumentException {
        try {
            // Разбираем строку лога формата: "[дата] пользователь операция"
            String[] parts = logEntry.split("] ");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Некорректный формат лога: " + logEntry);
            }

            // Парсим дату
            this.timestamp = LocalDateTime.parse(parts[0].trim(), DATE_FORMAT);

            // Парсим остальные части
            String[] userAndOperation = parts[1].split(" ", 2);
            this.username = userAndOperation[0];
            this.operation = userAndOperation[1];

            // Извлекаем сумму и целевого пользователя (если есть)
            if (operation.startsWith("balance inquiry")) {
                this.amount = Double.parseDouble(operation.split(" ")[2]);
                this.targetUser = null;
            }
            else if (operation.startsWith("transferred")) {
                String[] transferParts = operation.split(" ");
                this.amount = Double.parseDouble(transferParts[1]);
                this.targetUser = transferParts[3];
            }
            else if (operation.startsWith("withdrew")) {
                this.amount = Double.parseDouble(operation.split(" ")[1]);
                this.targetUser = null;
            }
            else if (operation.startsWith("received")) {
                String[] transferParts = operation.split(" ");
                this.amount = Double.parseDouble(transferParts[1]);
                this.targetUser = transferParts[3];
            }
            else {
                throw new IllegalArgumentException("Неизвестная операция: " + operation);
            }

        } catch (DateTimeParseException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка парсинга лога: " + logEntry, e);
        }
    }

    // Конструктор для создания "зеркальных" транзакций (получение средств)
    public Transaction(String targetUser, LocalDateTime timestamp, double amount, String sender) {
        this.username = targetUser;
        this.timestamp = timestamp;
        this.operation = "received " + amount + " from " + sender;
        this.amount = amount;
        this.targetUser = sender;
    }

    // Getters
    public String getUsername() { return username; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getOperation() { return operation; }
    public double getAmount() { return amount; }
    public String getTargetUser() { return targetUser; }

    // Для сортировки по времени
    @Override
    public int compareTo(Transaction other) {
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s",
            DATE_FORMAT.format(timestamp),
            username,
            operation);
    }
}
