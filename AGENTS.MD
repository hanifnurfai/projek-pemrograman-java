# Panduan Kolaborasi

## Tujuan proyek

Membuat aplikasi kasir sederhana berbasis CLI menggunakan Java.

Cakupan versi awal:
- Melihat dan mengelola produk serta stok.
- Membuat transaksi dan mengelola keranjang.
- Pembayaran tunai dan perhitungan kembalian.
- Mencetak struk.
- Melihat riwayat transaksi.
- Menyimpan data secara lokal agar tersedia setelah aplikasi ditutup.

Di luar cakupan versi awal: diskon, pembayaran non-tunai, login, dan banyak peran pengguna.

## Pembagian tanggung jawab

### Anggota 1 — Produk, stok, dan menu aplikasi

Bertanggung jawab atas:
- Model produk: kode unik, nama, harga, dan jumlah stok.
- Daftar produk serta fitur tambah, ubah, dan hapus produk.
- Validasi masukan produk dan jumlah stok.
- Penyimpanan dan pemuatan data produk.
- Menu utama CLI dan integrasi menu produk dengan alur transaksi.

Contoh area kerja: kelas produk, katalog/repository produk, layanan stok, dan menu utama.

### Anggota 2 — Transaksi, pembayaran, dan struk

Bertanggung jawab atas:
- Keranjang dan item transaksi.
- Perhitungan subtotal dan total.
- Validasi pembayaran tunai serta perhitungan kembalian.
- Penyimpanan dan pemuatan riwayat transaksi.
- Pembuatan tampilan struk dan ringkasan penjualan.

Contoh area kerja: kelas transaksi dan item keranjang, layanan checkout, repository transaksi, serta pencetak struk.

Anggota 2 menggunakan layanan produk/stok yang disepakati dengan Anggota 1 untuk memeriksa ketersediaan dan mengurangi stok saat checkout berhasil. Jangan mengubah file milik anggota lain tanpa koordinasi.

## Kesepakatan antarmodul

Sebelum implementasi paralel, sepakati bersama:
- Nama paket dan lokasi kelas.
- Nama serta tipe data setiap field dan metode publik.
- Format penyimpanan file untuk produk dan transaksi.
- Format harga: gunakan bilangan bulat rupiah (`long` atau `int` sesuai rentang yang disepakati), bukan `float`.
- Stok dan jumlah barang harus bilangan bulat positif.

Checkout hanya boleh menyimpan transaksi dan mengurangi stok setelah pembayaran valid. Jika pembayaran kurang atau transaksi dibatalkan, stok tidak berubah.

## Alur kerja kolaborasi

1. Baca struktur proyek dan ikuti build tool serta gaya kode yang sudah ada.
2. Sepakati kontrak kelas dan metode antarmodul sebelum mulai membuat implementasi.
3. Kerjakan pada branch terpisah, misalnya `fitur-produk-stok` dan `fitur-transaksi-struk`.
4. Hindari mengedit file yang sama secara bersamaan. Koordinasikan perubahan pada kelas atau antarmuka bersama terlebih dahulu.
5. Anggota 1 menyediakan layanan produk/stok; Anggota 2 mengintegrasikan checkout melalui layanan tersebut.
6. Tinjau perubahan satu sama lain sebelum digabungkan ke branch utama.
7. Jalankan build dan pengujian setelah integrasi; jangan menggabungkan perubahan jika proyek tidak dapat dijalankan.

## Pengujian minimum

- Produk dengan kode duplikat atau harga/stok tidak valid ditolak.
- Transaksi menolak jumlah yang melebihi stok.
- Total transaksi sesuai dengan jumlah dan harga barang.
- Pembayaran kurang tidak mengurangi stok atau menyimpan transaksi.
- Pembayaran cukup menghitung kembalian dengan benar, menyimpan transaksi, dan mengurangi stok.
- Data produk dan riwayat transaksi dapat dimuat kembali setelah aplikasi dimulai ulang.