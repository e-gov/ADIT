<?xml version="1.0" encoding="ISO-8859-1"?>

<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<fo:layout-master-set>
		<fo:simple-page-master master-name="A4"
			page-width="210mm" page-height="297mm" margin-top="1cm"
			margin-bottom="1cm" margin-left="1cm" margin-right="1cm">
			<fo:region-body margin="1cm"/>
			<fo:region-before extent="1cm" />
			<fo:region-after extent="1cm" />
			<fo:region-start extent="1cm" />
			<fo:region-end extent="1cm" />
		</fo:simple-page-master>
	</fo:layout-master-set>

	<fo:page-sequence master-reference="A4">
		<fo:flow flow-name="xsl-region-body">
			<fo:block space-after="20pt" font-weight="bold" text-align="center">ADIT vastuskiri</fo:block>
			<fo:block space-after="20pt" text-align="justify">
				Selle vastuskirja koostas ja saatis
				teile infosüsteem ADIT
				(Avalike Dokumentide Infrastruktuuri Teenus).
			</fo:block>
			<fo:block space-after="20pt" text-align="justify">
				Vastuskirja saatmise põhjus: infosüsteemis ADIT ei ole kasutajat
				<xsl:value-of select="document/recipient_name" />
				(
				<xsl:value-of select="document/recipient_personal_code" />
				) või
				puudub sellel kasutajal aktiivne konto või kasutab antud
				kasutaja dokumentide saatmiseks DVK keskkonda.
			</fo:block>
			<fo:block space-after="20pt">
			
				<fo:block space-after="10pt" font-weight="bold">
					Saadetud dokumendi andmed:
				</fo:block>

				<xsl:template match="/">
					<fo:block>
						Saatja organisatsiooni reg. kood:
						<xsl:value-of select="document/sender_org_code" />
					</fo:block>
					<fo:block>
						Saatja isikukood:
						<xsl:value-of select="document/sender_person_code" />
					</fo:block>
					<fo:block>
						Saatja organisatsiooni nimi:
						<xsl:value-of select="document/sender_org_name" />
					</fo:block>
					<fo:block>
						Saatja nimi:
						<xsl:value-of select="document/sender_name" />
					</fo:block>
					<fo:block>
						Vastuvõtmise aeg:
						<xsl:value-of select="document/receiving_date" />
					</fo:block>
					<fo:block>
						Sõnumi DVK ID:
						<xsl:value-of select="document/dhl_message_id" />
					</fo:block>
					<fo:block>
						Sõnumi GUID:
						<xsl:value-of select="document/guid" />
					</fo:block>
					<fo:block>
						Dokumendi pealkiri:
						<xsl:value-of select="document/title" />
					</fo:block>
					<fo:block>
						Adressaadi nimi:
						<xsl:value-of select="document/recipient_name" />
					</fo:block>
					<fo:block>
						Adressaadi (isiku/registri)kood:
						<xsl:value-of select="document/recipient_personal_code" />
					</fo:block>
				</xsl:template>
				
			</fo:block>
		</fo:flow>
	</fo:page-sequence>

</fo:root>