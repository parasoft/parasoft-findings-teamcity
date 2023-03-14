<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

    <xsl:variable name="isStaticAnalysisResult" select="count(/ResultsSession/CodingStandards) = 1" />

    <xsl:template match="/" >
        <xsl:if test="$isStaticAnalysisResult">
            <xsl:element name="pmd-cpd">
                <xsl:apply-templates select="/ResultsSession/CodingStandards/StdViols/DupViol[@supp != true()]"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="DupViol[@supp != true()]">
        <xsl:element name="duplication">
            <xsl:if test="@locEndLn and @locStartln">
                <xsl:attribute name="lines">
                    <xsl:value-of select="@locEndLn - @locStartln"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:attribute name="tokens">-1</xsl:attribute>
            <xsl:apply-templates select="ElDescList/ElDesc"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="ElDesc">
        <xsl:element name="file">
            <xsl:if test="@srcRngStartPos">
                <xsl:attribute name="column">
                    <xsl:value-of select="@srcRngStartPos"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@srcRngEndPos">
                <xsl:attribute name="endcolumn">
                    <xsl:value-of select="@srcRngEndPos"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@srcRngEndLn">
                <xsl:attribute name="endline">
                    <xsl:value-of select="@srcRngEndLn"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@srcRngStartln">
                <xsl:attribute name="line">
                    <xsl:value-of select="@srcRngStartln"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@srcRngFile">
                <xsl:variable name="projectName" select="/ResultsSession/CodingStandards/Projects/Project/@name"/>
                <xsl:attribute name="path">
                    <xsl:if test="../../@lang = 'java'">
                        <xsl:value-of select="substring-after(@srcRngFile, /ResultsSession/@prjModule)"/>
                    </xsl:if>
                    <xsl:if test="../../@lang = 'cpp' and /ResultsSession/@prjModule">
                        <xsl:value-of select="substring-after(@srcRngFile, $projectName)"/>
                    </xsl:if>
                    <xsl:if test="../../@lang = 'cpp' and not(/ResultsSession/@prjModule)">
                        <xsl:value-of select="substring-after(@srcRngFile, concat($projectName,'/', $projectName))"/>
                    </xsl:if>
                    <xsl:if test="../../@lang = 'dotnet'">
                        <xsl:value-of select="substring-after(@srcRngFile, /ResultsSession/@project)"/>
                    </xsl:if>
                </xsl:attribute>
            </xsl:if>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>