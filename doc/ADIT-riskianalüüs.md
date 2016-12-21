# ADIT - Riskianalüüs


## Sisukord

- [Sissejuhatus](#sissejuhatus)
- [Riskid](#riskid)
   * [Andmebaasi turvameetmete puudulikkus](#andmebaasi-turvameetmete-puudulikkus)
   * [Andmebaasi väljalangemine](#andmebaasi-väljalangemine)
   * [Andmekadu andmebaasis](#andmekadu)
   * [Võrgupõhise tarkvara riskid](#võrgupõhise-tarkvara-riskid)
   * [Tarkvara ülekoormus](#tarkvara-ülekoormus)
   * [Tarkvara vead](#tarkvara-vead)
   * [Riistvara tõrked](#riistvara-tõrked)


## Muutelugu

| Kuupäev | Versioon | Kirjeldus | Autor |
| --- | --- | --- | --- |
| 08.08.2010 | 1.0 | Dokumendi loomine | Marko Kurm |
| 23.11.2016 | 1.1 | Mainitud ka PostgreSQL riistvara tõrgete sektsioonis | Kertu Hiire |


## Sissejuhatus

Käesolevas dokumendis on kirjeldatud erinevad riskid, mis tooksid realiseerumisel kaasa ADIT tarkvara tõrked või seiskumise. ADIT tarkvara halvamist võimaldavad riskid:

1. Andmebaasi turvameetmete puudulikkus
2. Andmebaasi väljalangemine
3. Andmekadu andmebaasis
4. Riistvara tõrked
5. Tarkvara vead
6. Tarkvara ülekoormus
7. Võrgupõhise tarkvara riskid

## Riskid

### Andmebaasi turvameetmete puudulikkus

Juhul kui andmebaasi kasutajakontode andmeid ei hoita hoolikalt võib kõrvalistel isikutel tekkida võimalus manipuleerida andmebaasiga. Riski realiseerumise tõenäosus sõltub asutuse turvapoliitika tõhususest.


### Andmebaasi väljalangemine

Andmebaasi väljalangemine ehk andmebaasi toimimise peatumise risk realiseerub kas andmebaasimootori vea või riistvaralise tõrke tõttu. Realiseerumise tõenäosus sõltub järgmistest asjaoludest:

- Kas andmebaas on klasterdatud
- Milline on riistvara, millel andmebaas töötab

Antud riski minimeerimiseks tuleks võimalusel klasterdada ADIT tarkvara andmebaas.


### Andmekadu

Risk kaotada andmeid realiseerub juhul kui toimub andmebaasifailide hävimine/vigastamine kas füüsiliselt (riistvaralised kahjustused) või loogiliselt. Sellisel juhul kaovad andmed ajavahemikust mil tehti viimane andmete varukoopia kuni antud hetkeni. Riski minimeerimiseks tuleb võimalikult tihti teha varukoopiaid.


### Võrgupõhise tarkvara riskid

Kuna ADIT tarkvara kasutab üle interneti suhtlemiseks X-tee infrastruktuuri, siis võrgupõhised turvariskid (andmete muutmine, andmete lekkimine kõrvalistele isikutele jne.) on minimaalsed. X-tee infrastruktuur minimeerib sellised riskid.

Teine risk, mis tuleneb võrgupõhise tarkvara olemusest on võrguühenduse katkestused. Sellisel juhul võib ADIT tarkvara olla seda kasutavatele infosüsteemidele kättesaamatu. Kuna ADIT tarkvara kasutab oma töös ka kahte liidest (DVK, teavituskalender), siis võrguühenduse katkedes võib katkeda ka ühendus liidestatud infosüsteemidega. Riski minimeerimiseks on liidesed üles ehitatud selliselt, et kui liidese abil ühenduse loomine ebaõnnestub, siis ei teki andmekadu ega andmete loogilise terviklikkuse rikkumist. Kui teavituste saatmine teavituskalendrisse ei õnnestu, siis proovitakse seda perioodiliselt teha seni, kuni see õnnestub. Sama kehtib DVK liidese puhul.


### Tarkvara ülekoormus

ADIT tarkvara ülekoormuse risk on olemas, kuid selle realiseerumine on vähetõenäoline, kuna ADIT kasutab andmetega manipuleerimisel konveier tüüpi tehnoloogiaid (_streaming_). Sellisel juhul on minimeeritud rakenduse ülekoormus, kuna andmeid töödeldakse vastavalt olemasolevale jõudlusele. Sellest hoolimata on risk, et ADIT serverile tehakse sellises mahus päringuid, et süsteemi riistvaraline seadistus ei pea selle vastu. Antud riski saab minimeerida sellega, et klasterdada rakendusserverid ning kasutada rakendusserveri _load-balancing_ tehnoloogiat. 


### Tarkvara vead

Kuigi ADIT tarkvara läbib enne toodangukeskkonda minekut testid, ei ole välistatud programmeerimisvigade olemasolu. Testimine välistab siiski enamjaolt selliste vigade olemasolu, mis võiksid tekitada andmekadu. Riski minimeerimiseks tuleb tarkvara korralikult testida – läbida kõik testlood, teha seda võimalusel toodangukeskkonnaga võimalikult identses keskkonnas.


### Riistvara tõrked

Riistvara tõrgete risk väljendub ADIT tarkvara puhul selles, et kui andmetega manipuleerimise meetod on teostamisel ja ilmneb riistvaraline tõrge, võib juhtuda, et andmete koosseisus ilmnevad vastuolud. Antud risk on minimeeritud, kuna Oracle 10/11g / PostgreSQL on transaktsiooniline andmebaas ja JDBC tehnoloogia, mida ADIT server kasutab andmebaasiga suhtlemisel toetab transaktsioone. Seega kui andmebaasioperatsiooni ajal ilmneb riistvaraline tõrge, jääb transaktsiooni kinnitamine (_commit_) toimumata ja andmed jäävad terviklikuks.
