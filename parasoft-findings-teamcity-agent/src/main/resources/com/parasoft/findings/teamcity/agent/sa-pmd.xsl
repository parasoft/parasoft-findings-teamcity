<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://pmd.sourceforge.net/report/2.0.0" >
    <xsl:output method="xml" encoding="UTF-8" indent="yes" />

    <xsl:variable name="newLine" select="'&#xA;'" />
    <xsl:variable name="toolName" select="/ResultsSession/@toolName"/>
    <xsl:variable name="rules" select="/ResultsSession/CodingStandards/Rules/RulesList/Rule"/>
    <xsl:variable name="categories" select="/ResultsSession/CodingStandards/Rules/CategoriesList//Category"/>
    <xsl:variable name="isStaticAnalysisResult" select="count(/ResultsSession/CodingStandards) = 1" />

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$isStaticAnalysisResult">
                <xsl:element name="pmd">
                    <xsl:call-template name="transformStdViol"/>
                    <xsl:call-template name="transformSuppressedStdViols"/>
                    <xsl:call-template name="transformFlowViol"/>
                    <xsl:call-template name="transformSuppressedFlowViols"/>
                    <xsl:call-template name="transformMetViol"/>
                    <xsl:call-template name="transformSuppressedMetViols"/>
                </xsl:element>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="transformStdViol">
        <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/StdViol">
            <xsl:if test="@supp != true()">
                <xsl:call-template name="setFile"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="transformFlowViol">
        <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/FlowViol">
            <xsl:if test="@supp != true()">
                <xsl:call-template name="setFile"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="transformMetViol">
        <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/MetViol">
            <xsl:if test="@supp != true()">
                <xsl:call-template name="setFile"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="setFile">
        <xsl:element name="file">
            <xsl:if test="@locFile">
                <xsl:attribute name="name"><xsl:value-of select="@locFile"/></xsl:attribute>
            </xsl:if>
            <xsl:element name="violation">
                <xsl:if test="@locStartln"><xsl:attribute name="beginline"><xsl:value-of select="@locStartln" /></xsl:attribute></xsl:if>
                <xsl:if test="@locEndLn"><xsl:attribute name="endline"><xsl:value-of select="@locEndLn"/></xsl:attribute></xsl:if>
                <xsl:if test="@locStartPos"><xsl:attribute name="begincolumn"><xsl:value-of select="@locStartPos"/></xsl:attribute></xsl:if>
                <xsl:if test="@locEndPos"><xsl:attribute name="endcolumn"><xsl:value-of select="@locEndPos"/></xsl:attribute></xsl:if>
                <xsl:if test="@sev"><xsl:attribute name="priority"><xsl:value-of select="@sev"/></xsl:attribute></xsl:if>
                <xsl:attribute name="rule">
                    <xsl:value-of select="@rule"/>
                </xsl:attribute>
                <xsl:attribute name="ruleset">
                    <xsl:call-template name="getRuleCategoryDesc">
                        <xsl:with-param name="ruleId" select="@rule"/>
                    </xsl:call-template>
                </xsl:attribute>
                <xsl:if test="@pkg"><xsl:attribute name="package"><xsl:value-of select="@pkg"/></xsl:attribute></xsl:if>
                <xsl:value-of select="@msg"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template name="transformSuppressedStdViols">
        <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/StdViol">
            <xsl:choose>
                <xsl:when test="@supp = true()">
                    <xsl:call-template name="setSuppressedviolation"/>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="transformSuppressedFlowViols">
        <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/FlowViol">
            <xsl:choose>
                <xsl:when test="@supp = true()">
                    <xsl:call-template name="setSuppressedviolation"/>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="transformSuppressedMetViols">
        <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/MetViol">
            <xsl:choose>
                <xsl:when test="@supp = true()">
                    <xsl:call-template name="setSuppressedviolation"/>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="setSuppressedviolation">
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
    </xsl:template>

    <xsl:template name="getRuleCategoryDesc">
        <xsl:param name="ruleId"/>
        <xsl:variable name="matchingRule" select="$rules[@id = $ruleId]"/>
        <xsl:variable name="matchingCategory" select="$categories[@name = $matchingRule/@cat]"/>
        <xsl:choose>
            <xsl:when test="$matchingCategory/@desc"><xsl:value-of select="$matchingCategory/@desc"/></xsl:when>
            <xsl:otherwise>Others</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>