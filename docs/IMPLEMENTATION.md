# Status Implementasi AksiPhoto (v1.1.0)

README.md memuat visi penuh AksiPhoto. Halaman ini mencatat apa yang **sudah** diimplement
pada build saat ini (Kotlin + Jetpack Compose, minSdk 26, targetSdk 36).

## ✅ Sudah diimplement
- **SplashScreen** — logo + loading engine, workout onboarding first-run
- **OnboardingScreen** — 3 slide (Edit Pro, Seret & Oles, Offline & Lokal)
- **HomeScreen** — quick action: Galeri, Kamera (MediaStore pending uri), Foto Baru
  (kanvas baru), Folder Saya, Batch Proses + strip foto terbaru
- **GalleryScreen** — grid MediaStore + chip Semua Foto / Folder AksiPhoto,
  klik gambar membuka **ViewerScreen** (bukan langsung editor)
- **ViewerScreen** — viewer layar penuh: pinch zoom (1x–6x) + double-tap 2.5x,
  geser/panning saat zoom, swipe kiri-kanan antar foto, tap = sembunyikan bar,
  semua aksi melalui menu titik-tiga (⋯) kanan atas: Edit, Bagikan, Info,
  Hapus (dengan dialog konfirmasi/notifik sebelum hapus; dialog sistem Android R+)
- **EditorScreen** — Kanvas Compose:
  - Kuas (warna 8 pilihan, ukuran slider)
  - Penghapus (restore piksel asli via BitmapShader)
  - Teks (dialog, ukuran % lebar bitmap, warna)
  - Potong (1:1, 4:5, 16:9, tengah 80%)
  - 12 filter preset (B&W, Sepia, Vintage, Fade, Invert, Hangat, Dingin,
    Cinematic, Sunset, Cyberpunk, dst — ColorMatrix)
  - Penyesuaian live: Cahaya / Kontras / Saturasi + tombol Terapkan
  - Undo / redo stack (4 langkah)
- **ExportScreen** — format JPG/PNG/WebP, kualitas kompresi, resolusi
  (asli/2048/1080/720), simpan ke Pictures/AksiPhoto + Bagikan
- **BatchProcessingScreen** — multi-pilih, resize lebar, kualitas, konversi format,
  progress bar, semua hasil tersimpan Pictures/AksiPhoto; daftar di-refresh
  otomatis setelah batch selesai
- **FileManagerScreen** — browser Pictures/AksiPhoto: edit ulang, bagikan, hapus
  (dialog konfirmasi sistem Android R+)
- **SettingsScreen** — kualitas simpan bawaan, bersihkan cache, catatan privasi
- **AboutScreen** — profil Aksisoft Media, aksisoft.web.id, versi, lisensi
- Ikon adaptive ic_launcher (gradasi ungu→biru, aperture), logo 512, banner 1024×500

## ⏳ Roadmap (belum di build ini)
- Layer system (blend mode, reorder) + proyek `.aksiphoto`
- Selection tools (lasso/magic wand), retouch (clone/heal)
- GPUImage 40+ filter, background remover U²-Net
- Text 100+ font Google, sticker library, template IG/YouTube
- Upscale/de-noise/colorize on-device, plugin JS, Aksisoft Drive (opsional)
- Room version history untuk proyek

## Kebijakan Play 2026
Target API 36 (Android 16) ✓ · Release R8 minify + shrinkResources ✓
