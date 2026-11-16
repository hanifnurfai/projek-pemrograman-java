package id.kasir.model;

public record TransactionItem(String productCode, String productName, long unitPrice, int quantity) {
    public TransactionItem {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Kode produk wajib diisi.");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Nama produk wajib diisi.");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Harga harus lebih besar dari 0.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih besar dari 0.");
        }
        try {
            Math.multiplyExact(unitPrice, quantity);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Subtotal item melebihi batas maksimum.", exception);
        }
    }

    public long subtotal() {
        return Math.multiplyExact(unitPrice, quantity);
    }
}
