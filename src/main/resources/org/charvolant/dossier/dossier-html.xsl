<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dossier="http://www.charvolant.org/dossier#">
  <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes"/>
  <xsl:param name="label.and" select="' and '"/>
  <xsl:param name="label.any" select="'any'"/>
  <xsl:param name="label.annotation" select="'annotation'"/>
  <xsl:param name="label.asymmetric" select="'asymmetric'"/>
  <xsl:param name="label.backward-compatible-with" select="'Backward Compatible With'"/>
  <xsl:param name="label.classes" select="'Classes'"/>
  <xsl:param name="label.copyright" select="'Copyright'"/>
  <xsl:param name="label.copyright.detail" select="'Copyright notices'"/>
  <xsl:param name="label.datatype" select="'data type'"/>
  <xsl:param name="label.deprecated" select="'deprecated'"/>
  <xsl:param name="label.domain" select="'Domain'"/>
  <xsl:param name="label.functional" select="'functional'"/>
  <xsl:param name="label.home" select="'Home'"/>
  <xsl:param name="label.home.detail" select="'Main index'"/>
  <xsl:param name="label.imports" select="'Imports'"/>
  <xsl:param name="label.incompatible-with" select="'Incompatible With'"/>
  <xsl:param name="label.inverse-functional" select="'inverse functional'"/>
  <xsl:param name="label.irreflexive" select="'irreflexive'"/>
  <xsl:param name="label.metadata" select="'Metadata'"/>
  <xsl:param name="label.or" select="' or '"/>
  <xsl:param name="label.previous-version" select="'Previous Version'"/>
  <xsl:param name="label.properties" select="'Properties'"/>
  <xsl:param name="label.range" select="'Range'"/>
  <xsl:param name="label.reflexive" select="'reflexive'"/>
  <xsl:param name="label.signature" select="'signature '"/>
  <xsl:param name="label.super-class" select="'super-class '"/>
  <xsl:param name="label.super-property" select="'super-property '"/>
  <xsl:param name="label.symmetric" select="'symmetric'"/>
  <xsl:param name="label.transitive" select="'transitive'"/>
  <xsl:param name="label.version" select="'Version'"/>
  
  <xsl:template match="/">
    <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
    <html>
    <head>
      <title><xsl:call-template name="format-name"><xsl:with-param name="source" select="dossier:ontology[1]"/></xsl:call-template></title>
      <link rel="Stylesheet" type="text/css" href="css/dossier.css"/>
      <meta charset="UTF-8"/>
