package repo;

import model.SaleTransaction;
import model.TransactionItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TransactionRepository {
    private static final String HEADER =
            "id,waktu,kode_produk,nama_produk,harga_satuan,jumlah,subtotal,total,bayar,kembalian";

    private final Path file;

    public TransactionRepository(Path file) {
        this.file = file;
    }

    public List<SaleTransaction> load() throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !HEADER.equals(lines.get(0))) {
            throw new IOException("Format file riwayat transaksi tidak valid: " + file);
        }

        Map<String, TransactionBuilder> transactions = new LinkedHashMap<>();
        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            List<String> fields = parseCsvLine(lines.get(lineNumber));
            if (fields.size() != 10) {
                throw invalidLine(lineNumber);
            }
            try {
                String id = fields.get(0);
                Instant createdAt = Instant.parse(fields.get(1));
                TransactionItem item = new TransactionItem(
                        fields.get(2),
                        fields.get(3),
                        Long.parseLong(fields.get(4)),
                        Integer.parseInt(fields.get(5)));
                long subtotal = Long.parseLong(fields.get(6));
                long total = Long.parseLong(fields.get(7));
                long amountPaid = Long.parseLong(fields.get(8));
                long change = Long.parseLong(fields.get(9));
                if (subtotal != item.subtotal()) {
                    throw new IllegalArgumentException("Subtotal tidak sesuai.");
                }

                TransactionBuilder builder = transactions.get(id);
                if (builder == null) {
                    builder = new TransactionBuilder(id, createdAt, total, amountPaid, change);
                    transactions.put(id, builder);
                } else if (!builder.matches(createdAt, total, amountPaid, change)) {
                    throw new IllegalArgumentException("Data transaksi tidak konsisten.");
                }
                builder.items.add(item);
            } catch (IllegalArgumentException exception) {
                throw new IOException("Data transaksi pada baris " + (lineNumber + 1) + " tidak valid.", exception);
            }
        }

        List<SaleTransaction> result = new ArrayList<>();
        for (TransactionBuilder builder : transactions.values()) {
            try {
                result.add(new SaleTransaction(
                        builder.id, builder.createdAt, builder.items, builder.total, builder.amountPaid, builder.change));
            } catch (IllegalArgumentException exception) {
                throw new IOException("Data riwayat transaksi tidak konsisten.", exception);
            }
        }
        return List.copyOf(result);
    }

    public void save(SaleTransaction transaction) throws IOException {
        List<SaleTransaction> transactions = new ArrayList<>(load());
        for (SaleTransaction existing : transactions) {
            if (existing.id().equals(transaction.id())) {
                throw new IOException("ID transaksi sudah tersimpan: " + transaction.id());
            }
        }
        transactions.add(transaction);

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
            for (SaleTransaction sale : transactions) {
                for (TransactionItem item : sale.items()) {
                    lines.add(String.join(",",
                            escapeCsv(sale.id()),
                            escapeCsv(sale.createdAt().toString()),
                            escapeCsv(item.productCode()),
                            escapeCsv(item.productName()),
                            Long.toString(item.unitPrice()),
                            Integer.toString(item.quantity()),
                            Long.toString(item.subtotal()),
                            Long.toString(sale.total()),
                            Long.toString(sale.amountPaid()),
                            Long.toString(sale.change())));
                }
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

    private static IOException invalidLine(int zeroBasedLineNumber) {
        return new IOException("Data transaksi pada baris " + (zeroBasedLineNumber + 1) + " tidak valid.");
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
                    throw new IOException("Format CSV transaksi tidak valid.");
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
                throw new IOException("Format CSV transaksi tidak valid.");
            } else {
                field.append(current);
            }
        }

        if (quoted) {
            throw new IOException("Format CSV transaksi tidak valid.");
        }
        fields.add(field.toString());
        return fields;
    }

    private static final class TransactionBuilder {
        private final String id;
        private final Instant createdAt;
        private final long total;
        private final long amountPaid;
        private final long change;
        private final List<TransactionItem> items = new ArrayList<>();

        private TransactionBuilder(String id, Instant createdAt, long total, long amountPaid, long change) {
            this.id = id;
            this.createdAt = createdAt;
            this.total = total;
            this.amountPaid = amountPaid;
            this.change = change;
        }

        private boolean matches(Instant otherCreatedAt, long otherTotal, long otherAmountPaid, long otherChange) {
            return createdAt.equals(otherCreatedAt)
                    && total == otherTotal
                    && amountPaid == otherAmountPaid
                    && change == otherChange;
        }
    }
}
