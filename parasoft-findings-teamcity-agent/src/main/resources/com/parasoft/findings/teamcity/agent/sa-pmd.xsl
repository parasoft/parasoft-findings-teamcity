<?xml version="1.0"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://pmd.sourceforge.net/report/2.0.0" >
    <xsl:output method="xml" encoding="UTF-8" indent="yes" />

    <xsl:variable name="toolName" select="/ResultsSession/@toolName"/>
    <xsl:variable name="rules" select="/ResultsSession/CodingStandards/Rules/RulesList/Rule"/>
    <xsl:variable name="categories" select="/ResultsSession/CodingStandards/Rules/CategoriesList//Category"/>

    <xsl:template match="/ResultsSession">
        <xsl:element name="pmd">
            <xsl:call-template name="transformStdViols"/>
            <xsl:call-template name="transformSuppressedViols"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="transformStdViols">
        <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/StdViol">
            <xsl:choose>
                <xsl:when test="@supp != true()">
                    <xsl:element name="file">
                        <xsl:attribute name="name">
                            <xsl:value-of select="@locFile"/>
                        </xsl:attribute>
                        <xsl:call-template name="StdViol"/>
                    </xsl:element>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="StdViol">
        <xsl:element name="violation">
            <xsl:attribute name="beginline"><xsl:value-of select="@locStartln" /></xsl:attribute>
            <xsl:attribute name="endline"><xsl:value-of select="@locEndLn"/></xsl:attribute>
            <xsl:attribute name="begincolumn"><xsl:value-of select="@locStartPos"/></xsl:attribute>
            <xsl:attribute name="endcolumn"><xsl:value-of select="@locEndPos"/></xsl:attribute>
            <xsl:attribute name="priority"><xsl:value-of select="@sev"/></xsl:attribute>
            <xsl:attribute name="rule">
                <xsl:value-of select="@rule"/>
            </xsl:attribute>
            <xsl:attribute name="ruleset">
                <xsl:call-template name="getRuleCategoryDesc">
                    <xsl:with-param name="ruleId" select="@rule"/>
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="package"><xsl:value-of select="@pkg"/></xsl:attribute>
            <xsl:value-of select="@msg"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="transformSuppressedViols">
        <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/StdViol">
            <xsl:choose>
                <xsl:when test="@supp = true()">
                    <xsl:element name="suppressedviolation">
                        <xsl:attribute name="filename">
                            <xsl:value-of select="@locFile"/>
                        </xsl:attribute>
                        <xsl:attribute name="suppressiontype">
                            <xsl:call-template name="getRuleCategoryDesc">
                                <xsl:with-param name="ruleId" select="@rule"/>
                            </xsl:call-template>
                        </xsl:attribute>
                        <xsl:attribute name="msg">
                            <xsl:value-of select="@msg"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!-- TODO: With performance bottleneck, need optimization. -->
    <xsl:template name="getRuleCategoryDesc">
        <xsl:param name="ruleId"/>
        <xsl:value-of select="$categories[@name = $rules[@id = $ruleId]/@cat]/@desc"/>
    </xsl:template>
</xsl:stylesheet>