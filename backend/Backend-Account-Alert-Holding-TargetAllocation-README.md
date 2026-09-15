Backend- Account, Holding, Alert och TargetAllocation:

Detta är en sammanställning av metoderna och klasserna som bröts ut ur HoldingController och AlertController

Account, Holding, Alert och TargetAllocation följer forfarande schemat som fanns i seed.sql och är oförändrade:

CREATE TABLE target_allocations (
id SERIAL PRIMARY KEY,
user_id INT REFERENCES users(id),
account_type VARCHAR(10),
target_pct DECIMAL(5,2)
);

CREATE TABLE alerts (
id SERIAL PRIMARY KEY,
user_id INT REFERENCES users(id),
alert_type VARCHAR(50),
message TEXT,
dismissed BOOLEAN DEFAULT false,
created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE holdings (
id SERIAL PRIMARY KEY,
account_id INT REFERENCES accounts(id),
ticker VARCHAR(20),
instrument_name VARCHAR(100),
quantity DECIMAL(12,4),
avg_buy_price DECIMAL(12,2),
currency VARCHAR(3) DEFAULT 'SEK'
);

CREATE TABLE accounts (
id SERIAL PRIMARY KEY,
user_id INT REFERENCES users(id),
account_type VARCHAR(10),
account_name VARCHAR(100),
currency VARCHAR(3) DEFAULT 'SEK'
);

Det som kommer täckas här är:

Account
Holding
Alert
TargetAllocation
Rest-Endpoints för dessa
JPA Repository
Service Struktur
Pagination
DTO
Validering
Ownership-kontroller(IDOR)
Caching
Transactional service-metoder
Tester

Spring-Security/JWT kommer senare och då också  @PreAuthorize. Tills dess används HttpSession.

## Endpoints:

### Account

#### GET /api/accounts?page=0&size=20
-inloggad användares konton hämtas med 20 som default och 100 som max.
-401 returneras om  session saknas

### Holding

currentPrice hämtas via MarketDataService, det kan behöva ändras när fx-rate är klart.

marketValue, pnl och pnlPct beräknas i HoldingService.
Samma beräkning används för listan av holdings, GET för ett specifikt holding och responsen efter PATCH.

#### GET /api/holdings/{holdingId}
-returnerar ett specifikt holding för inloggad användare
-holding hämtas med både holdingId och userId så man inte kan läsa annan användares holdings
-returnerar HoldingResponse

#### PATCH /api/holdings/{holdingId}
-uppdaterar ett befintligt holding
-partial update används, endast fält som skickas i requesten ändras
-holding hämtas med både holdingId och userId så man inte kan ändra annan användares holdings
-HoldingPatchRequest används för input och validering
-returnerar uppdaterad HoldingResponse
-cacheevict eftersom cachade holdings kan ha ändrats

#### GET /api/accounts/{accountId}/holdings?page=0&size=20
-returnerar holdings för ett specifikt konto
-innan holdings hämtas kontrolleras att account tillhör användaren

#### POST /api/accounts/{accountId}/holdings
-skapa ett nytt holding för kontot
-innan holdings skapas kontrolleras att account tillhör användaren
-cachar vid GET och evictar vid POST


#### DELETE /api/holdings/{holdingId}
-ta bort en holding
-holding hämtas med både holdingId och userId så man inte kan ta bort annan användares holdings
-cacheevict

### Tester

HoldingServiceTest och HoldingControllerTest har uppdaterats för de nya ändringarna.

Tester täcker bland annat:
-hämtning av specifikt holding
-PATCH av holding
-att PATCH endast ändrar fält som skickas
-ownership-kontroll för GET/PATCH
-att MarketDataService används för pris
-marketValue
-pnl
-pnlPct
-pagination och sortering
-cache-relaterade serviceflöden vid ändringar


### Alert

#### GET /api/alerts?dismissed=false&page=0&size=20

-returnerar alerts för användaren
-filtrerar på dismissed först

-OBS här kan vara en bra plats att fundera på unik implementation(tex vissa alerts kanske är viktigare än andra och visas alltid först, vissa kanske kräver åtgärd innan dom försvinner)

#### PATCH /api/alerts/{alertId}/dismiss
-markera ett alert som dismissed
-i uppgiften står det PUT, jag skrev put här ursprungligen för jag va helt säker på att jag ändrade till PUT, men versionen jag skickat vidare till frontend var med patch, så låter det vara tills vidare.


#### GET /api/alerts/live
-finns kvar sen tidigare
-ingen cache eftersom dessa kan va kopplade till data som ändras hela tiden.


### TargetAllocation
-inga endpoints skulle skapas enligt v2-targets.md

GET /api/target-allocations?page=0&size=20
-skapde denna ändå. Tanken är delvis till frontend för att läsa en användares target-allocations.
-härifrån är det lätt att lägga till endpoints som put/post etc. för att låta användare sätta egna targetallocations via frontend.

## DTO:

REST-lager returnerar DTO istället för JPA-entiteter, tex AccountResponse.
Då exponeras inte databasfält eller JPA-struktur  direkt till frontend.
Request-DTO används när frontend skickar data som behöver valideras.

## Pagination:

List-endpoints använder Page. Tex ?page=0&size=20 för användare inte ska kunna läsa och skicka obegränsade mängder data. Default är 20 och max är 100. Finns flera metoder utan paginering som inte kan nås från frontend, endast för internt bruk i backend(till exempel för TargetAllocations eller Alert som kan behöva nå alla konton samtidigt(tänk annars att alert endast kan ge begränsat antal varningar eller TargetAllocation kan bara beräknas för delar av av användares holdings)).

## Ownership(IDOR)

begränsat användares åtkomst åt andras resurser genom att kontrollera att id tex accountId tillhör inloggad user.
Detta ska kompleteras med @PreAuthorize enligt instruktioner efter Spring Security/JWT är implementerat.
Detta är fortfarande inte klart, saknas @EnableMethodSecurity i config.

## Transactional

Implementerar för dataintegritet.

## Caching

Caching används för data som läses ofta så man inte behöver hämta från databasen vid varje request.
När PUT/POST/DELETE används så evictas cachen om data som är cachad ändras.


## Validering

Input från frontend valideras innan det sparas. För request-DTO används bland annat @NotBlank, @Size, @NotNull, @Digits, @DecimalMin.
Dessa är baserade på databasens tidigare implementation(seed.sql). Dock är vissa saker inte från databasen, tex kvantitet > 0 fanns inte med i start och va listat som en punkt som behövde åtgärdas.

## Ej avslutat

GET /api/target-allocations
Detta är för vidareutveckling om gruppen vill ett krav enligt v2-targets.md.


GET /api/alerts/live
Detta ligger kvar sen tidigare med hårdkodade värden. behöver kopplas till värden som ska skickas från C/C++ utvecklare.

Tester
Det saknas integrationstester unittester för targetallocation.





















PS C:\Users\swede\chas_projekt\Kurs7_Avanza\avanza3\infra> docker compose -f docker-compose.yml -f docker-compose.migration.yml exec db15 sh -c 'PGPASSWORD=avanza123 pg_dump -h db -U avanza -d avanza -Fc --no-owner --no-privileges -f /tmp/avanza12.dump'                                         
time="2026-09-15T11:44:56+02:00" level=warning msg="C:\\Users\\swede\\chas_projekt\\Kurs7_Avanza\\avanza3\\infra\\docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"

PS C:\Users\swede\chas_projekt\Kurs7_Avanza\avanza3\infra> docker compose -f docker-compose.yml -f docker-compose.migration.yml exec db15 ls -lh /tmp/avanza12.dump                                                                                                                                   
time="2026-09-15T11:55:34+02:00" level=warning msg="C:\\Users\\swede\\chas_projekt\\Kurs7_Avanza\\avanza3\\infra\\docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"
-rw-r--r-- 1 root root 14K Sep 15 09:44 /tmp/avanza12.dump

PS C:\Users\swede\chas_projekt\Kurs7_Avanza\avanza3\infra> docker compose -f docker-compose.yml -f docker-compose.migration.yml exec db15 sh -c 'PGPASSWORD=avanza123 pg_restore -U avanza -d avanza --no-owner --no-privileges /tmp/avanza12.dump'                                                   
time="2026-09-15T11:59:20+02:00" level=warning msg="C:\\Users\\swede\\chas_projekt\\Kurs7_Avanza\\avanza3\\infra\\docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"

PS C:\Users\swede\chas_projekt\Kurs7_Avanza\avanza3\infra> docker compose -f docker-compose.yml -f docker-compose.migration.yml exec db15 psql -U avanza -d avanza -c "\dt"                                                                                                                           
time="2026-09-15T12:17:36+02:00" level=warning msg="C:\\Users\\swede\\chas_projekt\\Kurs7_Avanza\\avanza3\\infra\\docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"
List of relations
Schema |        Name        | Type  | Owner  
--------+--------------------+-------+--------
public | accounts           | table | avanza
public | alerts             | table | avanza
public | holdings           | table | avanza
public | target_allocations | table | avanza
public | users              | table | avanza
(5 rows)



PS C:\Users\swede\chas_projekt\Kurs7_Avanza\avanza3\infra> docker compose -f docker-compose.yml -f docker-compose.migration.yml exec db psql -U avanza -d avanza -c "SELECT 'users' AS table_name, COUNT(*) FROM users UNION ALL SELECT 'accounts', COUNT(*) FROM accounts UNION ALL SELECT 'holdings', COUNT(*) FROM holdings UNION ALL SELECT 'alerts', COUNT(*) FROM alerts UNION ALL SELECT 'target_allocations', COUNT(*) FROM target_allocations;"
time="2026-09-15T12:19:06+02:00" level=warning msg="C:\\Users\\swede\\chas_projekt\\Kurs7_Avanza\\avanza3\\infra\\docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"
table_name     | count
--------------------+-------
users              |     2
accounts           |     3
holdings           |     5
alerts             |     2
target_allocations |     3
(5 rows)

PS C:\Users\swede\chas_projekt\Kurs7_Avanza\avanza3\infra> docker compose -f docker-compose.yml -f docker-compose.migration.yml exec db15 psql -U avanza -d avanza -c "SELECT 'users' AS table_name, COUNT(*) FROM users UNION ALL SELECT 'accounts', COUNT(*) FROM accounts UNION ALL SELECT 'holdings', COUNT(*) FROM holdings UNION ALL SELECT 'alerts', COUNT(*) FROM alerts UNION ALL SELECT 'target_allocations', COUNT(*) FROM target_allocations;"
time="2026-09-15T12:38:24+02:00" level=warning msg="C:\\Users\\swede\\chas_projekt\\Kurs7_Avanza\\avanza3\\infra\\docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"
table_name     | count
--------------------+-------
users              |     2
accounts           |     3
holdings           |     5
alerts             |     2
target_allocations |     3
(5 rows)
manuellt jämföra är kanske inte optimalt, så kommer skriva tester som visar detta också.





