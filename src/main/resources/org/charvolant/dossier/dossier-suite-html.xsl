<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dossier="http://www.charvolant.org/dossier#">
  <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes"/>
  <xsl:param name="label.backward-compatible-with" select="'Backward Compatible With'"/>
  <xsl:param name="label.copyright" select="'Copyright'"/>
  <xsl:param name="label.copyright.detail" select="'Copyright notices'"/>
  <xsl:param name="label.home" select="'Home'"/>
  <xsl:param name="label.home.detail" select="'Main index'"/>
  <xsl:param name="label.imports" select="'Imports'"/>
  <xsl:param name="label.ontology-index" select="'Ontology Index'"/>
  <xsl:param name="label.incompatible-with" select="'Incompatible With'"/>
  <xsl:param name="label.metadata" select="'Metadata'"/>
  <xsl:param name="label.previous-version" select="'Previous Version'"/>
  <xsl:param name="label.version" select="'Version'"/>
  
  <xsl:template match="/">
    <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
    <html>
    <head>
      <title><xsl:value-of select="$label.ontology-index"/></title>
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
    <div id="title"><xsl:value-of select="$label.ontology-index"/></div>
    </div>
    <div id="right-header"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
    </div>    
    <div class="toc"><ul><xsl:apply-templates select="dossier:ontologies/dossier:ontology" mode="toc"><xsl:sort select="@name"/></xsl:apply-templates></ul></div>
    <article>
    <xsl:apply-templates select="dossier:ontologies"/>
    </article>
    <div id="footer">
    <div id="left-footer"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
    <div id="centre-footer"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
    <div id="right-footer"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
    </div>    
    </body>
    </html>
  </xsl:template>
  
  <xsl:template match="dossier:ontologies">
    <xsl:if test="dossier:diagram">
    <section class="diagrams">
    <xsl:apply-templates select="dossier:diagram"></xsl:apply-templates>
    </section>
    </xsl:if>
    <xsl:apply-templates select="dossier:ontology"/>
  </xsl:template>
   
  <xsl:template match="dossier:ontology">
  <section class="ontology"><xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
  <h1><xsl:call-template name="format-reference"/></h1>
  <section class="identifier">
  <a><xsl:attribute name="href"><xsl:value-of select="@uri"/></xsl:attribute><xsl:value-of select="@uri"/></a>
  </section>
  <xsl:if test="dossier:label"><section class="labels"><xsl:apply-templates select="dossier:label"></xsl:apply-templates></section></xsl:if>
  <xsl:if test="dossier:description or dossier:imports">
  <section class="descriptions">
  <xsl:apply-templates select="dossier:description"></xsl:apply-templates>
  <xsl:apply-templates select="dossier:imports"></xsl:apply-templates>
  </section>
  </xsl:if>
  </section>
  </xsl:template>
  
  <xsl:template match="dossier:diagram">
  <div class="diagram">
  <object type="image/svg+xml">
  <xsl:attribute name="data"><xsl:value-of select="@href"/></xsl:attribute>
  <xsl:value-of select="@name"/>
  </object>  
  </div>
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
  <xsl:value-of select="$label.version"/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><xsl:call-template name="format-link"/>
  </div>
  </xsl:template>

  <xsl:template match="dossier:previous-version">
  <div class="previous-version">
  <xsl:value-of select="$label.previous-version"/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><xsl:call-template name="format-link"/>
  </div>
  </xsl:template>
 
  <xsl:template match="dossier:backward-compatible-with">
  <div class="backward-compatible-with">
  <xsl:value-of select="$label.backward-compatible-with"/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><xsl:call-template name="format-link"/>
  </div>
  </xsl:template>
 
  <xsl:template match="dossier:incompatible-with">
  <div class="incompatible-with">
  <xsl:value-of select="$label.incompatible-with"/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><xsl:call-template name="format-link"/>
  </div>
  </xsl:template>
 
  <xsl:template match="dossier:imports">
  <div class="imports">
  <xsl:value-of select="$label.imports"/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><xsl:call-template name="format-link"/>
  </div>
  </xsl:template>
  
  <xsl:template match="dossier:ontology" mode="toc">
  <li><a><xsl:attribute name="href">#<xsl:value-of select="@id"/></xsl:attribute><xsl:value-of select="@name"/></a></li>
  </xsl:template>
        
  <xsl:template name="format-link">
  <xsl:param name="source" select="."/>
  <xsl:choose>
  <xsl:when test="$source/@href">
  <a><xsl:attribute name="href"><xsl:value-of select="$source/@href"/></xsl:attribute><xsl:value-of select="$source/@uri"/></a>
  </xsl:when>
  <xsl:when test="$source/@ref">
  <a><xsl:attribute name="href">#<xsl:value-of select="$source/@ref"/></xsl:attribute><xsl:value-of select="$source/@uri"/></a>
  </xsl:when>
  <xsl:when test="$source/@uri">
  <a><xsl:attribute name="href"><xsl:value-of select="$source/@uri"/></xsl:attribute><xsl:value-of select="$source/@uri"/></a>
  </xsl:when>
  <xsl:otherwise>
  unreferenced
  </xsl:otherwise>
  </xsl:choose>
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
</xsl:stylesheet>