<!--       <link rel="shortcut icon" href="/argushiigi/favicon.ico"/>
      <link href="http://fonts.googleapis.com/css?family=Marcellus|Open+Sans:400,400italic,600,600italic,700,700italic&amp;subset=latin,latin-ext" rel="Stylesheet" type="text/css"/>
      <link rel="stylesheet" type="text/css" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.min.css"/>
      <script type="text/javascript" src="http://code.jquery.com/jquery-2.0.3.js"><xsl:text> </xsl:text></script>
      <script type="text/javascript" src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"><xsl:text> </xsl:text></script>
      <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css"/>
      <script type="text/javascript" charset="utf8" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"><xsl:text> </xsl:text></script>    
 -->    
    </head>
    <body>
    <div id="header">
    <div id="left-header"><a title="{$label.home.detail}" href="index.html"><span><xsl:value-of select="$label.home"/></span></a></div>
    <div id="centre-header">
    <div id="title"><xsl:call-template name="format-name"><xsl:with-param name="source" select="dossier:ontology[1]"/></xsl:call-template></div>
    </div>
    <div id="right-header"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
    </div>    
    <div class="toc"><ul><xsl:apply-templates select="dossier:ontology/dossier:class|dossier:ontology/dossier:property" mode="toc"><xsl:sort select="@name"/></xsl:apply-templates></ul></div>
    <article>
    <xsl:apply-templates select="dossier:ontology"/>
    </article>
    <div id="footer">
    <div id="left-footer"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
    <div id="centre-footer"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
    <xsl:for-each select="dossier:ontology/dossier:metadata/dossier:term[@term='terms:license' or @term='terms:rights' or @term='dc:rights']">
    <div id="copyright">
    <xsl:call-template name="format-metadata-value"/>
    </div>
    </xsl:for-each>
    </div>
    <div id="right-footer"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
    </div>    
    </body>
    </html>
  </xsl:template>
  
  <xsl:template match="dossier:ontology">
  <section class="ontology"><xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
  <h1><xsl:call-template name="format-name"/></h1>
  <xsl:apply-templates select="dossier:metadata"/>
  <section class="identifier">
  <xsl:value-of select="@name"/><xsl:text>: </xsl:text>
  <a><xsl:attribute name="href"><xsl:value-of select="@uri"/></xsl:attribute><xsl:value-of select="@uri"/></a>
  </section>
  <xsl:if test="dossier:label"><section class="labels"><xsl:apply-templates select="dossier:label"></xsl:apply-templates></section></xsl:if>
  <section class="descriptions">
  <xsl:apply-templates select="dossier:description"></xsl:apply-templates>
  <xsl:apply-templates select="dossier:version"></xsl:apply-templates>
  <xsl:apply-templates select="dossier:prior-version"></xsl:apply-templates>
  <xsl:apply-templates select="dossier:backward-compatible-with"></xsl:apply-templates>
  <xsl:apply-templates select="dossier:incompatible-with"></xsl:apply-templates>
  <xsl:apply-templates select="dossier:imports"></xsl:apply-templates>
  </section>
  <xsl:if test="dossier:diagram">
  <section class="diagrams">
  <xsl:apply-templates select="dossier:diagram"></xsl:apply-templates>
  </section>
  </xsl:if>
  </section>
  <section class="classes">
  <h2><xsl:value-of select="$label.classes"/></h2>
  <xsl:apply-templates select="dossier:class"></xsl:apply-templates>
  </section>
  <section class="properties">
  <h2><xsl:value-of select="$label.properties"/></h2>
  <xsl:apply-templates select="dossier:property"></xsl:apply-templates>
  </section>
  </xsl:template>
  
    
  <xsl:template match="dossier:diagram">
  <div class="diagram">
  <a><xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
  <img>
    <xsl:attribute name="src"><xsl:value-of select="@href"/></xsl:attribute>
    <xsl:attribute name="alt"><xsl:value-of select="@name"/></xsl:attribute>
  </img>
  </a>
  </div>
  </xsl:template>
  
  
  <xsl:template match="dossier:class">
  <section class="class"><xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
  <h3><xsl:value-of select="@name"/></h3>
  <xsl:apply-templates select="dossier:metadata"/>
  <section class="identifier">
  <a><xsl:attribute name="href"><xsl:value-of select="@uri"/></xsl:attribute><xsl:value-of select="@uri"/></a>
  </section>
  <xsl:if test="dossier:label"><section class="labels"><xsl:apply-templates select="dossier:label"></xsl:apply-templates></section></xsl:if>
  <xsl:if test="dossier:description">
  <section class="descriptions">
  <xsl:apply-templates select="dossier:description"></xsl:apply-templates>
  </section>
  </xsl:if>
  <section class="characteristics">
  <xsl:for-each select="dossier:sub-class-of|dossier:classifier">
  <xsl:if test="position() != 1"><xsl:text>, </xsl:text></xsl:if>
  <xsl:apply-templates select="."/>
  </xsl:for-each>
  <xsl:text> </xsl:text>  
  </section>
  </section>
  </xsl:template>
 
  <xsl:template match="dossier:property">
  <section class="property"><xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
  <h3><xsl:value-of select="@name"/></h3>
  <xsl:apply-templates select="dossier:metadata"/>
  <section class="identifier">
  <a><xsl:attribute name="href"><xsl:value-of select="@uri"/></xsl:attribute><xsl:value-of select="@uri"/></a>
  </section>
  <xsl:if test="dossier:label"><section class="labels"><xsl:apply-templates select="dossier:label"></xsl:apply-templates></section></xsl:if>
  <xsl:if test="dossier:description">
  <section class="descriptions">
  <xsl:apply-templates select="dossier:description"></xsl:apply-templates>
  </section>
  </xsl:if>
  <section class="characteristics">
  <span class="signature">
  <span class="signature-label"><xsl:value-of select="$label.signature"/></span>
  <xsl:if test="count(dossier:domain)=0 and count(dossier:sub-property-of)=0"><span class="any-label"><xsl:value-of select="$label.any"/></span></xsl:if>
  <xsl:apply-templates select="dossier:domain/*"/>
  <xsl:text> &#8594; </xsl:text>
  <xsl:value-of select="@name"/>
  <xsl:text> &#8594; </xsl:text>
  <xsl:if test="count(dossier:range)=0 and count(dossier:sub-property-of)=0"><span class="any-label"><xsl:value-of select="$label.any"/></span></xsl:if>
  <xsl:apply-templates select="dossier:range/*"/>
  </span>
  <xsl:for-each select="dossier:sub-property-of|dossier:classifier">
  <xsl:text> </xsl:text>
  <xsl:apply-templates select="."/>
  </xsl:for-each>  
  </section>
  </section>
  </xsl:template>

  <xsl:template match="dossier:sub-class-of">
  <span class="sub-class"><span class="super-label"><xsl:value-of select="$label.super-class"/></span><xsl:call-template name="format-reference"/></span>
  </xsl:template>
 
  <xsl:template match="dossier:sub-property-of">
  <span class="sub-property"><span class="super-label"><xsl:value-of select="$label.super-property"/></span><xsl:call-template name="format-reference"/></span>
  </xsl:template>
  
  <xsl:template match="dossier:class-ref">
  <xsl:call-template name="format-reference"/>
  </xsl:template>
  
  <xsl:template match="dossier:union">
  <xsl:for-each select="*">
  <xsl:if test="position() != 1"><span class="or-label"><xsl:value-of select="$label.or"/></span></xsl:if>
  <xsl:apply-templates select="."/>
  </xsl:for-each>
  </xsl:template>
 
  <xsl:template match="dossier:intersection">
  <xsl:for-each select="*">
  <xsl:if test="position() != 1"><span class="and-label"><xsl:value-of select="$label.and"/></span></xsl:if>
  <xsl:apply-templates select="."/>
  </xsl:for-each>
  </xsl:template>
 
  <xsl:template match="dossier:enumeration">
  {<xsl:for-each select="*">
  <xsl:if test="position() != 1">, </xsl:if>
  <xsl:apply-templates select="."/>
  </xsl:for-each>}
  </xsl:template>
  
  <xsl:template match="dossier:classifier">
  <span><xsl:attribute name="class"><xsl:value-of select="text()"/>-classifier</xsl:attribute>
  <xsl:choose>
  <xsl:when test="text()='datatype'"><xsl:value-of select="$label.datatype"/></xsl:when>
  <xsl:when test="text()='annotation'"><xsl:value-of select="$label.annotation"/></xsl:when>
  <xsl:when test="text()='deprecated'"><xsl:value-of select="$label.deprecated"/></xsl:when>
  <xsl:when test="text()='functional'"><xsl:value-of select="$label.functional"/></xsl:when>
  <xsl:when test="text()='asymmetric'"><xsl:value-of select="$label.asymmetric"/></xsl:when>
  <xsl:when test="text()='inverse-functional'"><xsl:value-of select="$label.inverse-functional"/></xsl:when>
  <xsl:when test="text()='irreflexive'"><xsl:value-of select="$label.asymmetric"/></xsl:when>
  <xsl:when test="text()='reflexive'"><xsl:value-of select="$label.asymmetric"/></xsl:when>
  <xsl:when test="text()='symmetric'"><xsl:value-of select="$label.asymmetric"/></xsl:when>
  <xsl:when test="text()='transitive'"><xsl:value-of select="$label.asymmetric"/></xsl:when>
  <xsl:otherwise><xsl:value-of select="text()"/></xsl:otherwise>
  </xsl:choose>
  </span>
  </xsl:template>
  
  <xsl:template match="dossier:metadata">
  <div class="metadata">
  <span class="label-metadata"><xsl:value-of select="$label.metadata"/></span>
  <table>
  <xsl:apply-templates select="*"/>
  </table>
  </div>
  </xsl:template>
  
  <xsl:template match="dossier:term">
  <tr><td class="metadata-term"><xsl:value-of select="@term"/></td><td class="metadata-value"><xsl:call-template name="format-metadata-value"/></td></tr>
  </xsl:template>
  
  <xsl:template match="dossier:label">
  <span class="label">
  <xsl:if test="@lang"><span><xsl:attribute name="class">language lang-<xsl:value-of select="@lang"/></xsl:attribute><xsl:value-of select="@lang"/></span><xsl:text> </xsl:text></xsl:if>
  <xsl:value-of select="text()"/>
  </span>
  </xsl:template>
  
  <xsl:template match="dossier:description">
  <div class="description">
  <xsl:if test="@lang"><span><xsl:attribute name="class">language lang-<xsl:value-of select="@lang"/></xsl:attribute><xsl:value-of select="@lang"/></span><xsl:text> </xsl:text></xsl:if>
  <xsl:value-of select="text()"/>
  </div>
  </xsl:template>
 
  <xsl:template match="dossier:version">
  <div class="version">
  <xsl:value-of select="$label.version"/> <xsl:call-template name="format-reference"/>
  </div>
  </xsl:template>

  <xsl:template match="dossier:previous-version">
  <div class="previous-version">
  <xsl:value-of select="$label.previous-version"/> <xsl:call-template name="format-reference"/>
  </div>
  </xsl:template>
 
  <xsl:template match="dossier:backward-compatible-with">
  <div class="backward-compatible-with">
  <xsl:value-of select="$label.backward-compatible-with"/> <xsl:call-template name="format-reference"/>
  </div>
  </xsl:template>
 
  <xsl:template match="dossier:incompatible-with">
  <div class="incompatible-with">
  <xsl:value-of select="$label.incompatible-with"/> <xsl:call-template name="format-reference"/>
  </div>
  </xsl:template>
 
  <xsl:template match="dossier:imports">
  <div class="incompatible-with">
  <xsl:value-of select="$label.imports"/> <xsl:call-template name="format-reference"/>
  </div>
  </xsl:template>
  
  <xsl:template match="dossier:class|dossier:property" mode="toc">
  <li><a><xsl:attribute name="href">#<xsl:value-of select="@id"/></xsl:attribute><xsl:value-of select="@name"/></a></li>
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
    
  <xsl:template name="format-metadata-value">
  <xsl:param name="source" select="."/>
  <xsl:choose>
  <xsl:when test="$source/@href">
  <a><xsl:attribute name="href"><xsl:value-of select="$source/@href"/></xsl:attribute><xsl:value-of select="$source/@value"/></a>
  </xsl:when>
  <xsl:otherwise>
  <xsl:value-of select="$source/@value"/>
  </xsl:otherwise>
  </xsl:choose>
  </xsl:template> 
</xsl:stylesheet>