# JEDA Android

**JEDA (Digital Wellness & Behavioral Interception System)** adalah aplikasi Android prototipe untuk mendukung kesadaran penggunaan perangkat melalui pemantauan perilaku, sampling visual, dan intervensi digital.

Aplikasi dibuat menggunakan **Kotlin** dan **Jetpack Compose**, serta berkomunikasi dengan [JEDA Backend](https://github.com/ShiroTenma/jeda-backend) untuk analisis perilaku dan risiko.

> **Status:** Prototipe akademik dalam pengembangan. Bukan produk siap rilis atau alat diagnosis kesehatan.

## Daftar Isi

- [Gambaran Sistem](#gambaran-sistem)
- [Fitur](#fitur)
- [Teknologi dan Persyaratan](#teknologi-dan-persyaratan)
- [Instalasi dan Build](#instalasi-dan-build)
- [Konfigurasi Backend](#konfigurasi-backend)
- [Izin Android](#izin-android)
- [Struktur Proyek](#struktur-proyek)
- [Pengujian](#pengujian)
- [Batasan dan Privasi](#batasan-dan-privasi)

## Gambaran Sistem

```text
Android Device
   ├── AccessibilityService → aktivitas aplikasi/perilaku
   ├── Behavioral Aggregator → snapshot perilaku
   ├── MediaProjection → sampling frame dengan izin pengguna
   └── UI/Intervention → soft & hard intervention
           |
           v
    Backend (FastAPI)
       ├── FDS
       ├── Visual + RRS
       └── Cognitive message
```

Backend dikembangkan dalam repository terpisah: [ShiroTenma/jeda-backend](https://github.com/ShiroTenma/jeda-backend). Keduanya merupakan bagian dari satu prototipe sistem.

## Fitur

Komponen yang sudah terdapat dalam kode:

- **Dashboard dan navigasi:** Tampilan dashboard, halaman rules, settings, dan informasi monitoring.
- **Monitoring lifecycle:** Controller, manager, foreground notification, dan opsi menghentikan monitoring.
- **Behavioral sensing:** Pengumpulan dan agregasi metrik perilaku menggunakan observation window.
- **Backend connectivity:** Klien Retrofit untuk health check, behavioral analysis, visual analysis, dan cognitive message.
- **Visual sampling:** Komponen MediaProjection dan pengelola sampling visual.
- **Intervention flow:** State, coordinator, UI, dan overlay untuk intervensi soft dan hard.
- **Pengaturan koneksi:** Konfigurasi host, port, dan token backend.
- **Unit tests:** Pengujian untuk konfigurasi, monitoring, behavioral aggregator, intervensi, dan visual sampling manager.

Keberadaan fitur pada source code tidak menjamin seluruh alur sudah teruji pada semua perangkat Android.

## Teknologi dan Persyaratan

| Komponen | Teknologi |
| --- | --- |
| Bahasa | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigasi | Navigation Compose |
| Asynchronous work | Kotlin Coroutines |
| HTTP client | Retrofit, OkHttp, Gson |
| Build system | Gradle (Kotlin DSL) |
| Backend | Python / FastAPI (repository terpisah) |
| Jaringan pengujian | Tailscale (disarankan) |

Konfigurasi Android pada `app/build.gradle.kts`:

- `minSdk = 26` (Android 8.0)
- `compileSdk = 35`
- `targetSdk = 35` (Android 15)
- Java/JVM target 17

Gunakan Android Studio versi yang kompatibel dengan Android Gradle Plugin dan SDK yang ditentukan oleh project. Sinkronkan Gradle sebelum membangun aplikasi.

## Instalasi dan Build

1. Clone repository:

   ```bash
   git clone https://github.com/ShiroTenma/jeda-android.git
   ```

2. Buka folder `jeda-android` di Android Studio.
3. Install Android SDK yang dibutuhkan dan jalankan **Gradle Sync**.
4. Pilih emulator atau perangkat fisik Android yang kompatibel.
5. Jalankan konfigurasi aplikasi melalui Android Studio.

Build APK debug dari terminal:

```bash
./gradlew assembleDebug
```

Pada Windows:

```powershell
.\gradlew.bat assembleDebug
```

Output standar: `app/build/outputs/apk/debug/app-debug.apk`.

## Konfigurasi Backend

Sebelum pengujian integrasi, jalankan server sesuai petunjuk pada [README JEDA Backend](https://github.com/ShiroTenma/jeda-backend#readme).

Aplikasi memiliki konfigurasi default:

| Pengaturan | Nilai default |
| --- | --- |
| Host | `jeda-backend` |
| Port | `8000` |
| Timeout | 3 detik |

Nilai tersebut didefinisikan di `BackendConfig.kt`, sedangkan pengaturan koneksi dikelola oleh komponen `BackendSettings` dan UI settings.

Untuk koneksi melalui Tailscale:

1. Hubungkan Android dan komputer backend ke tailnet yang sama.
2. Pastikan hostname MagicDNS `jeda-backend` mengarah ke komputer backend, atau gunakan IP Tailscale.
3. Atur host, port, serta token di pengaturan koneksi aplikasi.
4. Uji koneksi dengan endpoint health.
5. Pastikan nilai token Android cocok dengan `BACKEND_TOKEN` pada backend saat mengakses endpoint analisis.

Server menggunakan HTTP pada jaringan pengujian; jangan menganggap jaringan publik aman untuk konfigurasi prototipe ini.

## Izin Android

Manifest mendeklarasikan izin dan komponen terkait:

| Izin / komponen | Kegunaan |
| --- | --- |
| Internet dan network state | Komunikasi dengan backend |
| AccessibilityService | Mengamati aktivitas antarmuka sesuai konfigurasi layanan |
| Notification permission | Notifikasi monitoring pada versi Android yang memerlukannya |
| Foreground service | Menjalankan komponen monitoring / sampling terkait |
| MediaProjection foreground service | Sampling layar setelah persetujuan sistem |
| Overlay permission | Menampilkan UI intervensi di atas aplikasi lain |

Aktivasi izin sistem tetap memerlukan tindakan dan persetujuan pengguna. Beberapa komponen dapat memiliki batasan tambahan berdasarkan versi Android dan kebijakan sistem.

## Struktur Proyek

```text
jeda-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── java/com/jeda/android/
│   │       ├── data/remote/        # Backend API dan konfigurasi
│   │       ├── monitoring/
│   │       │   ├── behavior/      # Aggregator dan snapshot
│   │       │   ├── intervention/  # Intervensi dan state
│   │       │   └── visual/        # MediaProjection dan sampling
│   │       ├── ui/
│   │       │   ├── dashboard/
│   │       │   ├── intervention/
│   │       │   ├── rules/
│   │       │   ├── settings/
│   │       │   └── visual/
│   │       ├── JedaApplication.kt
│   │       └── MainActivity.kt
│   ├── src/test/
│   └── build.gradle.kts
├── gradle/
├── gradlew
└── settings.gradle.kts
```

## Pengujian

Jalankan unit test lokal:

```bash
./gradlew testDebugUnitTest
```

Pengujian manual yang disarankan mencakup:

1. Koneksi backend dari perangkat melalui Tailscale.
2. Aktivasi dan penghentian monitoring, termasuk notifikasi.
3. Pemberian serta pencabutan izin aksesibilitas, overlay, dan screen capture.
4. Pengiriman snapshot perilaku dan penanganan kegagalan koneksi.
5. Sampling visual dan pemulihan setelah sesi dihentikan.
6. Perilaku intervensi soft/hard pada kondisi yang relevan.

Pengujian tersebut merupakan rekomendasi verifikasi, bukan klaim bahwa semua skenario telah berhasil dijalankan.

## Batasan dan Privasi

- Proyek masih berupa **prototipe akademik** dan membutuhkan pengujian lebih lanjut.
- Monitoring perilaku dan capture layar melibatkan data sensitif. Gunakan hanya dengan pemahaman dan izin pengguna perangkat.
- Berikan perhatian khusus pada data yang tampil pada frame, proses transmisi, autentikasi, dan penghapusan data.
- Batasi pengujian pada lingkungan dan perangkat yang Anda kendalikan atau telah mendapat izin.
- Skor perilaku, deteksi risiko, dan intervensi bukan diagnosis atau rekomendasi medis.

## Repository Terkait

[JEDA Backend](https://github.com/ShiroTenma/jeda-backend) — FastAPI, FDS, RRS, analisis visual, dan layanan intervensi kognitif.
