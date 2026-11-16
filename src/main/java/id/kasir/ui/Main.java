package id.kasir.ui;

import id.kasir.model.Product;
import id.kasir.model.SaleTransaction;
import id.kasir.model.ShoppingCart;
import id.kasir.model.TransactionItem;
import id.kasir.repository.ProductRepository;
import id.kasir.repository.TransactionRepository;
import id.kasir.service.ProductService;
import id.kasir.service.TransactionService;

import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class Main {
    private final ProductService productService;
    private final TransactionService transactionService;
    private final Scanner scanner;

    private Main(ProductService productService, TransactionService transactionService, Scanner scanner) {
        this.productService = productService;
        this.transactionService = transactionService;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        Path dataFile = args.length == 0 ? Path.of("data", "products.csv") : Path.of(args[0]);
        Path transactionFile = args.length < 2 ? Path.of("data", "transactions.csv") : Path.of(args[1]);
        try (Scanner scanner = new Scanner(System.in)) {
            ProductService productService = new ProductService(new ProductRepository(dataFile));
            TransactionService transactionService =
                    new TransactionService(productService, new TransactionRepository(transactionFile));
            new Main(productService, transactionService, scanner).run();
        } catch (IOException exception) {
            System.err.println("Gagal memuat atau menyimpan data aplikasi: " + exception.getMessage());
        }
    }

    private void run() throws IOException {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Aplikasi Kasir ===");
            System.out.println("1. Kelola produk");
            System.out.println("2. Kelola stok");
            System.out.println("3. Transaksi penjualan");
            System.out.println("4. Riwayat transaksi");
            System.out.println("0. Keluar");
            int choice = readInt("Pilih menu: ");
            switch (choice) {
                case 1 -> productMenu();
                case 2 -> stockMenu();
                case 3 -> transactionMenu();
                case 4 -> showTransactionHistory();
                case 0 -> running = false;
                default -> System.out.println("Pilihan menu tidak tersedia.");
            }
        }
        System.out.println("Aplikasi ditutup.");
    }

    private void productMenu() throws IOException {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n=== Kelola Produk ===");
            System.out.println("1. Lihat produk");
            System.out.println("2. Tambah produk");
            System.out.println("3. Ubah produk");
            System.out.println("4. Hapus produk");
            System.out.println("0. Kembali");
            switch (readInt("Pilih menu: ")) {
                case 1 -> showProducts();
                case 2 -> addProduct();
                case 3 -> updateProduct();
                case 4 -> deleteProduct();
                case 0 -> inMenu = false;
                default -> System.out.println("Pilihan menu tidak tersedia.");
            }
        }
    }

    private void stockMenu() throws IOException {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n=== Kelola Stok ===");
            System.out.println("1. Lihat produk dan stok");
            System.out.println("2. Tambah stok");
            System.out.println("3. Kurangi stok");
            System.out.println("0. Kembali");
            switch (readInt("Pilih menu: ")) {
                case 1 -> showProducts();
                case 2 -> adjustStock(true);
                case 3 -> adjustStock(false);
                case 0 -> inMenu = false;
                default -> System.out.println("Pilihan menu tidak tersedia.");
            }
        }
    }

    private void showProducts() {
        List<Product> products = productService.getAll();
        if (products.isEmpty()) {
            System.out.println("Belum ada produk.");
            return;
        }
        System.out.printf("%-12s %-28s %15s %8s%n", "Kode", "Nama", "Harga (Rp)", "Stok");
        System.out.println("------------------------------------------------------------------");
        for (Product product : products) {
            System.out.printf("%-12s %-28s %15d %8d%n",
                    product.code(), product.name(), product.price(), product.stock());
        }
    }

    private void addProduct() throws IOException {
        String code = readText("Kode: ");
        String name = readText("Nama: ");
        long price = readLong("Harga (Rp): ");
        int stock = readInt("Stok awal: ");
        runOperation(() -> productService.addProduct(code, name, price, stock), "Produk berhasil ditambahkan.");
    }

    private void updateProduct() throws IOException {
        String code = readText("Kode produk yang diubah: ");
        String name = readText("Nama baru: ");
        long price = readLong("Harga baru (Rp): ");
        runOperation(() -> productService.updateProduct(code, name, price), "Produk berhasil diperbarui.");
    }

    private void deleteProduct() throws IOException {
        String code = readText("Kode produk yang dihapus: ");
        runOperation(() -> productService.deleteProduct(code), "Produk berhasil dihapus.");
    }

    private void adjustStock(boolean increase) throws IOException {
        String code = readText("Kode produk: ");
        int quantity = readInt("Jumlah: ");
        if (increase) {
            runOperation(() -> productService.increaseStock(code, quantity), "Stok berhasil ditambahkan.");
        } else {
            runOperation(() -> productService.decreaseStock(code, quantity), "Stok berhasil dikurangi.");
        }
    }

    private void transactionMenu() throws IOException {
        ShoppingCart cart = new ShoppingCart();
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n=== Transaksi Penjualan ===");
            System.out.println("1. Tambah produk ke keranjang");
            System.out.println("2. Lihat keranjang");
            System.out.println("3. Hapus produk dari keranjang");
            System.out.println("4. Bayar dan cetak struk");
            System.out.println("0. Batalkan transaksi");
            switch (readInt("Pilih menu: ")) {
                case 1 -> addToCart(cart);
                case 2 -> showCart(cart);
                case 3 -> removeFromCart(cart);
                case 4 -> {
                    if (checkout(cart)) {
                        inMenu = false;
                    }
                }
                case 0 -> inMenu = false;
                default -> System.out.println("Pilihan menu tidak tersedia.");
            }
        }
    }

    private void addToCart(ShoppingCart cart) {
        try {
            Product product = productService.findByCode(readText("Kode produk: "));
            cart.add(product, readInt("Jumlah: "));
            System.out.println("Produk berhasil ditambahkan ke keranjang.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Gagal: " + exception.getMessage());
        }
    }

    private void showCart(ShoppingCart cart) {
        try {
            List<TransactionItem> items = transactionService.preview(cart);
            long total = 0;
            System.out.printf("%-12s %-28s %8s %15s %15s%n",
                    "Kode", "Nama", "Jumlah", "Harga (Rp)", "Subtotal (Rp)");
            for (TransactionItem item : items) {
                System.out.printf("%-12s %-28s %8d %15d %15d%n",
                        item.productCode(), item.productName(), item.quantity(), item.unitPrice(), item.subtotal());
                total = Math.addExact(total, item.subtotal());
            }
            System.out.println("Total: Rp " + NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(total));
        } catch (IllegalArgumentException exception) {
            System.out.println("Gagal menampilkan keranjang: " + exception.getMessage());
        }
    }

    private void removeFromCart(ShoppingCart cart) {
        try {
            cart.remove(readText("Kode produk yang dihapus: "));
            System.out.println("Produk berhasil dihapus dari keranjang.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Gagal: " + exception.getMessage());
        }
    }

    private boolean checkout(ShoppingCart cart) throws IOException {
        List<TransactionItem> items;
        try {
            items = transactionService.preview(cart);
        } catch (IllegalArgumentException exception) {
            System.out.println("Gagal: " + exception.getMessage());
            return false;
        }
        long total = 0;
        try {
            for (TransactionItem item : items) {
                total = Math.addExact(total, item.subtotal());
            }
        } catch (ArithmeticException exception) {
            System.out.println("Gagal: total transaksi melebihi batas maksimum.");
            return false;
        }

        System.out.println("Total pembayaran: Rp "
                + NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(total));
        long amountPaid = readLong("Uang tunai: Rp ");
        try {
            SaleTransaction transaction = transactionService.checkout(cart, amountPaid);
            System.out.println(new ReceiptPrinter().format(transaction));
            return true;
        } catch (IllegalArgumentException exception) {
            System.out.println("Checkout gagal: " + exception.getMessage());
            return false;
        }
    }

    private void showTransactionHistory() {
        List<SaleTransaction> history = transactionService.getHistory();
        if (history.isEmpty()) {
            System.out.println("Belum ada transaksi.");
            return;
        }
        System.out.println("\n=== Riwayat Transaksi ===");
        System.out.printf("%-38s %-24s %15s%n", "ID", "Waktu (UTC)", "Total (Rp)");
        for (SaleTransaction transaction : history) {
            System.out.printf("%-38s %-24s %15d%n",
                    transaction.id(), transaction.createdAt(), transaction.total());
        }
        System.out.println("Jumlah transaksi: " + history.size());
        System.out.println("Total penjualan: Rp "
                + NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(transactionService.totalSales()));
        String transactionId = readText("Masukkan ID transaksi untuk cetak ulang struk (kosong untuk kembali): ");
        if (!transactionId.isEmpty()) {
            try {
                System.out.println(new ReceiptPrinter().format(transactionService.findById(transactionId)));
            } catch (IllegalArgumentException exception) {
                System.out.println("Gagal: " + exception.getMessage());
            }
        }
    }

    private void runOperation(IoOperation operation, String successMessage) throws IOException {
        try {
            operation.run();
            System.out.println(successMessage);
        } catch (IllegalArgumentException exception) {
            System.out.println("Gagal: " + exception.getMessage());
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Masukkan bilangan bulat yang valid.");
            }
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Masukkan bilangan bulat yang valid.");
            }
        }
    }

    @FunctionalInterface
    private interface IoOperation {
        void run() throws IOException;
    }
}
