package de.iltisauge.transport.utils;

import java.util.logging.*;

public class ConsoleLoggingFormatter extends ConsoleHandler {

    public ConsoleLoggingFormatter() {
        setFormatter(new Formatter() {

            @Override
            public String format(LogRecord record) {
                if (record.getThrown() != null) {
                    return "[" + Thread.currentThread().getName() + "] [" + record.getLevel().getName() + "]: " + record.getThrown().getMessage() + " " + record.getMessage() + "\n";
                }
                return "[" + Thread.currentThread().getName() + "] [" + record.getLevel().getName() + "]: " + record.getMessage() + "\n";
            }
        });
    }
}
