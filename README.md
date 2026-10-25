# AksiPhoto 📸

Aplikasi Android untuk mengelola & mengedit foto: **Grid Galeri**, **Kamera**, **Terang+**
(peningkatan kecerahan satu-tap), **Bagikan**, dan **Hapus**.

## Teknis
| Item | Nilai |
|---|---|
| Package | `id.aksisoft.aksiphoto` |
| Min SDK | 26 (Android 8.0) |
| Target/Compile SDK | 36 (Android 16) — sesuai kebijakan Play Console 2026 |
| VersionCode | 4 |
| VersionName | 1.1.2 |
| Signing | `keystore/tukangdigital.keystore` alias `tukangdigital` |
| Release build | R8 minify + shrinkResources aktif |

## Bangun release
```bash
./gradlew assembleRelease bundleRelease   # APK + AAB
```

## Struktur
```
app/src/main/java/id/aksisoft/aksiphoto/
  MainActivity.kt      — galeri grid + kamera
  ViewerActivity.kt    — pratinjau, Terang+, bagikan, hapus
  PhotoRepository.kt   — MediaStore loader
  ImageLoader.kt       — thumbnail/full decode + simpan ke galeri
  PhotoAdapter.kt      — RecyclerView grid
store/
  logo_512.png         — logo aplikasi (Play Store)
  banner_1024x500.png  — banner feature graphic Google Play
  screenshots/         — tangkapan halaman aplikasi
```

## Catatan rilis
- Target API 36 wajib mulai 31 Agustus 2026 untuk unggahan baru/update Play Console.
- Minify R8 wajib untuk rilis Play.
