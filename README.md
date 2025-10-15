# Aplikasi Manajemen Tagihan

Aplikasi web full-stack untuk mengelola tagihan, pembayaran, dan pelaporan otomatis dengan arsitektur multi-wilayah dan hak akses berbasis peran.

---
## ✨ Fitur Utama

### 1. Manajemen User & Keamanan
* **Hak Akses 2 Peran:** `ADMIN` (akses penuh) dan `USER` (terbatas pada areanya).
* **Manajemen Akun (Admin):**
    * Membuat akun `USER` baru dan menugaskannya ke area tertentu.
    * Halaman **Manajemen User** untuk mengaktifkan (`Enable`) atau menonaktifkan (`Disable`) akun.
* **Manajemen Kepala Area (Admin):** Halaman khusus untuk menunjuk `USER` sebagai Kepala Area.

### 2. Manajemen Data (CRUD)
* Fitur `Create`, `Read`, `Update`, `Delete` untuk **Vendor**, **Lokasi**, **Tagihan**, dan **Pembayaran**.
* Input **Nomor Invoice** (unik) dan **Note** (opsional) pada setiap tagihan.
* **Pop-up konfirmasi** sebelum menyimpan tagihan baru untuk mencegah kesalahan input.
* Tombol **"Edit"** pada tagihan otomatis disembunyikan jika status sudah lunas.
* Semua tabel data dilengkapi **DataTables** (pencarian, pengurutan, paginasi).
* File bukti bayar di-rename otomatis dengan format `[No.Invoice]-[Area]`.

### 3. Sistem Email Otomatis
* **Reminder Mingguan:**
    * Mengirim email pengingat untuk tagihan yang belum dibayar.
    * Baris tagihan "Belum Dibayar" diberi **tanda warna merah** untuk kejelasan.
* **Laporan Bulanan per Area:**
    * Mengirim **laporan terpisah** untuk setiap area (DKI, Jabar, dll).
    * Laporan (PDF & ringkasan email) hanya berisi data dari areanya masing-masing.
    * Email di-**CC ke Kepala Area** yang telah ditunjuk.
    * Kolom "Status" diganti menjadi **"Tanggal Bayar"** dan baris "Belum Dibayar" diberi warna.

### 4. Pengaturan Dinamis
* Admin dapat mengubah alamat email tujuan laporan (To/CC).
* Semua pengguna dapat mengubah password mereka sendiri.
* Admin dapat me-reset password pengguna lain.

---
## 🛠️ Tumpukan Teknologi (Tech Stack)

* **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA
* **Frontend:** Thymeleaf, HTML/CSS, JavaScript, Bootstrap (SB Admin 2)
* **Database:** MySQL
* **Lainnya:** iText (PDF), JavaMail Sender, Maven, Git

---
## 🚀 Cara Menjalankan & Menguji Aplikasi

### Prasyarat
* Java Development Kit (JDK) 17+
* Apache Maven
* Server Database MySQL

### Langkah-langkah
1.  **Clone Repository**
    ```bash
    git clone [URL_REPOSITORY_ANDA]
    ```
2.  **Konfigurasi Database**
    * Buat database baru di MySQL dengan nama `billingdb`.
    * Buka file `src/main/resources/application.properties` dan sesuaikan `spring.datasource.username` dan `spring.datasource.password`.
3.  **Jalankan Aplikasi**
    * Buka terminal di folder root proyek dan jalankan:
        ```bash
        mvn spring-boot:run
        ```
    * Aplikasi akan berjalan di `http://localhost:8080`.

### Akun Default untuk Login
* Jalankan perintah SQL berikut di `billingdb` untuk membuat akun Admin:
    ```sql
    -- Password: "admin123"
    INSERT INTO users (username, email, password, role, area, is_area_head, enabled) 
    VALUES ('admin', 'admin@app.com', '$2a$12$3sp8t2cngw3.tKffwDtzRucP7WCXb4m4fBwDOcM6xAYlzdjHTEtKK', 'ADMIN', NULL, 0, 1);
    ```
* **Username:** `admin`
* **Password:** `admin123`

