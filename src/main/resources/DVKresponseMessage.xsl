<?xml version="1.0" encoding="ISO-8859-1"?>

<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<fo:layout-master-set>
		<fo:simple-page-master master-name="A4">
			<fo:region-body />
		</fo:simple-page-master>
	</fo:layout-master-set>

	<fo:page-sequence master-reference="A4">
		<fo:flow flow-name="xsl-region-body">
			<fo:block>ADIT vastuskiri</fo:block>
			<fo:block>
				<xsl:template match="/">
					Saatja organisatsiooni reg. kood: <xsl:value-of select="sender_org_code"/>
					Saatja isikukood: <xsl:value-of select="sender_person_code"/>
					Saatja organisatsiooni nimi: <xsl:value-of select="sender_org_name"/>
					Saatja nimi: <xsl:value-of select="sender_name"/>
					Vastuvõtmise aeg: <xsl:value-of select="receiving_date"/>
					Sõnumi DVK ID: <xsl:value-of select="dhl_message_id"/>
					Sõnumi GUID: <xsl:value-of select="guid"/>
					Dokumendi pealkiri: <xsl:value-of select="title"/>
				</xsl:template>
			</fo:block>
		</fo:flow>
	</fo:page-sequence>

</fo:root>