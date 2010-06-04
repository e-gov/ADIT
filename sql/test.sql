'&ADIT_SCHEMA' Enter ADIT SQL Schema name: 

CREATE TABLE &&ADIT_SCHEMA..ACCESS_RESTRICTION
(
	remote_application  VARCHAR2(50) NOT NULL,    /* Viide välisele infosüsteemile (infosüsteemi lühinimi). */
	user_code           VARCHAR2(50) NOT NULL,    /* Viide kasutajale, kelle puhul antud piirang rakendub. */
	restriction         VARCHAR2(50)    /* Määrab ära piirangu tüübi. "WRITE" - kasutaja andmete muutmise piirang, "READ" - täielik piirang (infosüsteemil puudub õigus kasutaja andmete lugemiseks, samuti ka muutmiseks). */
);