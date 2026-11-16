package id.kasir.model;

public record CartLine(String productCode, int quantity) {
    public CartLine {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Kode produk wajib diisi.");
        }
        productCode = productCode.trim();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih besar dari 0.");
        }
    }
}
