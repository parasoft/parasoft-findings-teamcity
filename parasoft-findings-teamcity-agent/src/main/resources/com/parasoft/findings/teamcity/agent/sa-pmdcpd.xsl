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
                            <!-- For cppTest professional report, "prjModule" attribute is not present.
                                Project name presents twice in the full source path and needs to be removed. -->
                            <xsl:call-template name="handlePathForCppTestPro">
                                <xsl:with-param name="srcRngFile" select="@srcRngFile"/>
                                <xsl:with-param name="projects" select="/ResultsSession/CodingStandards/Projects"/>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:when test="/ResultsSession/@toolId = 'dottest'">
                            <!-- For DotTest report, project name prefix is missing in "resProjPath" of "Loc".
                                As a result, the root folder will be removed from full source path. -->
                            <xsl:value-of select="substring-after(@srcRngFile, '/')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- For Jtest and cppTest standard reports, use "resProjPath" in "Loc". -->
                            <xsl:apply-templates select="/ResultsSession/Scope/Locations/Loc">
                                <xsl:with-param name="elDescLocRef" select="@locRef"/>
                                <xsl:with-param name="srcRngFile" select="@srcRngFile"/>
                            </xsl:apply-templates>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template name="handlePathForCppTestPro">
        <xsl:param name="srcRngFile"/>
        <xsl:param name="projects"/>
        <xsl:for-each select="$projects/Project">
            <xsl:variable name="pathPrefix" select="concat('/', @name, '/' , @name, '/')"/>
            <xsl:if test="starts-with($srcRngFile, $pathPrefix)">
                <xsl:value-of select="substring-after($srcRngFile, $pathPrefix)"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="Loc">
        <xsl:param name="elDescLocRef"/>
        <xsl:param name="srcRngFile"/>
        <xsl:if test="$elDescLocRef = @locRef">
            <xsl:choose>
                <xsl:when test="@resProjPath">
                    <xsl:value-of select="@resProjPath"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- Reports structure for Jtest, dotTest, cppTest and cppTest professional are not strictly consistent.
                            Full source path will be displayed if no rules can be applied. -->
                    <xsl:value-of select="$srcRngFile"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>