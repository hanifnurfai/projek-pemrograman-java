# Aplikasi Kasir Java

Aplikasi kasir sederhana berbasis CLI untuk mengelola produk dan stok. Data produk tersimpan secara lokal dalam CSV dan tetap tersedia setelah aplikasi ditutup.

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

## Integrasi checkout

`ProductService` menyediakan `findByCode(code)` untuk memeriksa produk dan stok. Alur checkout dapat memanggil `decreaseStock(code, quantity)` hanya setelah pembayaran dinyatakan valid; metode tersebut menolak jumlah yang melebihi stok dan menyimpan perubahan sebelum mengubah stok dalam memori.
