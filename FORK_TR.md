# BOATFLIX geliştirme ve yayın

Ana depo: https://github.com/wGodfather/BOATFLIX. Ana dal: `main`.
Sürüm `composeApp/Configuration/DesktopVersion.properties` içinde tanımlanır.
Güncel sürüm `1.31`, derleme kodu `131` olur. Sonraki sürümlerde sürüm ve kod birlikte artırılır.

## Bağımsız kimlik

- Uygulama: BOATFLIX.
- Masaüstü paket kimliği: `com.wgodfather.boatflix.desktop`.
- Windows MSI yükseltme kimliği: `aef97f97-f7b0-5a11-abfd-11fd8c07ad51`. Sonraki BOATFLIX sürümlerinde aynı kalmalıdır.
- Windows ayarları: `%APPDATA%/BOATFLIX`.
- Windows önbelleği: `%LOCALAPPDATA%/BOATFLIX/Cache`.
- Güncelleme deposu: `wGodfather/BOATFLIX`.

Nuvio kimlikleri ve ayar klasörleriyle paylaşım yapılmaz. Nuvio verileri otomatik taşınmaz.

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

İlk yayımlanan platform Windows x64'tür. Android/macOS/Linux paketleri ayrı doğrulama gerektirir.
Özgün projenin teknik paket/kaynak kod adları derleme uyumluluğu için yer yer korunur.
Simge kaynağı ve tekrar üretim aracı `assets/branding/` içindedir.
Kaynak kod GPL-3.0 lisansındadır; `LICENSE` ve `UPSTREAM_README.md` korunur.
