# ADIT - Lahenduse kirjeldus

## Sisukord

- [Sissejuhatus](#sissejuhatus)
- [Kasutatud tehnoloogiad](#kasutatud-tehnoloogiad)
- [Lahenduse arhitektuur ja ülesehitus](#lahenduse-arhitektuur-ja-ülesehitus)
   * [Seosed teiste infosüsteemidega](#seosed-teiste-infosüsteemidega)
      * [Teavituskalender ja riigiportaal](#teavituskalender-ja-riigiportaal)
      * [DHX liides](#dhx-liides)
   * [Tagasiside ja veateated](#tagasiside-ja-veateated)
   * [Logimine](#logimine)
      * [Rakenduse logi](#rakenduse-logi)
      * [Andmebaasi logi](#andmebaasi-logi)
   * [Arhitektuur](#arhitektuur)
- [Monitooring](#monitooring)
   * [Aktiivne monitooring](#aktiivne-monitooring)
   * [Passiivne monitooring](#passiivne-monitooring)
   
## Sissejuhatus

ADIT (Ametlike Dokumentide Infrastruktuuri Teenus) on infosüsteem, mille abil avaliku sektori infoüsteemid saavad vahetada dokumente ning hoida nende menetlusinfot. ADIT-iga suhtlemiseks kasutavad teised infosüsteemid veebiteenuseid. ADIT-il puudub kasutajaliides kuna ta on mõeldud puhtal kujul teenusena, mille peale erinevad portaalid saavad ise ehitada kasutajaliidese.

## Kasutatud tehnoloogiad

ADIT rakendus on oma olemuselt veebirakendus, mis töötab Java rakendusserveris ning publitseerib erinevaid veebiteenuseid, mille abil saab süsteemi talletada dokumente ning neid teistele kasutajatele saata. Samuti käib veebiteenuste abil kasutajakontode haldamine – kasutajakonto loomine, muutmine ja kustutamine. ADIT teenused on mõeldud kasutamiseks üle X-tee – s.t. ADIT rakendus eeldab, et veebiteenuse päringu juures on määratud X-tee päised. Järgneb ADIT lahenduse loomise juures kasutatud tehnoloogiate ja raamistike tutvustus.

- **Spring Web Services** – lahenduse kokkupanemiseks ja veebiteenuste loomiseks (http://projects.spring.io/spring-ws/)
- **X-tee teek** – Spring Web Services teegiga ühilduv X-tee spetsiifiline abiteek, mis võimaldab tarbida ja publitseerida X-tee nõuetele vastavaid veebiteenuseid (https://github.com/nortal/j-road)
- **DHX adapter** – DHX protokolli järgi suhtlemise lihtsustamiseks mõeldud Java teek (https://github.com/e-gov/DHX-adapter)
- **Castor** – XML marshaller, mis võimaldab lihtsalt teisendada XML-i Java objektideks ja vastupidi (https://castor-data-binding.github.io/castor/)
- **Hibernate ORM** – andmebaasiliidese loomiseks ja andmebaasiobjektide teisendamiseks Java objektideks (http://hibernate.org/orm/)
- **JDigiDoc** – digitaalallkirjastamise abiteek. Teek pakub funktsionaalsust digitaalselt allkirjastatud failide loomiseks, lugemiseks, allkirjastamiseks
- **Log4J** – rakenduse töö ja vigade logimist lihtsustav teek (http://logging.apache.org/log4j/)
- **Quartz** – ajatatud toimingute käivitamise abiteek. Võimaldab perioodiliselt käivitada erinevaid protsesse (http://www.quartz-scheduler.org/)
- **Maven** – rakenduse ehitamiseks ja sõltuvuste (Java teegid) haldamiseks mõeldud abitarkvara (http://maven.apache.org/)

## Lahenduse arhitektuur ja ülesehitus

### Seosed teiste infosüsteemidega

ADIT on liidestatud teavituskalendriga ning kasutab DHX protokolli dokumentide saatmiseks.

![Arhitektuuri joonis](../doc/img/ADITiArhitektuurijoonis.png)

### Teavituskalender ja riigiportaal

ADIT rakendus on üle X-tee seotud teavituskalendriga, mille abil saadetakse kasutajatele neile huvi pakkuvaid ADIT sündmuseid, mis on järgmised:

1. Dokumendi saatmine
2. Dokumendi jagamine ja jagamise lõpetamine
3. Dokumendi vaatamine
4. Dokumendi muutmine
5. Dokumendi allkirjastamine

#### Teavituste saatmine teavituskalendrisse

ADIT kasutab teavituste saatmiseks teavituskalendrile veebiteenust „lisaSyndmus.v1“. Teavitusi saadetakse kasutajatele, kellel on ADIT teenuses vastavat liiki teavituste (näiteks teavitused allkirjastamise kohta) edastamine aktiveeritud.
ADIT lisab üldjuhul teavituskalendrisse teavituse niipea, kui vastav sündmus ADIT-is toimub. Lisaks toimub perioodilidelt selliste teavituste lisamine teavituskalendrisse, mida reaalajas ei õnnestunud mingil põhjusel lisada.

#### Teavituste staatuse pärimine riigiportaalist

ADIT andmekogu päring getNotifications tagastab oma vastuses andmeid kahest andmekogust – ADIT-ist ja riigiportaalist. S.t. getNotifications pöördub taustal riigiportaali andmekogu poole ja pärib sealt „tellimusteStaatus.v1“ päringuga, millised teavitused on antud kasutajal tellitud ja millistele e-posti aadressidele need suunatakse.


### DHX liides

ADIT rakendus kasutab dokumentide edastamisel ühe kanalina DHX protkolli. Dokumendid liiguvad ADIT ja DHX adressaatide vahel mõlemas suunas.

ADIT kasutab DHX protokolli järgi suhtlemiseks DHX adapter, mis on spetsiaalselt selleks otstarbeks loodud Java teek. DHX adapteri kasutamiseks tuleb seda eelnevalt seadistada. (lähemalt vaata paigaldusjuhendist)

ADIT-i vajadus on saata juhtudel, kui saaja üks asutustest on DHX kasutaja, dokumente talle läbi DHX teenuse. 

ADIT peab olema suuteline ka DHX-st tulevaid dokumente vastu võtma ning oma andmetabelitesse paigutama. 


#### Dokumendi saatmine DHX-i

Kui ADIT rakendusest saadetakse dokument (kutsutakse välja veebiteenus „sendDocument()“) adressaadile, siis enne saatmist kontrollitakse, kas adressaat on DHX-i adressaat. Kui vastus on positiivne, saadetaksegi dokument sellele konkreetsele adressaadile DHX kaudu. 
See tähendab seda, et saadetava dokumendi andmete põhjal koostatakse kapsli versioon 2.1 vastav konteiner. Seejärel saadetakse konteiner koos saatmist puudutavate andmetega DHX adressaadile, kasutades DHX adapteri poolt pakutud funktsioone. DHX adapter saadab dokumendi asünkroonselt ja pärast saatmist kutsub välja ADITi funktsiooni saatmise tulemuste salvestamiseks(tulemuste salvestamisel uuendatakse staatust ja salvestatakse DHX receipt id juhul kui saatmine õnnestus, vastasel juhul ainult uuendatakse staatust).


#### Dokumendi vastuvõtmine DHX-st

ADIT pakub DHX teenust sendDocument(DHX adapter abil), kuhu teised DHX kasutajad võivad dokumente saata. 

Dokumendi vastuvõtmisel  võetakse saabunud konteinerist välja andmed ning salvestatakse ADIT andmebaasis. ADIT andmebaasis seotakse dokument dokumendi adressaadiks olnud kasutajaga. 
Kui dokumendi sidumisel kasutajaga ilmnes, et kasutajat pole ADIT aktiivsete kasutajate hulgas, siis tagastatakse viga. Kui dokumendi adressaadiks on DHX kasutaja, siis talitatakse sarnaselt eeltoodule, kuna DHX kasutajad peavad suhtlema otse omavahel, mitte ADIT kaudu.

DHX kaudu vastuvõetava dokumendi puhul on vaja välja selgitada, millisele ADIT kasutajale see dokument mõeldud on. Selle jaoks tuleb panna ADIT kasutaja kood konteineris blokkidesse "transport" ning "saaja" blokki – täpsemalt `<Transport><DecRecipient><PersonalIdCode>` ning `<Recipient><Person><PersonalIdCode>` sisse:

```xml
<DecContainer xmlns="http://www.riik.ee/schemas/deccontainer/vers_2_1/">
    <Transport>
		...
        <DecRecipient>
            <OrganisationCode>adit</OrganisationCode>
            <PersonalIdCode>EE47101010033</PersonalIdCode>
        </DecRecipient>
        ...
    </Transport>
    ...
    <Recipient>
		...
        <Person>
        	<Name>Mari-Liis Männik</Name>
         	<GivenName>Mari-Liis</GivenName>
        	<Surname>Männik</Surname>
         	<PersonalIdCode>EE47101010033</PersonalIdCode>
          	<Residency>EE</Residency>
        </Person>
    	...
    </Recipient>
```


#### DHX kasutajate eristamine

DHX kasutajad erinevad  ADIT tavakasutajatest selle poolest, et DHX kasutajate kontol on määratud DHX organisatsiooni registrikood (tabel ADIT_USER.dvk_org_code). Kui see andmeväli ei ole määratud on tegemist nö. ADIT tavakasutajaga.
DHX kasutaja andmete muudatused (lisamised, nimemuutused, kustutamised) liiguvad ainult ühepoolselt – DHX-st ADITisse. DHX kasutajate andmeid ei saa muuta join või unJoin päringutega.
DHX kasutaja (pöördudes ADIT teenuse poole otseste x-tee päringutega)  saab kutsuda välja ainult päringuid:

- getDocumentList
- getDocument
- getDocumentFile
- getDocumentHistory

#### DHX kasutajate sünkroniseerimine

Kõik DHX kasutajad on automaatselt ka ADIT kasutajad. See tähendab seda, et ADIT rakenduse paigaldamisel on vaja tekitada ADIT-isse kõikidele DHX kasutajatele vastavad kasutajakontod. Samuti on vaja seadistuses määratud perioodi tagant kontrollida üle, kas DHX kasutajate ja ADIT kasutajate vahel on erinevusi. Selleks on ette nähtud ajatatud toiming, mida saab seadistada perioodiliselt käivituma ja mis teeb järgmist:

- Eemaldab DHX-st kustutatud kasutajate andmed ADIT andmebaasist (märgib kasutajate kontod mitteaktiivseks)
- Uuendab muutunud kasutajate andmed (kasutaja registrikoodi põhiselt)
- Loob lisandunud DHX kasutajate jaoks ADIT kasutajakontod

#### DHX kasutajate ADITis identifitseerimine

ADIT identifitseerib kasutajaid ühe parameetri järgi, mis on reeglina asutuse registrikood(või isikukood kui tegemist on isikuga). DHX kasutajaid aga identifitseeritakse asutuse registrikoodi ja alamsüsteemi järgi. 

Selle vastuolu lahendamiseks salvestatakse DHX kasutajaid ADIT-sse järgmiselt:

* Tavalised DHX adressaadid (EE/GOV/<registrikood>/DHX/sendDocument), lisatakse ADIT_USER tabelisse kujul dvk_org_code=registrikood.

* DHX-i alamsüsteemidega (kelle subSystemCode ei ole DHX vaid DHX.alamsüsteem) adressaadid registreeritakse ADIT_USER tabelis kujul dvk_org_code=alamsüsteem.registrikood

* Selleks et ADIT adressaatide nimekiri ei muutuks pärast DHX-le üleminekut, tehakse erandid DVKs registreeritud alamsüsteemide jaoks (adit,kovtp,rt,eelnoud). Neid ei registreeri ADITis kujul dvk_org_code=alamsüsteem.registrikood, vaid kujul dvk_org_code=alamsüsteem, kuna nii on nad ADITis registreeritud enne DHX-le üleminekut.

### Tagasiside ja veateated

Tagasiside andmiseks teenuse tarbijatele tagastatakse SOAP päringu vastuses vastavalt päringu õnnestumisele (vea)teade. Teated pannakse SOAP päringu vastuse kehas asuvasse elementi `<messages>`. Päringu üldist õnnestumist / ebaõnnestumist näitab elemendi `<success>` väärtus (kui see on „true“, siis päring õnnestus täielikult).  Kui päringu täitmisel ilmnes ootamatu viga, mille töötlemisega ei ole arvestatud, siis tagastatakse veateade „Service Error“ – sellisel juhul tuleb vaadata rakenduse logidest, mis vea põhjustas.

Näide päringu vastuses olevast veateatest:

```xml
<adit:success>true/false</adit:success>
<adit:messages>
<adit:message lang="en">[teade]</adit:message>
    <adit:message lang="et">[teade]</adit:message>
    …
</adit:messages>
```

Veateadete sisu on määratud erinevates seadistusfailides , millest iga fail kirjeldab teated mingis kindlas keeles. Seadistusfailide nimekuju on järgmine: „messages_[LANGUAGE_CODE].properties“, kus [LANGUAGE_CODE] määrab ära millise keelega on tegemist. Inglisekeelsed teated asuvad failis nimega „messages_en.properties“.

Teated on määratud järgmises võti-väärtus formaadis:

```
...
user.nonExistent = User does not exist: {0}
user.inactive User account deleted (inactive) for user {0}
document.nonExistent = Document does not exist. Document ID: {0}
...
```

### Logimine

ADIT rakenduse puhul rakendatakse kahte tüüpi logimist – rakenduse logi ning logimine andmebaasis.

#### Rakenduse logi

Rakenduse logi tekitamiseks kasutatakse Log4j teeki, mis võimaldab logimise seadistust muuta konfiguratsioonifaili abil.

#### Andmebaasi logi

Järgnevalt on toodud erinevad ADIT andmebaasis olevad logitabelid ning nende eesmärk:

- **ADIT_LOG** – hoiab andmeid kõikide andmebaasis toimunud muudatuste kohta.
- **REQUEST_LOG** - tehtud päringute logimiseks. Logitakse järgmised päringud:
   * saveDocument
   * saveDocumentFile
   * deleteDocumentFile
   * archieveDocument
   * deleteDocument
   * getDocumentHistory
   * sendDocument
   * shareDocument
   * unShareDocument
   * markDocumentViewed
   * prepareSignature
   * confirmSignature
   * modifyStatus
- **METADATA_REQUEST_LOG** – logitakse järgmised päringud:
   * getDocumentList
   * getDocument
- **DOWNLOAD_REQUEST_LOG** – logitakse järgmised päringud:
   * getDocument (juhul kui päringu parameetri „kas tagastada ka failide sisu“ väärtuseks on „jah“)
   * getDocumentFile
- **ERROR_LOG** – logitakse päringute käigus tekkinud veateated

### Arhitektuur

ADIT rakenduse ülesehituse defineerib Spring Context Configuration, mis on sisuliselt XML konfiguratsioonifail, mis näitab Spring raamistikule, kuidas on erinevad klassid omavahel seotud ning millised on nende klasside omadused. Selleks failiks on antud juhul adit-servlet.xml
Veebiteenuste nö. sisenemispunktiks on Spring Web Services juures alati „Endpoint“. See on punkt, kust algab veebiteenuse päringu töötlemine. Erinevate rakenduse kihtide vahelised seosed on samuti defineeritud selles samas XML failis. Rakenduse kihid on järgmised:

**Andmekiht** (_Data Layer_) – andmebaasikihi moodustavad _DAO (Data Access Object)_ klassid, mis asuvad koodipuus paketis „ee.adit.dao“. Andmebaasikiht defineerib meetodid, mille abil suheldakse andmebaasiga. Kuna ADIT puhul leidus ka keerulisemaid äriloogilisi funktsioone, mille puhul ei olnud võimalik paigutada kogu andmebaasisuhtlust _DAO_ klassidesse, siis mõnel juhul võib leida andmebaasisuhtluseks mõeldud meetodeid ka teenuskihist. _DAO_ klassid kasutavad andmebaasiobjektidega suhtlemiseks _POJO (Plain Old Java Object)_ stiilis klasse, mis on _Hibernate mapping_ abil seotud andmebaasiobjektidega. Andmebaasisuhtluseks kasutatav _sessionFactory_ defineeritakse koos informatsiooniga selle kohta, kuidas Java klassid on seotud andmetabelitega järgmiselt:

```xml
<bean id="sessionFactory"
class="org.springframework.orm.hibernate3.LocalSessionFactoryBean">
  ...
  <property name="mappingLocations">
    <list>
      <value>classpath:hbm/AccessRestriction.hbm.xml</value>
      <value>classpath:hbm/AditUser.hbm.xml</value>
      <value>classpath:hbm/Document.hbm.xml</value>
      <value>classpath:hbm/DocumentDvkStatus.hbm.xml</value>
      <value>classpath:hbm/DocumentFile.hbm.xml</value>
      <value>classpath:hbm/DocumentFileDeflateResult.hbm.xml</value>
      <value>classpath:hbm/DocumentHistory.hbm.xml</value>
      <value>classpath:hbm/DocumentHistoryType.hbm.xml</value>
      <value>classpath:hbm/DocumentSharing.hbm.xml</value>
      <value>classpath:hbm/DocumentSharingType.hbm.xml</value>
      <value>classpath:hbm/DocumentType.hbm.xml</value>
      <value>classpath:hbm/DocumentWfStatus.hbm.xml</value>
      <value>classpath:hbm/Notification.hbm.xml</value>
      <value>classpath:hbm/NotificationType.hbm.xml</value>
<value>classpath:hbm/RemoteApplication.hbm.xml</value>
<value>classpath:hbm/RequestLog.hbm.xml</value>
<value>classpath:hbm/Signature.hbm.xml</value>
<value>classpath:hbm/UserNotification.hbm.xml</value>
<value>classpath:hbm/Usertype.hbm.xml</value>
    </list>
  </property>
  ...
</bean>
```

**Äriloogika kiht** (_Business Logic Layer_) – äriloogika kihi moodustavad teenusklassid e. _Service_ klassid. Nendes klassides on implementeeritud meetodid, mis sisaldavad äriloogika reegleid. Teenusklassid suhtlevad andmetega manipuleerimiseks _DAO_ klassidega.

**Presentatsiooni kiht** (_Presentation Layer_) – nö rakenduse kasutajaliides kiht, milleks antud juhul on veebiteenused. Presentatsiooni kihi moodustavad Endpoint klassid, mis asuvad paketis „ee.adit.ws.endpoint“ ning implementeerivad SOAP veebiteenuste jaoks vajalikud meetodid. Presentatsiooni kihi klassid kutsuvad välja äriloogika kihi meetodeid ning väldivad suhtlemist otse andmekihiga. Antud lahenduse juures on presentatsiooni kihis seadistatud ka _Castor XML Marshaller_ , mis võimaldab automaatselt teisendada sissetuleva/väljamineva _XML_ stringi _Java_ objektiks, mida on seejärel mugav manipuleerida. _Endpoint_ saab päringu töötlemiseks nn. _request_ objekti ning tagastab nn. _response_ objekti.

## Monitooring

ADIT rakenduse monitooring koosneb aktiivsest ja passiivsest monitooringust. Aktiivseks rakenduse monitoorimiseks on mõeldud monitooringu servlet, mis paigaldatakse koos rakendusega. Passiivse monitooringu lahendab Log4J Nagios appender – rakendus logib tekkinud vead kindlas formaadis kindlasse väljundisse. Nagiose seadistamisest on täpsemalt juttu paigaldusjuhendis.

### Aktiivne monitooring

Aktiivse monitooringu eesmärgiks on kontrollida rakenduse olulisemate komponentide töötamist. Kontrollitakse järgmiseid päringuid ja komponente:

| IDENTIFITSEERIV STRING | Päring / komponent |
| --- | --- |
| SAVE_DOCUMENT | Päringu saveDocument kontroll – muudetakse andmebaasis olevat testdokumenti (dokumendi pealkiri ja dokumendi faili sisu muudetakse – päringu tegemise hetke kuupäev / kellaaeg) |
| GET_DOCUMENT | Päringu getDocument kontroll – päritakse testdokumendi andmed ning kontrollitakse, kas dokumendi sisu on viimase saveDocument päringuga muudetud. |
| DHX_SEND | DHX-i saatmise kontroll – kontrollitakse, kas andmebaasis on dokumente, mis on määratud DHX-i saatmisele, kuid mille saatmine sinna ei ole õnnestunud määratud perioodi vältel. |
| GET_USER_INFO | Päringu getUserInfo kontroll – kontrollitakse, kas testkasutaja on teenusega liitunud ning kas päring tagastab kasutaja andmed. |
| NOTIFICATIONS | Teavitusteenuse liidese kontroll – kontrollitakse, kas andmebaasis on teavitusi, mis ei ole määratud perioodi jooksul saadetud teavitusteenusele. |
| ERROR_LOG | Vigade logitabeli kontroll – kontrollitakse, kas andmebaasis olevasse vigade tabelisse on tekkinud vigu.  Vaadeldavate vigade taseme (WARN/ERROR/FATAL) saab määrata seadistuses. |
| SUMMARY_STATUS | Näitab rakenduse töötamise koondstaatust – arvestatakse kõikide komponentide staatuseid v.a. ERROR_LOG. Kui üks komponentidest ei tööta, siis saab koondstaatuse väärtuseks „FAIL“ (ei tööta). |

ADIT rakenduse monitooringu servlet asub aadressil http://[host]:[port]/adit/monitor. Monitooringurakenduse seadistamiseks on failis **adit-configuration.xml** vastav sektsioon (lähemalt vaata paigaldusjuhendist).

### Passiivne monitooring

Passiivne monitooring saadab teateid Log4J appender-i abil. Teated on järgmises formaadis:

[KOMPONENT] [STATUS] [TIME/ERROR_MESSAGE]

Nt. õnnestumisel „ADIT_DB_CONNECTION_READ OK 0.008 seconds“ või ebaõnnestumisel „ADIT_DB_CONNECTION FAIL ORA-12345: Could not connect to host uk.ria.ee”

Komponendid, mille kohta teateid saadetakse on järgmised:

| Komponent | Kirjeldus |
| --- | --- |
| ADIT_APP | Kontrollib rakenduse seadistust ja üldist töötamist. |
| ADIT_DB_CONNECTION | Kontrollib andmebaasiühendust |
| ADIT_DB_CONNECTION_READ | Kontrollib andmete lugemist andmebaasist |
| ADIT_DB_CONNECTION_WRITE | Kontrollib andmete kirjutamist andmebaasi |

