# BOATFLIX 1.33: torrent sezon paketi düzeltmesi

4 Ekim 2026. Doğru uygulama deposu: https://github.com/wGodfather/BOATFLIX.
Başlangıç kaynağı yayımlanmış BOATFLIX 1.32 (`fd902a2`).
Windows paket/test kaynağı `cab302aa2cb280759e9f47ddc9d8af6869875407`.
Linux paket/test kaynağı `8d0768df79444ceeaf82d2d61db865ca222c08c4`.
Android paket/test kaynağı `a42f1610d2dd3e752c36ab6af73f71afde56063c`.
Windows paketinden sonraki commit'ler yalnız test/CI doğrulamasını iyileştirir;
uygulama kaynak dosyaları bütün platformlarda Windows kaynağıyla aynıdır.
Çalışma alanı `C:/Users/BoB-Opr-01/Documents/cloudstream/BOATFLIX-Episode-Fix`.
Diğer sohbetin BOATFLIX checkout'u değiştirilmedi.

## Kullanıcının bildirdiği sorun

Suits S02E08 ve S02E09 için aynı sezon paketinin listelenmesi normaldir.
Uygulama seçilen bölümün kimliğini dosya seçiminde kullanmadığında aynı
videoyu açabiliyor/indirebiliyordu. Eski eklenti dosya indeksi/adı veya
en büyük video seçimi bölüm bağlamından öncelikliydi. Aynı torrent hash'iyle
bölüm değişirken oynatma akışı da eski bölüme ait kalabiliyordu.

Önceki Nuvio VPN çalışma dalındaki düzeltme, kullanıcının bağımsız BOATFLIX
uygulamasına ulaşmıyordu. Bu rapor ve paketler yalnız BOATFLIX içindir.
VPN kodu, servisi, sürücüsü veya bağımlılığı bu güncellemeye eklenmedi.

## Uygulanan düzeltme

- Windows/Linux ve Android oynatma/indirme istekleri sezon ve bölüm bilgisini
  ortak dosya seçicisine taşır. Gerçek torrent dosyasının orijinal indeksi
  kullanılır; video listesindeki sıra numarası kullanılmaz.
- `S02E08`, `2x08` ve sezon klasörü içindeki numaralı bölüm adları eşleştirilir.
  Eşleşen bölüm eski dosya ipucundan önceliklidir. Örnek/trailer dosyaları
  ayrılır; dizi adındaki "Trailer" sözcüğü yanlışlıkla video elemez.
- Bölüm güvenilir biçimde bulunamazsa başka bölümün en büyük videosu açılmaz.
  Birden fazla bölümü tek dosyada birleştiren kaynaklar, başlangıç/chapter
  bilgisi olmadan otomatik seçilmez. Bir bölümün birden fazla sürümünde açık
  dosya kimliği gerekir. Etiketsiz tek video/açık eklenti kimliği desteği
  korunur; bu kaynakların bölümünün video içeriğinden doğrulandığı söylenmez.
- Windows kaynak denetimi aynı seçiciyi kullanır; başlık/içerik denetimi
  korunur. Bölüm değiştiğinde oynatıcı yeniden çözümleme yapar; önceki
  bölümün geç gelen sonucu yeni akışa uygulanmaz.
- Yarım torrent dosyasına kimlik yan dosyası eklenir. Kimliği farklı/eksik
  eski yarım indirme baştan başlar; aynı dosyanın indirmesi devam eder.
  Tamamlanmış kullanıcı videoları değiştirilmez. Önceden yanlış tamamlanan
  bölüm, düzeltmeyi içeren sürüme geçildikten sonra yeniden indirilmelidir.

BOATFLIX adı, simgesi, paket kimlikleri, ayar/önbellek/indirme klasörleri ve
Windows UpgradeCode korunur. Sürüm 1.33, Android kodu 133; önceki sürüm 1.32/132.
Kullanıcının günlük bilgisayarına uygulama kurulumu veya VPN/ağ testi yapılmadı.

## Doğrulama

Testler Suits videosu indirmez. Depodaki küçük sentetik MP4 ile README ve
E08/E09 adları taşıyan üç dosyalı bir sezon paketi oluşturulur. Her iki
bölüm isteğine de bilerek eski E08 adı/indeksi verilir. Native motorun
ürettiği akış ve iki eşzamanlı indirme beklenen SHA-256 değerleriyle
karşılaştırılır. Dosyaların hash'leri farklıdır. Bu, dosya/akış seçimi
testidir; gerçek dizinin içerik analizi veya decoder testi değildir.

