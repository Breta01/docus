# Uživatelská Dokumentace
## Spuštění
- Minimální verze Android SDK 21 (Android 5.0)

Pro spuštění aplikace je nutné mít alespoň Android 5.0. Aplikace se nainstaluje pomocí Google Play (zatím nedostupné) nebo pomocí instalačního souboru APK. Soubor je nutné přetáhnout do telefonu a nainstalovat. Před instalací je nutné povolit instalace z neznámých zdrojů. Při prvním spuštění je nutné aplikaci udělit oprávnění přístupu k souborům a kameře. Aplikace by měla automaticky požádat o tato oprávnění.

## Použití
Aplikace se skládá ze 3 hlavních částí (skenování, seznam dokumentů a náhled dokumentu).

### Hlavní obrazovka (seznam dokumentů)
Na hlavní obrazovce se nachází seznam všech dokumentů. V seznamu lze vyhledávat textové řetězce pomocí ikonky lupy v pravém horním rohu. Nebo lze zobrazovat jednotlivé skupiny dokumentů pomocí vybírání tagů ze spodní lišty. Kliknutím na dokument se aplikace přesune na zobrazení náhledu dokumentu.

Každý dokument podporuje několik základních úprav: odstranění, exportování do PDF a přiřazení tagů. Nabídka těchto akcí se zobrazí dlouhým ztistknutím dokumentu v seznamu nebo pomocí ikonky tří teček, která se nachází u každého dokumentu.

Přiřazování tagů funguje jako dialog, ve kterém jsou uvedeny již vytvořené tagy. Ze seznamu je možné vybrat kategorie (tagy), do kterých dokument patří. V dolní části dialogu se nachází textové pole, pomocí kterého lze přidávat nové tagy.

Exportování do PDF funguje velmi jednoduše. Po kliknutí na tlačítko je soubor exportován a objeví se nabídka aplikací, pomocí kterých lze PDF dokument sdílet. Zároveň se dokument uloží v úložišti aplikace (`Android/data/com.bratahajek.scannerapp/files`).

V levém horním rohu se nachází tlačítko kamery, které přenese aplikaci do stavu skenování.

### Skenování
Skenování funguje podobně jako aplikace fotoaparátu. Na obrazovce se zobrazuje náhled z kamery a hlavní tlačítko slouží k pořizování fotek. Dále se pomocí bílého rámečku na obrazovce v reálném čase zobrazuje aktuální detekovaná stránka. V momentě, kdy rámeček ohraničí skenovanou stránku, je nutné pořídit fotku.

Pro úspěšné skenování se doporučuje umístit dokument tak, aby se nacházel na jednolitém pozadí, které je kontrastní s barvou dokumentu. Dokument by měl být ve středu obrazu a zabírat co největší část obrazu. Po namíření fotoaparátu na dokument se doporučuje chvíli počkat, než dojde k automatickému zaostření (zaostřuje se na střed obrazu) a ustálení hraničního rámečku.

Po pořízení fotky se zobrazí náhled s dialogem pro pojmenování dokumentu. Pro uložení dokumentu je nutné dokument pojmenovat. Dále je možné ukončit skenování nebo naskenovat další stránky, které budou zařazeny do stejného dokumentu.

## Pokročilé použití
Všechny data aplikace se nacházejí ve složce `Android/data/com.bretahajek.scannerapp/files`. Zde jsou data roztříděná do složek podle dokumentů. Jméno složky, vždy pokud je to možné, co nejvíce odpovídá jménu dokumentu. Ve složce se nacházejí obrázky jednotlivých stránek. Po exportování dokumentu do PDF se zde objeví i exportovaný soubor.
