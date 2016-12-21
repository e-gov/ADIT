# ADIT - Paigaldusjuhend

## Sisukord

- [Sissejuhatus](#sissejuhatus)
   * [Nõuded keskkonnale](#nõuded-keskkonnale)
- [ADIT andmebaas](#adit-andmebaas)
   * [Andmebaasi skriptide allalaadimine](#andmebaasi-skriptide-allalaadimine)
   * [Andmebaasi loomine](#andmebaasi-loomine)


## Muutelugu

| Muutmiskuupäev | Versioon | Kirjeldus | Autor |
|---|---|---|---|
| 12.05.2015 | 1.0 | Andmebaasi dokument | Kristo Kütt |
| 30.11.2016 | 1.1 | Dokument üle viidud MarkDown formaati ning lisatud _max_prepared_transaction_ parameetri selgitus | Kertu Hiire |

## Sissejuhatus

ADIT andmebaasi paigaldamine koosneb järgmistest sammudest:

1.	Andmebaasi skriptide laadimine SVN-ist
2.	Andmebaasi paigaldamine


### Nõuded keskkonnale

1.	Andmebaas Postgres 9.4 (UTF-8)
2.	Eraldiseisev DVK universaalklient ADIT-ile kasutamiseks. DVK universaalkliendi paigaldamiseks vaata [paigaldusjuhendit DVK kliendi repositooriumis](https://github.com/e-gov/DVK/tree/master/doc/client). Lisanõudena on vajalik lülitada välja DVK universaalkliendi andmebaasipäästik _“tr_dhl_message_id”_.


## ADIT andmebaas

### Andmebaasi skriptide allalaadimine

Andmebaasi skriptid ja rakenduse algkood on saadaval koos [lähtekoodiga](https://github.com/e-gov/ADIT) 

### Andmebaasi loomine

Andmebaasiskeemi loomise SQL skriptid asuvad paigalduspaketis kataloogis [/sql](../adit-war/sql). Enne aga, kui skripte käivitada, tuleb luua andmebaasiskeem / kasutaja (_schema_) ning tabeliruumid (_tablespace_) ADIT andmetabelite ja indeksite (_index_) jaoks. Paigaldamise sammud on järgmised:

1.	Tekitada eesti kodeeringus andmebaasi klaster: 

```
pg_createcluster --locale et_EE.UTF-8 9.4 main
```

2.	Tekitada kasutajana postgres kaks kasutajat, _adit_admin_ ja _adit_user_ 

   - **adit_admin** – kasutaja, kelle skeemi luuakse kõik tabelid / protseduurid / triggerid ja muud andmebaasiobjektid.
   - **adit_user** – kasutaja, kelle abil rakendus andmebaasiga suhtleb. 

```
CREATE ROLE adit_admin LOGIN password 'xxx';
ALTER ROLE adit_admin SET search_path = adit, public;
CREATE ROLE adit_user LOGIN password 'yyy';
ALTER ROLE adit_user SET search_path = adit, public;  
```

3.	Tekitada kasutajana postgres _create database_ adit kasutaja _adit_admin_ omandusse

```
CREATE DATABASE adit
WITH OWNER = adit_admin
ENCODING = 'UTF8'
TABLESPACE = pg_default
LC_COLLATE = 'et_EE.UTF-8'
LC_CTYPE = 'et_EE.UTF-8'
CONNECTION LIMIT = -1;
```

4.	Käivita SQL skript [latest_postgresql_database.sql](../adit-war/sql/latest_postgresql_database.sql) (ADIT andmebaasi loomine). Skript tuleb käivitada _adit_admin_ kasutaja poolt, kellel on tabelite, päästikute ja protseduuride loomise õigused.

5.	Kontrolli, kas tabelid, funktsioonid ja triggerid on loodud 

- Skeema „adit“

   * Tabeleid: 25
   * Triggereid: 19
   * Funktsioone: 3
   * Sequence: 13

- Skeema „aditlog“

   * Funktsioone: 20

Andmebaasikasutajal (_adit_admin_), kelle skeemi tabelid loodi, peavad olema järgmised õigused (juba antud kasutaja loomise käigus):

1.	Kõikidesse oma schema tabelitesse kirjutamise õigus
2.	Kõikide oma schema tabelite lugemisõigus
3.	Kõikide oma schema tabelite andmete muutmise õigus
4.	Kõikide oma schema _SEQUENCE_-te ja _TRIGGER_-ite käivitamise õigus


Et võimaldada transaktsiooniliste käskude kiiremat käitlemist, peab postgresql.conf faili lisama parameetri 
_"max_prepared_transactions"_. Selle väärtus peaks olema vähemalt sama suur kui parameetri "max_connections" väärtus.
