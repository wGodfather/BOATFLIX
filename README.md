# BOATFLIX

![BOATFLIX](composeApp/src/commonMain/composeResources/drawable/app_logo_wordmark_original.png)

BOATFLIX, Windows, Linux, Android mobil, Android TV ve Google TV için bağımsız bir medya uygulamasıdır.
Uygulama, eklediğiniz kaynaklarla film ve dizileri keşfetmenizi, kitaplık oluşturmanızı ve medya oynatmanızı sağlar.

## İndir

[BOATFLIX 1.36 — Windows ve Android telefon/tablet](https://github.com/wGodfather/BOATFLIX/releases/tag/1.36)

1.36, 1.33 bölüm seçme ve indirme düzeltmelerini koruyarak Windows kaynak listesini hızlandırır ve eklenti süre sınırlarını düzeltir.
Önceden yanlış tamamlanmış bölümleri güncellemeden sonra yeniden indirin.

| Cihaz | Dosya |
|---|---|
| Windows x64 | [MSI](https://github.com/wGodfather/BOATFLIX/releases/download/1.36/BOATFLIX-Windows-x64-1.36.msi) |
| Linux x86_64 (1.33) | [AppImage](https://github.com/wGodfather/BOATFLIX/releases/download/1.33/BOATFLIX-Linux-x86_64-1.33.AppImage) · [DEB](https://github.com/wGodfather/BOATFLIX/releases/download/1.33/BOATFLIX-Linux-x86_64-1.33.deb) · [RPM](https://github.com/wGodfather/BOATFLIX/releases/download/1.33/BOATFLIX-Linux-x86_64-1.33.rpm) · [Flatpak](https://github.com/wGodfather/BOATFLIX/releases/download/1.33/BOATFLIX-Linux-x86_64-1.33.flatpak) |
| Android telefon / tablet | [Evrensel APK](https://github.com/wGodfather/BOATFLIX/releases/download/1.36/BOATFLIX-Android-universal-1.36.apk) |
| Android TV / Google TV (1.33) | [Evrensel APK](https://github.com/wGodfather/BOATFLIX/releases/download/1.33/BOATFLIX-Android-universal-1.33.apk) |

1.36 yayını Windows ve Android telefon/tablet içindir. Linux ve TV indirmeleri önceki 1.33 sürümünde kalır. Android paket kimliği ve imzası korunur.
Daha küçük indirmeler için sürüm sayfasında `arm64-v8a`, `armeabi-v7a`, `x86` ve `x86_64` APK'ları da bulunur.
32 bit ARM TV kutuları için `armeabi-v7a`, 64 bit Android cihazlar için `arm64-v8a` seçilebilir;
mimariden emin değilseniz evrensel APK'yı kullanın. Android 7.0 veya üstü gerekir.
Linux paketleri x86_64 içindir; ARM Linux bu sürümde yoktur.

[Sürümler ve güncellemeler](https://github.com/wGodfather/BOATFLIX/releases)

BOATFLIX, Nuvio'dan ayrı kurulur. Kendi ayar, önbellek ve indirme klasörlerini kullanır;
güncellemelerini bu depodan alır. Nuvio'daki mevcut profil ve ayarlar otomatik taşınmaz.
Sürüm numaraları 1.31, 1.32, 1.33 şeklinde ilerler.

## Kaynak ve derleme

[Derleme ve yayın bilgileri](FORK_TR.md). Windows, Linux ve Android için ayrı derleme iş akışları bulunur.
macOS ve iOS kurulum paketleri bu sürüme dahil değildir.

## Kaynak proje ve lisans

BOATFLIX, [NuvioMedia/NuvioDesktop](https://github.com/NuvioMedia/NuvioDesktop)
ve [wGodfather/NuvioDesktop](https://github.com/wGodfather/NuvioDesktop) kaynaklarından türetilmiştir.
Başlangıç kaynak sürümü: `027683a` (BOATFLIX marka değişikliği), `f3e2502` (Türkiye fork tabanı).
Özgün proje yazarlarının telif hakları ve atıfları korunur.
Kaynak kod [GPL-3.0](LICENSE) ile sunulur. Özgün proje belgesi [UPSTREAM_README.md](UPSTREAM_README.md) içindedir.

Sağlanan kullanıcı görseli BOATFLIX simgesi olarak kullanılır. Medya uygulamayla birlikte sağlanmaz;
kullanıcı kaynaklarını kendisi ekler.
