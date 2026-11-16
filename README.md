# Aplikasi Kasir Java

Aplikasi kasir sederhana berbasis CLI untuk mengelola produk, stok, dan transaksi penjualan. Data produk dan riwayat transaksi tersimpan secara lokal dalam CSV dan tetap tersedia setelah aplikasi ditutup.

Panduan pembagian tanggung jawab dan kolaborasi tersedia di [AGENTS.MD](./AGENTS.MD).

## Menjalankan aplikasi

Gunakan Java 17 atau lebih baru. Dari direktori proyek, kompilasi dan jalankan:

```powershell
javac -d out (Get-ChildItem -Recurse src\main\java\*.java | ForEach-Object { $_.FullName })
java -cp out id.kasir.ui.Main
```

Secara default, data disimpan di `data\products.csv` dan `data\transactions.csv`. Argumen pertama mengatur file produk, argumen kedua (opsional) mengatur file transaksi:

```powershell
java -cp out id.kasir.ui.Main C:\lokasi\data-produk.csv
java -cp out id.kasir.ui.Main C:\lokasi\data-produk.csv C:\lokasi\transaksi.csv
```

## Fitur

- Melihat, menambah, mengubah, dan menghapus produk.
- Mengelola stok masuk dan keluar dengan validasi jumlah dan ketersediaan.
- Menolak kode produk duplikat; kode dinormalisasi menjadi huruf kapital.
- Menyimpan perubahan CSV secara aman agar data tetap konsisten jika penulisan terganggu.
- Membuat keranjang, memvalidasi pembayaran tunai, mengurangi stok saat checkout berhasil, dan mencetak atau mencetak ulang struk.
- Melihat riwayat transaksi dan total penjualan.
- Harga menggunakan rupiah bilangan bulat; stok awal dapat bernilai nol.

## Struktur kode

Kode sumber berada di `src/main/java/id/kasir` dan dipisahkan berdasarkan tanggung jawab:

- `model`: produk, keranjang, item transaksi, dan transaksi penjualan.
- `repository`: penyimpanan serta pemuatan data CSV.
- `service`: aturan bisnis produk, stok, checkout, dan riwayat transaksi.
- `ui`: menu CLI dan pencetak struk.

## Penggunaan

Menu utama menyediakan submenu **Kelola produk** untuk melihat, menambah, mengubah, dan menghapus produk; **Kelola stok** untuk melihat stok dan mencatat stok masuk atau keluar; serta **Transaksi penjualan** untuk mengelola keranjang dan checkout tunai. Pembayaran yang kurang tidak mengubah stok atau menyimpan transaksi. Menu **Riwayat transaksi** menampilkan ringkasan penjualan dan dapat mencetak ulang struk berdasarkan ID transaksi.

Kode produk tidak membedakan huruf besar/kecil. Harga harus lebih besar dari nol; stok awal boleh nol, sedangkan jumlah penyesuaian stok dan jumlah item transaksi harus bilangan bulat positif.

Data produk disimpan dalam CSV UTF-8 dengan kolom `kode,nama,harga,stok`. Riwayat transaksi menggunakan `id,waktu,kode_produk,nama_produk,harga_satuan,jumlah,subtotal,total,bayar,kembalian`, satu baris per item. File dibuat otomatis saat data pertama kali disimpan. Jangan mengedit file data ketika aplikasi sedang berjalan.

## Integrasi checkout

`ProductService` di paket `id.kasir.service` menyediakan kontrak untuk integrasi transaksi:

- `getAll()` mengembalikan daftar produk.
- `findByCode(code)` mengambil produk beserta harga dan stok saat ini.
- `decreaseStock(code, quantity)` mengurangi stok dan menolak jumlah yang tidak valid atau melebihi stok.

`TransactionService` menggunakan `findByCode` untuk membaca harga dan stok terbaru. Pembayaran dan seluruh ketersediaan stok divalidasi sebelum perubahan apa pun. Jika penulisan riwayat gagal setelah stok berkurang, layanan mencoba mengembalikan stok; bila pemulihan juga gagal, detailnya disertakan sebagai suppressed exception agar tidak tersembunyi. Produk dan transaksi berada di dua file terpisah, sehingga pemulihan lintas-file pada kejadian seperti listrik padam tidak dapat dijamin atomik.
