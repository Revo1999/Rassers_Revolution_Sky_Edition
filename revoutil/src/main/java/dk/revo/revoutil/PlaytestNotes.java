package dk.revo.revoutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.regex.Pattern;

final class PlaytestNotes {
    static final String LOCATION = "revo-playtest/notes.md";
    private static final Pattern HEADING = Pattern.compile("^## #([0-9]+) \\| (.+)$");

    private final Path file;

    PlaytestNotes(Path worldDirectory) {
        file = worldDirectory.resolve(LOCATION);
    }

    long append(String author, String dimension, String position, String message) throws IOException {
        long number = Math.addExact(read(0).lastNumber(), 1);
        Files.createDirectories(file.getParent());
        String header = "## #" + number + " | " + Instant.now() + " | " + author + " | " + dimension + " | " + position;
        String entry = "\n" + header + "\n\n    " + message + "\n";
        if (Files.notExists(file)) {
            entry = "# Revo playtest notes\n" + entry;
        }
        Files.writeString(file, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return number;
    }

    List<Note> recent(int count) throws IOException {
        return read(count).notes();
    }

    private History read(int count) throws IOException {
        if (Files.notExists(file)) {
            return new History(0, List.of());
        }
        var notes = new ArrayDeque<Note>();
        long lastNumber = 0;
        long number = 0;
        String details = null;
        try (var reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                var matcher = HEADING.matcher(line);
                if (matcher.matches()) {
                    number = Long.parseLong(matcher.group(1));
                    lastNumber = Math.max(lastNumber, number);
                    details = matcher.group(2);
                } else if (count > 0 && details != null && line.startsWith("    ")) {
                    if (notes.size() == count) {
                        notes.removeFirst();
                    }
                    notes.addLast(new Note(number, details, line.substring(4)));
                    details = null;
                }
            }
        }
        return new History(lastNumber, List.copyOf(notes));
    }

    record Note(long number, String details, String message) {}
    private record History(long lastNumber, List<Note> notes) {}
}
