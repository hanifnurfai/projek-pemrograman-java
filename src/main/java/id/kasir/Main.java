package id.kasir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public final class Main {
    private final ProductService productService;
    private final Scanner scanner;

    private Main(ProductService productService, Scanner scanner) {
        this.productService = productService;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        Path dataFile = args.length == 0 ? Path.of("data", "products.csv") : Path.of(args[0]);
        try (Scanner scanner = new Scanner(System.in)) {
            ProductService productService = new ProductService(new ProductRepository(dataFile));
            new Main(productService, scanner).run();
        } catch (IOException exception) {
            System.err.println("Gagal memuat atau menyimpan data produk: " + exception.getMessage());
        }
    }

    private void run() throws IOException {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Aplikasi Kasir ===");
            System.out.println("1. Kelola produk");
            System.out.println("2. Kelola stok");
            System.out.println("0. Keluar");
            int choice = readInt("Pilih menu: ");
            switch (choice) {
                case 1 -> productMenu();
                case 2 -> stockMenu();
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
