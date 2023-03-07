<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

    <xsl:variable name="isStaticAnalysisResult" select="count(/ResultsSession/CodingStandards) = 1" />

    <xsl:template match="/" >
        <xsl:choose>
            <xsl:when test="$isStaticAnalysisResult">
                <xsl:element name="pmd-cpd">
                    <xsl:call-template name="transformDupViol"/>
                </xsl:element>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="transformDupViol">
        <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/DupViol">
            <xsl:if test="not(@supp = 'true')">
                <xsl:element name="duplication">
                    <xsl:attribute name="lines">
                        <xsl:value-of select="@locEndLn - @locStartln + 1"/>
                    </xsl:attribute>
                    <xsl:apply-templates select="ElDescList/ElDesc"/>
                </xsl:element>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="ElDesc">
        <xsl:element name="file">
            <xsl:attribute name="column">
                <xsl:value-of select="@srcRngStartPos"/>
            </xsl:attribute>
            <xsl:attribute name="endcolumn">
                <xsl:value-of select="@srcRngEndPos"/>
            </xsl:attribute>
            <xsl:attribute name="endline">
                <xsl:value-of select="@srcRngEndLn"/>
            </xsl:attribute>
            <xsl:attribute name="line">
                <xsl:value-of select="@srcRngStartln"/>
            </xsl:attribute>
            <xsl:attribute name="path">
                <xsl:value-of select="@srcRngFile"/>
            </xsl:attribute>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>