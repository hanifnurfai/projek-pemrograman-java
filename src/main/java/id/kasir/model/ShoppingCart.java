package id.kasir.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ShoppingCart {
    private final Map<String, Integer> quantities = new LinkedHashMap<>();

    public void add(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Produk wajib dipilih.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih besar dari 0.");
        }

        String code = product.code().toUpperCase(Locale.ROOT);
        int updatedQuantity;
        try {
            updatedQuantity = Math.addExact(quantities.getOrDefault(code, 0), quantity);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Jumlah barang melebihi batas maksimum.", exception);
        }
        if (updatedQuantity > product.stock()) {
            throw new IllegalArgumentException("Jumlah melebihi stok tersedia.");
        }
        quantities.put(code, updatedQuantity);
    }

    public void remove(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Kode produk wajib diisi.");
        }
        if (quantities.remove(productCode.trim().toUpperCase(Locale.ROOT)) == null) {
            throw new IllegalArgumentException("Produk tidak ada di keranjang.");
        }
    }

    public List<CartLine> lines() {
        List<CartLine> result = new ArrayList<>();
        quantities.forEach((code, quantity) -> result.add(new CartLine(code, quantity)));
        return List.copyOf(result);
    }

    public boolean isEmpty() {
        return quantities.isEmpty();
    }

    public void clear() {
        quantities.clear();
    }
}
