<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dossier="http://www.charvolant.org/dossier#">
  <xsl:output method="text" encoding="UTF-8"/>
  
  <xsl:template match="/">
digraph "index" {
  <xsl:apply-templates select="dossier:ontologies"/>
}
  </xsl:template>
   
  <xsl:template match="dossier:ontologies">
    <xsl:apply-templates select="dossier:ontology"/>
  </xsl:template>
  
  <xsl:template match="dossier:ontology">
    <xsl:variable name="import-style" select="dossier:style[@family='graphviz-edge-import']"/>
    "<xsl:value-of select="@id"/>" <xsl:call-template name="format-attributes-inline"><xsl:with-param name="family" select="'graphviz-node'"/></xsl:call-template>;
    <xsl:for-each select="dossier:imports">
    <xsl:choose>
      <xsl:when test="$import-style/@reverseHierarchy='true'">
    "<xsl:value-of select="@ref"/>" -&gt; "<xsl:value-of select="../@id"/>" <xsl:call-template name="format-attribute-list-inline"><xsl:with-param name="style" select="$import-style"/><xsl:with-param name="label" select="''"/><xsl:with-param name="tooltip" select="''"/></xsl:call-template>;
      </xsl:when>
      <xsl:otherwise>
    "<xsl:value-of select="../@id"/>" -&gt; "<xsl:value-of select="@ref"/>" <xsl:call-template name="format-attribute-list-inline"><xsl:with-param name="style" select="$import-style"/><xsl:with-param name="label" select="''"/><xsl:with-param name="tooltip" select="''"/></xsl:call-template>;
      </xsl:otherwise>
    </xsl:choose>
    </xsl:for-each>
  </xsl:template>
       
  <xsl:template name="format-name">
  <xsl:param name="source" select="."/>
  <xsl:choose>
  <xsl:when test="$source/dossier:label">
  <xsl:value-of select="$source/dossier:label[1]"/>
  </xsl:when>
  <xsl:when test="$source/@name">
  <xsl:value-of select="$source/@name"/>
  </xsl:when>
  <xsl:when test="$source/@uri">
  <xsl:value-of select="$source/@uri"/>
  </xsl:when>
  <xsl:otherwise>
  anonymous
  </xsl:otherwise>
  </xsl:choose>
  </xsl:template>  
  
          
  <xsl:template name="format-attribute-list-inline">
  <xsl:param name="style" select="."/>
  <xsl:param name="label"/>
  <xsl:param name="url"/>
  <xsl:param name="tooltip"/>
  [<!-- 
 -->label="<xsl:value-of select="$label"/>"<!-- 
 -->,tooltip="<xsl:value-of select="$tooltip"/>"<!-- 
 --><xsl:if test="$url">,URL="<xsl:value-of select="$url"/>",target="_top"</xsl:if><!-- 
 --><xsl:if test="$style/@reverseHierarchy='true'">,dir="back"</xsl:if><!-- 
 --><xsl:for-each select="$style/dossier:attribute"><!-- 
   -->,<xsl:value-of select="@style"/>="<xsl:value-of select="@value"/>"<!-- 
 --></xsl:for-each>]
  </xsl:template>  
        
  <xsl:template name="format-attributes-inline">
  <xsl:param name="source" select="."/>
  <xsl:param name="label" select="$source/@name"/>
  <xsl:param name="url" select="$source/@href"/>
  <xsl:param name="tooltip"><xsl:call-template name="format-name"/></xsl:param>
  <xsl:param name="family"/>
  <xsl:variable name="style" select="$source/dossier:style[@family=$family]"/>
  <xsl:for-each select="$style">
    <xsl:call-template name="format-attribute-list-inline">
      <xsl:with-param name="label"><xsl:choose><xsl:when test="@showLabel='false'"></xsl:when><xsl:otherwise><xsl:value-of select="$label"/></xsl:otherwise></xsl:choose></xsl:with-param>
      <xsl:with-param name="tooltip" select="$tooltip"/>
      <xsl:with-param name="url" select="$url"/>
    </xsl:call-template>
  </xsl:for-each>
  </xsl:template>  
  
 </xsl:stylesheet>