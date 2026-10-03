# BOATFLIX 1.32

Windows, Linux, Android mobil, Android TV ve Google TV indirmeleri.

- BOATFLIX adı ve kullanıcının sağladığı siyah-altın simge.
- Nuvio ile yan yana kurulabilen ayrı kurulum kimliği.
- Ayrı profil, ayar, önbellek ve indirme klasörleri.
- Güncellemeler doğrudan wGodfather/BOATFLIX deposundan alınır.
- Sürüm numarası 1.32; sonraki sürümler 1.33, 1.34 şeklinde ilerler.

## İndirme seçimi

| Cihaz | Dosya |
|---|---|
| Windows x64 | `BOATFLIX-Windows-x64-1.32.msi` |
| Linux x86_64 | `BOATFLIX-Linux-x86_64-1.32.AppImage`, `.deb`, `.rpm` veya `.flatpak` |
| Android telefon / tablet | `BOATFLIX-Android-universal-1.32.apk` |
| Android TV / Google TV | `BOATFLIX-Android-universal-1.32.apk` |

Mobil ve TV tek bir imzalı APK kullanır. TV menüsünde BOATFLIX afişiyle görünür ve yatay açılır.
Daha küçük indirme için cihazınıza uygun `arm64-v8a`, `armeabi-v7a`, `x86` veya `x86_64` APK'sını seçebilirsiniz.
Mimariyi bilmiyorsanız evrensel APK'yı kullanın. Android 7.0 ve üstü gerekir.
Android APK kurulumunda cihazınızda bilinmeyen kaynaklardan kuruluma izin verilmelidir.

Linux DEB ve RPM gerekli libmpv/WebKitGTK bağımlılıklarını bildirir; AppImage bu sistem bileşenlerini gerektirir.
Flatpak için Flatpak ve Flathub kurulumu gerekir; GNOME 50 çalışma ortamını kullanır.
Linux ARM, macOS ve iOS paketleri bu yayına dahil değildir.

Nuvio'daki veriler otomatik taşınmaz. Hesap senkronizasyonu için hizmet ayarları eklenmemiştir;
paketler yerel/misafir kullanım için hazırlanır. Fiziksel TV cihazında test yapılmadı;
TV başlatıcısı ve kitaplık sekmelerinde kumanda testleri emülatörde çalıştırılır.
SHA-256 dosyaları paket bütünlüğünü kontrol etmek içindir.
NuvioDesktop tabanlıdır; GPL-3.0 lisansı ve kaynak proje atıfları korunur.
