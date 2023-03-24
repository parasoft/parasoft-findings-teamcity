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
                <xsl:attribute name="path">
                    <xsl:choose>
                        <xsl:when test="not(/ResultsSession/@prjModule)">
                            <xsl:apply-templates select="/ResultsSession/CodingStandards/Projects/Project">
                                <xsl:with-param name="srcRngFile" select="@srcRngFile"/>
                            </xsl:apply-templates>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates select="/ResultsSession/Scope/Locations/Loc">
                                <xsl:with-param name="projectLocRef" select="@locRef"/>
                                <xsl:with-param name="srcRngFile" select="@srcRngFile"/>
                            </xsl:apply-templates>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="Project">
        <xsl:param name="srcRngFile"/>
        <xsl:param name="resProjPath"/>
        <xsl:if test="$srcRngFile and contains($srcRngFile, concat(@name,'/', @name))">
            <xsl:value-of select="substring-after($srcRngFile, concat(@name,'/', @name))"/>
        </xsl:if>
        <xsl:if test="$resProjPath">
            <xsl:value-of select="concat('/', @name, '/', $resProjPath)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Loc">
        <xsl:param name="projectLocRef"/>
        <xsl:param name="srcRngFile"/>
        <xsl:if test="$projectLocRef = @locRef">
            <xsl:choose>
                <xsl:when test="@resProjPath">
                    <xsl:if test="/ResultsSession/@toolId = 'dottest'">
                        <xsl:apply-templates select="/ResultsSession/CodingStandards/Projects/Project">
                            <xsl:with-param name="resProjPath" select="@resProjPath"/>
                        </xsl:apply-templates>
                    </xsl:if>
                    <xsl:if test="/ResultsSession/@toolId != 'dottest'">
                        <xsl:value-of select="concat('/', @resProjPath)"/>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$srcRngFile"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>