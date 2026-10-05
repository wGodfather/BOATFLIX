# BOATFLIX 1.36 — Windows ve Mobil

1.33 sürümündeki bölüm seçme, oynatma ve indirme düzeltmeleri korunarak kaynak listeleme ve eklenti bekleme düzeltmeleri entegre edildi.

- Windows'ta bilgisi yeterli olan torrent kaynakları, torrent parçalarını indirme ve video analizini beklemeden listelenir.
- Eksik bilgili veya yavaş bir kaynak diğer hazır sonuçların görünmesini engellemez.
- Eklentilerin `setTimeout`, `clearTimeout`, `AbortController` ve `AbortSignal.timeout` desteği tamamlandı; masaüstünde iptal edilen HTTP isteği gerçekten durdurulur.
- BOATFLIX torrent sunucusu, Nuvio'dan ve eski sunucuların kilitli veritabanlarından ayrılır.
- Sezon paketlerinde istenen bölüm seçilir; eski dosya indeksi başka bir bölümü oynatamaz veya indiremez.
- Eksik bölümde açıklayıcı hata, bölüm değiştiğinde akışın yeniden çözülmesi ve yanlış yarım indirmenin yeniden başlaması korunur.
- Kaynakların çözünürlük, boyut, seeder ve içerik eşleştirme kuralları korunur. Sağlayıcı bilgisiyle listelenen torrentler ölçülmüş/doğrulanmış video olarak işaretlenmez.
- Uygulama kimliği, Windows yükseltme kimliği, Android imzası ve kullanıcı verilerinin konumu korunur. Sürüm 1.36, derleme kodu 136.

## İndir

| Cihaz | Dosya |
|---|---|
| Windows x64 | `BOATFLIX-Windows-x64-1.36.msi` |
| Android telefon / tablet | `BOATFLIX-Android-universal-1.36.apk` |

Daha küçük Android paketleri: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`.
Mimariden emin değilseniz evrensel APK'yı kullanın. Android 7.0 veya üstü gerekir.
Bu yayının hedefleri Windows ve Android telefon/tablettir. Linux ve TV için önceki 1.33 yayını kullanılabilir.
Önceden yanlış tamamlanmış bölüm dosyalarını güncellemeden sonra yeniden indirin.

`QA_REPORT.md` test ve 1.33 → 1.36 yükseltme sonuçlarını; `RELEASE_MANIFEST.json` kaynak commitini, paket ve imza bilgilerini; `SHA256SUMS.txt` dosya sağlama değerlerini içerir.
Windows bölüm paketi akış/indirme, kaynak listesi, eklenti süre sınırı ve sunucu yalıtımı testleri; Android host, telefon ve tablet emülatör testleri geçtikten sonra paketler aynı `main` commitinden yayımlanır.
Hesap senkronizasyonu için hizmet ayarları eklenmemiştir; paketler yerel/misafir kullanım için hazırlanır.
NuvioDesktop tabanlıdır; GPL-3.0 lisansı ve kaynak proje atıfları korunur.