- Yerel Kotlin/JUnit: 9 dosya seçimi + 3 yarım dosya kimliği testi, toplam
  12 test geçti.
- [Windows paket ve native torrent CI](https://github.com/wGodfather/BOATFLIX/actions/runs/37220114229): başarılı. 90 test; 0 hata, 0 başarısızlık, 0 atlanan test.
  Native E08/E09 akış ve eşzamanlı indirme testi geçti. 1.32 → 1.33 MSI
  yükseltmesi aynı UpgradeCode, tek kayıtlı kurulum ve korunmuş veri sentinel'ı
  ile geçti. MSI SHA-256 `953797d12b27cf3d14de8156bdfb176f5d2d66be1034c6a1cff6aa53e866de27`;
  indirilen dosya CI hash'iyle yerelde eşleşti.
- [Linux paket ve native torrent CI](https://github.com/wGodfather/BOATFLIX/actions/runs/37220599096): başarılı. 58 test; 0 hata, 0 başarısızlık, 0 atlanan test.
  Native E08/E09 akış ve eşzamanlı indirme testi geçti. DEB/RPM/AppImage ve
  Flatpak üretildi; kurulum/açılış ve ekran görüntüsü kontrolleri geçti.
  Dört Linux dosyası ve Flatpak'ın indirilen SHA-256 değerleri CI manifestleriyle eşleşti.
- [Android derleme ve mobil CI](https://github.com/wGodfather/BOATFLIX/actions/runs/37222642842):
  78 host testi ve 4 mobil emülatör testi geçti; 0 hata/başarısızlık/atlama.
  Beş APK bağımsız BOATFLIX imzasıyla üretildi; paket adı
  `com.wgodfather.boatflix`, sürüm `1.33`, kod `133` doğrulandı.
  Android 1.32 → 1.33 yükseltmesinde aynı imza sertifikası, UID ve test verisi korundu.
  İndirilen beş APK'nın SHA-256 değerleri CI dosyalarıyla eşleşti.
  Bu birleşik işin genel sonucu başarısızdır: Android TV emülatöründe ilk
  ADB `input keyevent 82` komutu, uygulama testleri başlamadan 255 koduyla durdu.
  Geçen mobil kontroller TV testi olarak sayılmadı; aynı APK ve kaynak commit'i
  ayrı, temiz TV makinelerinde yeniden doğrulandı.
- [Bağımsız Android TV ve Google TV CI](https://github.com/wGodfather/BOATFLIX/actions/runs/37223870421): her iki hedef başarılı.
  Her hedefte 1 kumanda/Kitaplık testi ve 1 native sezon paketi akış/indirme
  testi geçti; toplam 4 TV testi, 0 hata/başarısızlık/atlama.
  Test raporları ayrı korunup zaman damgaları denetlendi; önceki koşunun
  raporları yeniden sayılmadı. İmzalı x86_64 APK her iki emülatörde
  `com.wgodfather.boatflix/com.nuvio.app.BoatflixTvActivity` etkinliğiyle
  LEANBACK kategorisinden açıldı (`Status: ok`); kumanda ve çökme kontrolü geçti.

Windows ve Android yükseltme kontrolleri önceki yayımlanmış 1.32 paketinin
hash'ini denetler. Windows'ta aynı UpgradeCode, tek kurulu uygulama ve
uygulama veri klasöründeki sentinel; Android'de aynı sertifika, paket UID'si
ve uygulama verisindeki sentinel kontrol edilir. Bu testler yalnız geçici
CI makinelerinde/emülatörlerinde çalışır.

Linux DEB/RPM/AppImage/Flatpak paketleri mevcut BOATFLIX iş akışıyla yeniden
üretilir. Mobil ve TV paketleri bağımsız BOATFLIX imzasını kullanır.
Fiziksel telefon veya TV testi için cihaz bulunmadığından böyle bir test
yapıldığı iddia edilmez. Nihai dosyaların SHA-256 değerleri `SHA256SUMS.txt`,
paket başına derleme kaynağı ve CI kimliği `RELEASE_MANIFEST.json` içinde
yayımlanır. Bu rapor `QA_REPORT.md` olarak aynı BOATFLIX 1.33 yayınına eklenir.

Test amaçlı `tools/torrent-pack-peer`, loopback üzerinde MSE taşımasını
üretilmiş fixture'a aktarır. Host rotası, firewall veya VPN kurulumu yapmaz;
uygulama paketlerine dahil edilmez. `anacrolix/torrent` v1.61.0 MSE bileşeni
MPL-2.0 lisanslıdır; lisans metni test aracının yanında tutulur.
