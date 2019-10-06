# Programátorská dokumentace
Aplikace je napsaná v jazyce Java. Aplikace má single activity design. Skládá se tedy pouze z jedné aktivity a dále je rozdělena do 4 hlavních fragmentů, mezi kterými může uživatel přecházet:

- Hlavní obrazovka
- Náhled dokumentu
- Skenování
- Náhled a uložení naskenovaného snímku


## Správa dat
Pro ukládání dat je využito nativní SQLite databáze s knihovnou [Room](https://developer.android.com/topic/libraries/architecture/room) (ORM knihovna), která poskytuje snadné mapování záznamů na Java objekty. Databáze se skládá ze tří tabulek (Document, Tag, DocumentTagJoin). Tabulka dokumentů a tagů poskytuje základní informace o daných objektech. Tabulka DocumentTagJoin slouží k vytvoření relace many-to-many mezi dokumenty a tagy (jeden dokument může mít více tagů a jeden tag může být přiřazen k více dokumentů).

Kód obsluhující databázi je umístěn ve složce `db/` a má standardní strukturu. Ke každé třídě, která zároveň definuje podobu tabulky v databázi, je vytvořená abstraktní třída, která definuje všechny dotazy pro danou tabulku. Databáze je pak poskytována třídou `AppDatabase`.

Dokumenty (naskenované stránky) jsou ukládány do složky pro externí soubory aplikace. To umožňuje uživateli snadný přístup k souborům i bez použití aplikace. Složka s externími soubory se nachází ve složce `Android/data/docus/files`. Aplikace třídí dokumenty vytvořené uživatelem do složek. Jméno složky se vytváří z názvu dokumentu (odstraněním diakritiky a neplatných znaků) a přidáním aktuálního timestampu.

## Hlavní obrazovka
Na hlavní obrazovce se zobrazují dva hlavní seznamy: seznam dokumentů a seznam tagů. Oba seznamy jsou implementovány pomocí komponenty `RecyclerView`. Seznamy jsou napojené na databázi, ze které načítají a zobrazují data. Android zakazuje, aby přístupy do databáze probíhaly na hlavním vlákně. Proto jsou objekty z databáze předávány pomocí třídy [LiveData](https://developer.android.com/topic/libraries/architecture/livedata). Ta vyvolá událost změny pokaždé, když dojde k načtení nových nebo změněných dat z databáze. Výběr dokumentů (nebo tagů), které se mají načíst z databáze zajišťují `ViewModel` třídy umístěné ve stejnojmenné složce.

Obsluhu seznamů zajišťují adaptéry umístěné ve složce `ui/`. Ty zajišťují vykreslování jednotlivých položek. Při změně obsahu seznamu předávají informace o tom, které položky se mají překreslit. 

## Náhled dokumentu
Tento fragment pouze dostane složku požadovaného dokumentu. Z této složky pak načítá jednotlivé stránky (obrázky ve formátu JPG) a zobrazuje je uživateli. Obrázky jsou zobrazovány pomocí komponenty `PageView`.

## Skenování
Fragment skenování pro práci s kamerou využívá knihovny [CameraX](https://developer.android.com/training/camerax). CameraX obaluje předchozí API Camera2 a zjednodušuje práci s hardwarovou kamerou. Díky této knihovně je práce s kamerou rozdělena do tří hlavních celků: zobrazení náhledu, pořizování fotek a analýza náhledu.

Tato aplikace využívá všech třech částí. Nejnovější snímky jsou vždy zobrazovány jako náhled (pokud možno, jsou zobrazovány přes celou obrazovku). A v případě, že uživatel zmáčkne tlačítko fotoaparátu, aplikace pořídí snímek a předá ho fragmentu pro náhled (předává umístění dočasného souboru).

Analýza náhledu vždy předá nejnovější snímek algoritmu pro detekci stránky. Algoritmus vrací pozici rohů stránky. Pomocí kterých je vykreslován čtyřúhelník ohraničující stránku. Analýza náhledu vždy čeká na vyhodnocení než předá nový snímek, z toho důvodu je důležité, aby detekce probíhala co nejrychleji.

Ohraničení náhledu stránky je vykreslováno pomocí třídy `PageSurface`. Ta vytváří nové vlákno, ve kterém pomocí pozic rohů vykresluje ohraničující rámeček. Pokud dostane nové pozice rohů, tak vykreslí plynulý přechod. Jelikož je ohraničení vykreslováno nezávisle na náhledu z kamery, při prudkém pohybu kamerou chvíli trvá než třída dostane nové hodnoty a posune ohraničení. To muže být mírně nepříjemné, ale je to uživatelsky přívětivější, než aby rámeček nečekaně skákal.

## Náhled snímku
Po pořízení a uložení snímku z fragmentu skenování je snímek předán náhledu. Tento fragment nabízí uživateli možnost pojmenování dokumentu, pokud ještě není pojmenovaný. Dále pak může uživatel přidat další stránku do dokumentu nebo ukončit skenování. Fragment pak vytvoří složku dokument a přesune do ní naskenovaný snímek (snímek dostane jméno podle pořadí). Dále se vytvoří nebo aktualizuje záznam o dokumentu v databázi.

## Algoritmus detekce stránky
Algoritmus využívá základních operací zpracování obrazu poskytovaných knihovnou OpenCV. Hlavní myšlenkou algoritmu je využití detekce hran a nalezení čtyřúhelníkové kontury ve výsledném obrázku. Pro úspěšné detekování musí snímek splňovat několik předpokladů:

- Dokument je přibližně uprostřed snímku
- Dokument zabírá velkou část snímku (alespoň 40%)
- Existuje dostatečný kontrast mezi dokumentem a pozadím

*Tyto předpoklady vylučují některé snímky. Ale předpokládá se, že je v zájmu uživatele pořídit dobrý snímek, a proto umístí dokument do středu a pokud možno tak, aby zabíral většinu snímku.*

Jedním z cílů algoritmu je vyvarovat se zbytečným operacím a docílit co nejrychlejší detekce. Protože je důležité plynule zobrazovat ohraničení stránky v náhledu. Zároveň je logické, aby ohraničení z náhledu bylo identické s finálním ohraničením, a proto se ve finální detekci nevyužívá žádné složitější zpracování.

Celé zpracování začíná zmenšením obrázku na předem určenou velikost (400 px na výšku), což výrazně zrychlí běh algoritmu. Dále se provádí filtrování pomocí bilaterálního filtru, který potlačuje šum a zároveň zachovává hrany. Následně je na obrázku provedeno adaptivní prahování. Pro zpřesnění detekce jsou vybrány pouze regiony, kde je barva podobná barvě ve středu snímku. Z výsledného obrázku se vyberou uzavřené kontury, mezi kterými se dále hledá největší čtyřúhelníková kontura splňující předpoklady dokumentu. Nalezené rohy jsou naškálovány na velikost původního obrázku. Pozice rohů se vrací aplikaci nebo jsou rohy dále využity k vyříznutí dokumentu.

