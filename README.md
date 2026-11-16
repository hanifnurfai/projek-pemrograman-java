# Aplikasi Kasir Java

Aplikasi kasir sederhana berbasis CLI untuk mengelola produk dan stok. Data produk tersimpan secara lokal dalam CSV dan tetap tersedia setelah aplikasi ditutup.

Panduan pembagian tanggung jawab dan kolaborasi tersedia di [AGENTS.MD](./AGENTS.MD).

## Menjalankan aplikasi

Gunakan Java 17 atau lebih baru. Dari direktori proyek, kompilasi dan jalankan:

```powershell
javac -d out (Get-ChildItem -Recurse src\main\java\*.java | ForEach-Object { $_.FullName })
java -cp out id.kasir.Main
```

Secara default, data disimpan di `data\products.csv`. Untuk memakai lokasi file lain, berikan path sebagai argumen:

```powershell
java -cp out id.kasir.Main C:\lokasi\data-produk.csv
```

## Fitur

- Melihat, menambah, mengubah, dan menghapus produk.
- Mengelola stok masuk dan keluar dengan validasi jumlah dan ketersediaan.
- Menolak kode produk duplikat; kode dinormalisasi menjadi huruf kapital.
- Menyimpan perubahan CSV secara aman agar data tetap konsisten jika penulisan terganggu.
- Harga menggunakan rupiah bilangan bulat; stok awal dapat bernilai nol.

## Penggunaan

Menu utama menyediakan submenu **Kelola produk** untuk melihat, menambah, mengubah, dan menghapus produk, serta submenu **Kelola stok** untuk melihat stok dan mencatat stok masuk atau keluar. Kode produk tidak membedakan huruf besar/kecil. Harga harus lebih besar dari nol; stok awal boleh nol, sedangkan jumlah penyesuaian stok harus bilangan bulat positif dan stok keluar tidak boleh melebihi stok tersedia.

Data disimpan dalam CSV UTF-8 dengan kolom `kode,nama,harga,stok`. File dibuat otomatis saat perubahan produk pertama kali disimpan. Jangan mengedit file data ketika aplikasi sedang berjalan.

## Integrasi checkout

`ProductService` di paket `id.kasir` menyediakan kontrak untuk integrasi transaksi:

- `getAll()` mengembalikan daftar produk.
- `findByCode(code)` mengambil produk beserta harga dan stok saat ini.
- `decreaseStock(code, quantity)` mengurangi stok dan menolak jumlah yang tidak valid atau melebihi stok.

Alur checkout harus memanggil `decreaseStock` hanya setelah pembayaran dinyatakan valid dan transaksi siap disimpan. Perubahan stok ditulis ke file sebelum diterapkan ke memori.
