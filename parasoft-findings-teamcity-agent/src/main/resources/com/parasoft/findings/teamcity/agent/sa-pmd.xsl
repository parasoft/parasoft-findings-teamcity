<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" encoding="UTF-8" indent="yes" />

    <xsl:variable name="isStaticAnalysisResult" select="count(/ResultsSession/CodingStandards) = 1" />

    <xsl:template match="/">
        <xsl:if test="$isStaticAnalysisResult">
            <xsl:element name="pmd">
                <xsl:apply-templates select="/ResultsSession/CodingStandards/StdViols/StdViol[@supp != true()]"/>
                <xsl:apply-templates select="/ResultsSession/CodingStandards/StdViols/StdViol[@supp = true()]"/>
                <xsl:apply-templates select="/ResultsSession/CodingStandards/StdViols/FlowViol[@supp != true()]"/>
                <xsl:apply-templates select="/ResultsSession/CodingStandards/StdViols/FlowViol[@supp = true()]"/>
                <xsl:apply-templates select="/ResultsSession/CodingStandards/StdViols/MetViol[@supp != true()]"/>
                <xsl:apply-templates select="/ResultsSession/CodingStandards/StdViols/MetViol[@supp = true()]"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="StdViol[@supp != true()]">
        <xsl:call-template name="file"/>
    </xsl:template>

    <xsl:template match="FlowViol[@supp != true()]">
        <xsl:call-template name="file"/>
    </xsl:template>

    <xsl:template match="MetViol[@supp != true()]">
        <xsl:call-template name="file"/>
    </xsl:template>

    <xsl:template name="file">
        <xsl:element name="file">
            <xsl:if test="@locFile">
                <xsl:attribute name="name">
                    <xsl:value-of select="@locFile"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:call-template name="violation"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="violation">
        <xsl:element name="violation">
            <xsl:if test="@locStartln">
                <xsl:attribute name="beginline">
                    <xsl:value-of select="@locStartln" />
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@locEndLn"><xsl:attribute name="endline">
                <xsl:value-of select="@locEndLn"/>
            </xsl:attribute></xsl:if>
            <xsl:if test="@locStartPos">
                <xsl:attribute name="begincolumn">
                    <xsl:value-of select="@locStartPos"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@locEndPos">
                <xsl:attribute name="endcolumn">
                    <xsl:value-of select="@locEndPos"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@sev">
                <xsl:attribute name="priority">
                    <xsl:value-of select="@sev"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:attribute name="rule">
                <xsl:value-of select="@rule"/>
            </xsl:attribute>
            <xsl:attribute name="ruleset">
                <xsl:call-template name="getRuleCategoryDesc">
                    <xsl:with-param name="ruleId" select="@rule"/>
                </xsl:call-template>
            </xsl:attribute>
            <xsl:if test="@pkg">
                <xsl:attribute name="package">
                    <xsl:value-of select="@pkg"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="@msg"/>
            <xsl:apply-templates select="ElDescList/ElDesc"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="StdViol[@supp = true()]">
        <xsl:call-template name="suppressedviolation"/>
    </xsl:template>

    <xsl:template match="FlowViol[@supp = true()]">
        <xsl:call-template name="suppressedviolation"/>
    </xsl:template>

    <xsl:template match="MetViol[@supp = true()]">
        <xsl:call-template name="suppressedviolation"/>
    </xsl:template>

    <xsl:template name="suppressedviolation">
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

    <xsl:template match="ElDesc">
        <xsl:param name="indent"/>
        <xsl:value-of select="concat('&#xa;        ', $indent, '- ')"/>
        <xsl:apply-templates select="Anns/Ann" mode="annotation">
            <xsl:with-param name="annIndent" select="$indent"/>
        </xsl:apply-templates>
        <xsl:call-template name="srcRngFilename">
            <xsl:with-param name="string" select="translate(@srcRngFile,'\','/')"/>
            <xsl:with-param name="delimiter" select="'/'"/>
        </xsl:call-template>
        <xsl:value-of select="concat(':', @ln, '   ', @desc)"/>
        <xsl:apply-templates select="Anns/Ann" mode="annotationDetail"/>
        <xsl:apply-templates select="ElDescList/ElDesc">
            <xsl:with-param name="indent" select="concat('   ', $indent)"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template name="srcRngFilename">
        <xsl:param name="string" />
        <xsl:param name="delimiter" />
        <xsl:choose>
            <xsl:when test="contains($string, $delimiter)">
                <xsl:call-template name="srcRngFilename">
                    <xsl:with-param name="string" select="substring-after($string, $delimiter)" />
                    <xsl:with-param name="delimiter" select="$delimiter" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="Ann" mode="annotation">
        <xsl:param name="annIndent"/>
        <xsl:if test="@kind = 'cause' or @kind = 'point'">
            <xsl:value-of select="concat(@msg, '&#xa;           ', $annIndent)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Ann" mode="annotationDetail">
        <xsl:if test="@kind = 'valEval'">
            <xsl:value-of select="concat('  *** ', @msg)"/>
        </xsl:if>
    </xsl:template>

    <xsl:key name="ruleById" match="Rule" use="@id" />
    <xsl:key name="categoryByName" match="Category" use="@name" />
    <xsl:template name="getRuleCategoryDesc">
        <xsl:param name="ruleId" />
        <xsl:variable name="matchingRule" select="key('ruleById', $ruleId)" />
        <xsl:variable name="matchingCategory" select="key('categoryByName', $matchingRule/@cat)" />
        <xsl:if test="$matchingCategory/@desc">
            <xsl:value-of select="$matchingCategory/@desc"/>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>