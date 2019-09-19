# Uživatelská Dokumentace
## Spuštění
- Minimální verze Android SDK 21 (Android 5.0)

Pro spuštění aplikace je nutné mít alespoň Android 5.0. Aplikace se nainstaluje pomocí Google Play (zatím nedostupné) nebo pomocí instalačního souboru APK. Soubor je nutné přetáhnout do telefonu a nainstalovat. Před instalací je nutné povolit instalace z neznámých zdrojů. Při prvním spuštění je nutné aplikaci udělit oprávnění přístupu k souborům a kameře. 

## Použití
Aplikace se skládá ze 3 hlavních sekcí (skenování, seznam dokumentů a náhled dokumentu).

### Hlavní obrazovka (seznam dokumentů)
Na hlavní obrazovce se nachází seznam všech dokumenů. V seznamu lze vyhledávat textové řetězce pomocí ikonky lupy v pravém horním rohu. Nebo lze zobrazovat jednotlivé skupiny pomocí vybírání tagů ze spodní lišty. Kliknutím na dokument se aplikace přesune na zobrazení náhledu dokumentu.

Každý dokument podporuje základní úpravy: odstranění a exportování do PDF. Dále je k dokumentu možné přiřazovat tagy z již vytvořených tagů nebo je možné přidávat další tagy.

V levém horním rohu se nachází tlačítko kamery, které přenese aplikaci do stavu skenování.

### Skenování
Skenování funguje podobně jako aplikace fotoaparátu. Na obrazovce se zobrazuje náhled z kamery a hlavní tlačítko slouží k pořizování fotek. Dále se pomocí bílého rámečku na obrazovce v reálném čase zobrazuje aktuální detekovaná stránka. V momentě, kdy rámeček ohraničí skenovanou stránku, je nutné pořídit fotku.

Po pořízení fotky se zobrazí náhled s dialogem pro pojmenování dokumentu. Dále je možné ukončit skenování nebo naskenovat další stránky, které budou zařazeny do stejného dokumentu.


## Pokročilé použití
Všechny data aplikace se nacházejí ve složce `Android/data/com.bretahajek.scannerapp/files`. Zde jsou data roztříděná do složek podle dokumentů. Jméno složky, vždy pokud je to možné, co nejvíce odpovídá jménu dokumentu. Ve složce se nacházejí obrázky jednotlivých stránek a pokud byl soubor exportován, tak i exportovaný PDF soubor.
