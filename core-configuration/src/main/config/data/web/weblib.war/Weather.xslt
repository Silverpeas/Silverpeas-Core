<?xml version="1.0" encoding="ISO-8859-1"?>
	<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:dc="http://purl.org/dc/elements/1.1/">
	<xsl:output method="html" version="4" encoding="iso-8859-1" indent="yes" />
	<xsl:template match="Location[@Name='Genève']">

	<xsl:call-template name="Days" />
	</xsl:template>

	<xsl:template match="Day" name="Days">
		<table><tr>
		<xsl:for-each select="Day">
			<xsl:if test="position() &lt; 4">
			<td align="center">
			<xsl:call-template name="affJourExt">
				<xsl:with-param name="jour"><xsl:value-of select="substring(@val,7,2)" /></xsl:with-param>
				<xsl:with-param name="mois"><xsl:value-of select="substring(@val,5,2)" /></xsl:with-param>
				<xsl:with-param name="annee"><xsl:value-of select="substring(@val,0,4)" /></xsl:with-param>
			</xsl:call-template><br/><xsl:value-of select="TempAft" />°
			</td>
			<td>
			<img>
				<xsl:attribute name="src">/weblib/meteo/picto/<xsl:value-of select="Symb"/>.gif</xsl:attribute>
			</img>
			</td>
			</xsl:if>
		</xsl:for-each>
		</tr></table>
	</xsl:template>

	<xsl:template name="affJourExt">
        <xsl:param name="jour"/>
        <xsl:param name="mois"/>
        <xsl:param name="annee"/>
        <xsl:variable name="S">
                <xsl:value-of select="floor($annee div 100)" />
        </xsl:variable>
        <xsl:variable name="A">
                <xsl:value-of select="$annee mod 100" />
        </xsl:variable>
        <xsl:variable name="C">
                <xsl:choose>
                        <xsl:when test="($jour &lt; 9) and ($mois &lt; 12) and ($annee &lt; 1582)">
                                <xsl:value-of select="(6 * $S + 4) mod 7" />
                        </xsl:when>
                        <xsl:otherwise>
                                <xsl:value-of select="(5 * $S + floor($S div 4) -1) mod 7" />
                        </xsl:otherwise>
                </xsl:choose>
        </xsl:variable>
        <xsl:variable name="Y">
                <xsl:value-of select="($A + floor($A div 4)) mod 7" />
        </xsl:variable>
        <xsl:variable name="B">
                <xsl:choose>
                        <xsl:when test="($annee mod 400) = 0">1</xsl:when>
                        <xsl:when test="($annee mod 100) = 0">0</xsl:when>
                        <xsl:when test="($annee mod 4) = 0">1</xsl:when>
                        <xsl:otherwise>0</xsl:otherwise>
                </xsl:choose>
        </xsl:variable>
        <xsl:variable name="M">
                <xsl:choose>
                        <xsl:when test="($mois = 1) and ($B = 0)">0</xsl:when>
                        <xsl:when test="($mois = 1) and ($B = 1)">6</xsl:when>
                        <xsl:when test="($mois = 2) and ($B = 0)">3</xsl:when>
                        <xsl:when test="($mois = 2) and ($B = 1)">2</xsl:when>
                        <xsl:when test="$mois = 3">3</xsl:when>
                        <xsl:when test="$mois = 4">6</xsl:when>
                        <xsl:when test="$mois = 5">1</xsl:when>
                        <xsl:when test="$mois = 6">4</xsl:when>
                        <xsl:when test="$mois = 7">6</xsl:when>
                        <xsl:when test="$mois = 8">2</xsl:when>
                        <xsl:when test="$mois = 9">5</xsl:when>
                        <xsl:when test="$mois = 10">0</xsl:when>
                        <xsl:when test="$mois = 11">3</xsl:when>
                        <xsl:when test="$mois = 12">5</xsl:when>
                        <xsl:otherwise>ERREUR</xsl:otherwise>
                </xsl:choose>
        </xsl:variable>
        <xsl:variable name="Q">
                <xsl:value-of select="$jour mod 7" />
        </xsl:variable>
        <xsl:variable name="J">
                <xsl:value-of select="($C +$Y +$Q +$M) mod 7" />
        </xsl:variable>
        <xsl:choose>
                <xsl:when test="$J = 0"><xsl:text>dimanche</xsl:text></xsl:when>
                <xsl:when test="$J = 1"><xsl:text>lundi</xsl:text></xsl:when>
                <xsl:when test="$J = 2"><xsl:text>mardi</xsl:text></xsl:when>
                <xsl:when test="$J = 3"><xsl:text>mercredi</xsl:text></xsl:when>
                <xsl:when test="$J = 4"><xsl:text>jeudi</xsl:text></xsl:when>
                <xsl:when test="$J = 5"><xsl:text>vendredi</xsl:text></xsl:when>
                <xsl:when test="$J = 6"><xsl:text>samedi</xsl:text></xsl:when>
                <xsl:otherwise><xsl:text>inconnu</xsl:text></xsl:otherwise>
        </xsl:choose>
</xsl:template>
</xsl:stylesheet>