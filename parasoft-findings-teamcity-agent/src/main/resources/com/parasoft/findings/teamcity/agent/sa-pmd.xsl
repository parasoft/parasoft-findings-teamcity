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
                    <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/StdViol">
                        <xsl:call-template name="setFile"/>
                    </xsl:for-each>

                    <xsl:call-template name="transformSuppressedViols"/>

                    <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/FlowViol">
                        <xsl:call-template name="setFile"/>
                    </xsl:for-each>

                    <xsl:for-each select="/ResultsSession/CodingStandards/StdViols/MetViol">
                        <xsl:call-template name="setFile"/>
                    </xsl:for-each>


                </xsl:element>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="setFile">
        <xsl:if test="@supp != true()">
            <xsl:element name="file">
                <xsl:if test="@locFile">
                    <xsl:attribute name="name"><xsl:value-of select="@locFile"/></xsl:attribute>
                </xsl:if>
                <xsl:call-template name="setViolations"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template name="setViolations">
        <xsl:variable name="ElDesc" select="ElDescList/ElDesc"/>
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
            <xsl:choose>
                <xsl:when test="$ElDesc">
                    <xsl:call-template name="setFlowMessages">
                        <xsl:with-param name="ElDesc" select="$ElDesc"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@msg"/>
                </xsl:otherwise>
            </xsl:choose>
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

    <xsl:template name="getRuleCategoryDesc">
        <xsl:param name="ruleId"/>
        <xsl:variable name="matchingRule" select="$rules[@id = $ruleId]"/>
        <xsl:variable name="matchingCategory" select="$categories[@name = $matchingRule/@cat]"/>
        <xsl:choose>
            <xsl:when test="$matchingCategory/@desc"><xsl:value-of select="$matchingCategory/@desc"/></xsl:when>
            <xsl:otherwise>Others</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="setFlowMessages">
        <xsl:param name="ElDesc"/>
        <xsl:for-each select="$ElDesc">
            <xsl:value-of select="$newLine"/>
            <xsl:call-template name="getSrcRngFile">
                <xsl:with-param name="srcRngFile" select="@srcRngFile"/>
            </xsl:call-template>
            <xsl:variable name="message" select="concat(':', @ln, '***', @desc)"/>
            <xsl:value-of select="$message"/>
            <xsl:variable name="anns" select="Anns/Ann"/>
            <xsl:for-each select="$anns">
                <xsl:value-of select="concat('***', @msg)"/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="getSrcRngFile">
        <xsl:param name="srcRngFile"/>
        <xsl:variable name="newsrcRngFile" select="substring-after($srcRngFile,'/')"/>
        <xsl:choose>
            <xsl:when test="contains($newsrcRngFile,'/')">
                <xsl:call-template name="getSrcRngFile">
                    <xsl:with-param name="srcRngFile" select="$newsrcRngFile" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$newsrcRngFile"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>