package id.kasir.model;

import java.time.Instant;
import java.util.List;

public record SaleTransaction(
        String id,
        Instant createdAt,
        List<TransactionItem> items,
        long total,
        long amountPaid,
        long change) {
    public SaleTransaction {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID transaksi wajib diisi.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Waktu transaksi wajib diisi.");
        }
        items = List.copyOf(items);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Transaksi harus memiliki barang.");
        }

        long calculatedTotal = 0;
        try {
            for (TransactionItem item : items) {
                calculatedTotal = Math.addExact(calculatedTotal, item.subtotal());
            }
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Total transaksi melebihi batas maksimum.", exception);
        }
        if (total != calculatedTotal) {
            throw new IllegalArgumentException("Total transaksi tidak sesuai dengan barang.");
        }
        if (amountPaid < total || change != amountPaid - total) {
            throw new IllegalArgumentException("Pembayaran atau kembalian transaksi tidak valid.");
        }
    }
}
