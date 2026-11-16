package id.kasir.service;

import id.kasir.model.CartLine;
import id.kasir.model.Product;
import id.kasir.model.SaleTransaction;
import id.kasir.model.ShoppingCart;
import id.kasir.model.TransactionItem;
import id.kasir.repository.TransactionRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TransactionService {
    private final ProductService productService;
    private final TransactionRepository repository;
    private final List<SaleTransaction> transactions;

    public TransactionService(ProductService productService, TransactionRepository repository) throws IOException {
        this.productService = productService;
        this.repository = repository;
        this.transactions = new ArrayList<>(repository.load());
    }

    public synchronized List<TransactionItem> preview(ShoppingCart cart) {
        return resolveItems(cart);
    }

    public synchronized SaleTransaction checkout(ShoppingCart cart, long amountPaid) throws IOException {
        List<TransactionItem> items = resolveItems(cart);
        long total = calculateTotal(items);
        if (amountPaid < total) {
            throw new IllegalArgumentException("Pembayaran kurang dari total transaksi.");
        }
        long change = amountPaid - total;
        SaleTransaction transaction = new SaleTransaction(
                UUID.randomUUID().toString(), Instant.now(), items, total, amountPaid, change);

        List<TransactionItem> decreased = new ArrayList<>();
        try {
            for (TransactionItem item : items) {
                productService.decreaseStock(item.productCode(), item.quantity());
                decreased.add(item);
            }
            repository.save(transaction);
        } catch (IOException | RuntimeException exception) {
            restoreStock(decreased, exception);
            throw exception;
        }

        transactions.add(transaction);
        cart.clear();
        return transaction;
    }

    public synchronized List<SaleTransaction> getHistory() {
        return List.copyOf(transactions);
    }

    public synchronized SaleTransaction findById(String id) {
        for (SaleTransaction transaction : transactions) {
            if (transaction.id().equals(id)) {
                return transaction;
            }
        }
        throw new IllegalArgumentException("Transaksi tidak ditemukan.");
    }

    public synchronized long totalSales() {
        long total = 0;
        for (SaleTransaction transaction : transactions) {
            total = Math.addExact(total, transaction.total());
        }
        return total;
    }

    private List<TransactionItem> resolveItems(ShoppingCart cart) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Keranjang masih kosong.");
        }

        List<TransactionItem> items = new ArrayList<>();
        for (CartLine line : cart.lines()) {
            Product product = productService.findByCode(line.productCode());
            if (line.quantity() > product.stock()) {
                throw new IllegalArgumentException("Stok " + product.name() + " tidak mencukupi.");
            }
            items.add(new TransactionItem(product.code(), product.name(), product.price(), line.quantity()));
        }
        calculateTotal(items);
        return List.copyOf(items);
    }

    private static long calculateTotal(List<TransactionItem> items) {
        long total = 0;
        try {
            for (TransactionItem item : items) {
                total = Math.addExact(total, item.subtotal());
            }
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Total transaksi melebihi batas maksimum.", exception);
        }
        return total;
    }

    private void restoreStock(List<TransactionItem> decreased, Exception originalFailure) {
        // Perubahan stok disimpan satu per satu, jadi batalkan perubahan yang sudah berhasil jika checkout gagal.
        for (int index = decreased.size() - 1; index >= 0; index--) {
            TransactionItem item = decreased.get(index);
            try {
                productService.increaseStock(item.productCode(), item.quantity());
            } catch (IOException | RuntimeException rollbackFailure) {
                originalFailure.addSuppressed(rollbackFailure);
            }
        }
    }
}
