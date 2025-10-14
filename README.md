Aplikasi Manajemen Tagihan
Aplikasi web full-stack untuk mengelola tagihan, pembayaran, dan pelaporan otomatis dengan arsitektur multi-wilayah dan hak akses berbasis peran.


✨ Fitur Utama
1. Manajemen User & Keamanan
-  Hak Akses 2 Peran:
    - ADMIN: Akses penuh ke semua data di semua area.
    - USER: Akses terbatas, hanya bisa melihat dan mengelola data di areanya sendiri.

- Manajemen Akun (Khusus Admin):
  - Membuat akun baru (Register) untuk USER dan menugaskannya ke area tertentu.
  - Halaman Manajemen User untuk mengaktifkan (Enable) atau menonaktifkan (Disable) akun.
  
- Manajemen Kepala Area (Khusus Admin):
  - Halaman khusus untuk menunjuk satu USER di setiap area sebagai Kepala Area.

2. Manajemen Data (CRUD)
- Fitur Create, Read, Update, Delete untuk Vendor, Lokasi, Tagihan, dan Pembayaran.
- Semua tabel data dilengkapi dengan DataTables (pencarian, pengurutan, paginasi).
- Fitur "Note" opsional pada tagihan yang bisa diedit.
- Tombol "Edit" pada tagihan otomatis disembunyikan jika status sudah lunas.

3. Sistem Email Otomatis
- Reminder Mingguan: Mengirim email pengingat otomatis untuk tagihan yang belum dibayar.
- Laporan Bulanan per Area:
  - Mengirim laporan terpisah untuk setiap area.
  - Laporan (PDF & ringkasan email) hanya berisi data dari areanya masing-masing.
  - Email di-CC ke Kepala Area yang telah ditunjuk.

4. Pengaturan Dinamis
- Admin dapat mengubah alamat email tujuan laporan (To/CC).
- Semua pengguna dapat mengubah password mereka sendiri.
- Admin dapat me-reset password pengguna lain.


🛠️ Tumpukan Teknologi (Tech Stack)
- Backend: Java, Spring Boot, Spring Security, Spring Data JPA
- Frontend: Thymeleaf, HTML/CSS, JavaScript, Bootstrap (SB Admin 2)
- Database: MySQL
- Lainnya: iText (PDF), JavaMail Sender, Maven, Git


🚀 Cara Menjalankan & Menguji Aplikasi
Prasyarat
- Java Development Kit (JDK) 17+
- Apache Maven
- Server Database MySQL

Langkah-langkah
1. Clone Repository
Bash
git clone [URL_REPOSITORY_ANDA]

2. Konfigurasi Database:
- Buat sebuah database baru di MySQL dengan nama billingdb.
- Buka file src/main/resources/application.properties.
- Sesuaikan spring.datasource.username dan spring.datasource.password dengan pengaturan MySQL Anda.

3. Jalankan Aplikasi:
- Buka terminal di folder root proyek.
- Jalankan perintah: mvn spring-boot:run
- Aplikasi akan berjalan di http://localhost:8080.

Akun Default untuk Login:
Untuk login pertama kali sebagai Admin, Anda perlu menambahkan data admin ke database. Jalankan perintah SQL berikut di database billingdb Anda:
SQL

-- Password untuk akun ini adalah "admin123"

INSERT INTO users (username, email, password, role, area, is_area_head, enabled) 

VALUES ('admin', 'emailadmin@gmail.com', '$2a$12$3sp8t2cngw3.tKffwDtzRucP7WCXb4m4fBwDOcM6xAYlzdjHTEtKK', 'ADMIN', NULL, 0, 1);

Username: admin
Password: admin123



Skenario Pengujian yang Disarankan:
1. Login sebagai admin.
2. Gunakan menu Register untuk membuat akun USER baru dan tugaskan ke Area tertentu (misal: DKI).
3. Gunakan menu Manajemen User untuk mencoba menonaktifkan dan mengaktifkan kembali akun.
4. Buka Vendor & Lokasi, tambahkan beberapa Lokasi di area DKI dan area lain (misal: JABAR).
5. Logout, lalu login sebagai USER DKI yang baru dibuat.
6. Verifikasi: Pastikan di halaman Dashboard, Data Lokasi, dan Data Tagihan, user tersebut hanya melihat angka dan data yang berhubungan dengan DKI.
7. Coba tambah tagihan baru dan verifikasi bahwa pilihan Lokasi hanya menampilkan lokasi DKI.
8. Login kembali sebagai admin, buka menu Kepala Area dan tunjuk user DKI sebagai kepala area. Simpan.
9. Buka http://localhost:8080/manual-trigger/send-report untuk memicu laporan bulanan dan /manual-trigger/send-reminders untuk memicu mingguan.
10. Verifikasi di log konsol bahwa email untuk DKI dikirim dengan CC ke email user DKI.

