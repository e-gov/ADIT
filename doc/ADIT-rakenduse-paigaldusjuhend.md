# ADIT - Rakenduse paigaldusjuhend

## Sisukord

- [Sissejuhatus](#sissejuhatus)
   * [Nõuded keskkonnale](#nõuded-keskkonnale)
- [ADIT rakendus](#adit-rakendus)
   * [Rakenduse ehitamine](#rakenduse-ehitamine)
   * [Seadistamine](#rakenduse-seadistamine)
      * [Fail adit-configuration.xml](#conf1)
      * [Fail adit-datasource.xm](#conf2)
      * [Fail adit-jobs.xml](#conf3)
      * [Fail log4j.xml](#conf4)
      * [Fail xtee.properties](#conf5)
      * [Fail jta.properties](#conf6)
   * [Rakenduse paigaldamine Tomcat 7.x  rakendusserverisse](#tomcat)
   * [Rakenduse paigaldamine Weblogic 10.x rakendusserverisse](#weblogic)
- [DVK liidese seadistamine](#DVK)
- [Teavituskalendri ja riigiportaali X-Tee liidese seadistamine](#notification)
- [Monitooringu rakendus ja rakenduse kontroll](#monitoring)
   * [Seadistamine](#monitoring-conf)
   * [Nagiose seaded](#nagios)
      * [Rakenduse log4j-nagiosappender seadistus](#nagios-log4j)
      * [Nagiose seadistus – passiivne monitoring](#nagios-passive)
      * [Nagiose seadistus – aktiivne monitooring](#nagios-active)



## Muutelugu

| Muutmiskuupäev | Versioon | Kirjeldus | Autor |
|---|---|---|---|
| 08.08.2010 | 1.0 | Dokumendi loomine | Marko Kurm |
| 06.09.2010 | 1.1 | Täiendatud punkte „Rakenduse paigaldamine“ ja „Rakenduse paigaldamine rakendusserverisse (Tomcat)“ - andmebaasi parameetrite seadistamisel  kasutatakse JNDI-d. | Marko Kurm |
| 14.09.2010 | 1.2 | Täiendatud punkti „Andmebaasi paigaldamine“, alampunkt 4 – skript „synonyms.sql“. | Marko Kurm |
| 21.09.2010 | 1.2.1 | Täiendatud / parandatud punkti „Andmebaasi paigaldamine“: - Vahetatud punktid 1. ja 2. - Täiendatud punkti 2. | Marko Kurm |
| 24.09.2010 | 1.2.2 | Asendatud failirajad relatiivsete teekondadega. - Täiendatud punkti „Rakenduse paigaldamine rakendusserverisse (WebLogic)“: - „Prepend classpath“ - Lisatud joonis rakenduse komponentide kirjeldamiseks | Marko Kurm |
| 27.09.2010 | 1.2.3 | Lisatud logimise alajaotusesse Nagios logimise konfiguratsiooni näide ja lühikirjeldus. | Jaak Lember |
| 15.09.2010 | 1.3 | Muudetud punkti „ADIT rakendus“ – adit.ear / adit.war. Sissejuhatuses uus rakendust kirjeldav diagramm, lisatud monitooringu komponent. | Marko Kurm |
| 25.10.2010 | 1.4 | Lisatud punkt “Monitooringu rakendus ja rakenduse kontroll” | Marko Kurm |
| 15.02.2011 | 1.4.1 | Lisatud peatükki “Seadistamine” konfiguratsiooniparameetri “documentRetentionDeadlineDays” kirjeldus. | Jaak Lember |
| 02.03.2011 | 1.4.2 | Lisatud adit-configuration.xml faili näitesse ja kirjeldusse mitme konfiguratsioonimuutuja kirjeldused, mis varem olid kirjeldamata jäänud. | Jaak Lember |
| 10.03.2014 | ??? | Täpsustatud andmebaasi paigaldusjuhendit | Alex Ehrlich |
| 07.05.2015 | 1.5 | Oracle andmebaasilt migratsioon Postgres andmebaasile | Kristo Kütt |
| 12.05.2015 | 1.5.1 | Rakenduse ja andmebaasi paigaldusjuhendi eraldamine | Kristo Kütt |
| 25.02.2016 | 1.5.2 | SVN asendatud Gitiga | Kertu Hiire |
| 24.11.2016 | 1.5.3 | Dokumentatsioon üle viidud MarkDown formaati ning teostatud pisiparandusi | Kertu Hiire |


## Sissejuhatus

ADIT rakenduse paigaldamine koosneb järgmistest sammudest:

1.	Lähtekoodi laadimine git-ist
2.	Rakenduse seadistamine
3.	Rakenduse ehitamine
4.	Rakenduse paigaldamine

Rakenduse komponendid:

1.	Andmebaas
2.	Veebiteenused
3.	DVK liides
4.	X-tee teavituskalendri liides
5.	Monitooringu komponent

### Nõuded keskkonnale

1.	Java Runtime Environment 1.7
2.	Rakendusserver Tomcat 7.x / WebLogic 12.x
3.	Andmebaas Postgres 9.4 (UTF-8)
4.	Ligipääs X-tee turvaserverile (X-tee teenuste publitseerimiseks ja tarbimiseks)
5.	Eraldiseisev DVK universaalklient ADIT-ile kasutamiseks. DVK universaalkliendi paigaldamiseks vaata [paigaldusjuhendit DVK kliendi repositooriumis](https://github.com/e-gov/DVK/tree/master/doc/client)

## ADIT rakendus

### Rakenduse ehitamine

Rakenduse algkood on saadaval avalikust Git-ist:
`git clone https://github.com/e-gov/ADIT`

Rakenduse ehitamiseks ning erinevate pakete koostamiseks on kasutusel Maven 2.x nimeline tarkvara (http://maven.apache.org/ ). Rakendus koosneb Maven mõistes kahest alamprojektist: adit-war ja adit-ear. Selline alajaotus on vajalik selleks, et koostada pake, mis sobiks Weblogic rakendusserverisse paigaldamiseks. Rakenduse sisuline osa paikneb alamprojektis adit-war ning Weblogic rakendusserveri jaoks vajalikud metaandmeid sisaldavad failid paiknevad adit-ear alamprojektis. Projekti ehitamiseks vajalikud seadistused paiknevad nö ülemprojektis ehk otse ADIT projekti juurkaustas asuvas _pom.xml_ failis.

Selleks, et koostada rakenduse pake, tuleb määrata seadistused projekti kirjeldavas failis _pom.xml_. Seadistatavad parameetrid on järgmised:
`<resourceDir/> ` - määrab ära, millisele keskkonnale omased rakenduse seadistusfailid pakkesse lisatakse (arenduskeskkond, testkeskkond, tootekeskkond).
Algkoodiga tulevad kaasa seadistusfailide näidised erinevate keskkondade tarbeks:

| Nimi | Failirada | Kirjeldus |
|---|---|---|
| adit-arendus-tomcat | adit-war/src/main/resources/conf/adit-arendus-tomcat/ | Arendus Tomcat |
| adit-test-tomcat | adit-war/src/main/resources/conf/adit-test-tomcat/ | Test Tomcat |
| adit-toodang-tomcat | adit-war/src/main/resources/conf/adit-toodang-tomcat/ | Toodang Tomcat |
| adit-toodang | adit-war/src/main/resources/conf/adit-toodang/ | RIA toodang (WebLogic) |
| girf-arendus | adit-war/src/main/resources/conf/girf-arendus/ | Girf arenduskeskkond |

**Rakenduse ehitamine:**

1.	Lisa Maven2 /bin kataloog keskkonnamuutujasse „PATH“ – see on vajalik selleks, et Maven-i käske mugavalt välja kutsuda (soovitatav).
2.	Käivita käsklus „mvn clean“ – kustutab eelmise rakenduse ehitamise ajutised tööfailid ning rakenduse paketi. Välistab ajutistest tööfailidest tekkida võivad vead.
3.	Käivita käsklus „mvn package“ – paneb kokku paigalduspaketid. Tekivad failid /adit-ear/target/adit.ear (sobib Weblogic rakendusserverisse paigaldamiseks)  ning  /adit-war/target/adit.war (sobib Tomcat rakendusserverisse paigaldamiseks).

### Rakenduse seadistamine

ADIT rakenduse seadistamine koosneb mitmest failist, mis on järgnevalt toodud välja koos seletustega:

| Faili nimi | Kirjeldus |
|---|---|
| adit-configuration.xml | rakenduse peamine seadistusfail |
| adit-datasource.xml | rakenduse andmebaasi ühenduste seadistusfail |
| log4j.xml | logimise seadistus |
| messages_et.properties | Eestikeelsete (vea)teadete seadistusfail |
| messages_en.properties | inglisekeelsete (vea)teadete seadistusfail |
| messages_ru.properties | venekeelsete (vea)teadete seadistusfail |
| DVKresponseMessage.xsl | DVK vastuskirja XSL stiilileht |
| xtee.properties | X-Tee seadistus (vajalik X-Tee teavituskalendri funktsionaalsuse jaoks) |

<a name="conf1"></a>
#### Fail adit-configuration.xml

```xml
<!-- Configuration -->
<bean id="configuration" class="ee.adit.util.Configuration">
  <property name="getJoinedMaxResults" value="10" />
  <property name="tempDir" value="tmp" />
  <property name="deleteTemporaryFiles" value="true" />
  <property name="schedulerEventTypeName" value="Minu dokumentide teavitus" />
  <property name="dvkResponseMessageStylesheet" value="DVKresponseMessage.xsl" />

  <!-- Üldine kettamahu piirang ühe kasutaja kohta baitides -->
  <property name="globalDiskQuota" value="10240000" />

  <!-- Dokumentide säilitustähtaeg päevades -->
  <property name="documentRetentionDeadlineDays" value="365" />

  <property name="locales">
    <list>
	<value>en_us</value>
	<value>et_ee</value>
    </list>
  </property>

  <property name="dvkOrgCode" value="70000007" />
  <property name="xteeInstitution" value="70000007" />
  <property name="xteeSecurityServer" value="http://10.0.15.1/cgi-bin/consumer_proxy" />
  <property name="xteeIdCode" value="00000000000" />
</bean>

<bean id="monitorConfiguration" class="ee.adit.util.MonitorConfiguration">
  <property name="aditServiceUrl" value="http://localhost:7001/adit/service" />
  <property name="remoteApplicationShortName" value="MONITOR_TEST_APP" />
  <property name="userCode" value="EE00000000000" />
  <property name="institutionCode" value="123456789" />
  <property name="testDocumentId" value="999999999999" />
  <property name="dvkTestDocumentID" value="9999999999" />
  <property name="testDocumentFileId" value="999999999999" />
  <property name="testUserCode" value="EE00000000000" />
  <property name="documentSaveInterval" value="60000" />
  <property name="documentSendToDvkInterval" value="60000" />
  <property name="documentSendToDvkServerInterval" value="60000" />
  <property name="documentSendToDvkFromDvkServerInterval" value="60000" />
  <property name="documentSendToAditInterval" value="60000" />
  <property name="notificationSendInterval" value="60000" />
  <property name="errorInterval" value="60000" />
  <property name="errorLevel" value="FATAL" />
</bean>
```

- **getJoinedMaxResults** – sätestab päringu „GetJoined“ maksimaalse tagastatavate tulemuste arvu.
- **tempDir** – kataloog, kuhu ADIT rakendus paigutab töötamise ajal loodud ajutised failid. Sellele kataloogile peab olema kasutajal, kellena käivitatakse rakendusserver, lugemis- ja kirjutamisõigus.
- **deleteTemporaryFiles** – tõeväärtus tüüpi muutuja, mis näitab, kas ajutised failid kustutatakse peale nendega töötamist või mitte. PS! Testimise ajaks ja vigade otsimisel on soovitatav ajutised failid alles jätta.
- **schedulerEventTypeName** – X-Tee teavituskalendris kasutatav sündmuse nimi ADIT sündmuste jaoks.
- **dvkResponseMessageStylesheet** – määrab ära, millist stiililehte kasutatakse DVK vastuskirja koostamiseks.
- **globalDiskQuota** – üldine kettamahu piirang ühe kasutaja kohta baitides.
- **documentRetentionDeadlineDays** – dokumentide säilitustähtaeg päevades. Arvestatakse dokumendi viimase muutmise kuupäevast. Reaalselt kasutatakse seda ainult getDocument ja getDocumentList päringute vastustes dokumendi eeldatava kustutamisaja arvutamiseks.
- **locales** – nimekiri keeltest, milles tagastatakse päringute (vea)teated. Keele määramisel kasutatakse formaati `[keelekood]_[riigikood]`. Nt. „en_us“. Keele- ja riigikoodidena kasutatakse kahetähelisi ISO 639-1 formaadis koode.
- **dvkOrgCode** – asutuse kood, kelle nimel saadetakse dokumendid DVK-sse.
- **xteeInstitution** – asutuse kood, kelle nimel saadetakse teavitusi teavituskalendrisse.
- **xteeSecurityServer** – turvaserveri aadress, mille kaudu saadetakse teavitusi teavituskalendrisse.
- **xteeIdCode** – Isikukood, mida kasutatakse teavituskalendri x-tee päringute tegemiseks.
- **aditServiceUrl** – ADIT veebiteenuse URL (kui tahetakse monitoorida üle X-Tee, siis turvaserveri aadress).
- **remoteApplicationShortName** – monitooringu päringute jaoks kasutatav test-infosüsteemi lühinimi. Selle väärtuse muutmisel peab olema veendunud, et andmebaasis on ka vastav kirje tabelis REMOTE_APPLICATION.
- **userCode** – monitooringu päringute kasutaja kood.
- **institutionCode** – monitooringu päringute asutuse kood.
- **testDocumentId** – monitooringu päringute testdokumendi ID.
- **dvkTestDocumentID** – monitooringu päringute DVK testdokumendi ID.
- **testDocumentFileId** – monitooringu päringute testdokumendi faili ID.
- **testUserCode** – monitooringu kasutajate nimekirja päringu testkasutaja kood.
- **documentSaveInterval** – päringu „saveDocument“ testimise periood millisekundites.
- **documentSendToDvkInterval** – dokumentide DVK-sse saatmise periood.
- **documentSendToAditInterval** – dokumentide ADIT-isse importimise periood.
- **notificationSendInterval** – teavituste saatmise intervall.
- **errorInterval** – rakenduse vigade tabeli kontrollimise periood.
- **errorLevel** – määrab vaadeldavate vigade taseme vigade tabelis. Võimalikud väärtused – WARN, ERROR, FATAL.

<a name="conf2"></a>
#### Fail adit-datasource.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:tx="http://www.springframework.org/schema/tx"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
						http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
						http://www.springframework.org/schema/context
						http://www.springframework.org/schema/context/spring-context-2.5.xsd
						http://www.springframework.org/schema/tx
						http://www.springframework.org/schema/tx/spring-tx-2.5.xsd">

	<bean id="sessionFactory"
		class="org.springframework.orm.hibernate3.LocalSessionFactoryBean">
		<property name="dataSource" ref="aditDataSource" />
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
				<value>classpath:hbm/ErrorLog.hbm.xml</value>
				<value>classpath:hbm/DownloadRequestLog.hbm.xml</value>
				<value>classpath:hbm/MetadataRequestLog.hbm.xml</value>
				<value>classpath:hbm/MaintenanceJob.hbm.xml</value>
			<value>classpath:hbm/SetJobRunningStatusResult.hbm.xml</value>
				<value>classpath:hbm/UserContact.hbm.xml</value>
			</list>
		</property>
		<property name="hibernateProperties">
			<props>
				<prop key="hibernate.dialect">org.hibernate.dialect.PostgreSQLDialect</prop>
				<prop key="hibernate.connection.driver_class">org.postgresql.Driver</prop>
				<prop key="hibernate.cache.provider_class">org.hibernate.cache.NoCacheProvider</prop>
				<prop key="hibernate.current_session_context_class">org.hibernate.context.JTASessionContext</prop>
				<prop key="hibernate.transaction.manager_lookup_class">org.hibernate.transaction.WeblogicTransactionManagerLookup</prop>
				<prop key="hibernate.show.sql">true</prop>
				<prop key="hibernate.connection.useUnicode">true</prop>
				<prop key="hibernate.connection.characterEncoding">UTF-8</prop>
				<prop key="hibernate.connection.charSet">UTF-8</prop>
			</props>
		</property>
	</bean>

	<bean id="dvkSessionFactory"
		class="org.springframework.orm.hibernate3.LocalSessionFactoryBean">
		<property name="dataSource" ref="aditDVKDataSource" />
		<property name="mappingLocations">
			<list>
				<value>classpath:dvkapi/Counter.hbm.xml</value>
				<value>classpath:dvkapi/Subdivision.hbm.xml</value>
				<value>classpath:dvkapi/Occupation.hbm.xml</value>
				<value>classpath:dvkapi/Organization.hbm.xml</value>
				<value>classpath:dvkapi/SettingsFolders.hbm.xml</value>
				<value>classpath:dvkapi/Settings.hbm.xml</value>
				<value>classpath:dvkapi/Message.hbm.xml</value>
				<value>classpath:dvkapi/MessageRecipient.hbm.xml</value>
			</list>
		</property>
		<property name="hibernateProperties">
			<props>
				<prop key="hibernate.dialect">org.hibernate.dialect.PostgreSQLDialect</prop>
				<prop key="hibernate.connection.driver_class">org.postgresql.Driver</prop>
				<prop key="hibernate.cache.provider_class">org.hibernate.cache.NoCacheProvider</prop>
				<prop key="hibernate.current_session_context_class">org.hibernate.context.JTASessionContext</prop>
				<prop key="hibernate.transaction.manager_lookup_class">org.hibernate.transaction.WeblogicTransactionManagerLookup</prop>
				<prop key="hibernate.connection.autocommit">false</prop>
				<prop key="hibernate.connection.useUnicode">true</prop>
				<prop key="hibernate.connection.characterEncoding">UTF-8</prop>
				<prop key="hibernate.connection.charSet">UTF-8</prop>
			</props>
		</property>
	</bean>

	<!-- ADIT transaction manager -->
	<bean class="org.springframework.transaction.jta.WebLogicJtaTransactionManager" id="transactionManager"/>

	<!--
		Enable the configuration of transactional behavior based on
		annotations
	-->
	<tx:annotation-driven transaction-manager="transactionManager" />
	
</beans>
```

- **sessionFactory** – ADIT andmebaasi ühenduse konfiguratsioon
- **dvkSessionFactory** – DVK andmebaasi ühenduse konfiguratsioon
- **dataSource** – ADIT andmebaasi ühendus
- **mappingLocations** – Andmebaasi objektid, mis kasutavad seda ühendust
- **hibernateProperties** – Hibernate ühenduse kirjeldused
- **hibernate.dialect** –Andmebaasi dialekt mida kasutatakse
- **hibernate.connection.driver_class** –Andmebaasi driver
- **hibernate.cache.provider_class** –Hibernate cache 
- **hibernate.show.sql** – Kas hibernate näitab päringuks loodud SQL-i
- **hibernate.connection.characterEncoding** – Andmebaasi ühenduse kodeering
- **hibernate.connection.charSet** - Andmebaasi ühenduse kodeering

<a name="conf3"></a>
#### Fail adit-jobs.xml

Ajatatud toimingud (DVK-ga suhtlemine ja teavituste saatmine X-Tee teavituskalendrisse) on seadistatud _adit-jobs.xml_ failis:

```xml
<bean id="dvkSendTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerBean">
  <property name="jobDetail" ref="sendJobDetails"/>
  <!-- 10 seconds -->
  <property name="startDelay" value="60000"/>
  <!-- repeat every 20 seconds -->
  <property name="repeatInterval" value="60000"/>
</bean>
	
<bean id="dvkReceiveTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerBean">
  <property name="jobDetail" ref="receiveJobDetails"/>
  <!-- 10 seconds -->
  <property name="startDelay" value="120000"/>
  <!-- repeat every 20 seconds -->
  <property name="repeatInterval" value="60000"/>
</bean>
	
<bean id="dvkUpdateStatusTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerBean">
  <property name="jobDetail" ref="updateStatusJobDetails"/>
  <!-- 10 seconds -->
  <property name="startDelay" value="180000"/>
  <!-- repeat every 20 seconds -->
  <property name="repeatInterval" value="60000"/>
</bean>
	
<bean id="dvkUpdateStatusToDVKTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerBean">
  <property name="jobDetail" ref="UpdateStatusToDVKJobDetails"/>
  <!-- 10 seconds -->
  <property name="startDelay" value="240000"/>
  <!-- repeat every 20 seconds -->
  <property name="repeatInterval" value="60000"/>
</bean>
	
<bean id="DeleteDocumentsFromDVKJobTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerBean">
  <property name="jobDetail" ref="DeleteDocumentsFromDVKJobDetails"/>
  <!-- 10 seconds -->
  <property name="startDelay" value="0"/>
  <!-- repeat every 20 seconds -->
  <property name="repeatInterval" value="3600000"/>
</bean>
	
<bean id="UserSyncJobTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerBean">
  <property name="jobDetail" ref="UserSyncJobDetails"/>
  <!-- 10 seconds -->
  <property name="startDelay" value="0"/>
  <!-- repeat every 20 seconds -->
  <property name="repeatInterval" value="30000"/>
</bean>

<bean id="notificationTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerBean">
  <property name="jobDetail" ref="sendNotificationsJobDetails"/>
  <property name="startDelay" value="120000"/>
  <property name="repeatInterval" value="120000"/>
</bean>

<bean id="temporaryFolderCleanerTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerBean">
  <property name="jobDetail" ref="temporaryFolderCleanerJobDetails"/>
  <!-- First execute 10 seconds after application start -->
  <property name="startDelay" value="10000"/>
  <!-- Repeat every 1 hour -->
  <property name="repeatInterval" value="360000"/>
</bean>
```

- **dvkSendTrigger** – määrab, kui tihti käivitatakse dokumentide DVK-sse saatmise protsess.
- **dvkReceiveTrigger** – määrab, kui tihti käivitatakse DVK-st dokumentide vastuvõtmise protsess
- **dvkUpdateStatusTrigger** – määrab, kui tihti uuendatakse dokumentide staatuseid DVK-st ADIT-isse (DVK-sse saadetud dokumentide puhul)
- **dvkUpdateStatusToDVKTrigger** – määrab, kui tihti uuendatakse dokumentide staatuseid ADIT-ist DVK-sse (ADIT-isse saadetud dokumentide puhul)
- **DeleteDocumentsFromDVKJobTrigger** – määrab, kui tihti käiakse DVK-st kustutamas dokumente, millel on vastavad „lõpetatud“ staatused. Kustutatakse ainult dokumendi DVK XML konteiner.
- **UserSyncJobTrigger** – määrab, kui tihti sünkroniseeritakse DVK kasutajad ADIT kasutajatega (sünkroniseerimine toimub suunal DVK -> ADIT).
- **notificationTrigger** – määrab, kui tihti saadetakse teavitused X-Tee teavituskalendrisse
- **temporaryFolderCleanerTrigger** – määrab, kui tihti kustutatakse töökataloogist ajutised failid

Kõikides eeltoodud seadistustes saab määrata järgmised parameetrid:

- **startDelay** – kui kaua pärast rakenduse käivitamist toimub esimene käivitamine
- **repeatInterval** – kui tihti protsessi käivitatakse

<a name="conf4"></a>
#### Fail log4j.xml

Logimine on ADIT rakenduses lahendatud Log4J raamistikku kasutades, seega toimub ka _log4j.xml_ faili seadistamine vastavalt. Vaikimisi on seadistatud logimine kahte eraldi faili:

1. DVK liides logi

```xml
<appender name="file" class="org.apache.log4j.DailyRollingFileAppender">
  <param name="file" value="log/adit/adit_dvk.log" />
  <param name="datePattern" value="'.'yyyy-MM" />
  <param name="append" value="true" />
  <layout class="org.apache.log4j.PatternLayout">
    <param name="ConversionPattern" value="%d{ABSOLUTE}%5p %C:%L - %m%n"/>
  </layout>
</appender>
```

2. ADIT rakenduse logi

```xml
<appender name="dvk" class="org.apache.log4j.DailyRollingFileAppender">
  <param name="file" value="log/adit/adit.log" />
  <param name="datePattern" value="'.'yyyy-MM" />
  <param name="append" value="true" />
  <layout class="org.apache.log4j.PatternLayout">
    <param name="ConversionPattern" value="%d{ABSOLUTE}%5p %C:%L - %m%n"/>
  </layout>
</appender>
```

_NB! Rakenduse testimise ajaks on mõistlik sisse lülitada logimine tasemel „DEBUG“. Tootmisesse minekul tuleks logida tasemel „INFO“ või „WARN“._

ADIT rakenduse saab seadistada nii rakenduse enda kui ka DVK liidese veateateid logima ka Nagios monitooringusüsteemi. Selleks tuleb lisada _log4j.xml_ faili eraldi Nagiose appender:

```xml
<appender name="nagios" class="org.apache.log4j.nagios.NagiosAppender">
   <param name="Host" value="localhost"/>
   <param name="Port" value="5667"/>

   <param name="ConfigFile" value="nsca_send_clear.cfg"/>

   <param name="ServiceNameDefault" value="ADIT"/>

   <param name="useMDCServiceName" value="false"/>
   <param name="MDCServiceNameKey" value="nagios_service_name"/>

   <param name="useMDCHostName" value="true"/>
   <param name="MDCHostNameKey" value="virtual_host"/>
   <param name="InitializeMDCHostNameValue" value="production"/>
 
   <param name="useShortHostName" value="false"/>
   <param name="MDCCanonicalHostNameKey" value="nagios_canonical_hostname"/>

   <param name="Log4j_Level_WARN"     value="NAGIOS_WARN"/>
   <param name="Log4j_Level_ERROR"    value="NAGIOS_CRITICAL"/>
   <param name="Log4j_Level_FATAL"    value="NAGIOS_CRITICAL"/>

   <param name="SendStartupMessageOK" value="Application Errors Cleared"/>

   <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%X{nagios_canonical_hostname}: %m%n"/>
   </layout>
</appender>
```

Parameetris _„ConfigFile“_ viidatud konfiguratsioonifail _„ncsa_send_clear.cfg“_ sisaldab Nagios andmevahetuse krüpteeringu seadeid.

<a name="conf5"></a>
#### Fail xtee.properties

Kuna ADIT kasutab oma töös X-Tee teenuseid (teavituskalender ja riigiportaal), siis on vajalik ka sellekohane seadistus (_xtee.properties_):

```xml
institution=70006317
security.server=http://turvaserver/cgi-bin/consumer_proxy
database=teavituskalender
method=lisaSyndmus
version=v1
idcode=00000000000
```

Parameetrid:

- **Institution** – Asutuse registrikood. Määrab, millise asutuse nimel ADIT andmekogu teiste X-Tee andmekogude päringuid käivitab.
- **Security.server** – Väljuvate X-Tee päringute teostamiseks kasutatava turvaserveri aadress.
- **Database** – Vaikimisi kasutatava andmekogu nimi. Selle parameetri täitmine pole eriti oluline, kuna iga konkreetse päringu teostamisel valib rakendus ise vajaliku andmekogu.
- **Method** – Vaikimisi kasutatava päringu nimi.Selle parameetri täitmine pole eriti oluline, kuna iga konkreetse päringu teostamisel määrab rakendus ise vajaliku päringu nime ja versiooni.
- **Version** – Vaikimisi kasutatava päringu versioon.Selle parameetri täitmine pole eriti oluline, kuna iga konkreetse päringu teostamisel määrab rakendus ise vajaliku päringu nime ja versiooni.
- **Idcode** – Isikukood. Määrab, millise isiku nimel ADIT andmekogu teiste X-Tee andmekogude päringuid käivitab.

<a name="conf6"></a>
#### Fail jta.properties

```xml
com.atomikos.icatch.service=com.atomikos.icatch.standalone.UserTransactionServiceFactory
com.atomikos.icatch.automatic_resource_registration=true
com.atomikos.icatch.force_shutdown_on_vm_exit = false

# Change the below settings according to your requirements
com.atomikos.icatch.log_base_dir=/tmp
com.atomikos.icatch.console_log_level = DEBUG
```

_NB! Rakenduse testimise ajaks on mõistlik sisse lülitada logimine tasemel „DEBUG“. Tootmisesse minekul tuleks logida tasemel „INFO“ või „WARN“._


<a name="tomcat"></a>
### Rakenduse paigaldamine Tomcat 7.x  rakendusserverisse

NB! Enne rakenduse paigaldamist veendu, et seadistusfailid on muudetud vastavalt paigaldatavale keskkonnale.
Rakenduse paigaldamiseks rakendusserverisse toimi järgnevalt:

1) lisa TOMCAT_HOME/lib kausta vajalikud draiverid ja teegid:
 - PostgreSQL JDBC draiver (soovitavalt [viimane versioon](https://jdbc.postgresql.org/download.html))
 - Atomikos sõltuvused

ADIT lähtekoodi ehitamisel tõmmatakse Maveni tsentraalsest repositooriumist vajalikud Atomikos ja postgreSQL sõltuvused ning paigutatakse need eraldi _adit/adit-war/target/tomcat_lib_ kasuta, millest saab hõlpsalt vajalikud sõltuvused Tomcati kausta liigutada.

2) Lisa TOMCAT_HOME/conf/server.xml faili järmine _listener_ (kohe pärast viimati lisatud _listner_-i):

```xml
<!-- Atomikos -->
<Listener className="com.atomikos.tomcat.AtomikosLifecycleListener" />
```

3) Loo _TOMCAT_HOME/conf/context.xml_ faili JNDI ressurss, mis viitaks ADIT andmebaasile ja DVK UK andmebaasile (lisada `<WatchedResource>` elemendi järele):

```xml
<!-- ADIT main data source -->
<Resource name="jdbc/adit-arendus-postgres"
          uniqueResourceName="AditDataSource"   
          auth="Container"
          
          type="com.atomikos.jdbc.AtomikosDataSourceBean"
          factory="com.atomikos.tomcat.EnhancedTomcatAtomikosBeanFactory"
          minPoolSize="5"
          maxPoolSize="15"
          maxIdleTime="60"
          testQuery="select 1"
          
          xaDataSourceClassName="org.postgresql.xa.PGXADataSource"
          xaProperties.serverName="10.0.13.170" 
          xaProperties.portNumber="5432"
          xaProperties.databaseName="adit"
          xaProperties.user="adit_user"
          xaProperties.password="yyy"
/>

<!-- DVK data source -->
<Resource name="jdbc/adit_dvk-arendus-postgres"
          uniqueResourceName="AditDVKDataSource"
          auth="Container"
          
          type="com.atomikos.jdbc.AtomikosDataSourceBean"
          factory="com.atomikos.tomcat.EnhancedTomcatAtomikosBeanFactory"
          minPoolSize="5"
          maxPoolSize="15"
          maxIdleTime="60"
          testQuery="select 1"
          
          xaDataSourceClassName="org.postgresql.xa.PGXADataSource"
          xaProperties.serverName="10.0.13.170" 
          xaProperties.portNumber="5432"
          xaProperties.databaseName="adit"
          xaProperties.user="adit_dvkuk_user"
          xaProperties.password="yyy"
/>
```

   Keskkonnale vastavaks muuda järgmised parameetrid:

	- **servername** – andmebaasi Host
	- **portNumber** - andmebaasi port
	- **username** – andmebaasikasutaja kasutajanimi
	- **password** – andmebaasikasutaja parool

   NB! Parameeter _“name”_ väärtus peab olema kindlasti sama, mis deklareeritud _adit-servlet.xml_ failis (bean _**aditDataSource**_).

4) Paki paigalduspakett lahti kataloogi _„[TOMCAT_HOME]/webapps/adit“_

5) Tee rakendusserverile taaskäivitus



<a name="weblogic"></a>
### Rakenduse paigaldamine Weblogic 10.x rakendusserverisse

NB! Enne rakenduse paigaldamist veendu, et seadistusfailid on muudetud vastavalt paigaldatavale keskkonnale.

Ettevalmistus:

1.	Tekita 2 Weblogicu serverit ühte klastrisse
2.	Tekita järgmised JNDI andmebaasiühendused (_DataSource_):
   a.	_ADIT_DS_ (jndi name: **jdbc/adit**) – viide ADIT andmebaasiskeemile (kasutaja _ADIT_APP_)
   b.	_ADIT_DVK_CLIENT_DS_ (jndi name: **jdbc/adit_dvk**) – viide ADIT DVK Universaalkliendi andmebaasiskeemile

Sammud paigaldamiseks:

1.	Paigalda rakenduse pakett-fail (_adit.war_) rakendusserveri masina kõvakettale sellisesse kohta, kust rakendusserver sellele ligi pääseb.
2.	Ava rakendusserveri administreerimiskonsool – _http://[HOST]:[PORT]/console_ (nt. _http://10.0.13.36:7001/console_)  ja logi sisse.
3.	Vajuta lingile _„Deployments“_
4.	Avaneb sellesse rakendusserverisse paigaldatud rakenduse loetelu. Uue rakenduse paigaldamiseks vajuta nupule _„Take lock and edit“_ ning seejärel nupule _„Install“_.
5.	Sirvi kataloogi, kus asub punktis 1 nimetatud fail _„adit.war“_ ning märgista see.
6.	Vajuta kataloogipuu all olevale nupule _„Next“_.
7.	Kuvatakse ekraan, kus valitakse rakenduse paigaldamise „stiil“, vali _„Install this deployment as an application“_ ja vajuta _„Next“_.
8.	Kuvatakse seadistuse ekraan, millel peaksid olema määratud järgmised parameetrid:
   a.	Name: rakenduse nimi
   b.	Security: DD only
   c.	Source accessibility: Copy this application onto every target for me
9.	Vajuta nupule _„Finish“_.
10.	Vajuta nupule _„Release configuration / Activate changes“_.
11.	Tee WebLogicule restart.

<a name="DVK"></a>
## DVK liidese seadistamine

Kindlasti tuleb kontrollida, et DVK universaalkliendi andmetabelis _DHL_CLASSIFIER_ olevad DVK staatuste klassifikaatorid vastaksid ADIT-is kasutatavatele, mis asuvad tabelis _DOCUMENT_DVK_STATUS_. Lisaks on vajalik lülitada välja DVK universaalkliendi andmebaasis olev andmebaasipäästik _“TR_DHL_MESSAGE_ID”_.

<a name="notification"></a>
## Teavituskalendri ja riigiportaali X-Tee liidese seadistamine

Teavituskalendri ja riigiportaali liideste töölesaamiseks tuleb teha järgmised seadistused:

1 Määrata teavituskalendris kasutatava ADIT teavituse nimetus (nimetus, mille alusel kasutajad teavituskalendris ADIT teavitusi tellida saavad).
2 Määrata teavituste väljasaatmise intervall.
3 Määrata väljuvate x-tee päringute parameetrid.
4 Tagada, et teavitusportaali ja riigiportaali andmekogude poole pöörduval asutusel (määratud failis _xtee.properties_) oleks järgmiste x-tee päringute käivitamise õigus:
   4.1 _teavituskalender.lisaSyndmus_
   4.2 _riigiportaal.tellimusteStaatus_


Teavituse nimetuse teavituskalendris saab määrata failis **_adit-configuration.xml_**.

Teavituskalendri teavituse nimetuse määramine:

```xml
<!-- Configuration -->
<bean id="configuration" class="ee.adit.util.Configuration">
  <property name="getJoinedMaxResults" value="10"/>
  <property name="tempDir" value="/tmp"/>
  <property name="deleteTemporaryFiles" value="true"/>
  <property name="schedulerEventTypeName" value="Minu dokumentide teavitus"/>
  <property name="dvkResponseMessageStylesheet"     value="DVKresponseMessage.xsl"/>
  <!-- Üldine kettamahu piirang ühe kasutaja kohta baitides -->
  <property name="globalDiskQuota" value="10240000"/>
</bean>
```

Teavituskalendri teenuse nimetuse saad teada teavituskalendrist – tegemist on sama nimetusega, mida kuvatakse teavituste tellimise nimekirjas.

Teavituste väljasaatmise intervalli määramine (failis **_adit-jobs.xml_**):

```xml
<bean id="notificationTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerBean">
  <property name="jobDetail" ref="sendNotificationsJobDetails"/>
  <!-- First send 10 minutes after server start -->
  <property name="startDelay" value="600000"/>
  <!-- Repeat sending every 10 minutes -->
  <property name="repeatInterval" value="600000"/>
</bean>
```

Üldjuhul saadab ADIT andmekogu oma teavitusi välja reaalajas (s.t. vahetult pärast teavitatava sündmuse toimumist). Perioodiliselt saadetakse välja teavitusi, mille reaalajas saatmine mingil põhjusel ebaõnnestus.
Seadete **_startDelay_** ja **_repeatInterval_** väärtused on arvestatud millisekundites.


Väljuvate X-Tee päringute seadistamine failis **_xtee.properties_**

```
institution=70006317
security.server=http://turvaserver/cgi-bin/consumer_proxy
database=teavituskalender
method=lisaSyndmus
version=v1
idcode=00000000000
```

Failis _xtee.properties_ võib ignoreerida seadete database, method ja version väärtusi, kuna need määrab ADIT iga teostatava päringu puhul automaatselt.

<a name="monitoring"></a>
## Monitooringu rakendus ja rakenduse kontroll
Rakenduse esmaseks kontrolliks mine veebilehitsejaga aadressile _http://[SERVER]:[PORT]/adit_ . Siin asuvad viidad järgmistele rakenduse komponentidele:

1.	Rakenduse veebiteenuse aadress (_http://[SERVER]:[PORT]/adit/service_)
2.	Rakenduse veebiteenuse WSDL (_http://[SERVER]:[PORT]/adit/service/adit.wsdl_)
3.	Rakenduse monitooringuleht (_http://[SERVER]:[PORT]/adit/monitor_)

<a name="monitoring-conf"></a>
### Seadistamine

Monitooringurakenduse seadistamiseks on failis **_adit-configuration.xml_** järgmine sektsioon:

```xml
<bean id="monitorConfiguration" class="ee.adit.util.MonitorConfiguration">
  <property name="aditServiceUrl“  value="http://10.0.13.36:7020/adit/service" />
  <property name="remoteApplicationShortName" value="MONITOR_TEST_APP" />
  <property name="userCode" value="EE00000000000" />
  <property name="institutionCode" value="123456789" />
  <property name="testDocumentId" value="999999999999" />
  <property name="dvkTestDocumentID" value="9999999999" />
  <property name="testDocumentFileId" value="999999999999" />
  <property name="testUserCode" value="EE00000000000" />
  <property name="documentSaveInterval" value="60000" />
  <property name="documentSendToDvkInterval" value="60000" />
  <property name="documentSendToAditInterval" value="60000" />
  <property name="notificationSendInterval" value="60000" />
  <property name="errorInterval" value="60000" />
  <property name="errorLevel" value="FATAL" />
</bean>
```

- **aditServiceUrl** – adit teenuse URL. Kui tahetakse testida ka X-tee toimimist, siis võib siia määrata X-tee turvaserveri aadressi.
- **remoteApplicationShortName** – monitooringupäringutes kasutatav välise infosüsteemi lühinimi. Vastava lühinimega infosüsteem peab olema registreeritud ADIT andmebaasis (_REMOTE_APPLICATION_).
- **userCode** – monitooringupäringutes kasutatav kasutajakood, kelle nimel tehakse X-tee päringuid. ADIT rakenduses peab olema registreeritud vastav kasutaja (ADIT_USER).
- **institutionCode** – monitooringupäringutes kasutatav asutuse kood.
- **testDocumentId** – testdokumendi ID. Määratud ID-ga dokumenti kasutatakse monitooringupäringutes _„saveDocument“_ ja _„getDocument“_.
- **dvkTestDocumentID** – DVK Universaalkliendis oleva testdokumendi ID. Kasutatakse selleks, et kontrollida DVK liidest.
- **testDocumentFileId** – monitooringupäringutes kasutatav testdokumendi faili ID.
- **testUserCode** – monitooringupäringus „getUserInfo“ kasutatav kasutajakood – kasutaja, kelle andmeid ADIT päringuga päritakse.
- **documentSaveInterval** – intervall, mille jooksul testitakse dokumendi muutmise päringut (millisekundites)
- **documentSendToDvkInterval** – intervall, mille jooksul testitakse dokumentide saatmist DVK-sse (millisekundites).
- **documentSendToAditInterval** – intervall, mille jooksul testitakse dokumentide vastuvõtmist DVK-st (millisekundites).
- **notificationSendInterval** – intervall, mille jooksul saadetakse teavitusi teavitusteenusele (millisekundites).
- **errorInterval** – intervall, mille jooksul kontrollitakse veateadete logitabelit.
- **errorLevel** – määrab vaadeldavate vigade taseme vigade tabelis. Võimalikud väärtused – WARN, ERROR, FATAL.

<a name="nagios"></a>
### Nagiose seaded

<a name="nagios-log4j"></a>
#### Rakenduse log4j-nagiosappender seadistus

Nagiose NSCA teenus, kuulab mon1a.sise.kit (IP 10.0.5.10 – soovituslik on usaldada DNSi) ning TCP pordil 5667. Suhtluse krüpteerimiseks kasutatakse NSCA konfiguratsioonis 16 = RIJNDAEL-256 algoritmi. Ühtlasi tuleb defineerida ka parool, mida siinkohal ülesse ei märgi.

<a name="nagios-passive"></a>
#### Nagiose seadistus – passiivne monitoring

Nagioses on defineeritud teenuse template, adit-service3, milles määratakse _active_check_enabled_ 0 parameetriga passiivne kontroll.

```
define service{
    name                            adit-service3
    use                             generic-service-template1
    check_period                    24x7
    contact_groups                  adit-admins1
    notifications_enabled         	1
    notification_interval           2880
    notification_period             16x7
    active_checks_enabled       	0
    max_check_attempts         	 	1
    check_freshness                 1
    freshness_threshold            	86400
    check_command                 	return-critical
    register                       	0
}
```

ADIT rakendusserverile seadistatakse teenus, mille nimelist peab edastama _nagios appender_.

```
define service{
    use                             adit-service3
    host_name                       adit.avalik.kit
    service_description             ADIT
    check_command                   return-critical
}
```

<a name="nagios-active"></a>
#### Nagiose seadistus – aktiivne monitooring

Nagioses on defineeritud teenuse template, adit-service2.

```
define service{
    name                             adit-service2
    use                              generic-service-template1
    check_period                     24x7
    contact_groups                   adit-admins1
    notifications_enabled         	 1
    notification_interval            2880
    notification_period              16x7
    register                       	 0
}
```
ADIT rakendusserverile on defineeritud teenused, mida soovitakse kontrollida.

```
define service{
    use                             adit-service2
    host_name                       adit.avalik.kit
    service_description             ADIT_active_check
    check_command                   check_http
}
```
