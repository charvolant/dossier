<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dossier="http://www.charvolant.org/dossier#">
  <xsl:output method="text" encoding="UTF-8"/>
  <xsl:param name="label.any" select="'any'"/>
  
  <xsl:template match="/">
digraph "<xsl:call-template name="format-name"><xsl:with-param name="source" select="dossier:ontology[1]"/></xsl:call-template>" {
  ranksep="1.0 equally";
  rankdir="LR";
  concentrate=true;

  <xsl:apply-templates select="dossier:ontology"/>
  <xsl:apply-templates select="dossier:ontology/dossier:ontology"/>  
}
  </xsl:template>
  
  <xsl:template match="dossier:ontology">
  <xsl:choose>
  <xsl:when test="parent::dossier:ontology">
  subgraph cluster_<xsl:value-of select="position()"/> {
   </xsl:when>
  <xsl:otherwise>
  node <xsl:call-template name="format-attributes-inline"><xsl:with-param name="family" select="'graphviz-node'"/></xsl:call-template>;
  edge <xsl:call-template name="format-attributes-inline"><xsl:with-param name="family" select="'graphviz-edge'"/></xsl:call-template>;

  subgraph main {
  </xsl:otherwise>
  </xsl:choose>
   <xsl:call-template name="format-attributes-statements"><xsl:with-param name="tooltip" select="@uri"/><xsl:with-param name="family" select="'graphviz-graph'"/></xsl:call-template>
  <xsl:apply-templates select="dossier:class"/>
  <xsl:apply-templates select="dossier:property"/>
  }
  </xsl:template>
  
  <xsl:template match="dossier:class">
    <xsl:variable name="super-style" select="/dossier:ontology/dossier:style[@family='graphviz-edge-super-class']"/>
    "<xsl:value-of select="@id"/>" <xsl:call-template name="format-attributes-inline"><xsl:with-param name="family" select="'graphviz-node'"/></xsl:call-template>;
    <xsl:for-each select="dossier:sub-class-of">
    <xsl:choose>
      <xsl:when test="$super-style/@reverseHierarchy='true'">
    "<xsl:value-of select="@ref"/>" -&gt; "<xsl:value-of select="../@id"/>" <xsl:call-template name="format-attribute-list-inline"><xsl:with-param name="style" select="$super-style"/><xsl:with-param name="label" select="''"/><xsl:with-param name="tooltip" select="''"/></xsl:call-template>;
      </xsl:when>
      <xsl:otherwise>
    "<xsl:value-of select="../@id"/>" -&gt; "<xsl:value-of select="@ref"/>" <xsl:call-template name="format-attribute-list-inline"><xsl:with-param name="style" select="$super-style"/><xsl:with-param name="label" select="''"/><xsl:with-param name="tooltip" select="''"/></xsl:call-template>;
      </xsl:otherwise>
    </xsl:choose>
    </xsl:for-each>
  </xsl:template>
 
  <xsl:template match="dossier:property">
    <xsl:variable name="super-style" select="/dossier:ontology/dossier:style[@family='graphviz-edge-super-property']"/>
    <xsl:variable name="anonymous-style" select="/dossier:ontology/dossier:style[@family='graphviz-node-anonymous']"/>
    "<xsl:value-of select="@id"/>"  <xsl:call-template name="format-attributes-inline"><xsl:with-param name="family">graphviz-node</xsl:with-param></xsl:call-template>;
    <xsl:for-each select="dossier:sub-property-of">
    <xsl:choose>
      <xsl:when test="$super-style/@reverseHierarchy='true'">
    "<xsl:value-of select="@ref"/>" -&gt; "<xsl:value-of select="../@id"/>" <xsl:call-template name="format-attribute-list-inline"><xsl:with-param name="style" select="$super-style"/><xsl:with-param name="label" select="''"/><xsl:with-param name="tooltip" select="''"/></xsl:call-template>;
      </xsl:when>
      <xsl:otherwise>
    "<xsl:value-of select="../@id"/>" -&gt; "<xsl:value-of select="@ref"/>" <xsl:call-template name="format-attribute-list-inline"><xsl:with-param name="style" select="$super-style"/><xsl:with-param name="label" select="''"/><xsl:with-param name="tooltip" select="''"/></xsl:call-template>;
      </xsl:otherwise>
    </xsl:choose>
    </xsl:for-each>
    <xsl:choose>
    <xsl:when test="not(../parent::dossier:ontology) and count(dossier:domain)=0 and count(dossier:sub-property-of)=0">
    "<xsl:value-of select="@id"/>:domain" <xsl:call-template name="format-attribute-list-inline"><xsl:with-param name="style" select="$anonymous-style"/><xsl:with-param name="label" select="$label.any"/><xsl:with-param name="tooltip" select="$label.any"/></xsl:call-template>
    
    "<xsl:value-of select="@id"/>:domain" -&gt; "<xsl:value-of select="@id"/>" <xsl:call-template name="format-attributes-inline"><xsl:with-param name="family" select="'graphviz-edge-incoming'"/><xsl:with-param name="tooltip"><xsl:value-of select="$label.any"/><xsl:text> &#8594; </xsl:text><xsl:value-of select="@name"/></xsl:with-param></xsl:call-template>;
    </xsl:when>
    <xsl:otherwise>
    <xsl:apply-templates select="dossier:domain/*"><xsl:with-param name="property" select="."/><xsl:with-param name="type" select="'domain'"/></xsl:apply-templates>
    </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
    <xsl:when test="not(../parent::dossier:ontology) and count(dossier:range)=0 and count(dossier:sub-property-of)=0">
    "<xsl:value-of select="@id"/>:range" <xsl:call-template name="format-attribute-list-inline"><xsl:with-param name="style" select="$anonymous-style"/><xsl:with-param name="label" select="$label.any"/><xsl:with-param name="tooltip" select="$label.any"/></xsl:call-template>
    "<xsl:value-of select="@id"/>" -&gt; "<xsl:value-of select="@id"/>:range" <xsl:call-template name="format-attributes-inline"><xsl:with-param name="family" select="'graphviz-edge-outgoing'"/><xsl:with-param name="tooltip"><xsl:value-of select="@name"/><xsl:text> &#8594; </xsl:text><xsl:value-of select="$label.any"/></xsl:with-param></xsl:call-template>;
    </xsl:when>
    <xsl:otherwise>
    <xsl:apply-templates select="dossier:range/*"><xsl:with-param name="property" select="."/><xsl:with-param name="type" select="'range'"/></xsl:apply-templates>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="dossier:class-ref">
  <xsl:param name="property"/>
  <xsl:param name="type"/>
  <xsl:choose>
  <xsl:when test="$type='domain'">
    "<xsl:value-of select="@ref"/>" -&gt; "<xsl:value-of select="$property/@id"/>" <xsl:call-template name="format-attributes-inline">
      <xsl:with-param name="source" select="$property"/>
      <xsl:with-param name="family" select="'graphviz-edge-incoming'"/>
      <xsl:with-param name="tooltip"><xsl:value-of select="@name"/><xsl:text> &#8594; </xsl:text><xsl:value-of select="$property/@name"/></xsl:with-param>
    </xsl:call-template>;
  </xsl:when>
  <xsl:otherwise>
    "<xsl:value-of select="$property/@id"/>" -&gt; "<xsl:value-of select="@ref"/>" <xsl:call-template name="format-attributes-inline">
      <xsl:with-param name="source" select="$property"/>
      <xsl:with-param name="family" select="'graphviz-edge-outgoing'"/>
      <xsl:with-param name="tooltip"><xsl:value-of select="$property/@name"/><xsl:text> &#8594; </xsl:text><xsl:value-of select="@name"/></xsl:with-param>
    </xsl:call-template>;
  </xsl:otherwise>
  </xsl:choose>
  </xsl:template>
  
  <xsl:template match="dossier:union">
  <xsl:param name="property"/>
  <xsl:param name="type"/>
  <xsl:apply-templates select="*"><xsl:with-param name="property" select="$property"/><xsl:with-param name="type" select="$type"/></xsl:apply-templates>
  </xsl:template>
 
  <xsl:template match="dossier:intersection">
  <xsl:param name="property"/>
  <xsl:param name="type"/>
  <xsl:apply-templates select="*"><xsl:with-param name="property" select="$property"/><xsl:with-param name="type" select="$type"/></xsl:apply-templates>
  </xsl:template>
 
  <xsl:template match="dossier:enumeration">
  <xsl:param name="property"/>
  <xsl:param name="type"/>
  <xsl:variable name="enumid" select="generate-id(.)"/>
  <xsl:variable name="label">{<xsl:for-each select="*"><xsl:if test="position() != 1">, </xsl:if><xsl:value-of select="@name"/></xsl:for-each>}</xsl:variable>
  <xsl:variable name="style" select="/dossier:ontology/dossier:style[@family='graphviz-node-class']"/>
  "<xsl:value-of select="$enumid"/>" <!-- 
  --><xsl:call-template name="format-attribute-list-inline">
      <xsl:with-param name="label" select="$label"/>
      <xsl:with-param name="tooltip" select="$label"/>
      <xsl:with-param name="style" select="$style"/>
    </xsl:call-template>;
  "<xsl:value-of select="$property/@id"/>" -&gt; "<xsl:value-of select="$enumid"/>" <xsl:call-template name="format-attributes-inline">
      <xsl:with-param name="source" select="$property"/>
      <xsl:with-param name="family" select="'graphviz-edge-outgoing'"/>
      <xsl:with-param name="tooltip"><xsl:value-of select="$property/@name"/><xsl:text> &#8594; </xsl:text><xsl:value-of select="$label"/></xsl:with-param>
    </xsl:call-template>;
  </xsl:template>
       
  <xsl:template name="format-reference">
  <xsl:param name="source" select="."/>
  <xsl:choose>
  <xsl:when test="$source/@href">
  <a><xsl:attribute name="href"><xsl:value-of select="$source/@href"/></xsl:attribute><xsl:value-of select="$source/@name"/></a>
  </xsl:when>
  <xsl:when test="$source/@ref">
  <a><xsl:attribute name="href">#<xsl:value-of select="$source/@ref"/></xsl:attribute><xsl:value-of select="$source/@name"/></a>
  </xsl:when>
  <xsl:when test="$source/@uri">
  <a><xsl:attribute name="href"><xsl:value-of select="$source/@uri"/></xsl:attribute><xsl:value-of select="$source/@name"/></a>
  </xsl:when>
  <xsl:when test="$source/@name">
  <xsl:value-of select="$source/@name"/>
  </xsl:when>
  <xsl:otherwise>
  unreferenced
  </xsl:otherwise>
  </xsl:choose>
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
          
  <xsl:template name="format-attribute-list-statements">
  <xsl:param name="style" select="."/>
  <xsl:param name="label"/>
  <xsl:param name="tooltip"/>
  label="<xsl:value-of select="$label"/>";
  tooltip="<xsl:value-of select="$tooltip"/>";
  <xsl:for-each select="$style/dossier:attribute">
  <xsl:value-of select="@style"/>="<xsl:value-of select="@value"/>";</xsl:for-each>
  </xsl:template>  
        
  <xsl:template name="format-attributes-statements">
  <xsl:param name="label" select="@name"/>
  <xsl:param name="tooltip"><xsl:call-template name="format-name"/></xsl:param>
  <xsl:param name="source" select='.'/>
  <xsl:param name="family"/>
  <xsl:variable name="style" select="$source/dossier:style[@family=$family]"/>
  <xsl:for-each select="$style">  
  <xsl:call-template name="format-attribute-list-statements">
    <xsl:with-param name="label"><xsl:choose><xsl:when test="@showLabel='false'"></xsl:when><xsl:otherwise><xsl:value-of select="$label"/></xsl:otherwise></xsl:choose></xsl:with-param>
    <xsl:with-param name="tooltip" select="$tooltip"/>
  </xsl:call-template>
  </xsl:for-each>
  </xsl:template>   
  
  <xsl:template match="dossier:style" mode="dump-style">
  family=<xsl:value-of select="@family"/> showLabel=<xsl:value-of select="@showLabel"/>  reverseHierarchy=<xsl:value-of select="@reverseHierarchy"/> <xsl:for-each select="dossier:attribute"><xsl:value-of select="@style"/>=<xsl:value-of select="@value"/> </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>