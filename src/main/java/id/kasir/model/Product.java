package id.kasir.model;

import java.util.Locale;

public record Product(String code, String name, long price, int stock) {
    public Product {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Kode produk wajib diisi.");
        }
        code = code.trim().toUpperCase(Locale.ROOT);
        if (code.contains("\n") || code.contains("\r")) {
            throw new IllegalArgumentException("Kode produk tidak boleh berisi baris baru.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama produk wajib diisi.");
        }
        name = name.trim();
        if (name.contains("\n") || name.contains("\r")) {
            throw new IllegalArgumentException("Nama produk tidak boleh berisi baris baru.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Harga harus lebih besar dari 0.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stok tidak boleh negatif.");
        }
    }
}
