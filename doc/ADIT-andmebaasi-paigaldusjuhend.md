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
| 25.11.2016 | 1.1 | Dokument üle viidud MarkDown formaati | Kertu Hiire |
| 10.03.2017 | 1.2 | Muudatused seoses DHX protokolli kasutuselevõtuga | Aleksei Kokarev |

## Sissejuhatus

ADIT andmebaasi paigaldamine koosneb järgmistest sammudest:

1.	Andmebaasi skriptide laadimine Githubist
2.	Andmebaasi paigaldamine


### Nõuded keskkonnale

1.	Andmebaas Postgres 9.4 (UTF-8)


## ADIT andmebaas

### Andmebaasi skriptide allalaadimine

Andmebaasi skriptid ja rakenduse algkood on saadaval koos [lähtekoodiga](https://github.com/e-gov/ADIT) 

### Andmebaasi loomine

Andmebaasiskeemi loomise SQL skriptid asuvad paigalduspaketis kataloogis [/sql](../adit-war/sql). Enne skriptide käivitamist tuleb luua andmebaasiskeem / kasutaja (_schema_) ning tabeliruumid (_tablespace_) ADIT andmetabelite ja indeksite (_index_) jaoks. Paigaldamise sammud on järgmised:

1.	Tekitada eesti kodeeringus andmebaasi klaster: 

```
pg_createcluster --locale et_EE.UTF-8 9.4 main
```

2.	Tekitada kasutajana postgres kaks kasutajat, _adit_admin_ ja _adit_user_ 

   - **adit_admin** – kasutaja, kelle skeemi luuakse kõik tabelid / protseduurid / trigerid ja muud andmebaasiobjektid.
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

4.	Käivita SQL skript [latest_postgresql_database.sql](../adit-war/sql/latest_postgresql_database.sql) (ADIT andmebaasi loomine). Skript tuleb käivitada _adit_admin_ kasutaja poolt, kellel on tabelite, trigerite ja protseduuride loomise õigused.

5.	Kontrolli, kas tabelid, funktsioonid ja trigerid on loodud 

- Skeema „adit“

   * Tabeleid: 26
   * Trigereid: 19
   * Funktsioone: 3
   * Sequence: 13

- Skeema „aditlog“

   * Funktsioone: 20

Andmebaasikasutajal (_adit_admin_), kelle skeemi tabelid loodi, peavad olema järgmised õigused (antud juba kasutaja loomise käigus):

1.	Kõikidesse oma schema tabelitesse kirjutamise õigus
2.	Kõikide oma schema tabelite lugemisõigus
3.	Kõikide oma schema tabelite andmete muutmise õigus
4.	Kõikide oma schema _SEQUENCE_-te ja _TRIGGER_-ite käivitamise õigus


### Andmebaasi uuendamine

See samm on vajalik siis kui ADIT andmebaas on juba paigaldatud ja on vaja andmebaasi uuendada(paigaldada muudatusi mis olid tehtud andmebaasis uues ADITi versioonis)

1. Kävitada SQL skriptid mis asuvad kaustas adit-war/sql/updates. Skriptide käivitamisel on vaja teada mis versioon on hetkel paigaldatud ja käivitada kõik skriptid mis on suurema versiooninumbriga.


