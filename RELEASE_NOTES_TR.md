# BOATFLIX 1.33

Torrent sezon paketlerinde doğru bölümün oynatılması ve indirilmesi düzeltildi.

- Aynı torrent farklı bölümlerde kullanıldığında istenen sezon/bölümün gerçek dosyası seçilir.
- Kaynağın eski veya yanlış dosya indeksi, eşleşen bölüm dosyasını geçersiz kılamaz.
- Bölüm bulunamazsa başka bölümün en büyük videosu açılmaz; açıklayıcı hata gösterilir.
- Bölüm değiştiğinde oynatma akışı yeniden çözülür.
- Yanlış dosyaya ait eski yarım torrent indirmeleri güvenli biçimde baştan başlar.
- Önceden yanlış tamamlanan videolar değiştirilmez; güncellemeden sonra yeniden indirilmelidir.
- BOATFLIX uygulama kimliği, imzası, simgesi ve kullanıcı verilerinin konumu korunur.
- Sürüm 1.33, Android derleme kodu 133. Bu güncelleme VPN özelliği eklemez.

## İndirme seçimi

| Cihaz | Dosya |
|---|---|
| Windows x64 | `BOATFLIX-Windows-x64-1.33.msi` |
| Linux x86_64 | `BOATFLIX-Linux-x86_64-1.33.AppImage`, `.deb`, `.rpm` veya `.flatpak` |
| Android telefon / tablet | `BOATFLIX-Android-universal-1.33.apk` |
| Android TV / Google TV | `BOATFLIX-Android-universal-1.33.apk` |

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
