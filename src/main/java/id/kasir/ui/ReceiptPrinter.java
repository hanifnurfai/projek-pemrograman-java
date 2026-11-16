package id.kasir.ui;

import id.kasir.model.SaleTransaction;
import id.kasir.model.TransactionItem;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ReceiptPrinter {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    public String format(SaleTransaction transaction) {
        NumberFormat rupiah = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"));
        StringBuilder receipt = new StringBuilder();
        receipt.append("=========== STRUK KASIR ===========\n");
        receipt.append("Transaksi : ").append(transaction.id()).append('\n');
        receipt.append("Waktu     : ").append(DATE_FORMAT.format(transaction.createdAt())).append('\n');
        receipt.append("-----------------------------------\n");
        for (TransactionItem item : transaction.items()) {
            receipt.append(item.productName()).append(" (").append(item.productCode()).append(")\n");
            receipt.append("  ").append(item.quantity()).append(" x Rp ")
                    .append(rupiah.format(item.unitPrice())).append(" = Rp ")
                    .append(rupiah.format(item.subtotal())).append('\n');
        }
        receipt.append("-----------------------------------\n");
        receipt.append("Total     : Rp ").append(rupiah.format(transaction.total())).append('\n');
        receipt.append("Bayar     : Rp ").append(rupiah.format(transaction.amountPaid())).append('\n');
        receipt.append("Kembalian : Rp ").append(rupiah.format(transaction.change())).append('\n');
        receipt.append("===================================");
        return receipt.toString();
    }
}
