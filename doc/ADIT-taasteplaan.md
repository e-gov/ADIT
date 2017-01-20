# ADIT - taasteplaan

## Sisukord

- [Sissejuhatus](#sissejuhatus)
- [Varundamine](#varundamine)
   * [Oracle 10g andmebaasi varundamine](#varundus-oracle)   
- [Taastamine](#taastamine)
   * [Andmete taastamine Oracle 10g puhul](#taaste-oracle)   
   
   
## Muutelugu

| Kuupäev | Versioon | Kirjeldus | Autor |
| --- | --- | --- | --- |
| 08.08.2010 | 1.0 | Dokumendi loomine | Marko Kurm |
| 23.11.2016 | 1.1 | Oracle andmebaasi osa eraldamine | Kertu Hiire |

## Sissejuhatus

Dokument annab ülevaate sellest, kuidas varundada ADIT rakendust ja andmeid ning neid hiljem rakenduse / andmete taastamisel kasutada.

## Varundamine

ADIT rakenduse varundamiseks on vajalik tekitada perioodiliselt koopia ADIT rakendusest. Varukoopia on mõistlik teha ennem järgmiseid tegevusi:

1. Versiooniuuendus
2. Versiooni tagasikeeramist (vanema versiooni taastamist)

Lisaks sellele tuleks rakendusest teha varukoopia regulaarse intervalliga, mis võiks olla kord kuus. Varukoopia tegemiseks toimi järgmiselt:

1. Kopeeri rakendusserveris paiknev rakenduse kataloog „[TOMCAT_HOME]/webapps/adit
2. Paiguta koopia turvalisse kohta

Rakenduse taastamiseks piisab kopeeritud rakenduse kataloogi paigutamisest kataloogi „[TOMCAT_HOME]/webapps“ ning rakendusserverile taaskäivituse tegemisest.

Andmete (andmebaasi) varundamine on esmatähtis kuna andmete kadumisel või vigastamisel ei ole alternatiivset allikat kust neid andmeid taastada. 

<a name="varundus-oracle"></a>
### Oracle 10g andmebaasi varundamine
Oracle 10g andmebaasis on andmete varundamiseks olemas mugavad lahendused. Võimalik on valida kahe meetodi vahel, kuidas andmeid varundada:

1. Varundada kogu failisüsteem, millel andmebaas asub – see lahendus on kõige töökindlam ja kiirem kuna varukoopia tehakse kõigist ketta failidest ning taastamisel ei pea andmebaasitarkvara uuesti paigaldama ja seadistama. Samas on selle meetodi miinuseks suur kettamahu hõivamine, kuna igal varundamisel tekib suur andmehulk (suurem kui tegelikult on meid huvitavate andmete maht).
2. Varundada kogu andmebaas (full database backup) – kogu andmebaasis olevast informatsioonist tehakse varukoopia. See meetod on vähem andmemahukas.

Oracle 10g puhul on võimalik andmebaasi varundamiseks kasutada kas utiliiti „Recovery Manager“ (RMAN) või hallata varundamist manuaalselt („user-managed backup and recovery“). Soovitatav on kasutada esimest. Oracle koduleheküljel (http://www.oracle.com/technology/documentation) on detailne informatsioon „Recovery Manager“ utiliidi kasutamiseks. Oluline on jälgida, et varukoopia tegemisel kasutataks sätet „Full Database Backup“ ehk et varukoopiasse kirjutataks täielik andmebaas.

## Taastamine

Rakenduse taastamiseks toimi järgnevalt: 

1. Tee kindlaks, millist versiooni rakendusest ja seadistusest on vaja taastada
2. Kopeeri varukoopia (terviklik rakenduse kataloog) rakendusserverisse, kirjutades üle kõik olemasolevad failid).
3. Taaskäivita rakendusserver.
4. Kontrolli rakenduse toimimist testlugude abil.

<a name="taaste-oracle"></a>
### Andmete taastamine Oracle 10g puhul

Andmete taastamiseks on jällegi mõtekas kasutada Oracle 10g utiliiti „Recovery Manager“. Juhised varukoopiast taastamise kohta leiad Oracle koduleheküljelt (http://www.oracle.com/technology/documentation) :

Oracle® Database Backup and Recovery Basics 10g Release 2 (10.2)
Oracle® Database Backup and Recovery Advanced User's Guide10g Release 2 (10.2)

