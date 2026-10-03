# BOATFLIX geliştirme ve yayın

Ana depo: https://github.com/wGodfather/BOATFLIX. Ana dal: `main`.
Sürüm `composeApp/Configuration/DesktopVersion.properties` içinde tanımlanır.
Güncel kaynak sürümü `1.33`, derleme kodu `133` olur. Sonraki sürümlerde sürüm ve kod birlikte artırılır.

## Bağımsız kimlik

- Uygulama: BOATFLIX.
- Masaüstü paket kimliği: `com.wgodfather.boatflix.desktop`.
- Android paket kimliği: `com.wgodfather.boatflix`; geliştirme paketi: `com.wgodfather.boatflix.debug`.
- Windows MSI yükseltme kimliği: `aef97f97-f7b0-5a11-abfd-11fd8c07ad51`. Sonraki BOATFLIX sürümlerinde aynı kalmalıdır.
- Windows ayarları: `%APPDATA%/BOATFLIX`.
- Windows önbelleği: `%LOCALAPPDATA%/BOATFLIX/Cache`.
- Güncelleme deposu: `wGodfather/BOATFLIX`.

Nuvio kimlikleri ve ayar klasörleriyle paylaşım yapılmaz. Nuvio verileri otomatik taşınmaz.

1.33 masaüstünde torrent sunucusunu da ayırır: tercih edilen yerel port `8092` olur;
bu port doluysa boş bir yerel port seçilir. Uygulama yalnızca kendi başlattığı sunucuyu
kullanır ve kapatır. Eşzamanlı başlatma işlemleri sıralanır.
Kaynak doğrulama ret nedenleri ayar klasöründeki `logs/source-verification.log` dosyasına
yazılır. Bu dosya 1 MiB ile sınırlıdır; kaynak URL'leri ve istek başlıkları kaydedilmez.

## Windows derleme

JDK 17, MSVC C++ araçları, WebView2 SDK ve Git LFS gerekir.
`local.properties` dosyasını yerel olarak oluşturun; hesap/hizmet ayarlarını burada tutun.

```powershell
git clone https://github.com/wGodfather/BOATFLIX.git
cd BOATFLIX
git lfs pull
New-Item -ItemType File -Path local.properties -Force
.\gradlew.bat :composeApp:packageMsi "-Pnuvio.webview2.dir=C:/path/to/Microsoft.Web.WebView2"
```

GitHub Actions içindeki **BOATFLIX Windows** işini `main` dalında çalıştırın.
`create_draft=false` test ve paket çıktısı üretir; `create_draft=true` taslak sürüm hazırlar.
Doğrulama sonrasında GitHub Releases üzerinden paket ve SHA-256 dosyası yayımlanır.
Hizmet ayarları isteğe bağlı `NUVIO_DESKTOP_LOCAL_PROPERTIES_BASE64` secret'ından okunabilir.
Bu secret yoksa yerel/misafir kullanım için derlenir. Başka bir deponun secret'ları otomatik taşınmaz.

## Android mobil ve TV

**BOATFLIX Android and TV** iş akışı dört ABI ve evrensel imzalı APK üretir.
İmza için `BOATFLIX_ANDROID_KEYSTORE_BASE64`, `BOATFLIX_ANDROID_STORE_PASSWORD`,
`BOATFLIX_ANDROID_KEY_ALIAS`, `BOATFLIX_ANDROID_KEY_PASSWORD` depo secret'ları kullanılır.
Yeni sürümler aynı imza anahtarıyla hazırlanmalıdır; anahtar depoya eklenmez.
Mobil cihazda arama/torrent/indirme testleri, TV sistem imajında kumanda ve başlatıcı testleri çalışır.
Android TV ve Google TV aynı `LEANBACK_LAUNCHER` etkinliğini kullanır.

## Linux

**BOATFLIX Linux** iş akışı x86_64 AppImage, DEB, RPM ve Flatpak dosyaları üretir.
DEB ve Flatpak kurularak sanal ekranda açılışları kontrol edilir.
AppImage ve sistem paketleri libmpv, GTK3 ve WebKitGTK 4.1 sistem bileşenlerini kullanır;
DEB/RPM paketlerinin bağımlılıkları bu gereksinimleri tanımlar. Flatpak kendi çalışma ortamını kurar.
Linux ayarları `~/.config/boatflix`, önbelleği `~/.cache/boatflix` içindedir.

İlk yayın 1.31 Windows x64'tür. 1.32 Android ve Linux paketlerini ekler.
macOS ve iOS bu yayına dahil değildir.
Özgün projenin teknik paket/kaynak kod adları derleme uyumluluğu için yer yer korunur.
Simge kaynağı ve tekrar üretim aracı `assets/branding/` içindedir.
Kaynak kod GPL-3.0 lisansındadır; `LICENSE` ve `UPSTREAM_README.md` korunur.
