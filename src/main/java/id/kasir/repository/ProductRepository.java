package id.kasir.repository;

import id.kasir.model.Product;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class ProductRepository {
    private static final String HEADER = "kode,nama,harga,stok";

    private final Path file;

    public ProductRepository(Path file) {
        this.file = file;
    }

    public List<Product> load() throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !HEADER.equals(lines.get(0))) {
            throw new IOException("Format file produk tidak valid: " + file);
        }

        List<Product> products = new ArrayList<>();
        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            List<String> fields = parseCsvLine(lines.get(lineNumber));
            if (fields.size() != 4) {
                throw new IOException("Data produk pada baris " + (lineNumber + 1) + " tidak valid.");
            }
            try {
                products.add(new Product(
                        fields.get(0),
                        fields.get(1),
                        Long.parseLong(fields.get(2)),
                        Integer.parseInt(fields.get(3))));
            } catch (IllegalArgumentException exception) {
                throw new IOException("Data produk pada baris " + (lineNumber + 1) + " tidak valid.", exception);
            }
        }
        return products;
    }

    public void saveAll(Iterable<Product> products) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        String prefix = file.getFileName().toString();
        if (prefix.length() < 3) {
            prefix = (prefix + "___").substring(0, 3);
        }
        Path temporaryFile = Files.createTempFile(parent, prefix, ".tmp");
        try {
            List<String> lines = new ArrayList<>();
            lines.add(HEADER);
            for (Product product : products) {
                lines.add(String.join(",",
                        escapeCsv(product.code()),
                        escapeCsv(product.name()),
                        Long.toString(product.price()),
                        Integer.toString(product.stock())));
            }
            Files.write(temporaryFile, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static List<String> parseCsvLine(String line) throws IOException {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;

        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (quoted) {
                if (current == '"') {
                    if (index + 1 < line.length() && line.charAt(index + 1) == '"') {
                        field.append('"');
                        index++;
                    } else {
                        quoted = false;
                        closedQuote = true;
                    }
                } else {
                    field.append(current);
                }
            } else if (closedQuote) {
                if (current != ',') {
                    throw new IOException("Format CSV produk tidak valid.");
                }
                fields.add(field.toString());
                field.setLength(0);
                closedQuote = false;
            } else if (current == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else if (current == '"' && field.isEmpty()) {
                quoted = true;
            } else if (current == '"') {
                throw new IOException("Format CSV produk tidak valid.");
            } else {
                field.append(current);
            }
        }

        if (quoted) {
            throw new IOException("Format CSV produk tidak valid.");
        }
        fields.add(field.toString());
        return fields;
    }
}
