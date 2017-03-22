![](EL_Regionaalarengu_Fond_horisontaalne.jpg)

# SoapUI testide käivitamise juhend

## Sissejuhatus

SoapUI on tarkvara, mis võimaldab lihtsal viisil teha SOAP päringuid. SoapUI saab alla laadida ja rohkem infot leida on võimalik leheküljel: [https://www.soapui.org/](https://www.soapui.org/).

Käesolev juhend kirjeldab ADIT veebiteenuste testimiseks väljatöötatud SoapUI testide konfigureerimist ja käivitamist.

SoapUi testid on koostatud ja nimetatud vastavalt [testilugudele](ADIT-DHX-testilood.md).

## SoapUI testide konfigureerimine.

* Importida SoapUI project (SoapUI projekti importimise kohta võib rohkem infot  leida [siin](https://www.soapui.org/articles/import-project.html))
* SoapUi projekt asub [siin](../test), fail adit-soapui.xml

* Muuta ADIT_DHX SoapUi projekti parameetrid (SoapUI parameetrite kohta võib rohkem infot leida  [siin](https://www.soapui.org/functional-testing/properties/working-with-properties.html)):


| Parameetri nimi | Näidisväärtus | Kommentaar |
|-------|----------|----------------|
| aditDhxEndpoint | http://10.0.13.198/cgi-bin/consumer_proxy  | endpoint kuhu tuleb saata DHX päringud. Tavaliselt turvaserveri aadress. |
| aditEndpoint | http://10.0.13.198/cgi-bin/consumer_proxy  | endpoint kuhu tuleb saata ADIT päringud. Tavaliselt turvaserveri aadress. |
| xroadInstance | ee-dev | SOAP headeri Xtee parameetri xroadInstance väärtus |
| dhs1MemberClass | COM | testilugudes kirjeldatud DHS1 Xtee liikme memberClass |
| dhs1MemberCode | EE30000001 | testilugudes kirjeldatud DHS1 Xtee liikme memberCode |
| dhs1SubsystemDHX | DHX | testilugudes kirjeldatud DHS1 Xtee liikme DHX alamsüsteem |
| dhs1SubsystemAditRecipient | EEraamatupidamine.30000001 |  testilugudes kirjeldatud DHS1 Xtee liikme ADIT-i saaja |
| dhs1RepresenteeCode | EE70000001 | testilugudes kirjeldatud DHS1 Xtee liikme poolt vahendatava ettevõtte registrikood |
| aditMemberClass | GOV | testilugudes kirjeldatud ADIT Xtee liikme memberClass |
| aditMemberCode | 70006317 | testilugudes kirjeldatud ADIT Xtee liikme memberCode |
| aditSubsystem | adit | testilugudes kirjeldatud ADIT Xtee liikme alamsüsteem |
| aditSubsystemDHX | DHX.adit | testilugudes kirjeldatud ADIT Xtee liikme DHX alamsüsteem |
| consumerMemberClass | COM | testilugudes kirjeldatud CONSUMER Xtee liikme memberClass |
| consumerMemberCode | 12345678 | testilugudes kirjeldatud CONSUMER Xtee liikme memberCode |
| consumerSubsystem | DHX | testilugudes kirjeldatud CONSUMER Xtee liikme alamsüsteem |
| consumerSubsystem2 | DHX.test | testilugudes kirjeldatud CONSUMER Xtee liikme alamsüsteem |
| userCode | EE37901130250 | päringu teinud isiku kood ja isik, kellele dokument on suunatud  |
| infoSystem | KOV | päringut tegeva infosüsteemi nimi |
| goodCapsule | C:\Users\alex\Desktop\xmls/kapsel_21.xml | viide failile, mis sisaldab Elektroonilise andmevahetuse metaandmete loendile 2.1 vastavalt korrektselt kapseldatud faili.|
| badCapsule | C:\Users\alex\Desktop\xmls/kapsel_21_wrong.xml | viide failile, mis sisaldab XML-i, mis ei vasta Elektroonilise andmevahetuse metaandmete loendile 2.1 |
| notCapsule | C:\Users\alex\Desktop\xmls/kapsel_21_not_kapsel.xml | viide failile, mis ei ole XML-vormingus või on XML vales vormingus.  |
| document | C:\Users\alex\Desktop\xmls/document.xml | viide dokumendile, mida saadetakse kasutajale |

**Failid asuvad [xmls](../test/xmls) kaustas. Faili viidetega parameetrid (goodCapsule, badCapsule, notCapsule, document) tuleb muuta igas keskkonnas kus teste käivitatakse. Faili viide peab olema absolute path failini.**

## SoapUI testide käivitamine
Testide struktuuri ja käivitamise kirjeldus on esitatud [siin](https://www.soapui.org/functional-testing/structuring-and-running-tests.html).

Pärast testide läbimist tuleb veenduda, et kõik testid on õnnestunud.
