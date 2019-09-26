# Programátorská dokumentace
Aplikace je napsaná v jazyce Java. Aplikace je rozdělená do hlavních 4 fragmentů (stavů), mezi kterými může uživatel přecházet:

- Hlavní obrazovka
- Náhled dokumentu
- Skenování
- Náhled naskenovaného dokumentu

Pro ukládání dat je využito nativní SQLite databáze s knihovnou [Room](https://developer.android.com/topic/libraries/architecture/room) (ORM knihovna), která poskytuje snadné mapování na Java objekty. Databáze se skládá ze tří tabulek (Document, Tag, DocumentTagJoin). Tabulka dokumentů a tagů poskytuje základí informace o daných ojbektech. Tabulka DocumentTagJoin slouží k vytvoření relace many-to-many, protože dokument může mít více tagů a jeden tag může být přiřazen k více dokumentům.

Dokumenty jsou ukládány do paměti do složky pro externí soubory (to umožňuje snadný přístup k souborům i pomocí prohlížeče souborů). Složka s externími soubory se nachází ve složce `Android/data/com.bretahajek.scannerapp/files`. Aplikace třídí dokumenty vytvořené uživatelem do složek. Jméno složky se vytváří z názvu dokumentu (odstraněním diakritiky a neplatných znaků) a přidáním timestampu vytvoření dokumentu. Pro práci se soubory (přístupy do databáze) aplikace využívá odděleného vlákna, což umožnujě plynulejší vykreslování.

## Hlavní obrazovka
Na hlavní obrazovce se zobrazují dva hlavní seznamy: seznam dokumentů a tagů. Oba seznamy jsou napojené na databázi a při změně databáze dojde k překreslení změněných data. Podle toho zda jsou vybrané nějaké tagy nebo zadán hledaný text, se vytváří query příkaz do dataváze pro vybrání dokumentů. Dokumenty jsou předávány pomocí třídy [LiveData](https://developer.android.com/topic/libraries/architecture/livedata], které zpouští událost změny pokaždé když dojde k načtení nových dat z databáze. Díky tomu čekání na data z databáze neblokuje vykreslování a další operace.

## Náhled dokumentu
Tento fragment pouze dostává složku požadovaného souboru. Z této složky pak načítá jednotlivé stránky (obrázky ve formátu JPG) a zobrazuje uživateli.

## Skenování
Fragment skenování využvá nejnovější knihovny [CameraX](https://developer.android.com/training/camerax). Ta umožňuje snadnější práci s hardwarovou kamerou. Nejnovější snímky jsou zobrazovány jako náhled na obrazovce. Dále jsou snímky předávány do algoritmu pro detekci, který vrací pozice rohů detekované stránky. Z rohů se vytvoří obrys stránky, který je vykreslován na náhledu z kamery. Aby vykreslovaní nebylo příliš trhané (samotná analýza trvá nějakou dobu) je vykreslování prováděno pomocí vlastní třídy, která animuje přechody. Pro plynulí běh aplikace musí běžet jednotlivé operace (náhled, analýza a vykreslování obrysu) v odlišných vláknech.

## Náhled naskenovanáho dokumentu
Náhledu je pouze předán pořízený z kamery. Tento fragment nabízí uživateli možnost pojmenování dokumenta a jeho vytvoření. Uživatel má dále možnost pokračovat ve skenování nebo dokument uzavřít.

## Algoritmus detekce stránky
Pro základní operace zpracování obrazu se využívá knihovny OpenCV. Z jednodušeně řečeno se algoritmus využívá detekce hran a ve výsledném obrázku se snaží nají největší čtyřúhelníkovou conturu.

Celé zpracování začíná zmenšením obrázku na předem určenou velikost (800 px na výšku), což výrazně zrychlí běh algoritmu. Dále se provádí filtrování pomocí bilaterálního filtru, který potlačuje šum a zároveň se znaží zachovat hrany. Na obrázku je následně provedena Cannyho detekce hran následovaná uzavíráním mezer v hranách. Z hran se vyberou uzavřené kontury, mezi kterými se dále hledá nejveší čtyřúhelníková kontura. Nalezené rohy jsou přeškálovány na velikost původního obrázku. Pozice rohů se vrací aplikaci nebo jsou rohy dále využity k provedení transformace perspektivy (vyříznutí stránky z obrázku).

Vhodné proměnné algoritmu byly nastaveny experimentálně.
