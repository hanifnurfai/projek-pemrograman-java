package id.kasir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ProductService {
    private final ProductRepository repository;
    private Map<String, Product> products;

    public ProductService(ProductRepository repository) throws IOException {
        this.repository = repository;
        this.products = new LinkedHashMap<>();
        for (Product product : repository.load()) {
            if (products.putIfAbsent(product.code(), product) != null) {
                throw new IOException("Kode produk duplikat di file penyimpanan: " + product.code());
            }
        }
    }

    public synchronized List<Product> getAll() {
        List<Product> result = new ArrayList<>(products.values());
        result.sort(Comparator.comparing(Product::code));
        return List.copyOf(result);
    }

    public synchronized Product findByCode(String code) {
        Product product = products.get(normalizeCode(code));
        if (product == null) {
            throw new IllegalArgumentException("Produk dengan kode tersebut tidak ditemukan.");
        }
        return product;
    }

    public synchronized void addProduct(String code, String name, long price, int stock) throws IOException {
        Product product = new Product(code, name, price, stock);
        if (products.containsKey(product.code())) {
            throw new IllegalArgumentException("Kode produk sudah digunakan.");
        }

        Map<String, Product> updated = new LinkedHashMap<>(products);
        updated.put(product.code(), product);
        saveAndReplace(updated);
    }

    public synchronized void updateProduct(String code, String name, long price) throws IOException {
        Product current = findByCode(code);
        Product updatedProduct = new Product(current.code(), name, price, current.stock());
        Map<String, Product> updated = new LinkedHashMap<>(products);
        updated.put(current.code(), updatedProduct);
        saveAndReplace(updated);
    }

    public synchronized void deleteProduct(String code) throws IOException {
        String normalizedCode = normalizeCode(code);
        if (!products.containsKey(normalizedCode)) {
            throw new IllegalArgumentException("Produk dengan kode tersebut tidak ditemukan.");
        }

        Map<String, Product> updated = new LinkedHashMap<>(products);
        updated.remove(normalizedCode);
        saveAndReplace(updated);
    }

    public synchronized void increaseStock(String code, int quantity) throws IOException {
        changeStock(code, quantity);
    }

    public synchronized void decreaseStock(String code, int quantity) throws IOException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih besar dari 0.");
        }
        Product current = findByCode(code);
        if (quantity > current.stock()) {
            throw new IllegalArgumentException("Stok tidak mencukupi.");
        }
        saveStockChange(current, current.stock() - quantity);
    }

    private void changeStock(String code, int quantity) throws IOException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih besar dari 0.");
        }
        Product current = findByCode(code);
        int newStock;
        try {
            newStock = Math.addExact(current.stock(), quantity);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Jumlah stok melebihi batas maksimum.", exception);
        }
        saveStockChange(current, newStock);
    }

    private void saveStockChange(Product current, int newStock) throws IOException {
        Map<String, Product> updated = new LinkedHashMap<>(products);
        updated.put(current.code(), new Product(current.code(), current.name(), current.price(), newStock));
        saveAndReplace(updated);
    }

    private void saveAndReplace(Map<String, Product> updated) throws IOException {
        repository.saveAll(updated.values());
        products = updated;
    }

    private static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Kode produk wajib diisi.");
        }
        return code.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
