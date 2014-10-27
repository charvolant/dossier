package org.charvolant.dossier;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.charvolant.dossier.vocabulary.Dossier;
import org.charvolant.dossier.vocabulary.Skos;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public abstract class Generator { 
  /** The location of the dossier schema */
  @SuppressWarnings("unused")
  private static final URL SCHEMA_LOCATION = Generator.class.getResource("dossier.xsd");

  /** The URI for the dossier schema: {@value} */
  public static final String DOSSIER_URI = "http://www.charvolant.org/dossier#";

  /** The list of XSD datatypes that automatically make the resource a datatype and the class present */
  protected static final Resource[] XSD_DATATYPES = {
      XSD.anyURI,
      XSD.base64Binary,
      XSD.date,
      XSD.dateTime,
      XSD.decimal,
      XSD.duration,
      XSD.ENTITIES,
      XSD.ENTITY,
      XSD.gDay,
      XSD.gMonth,
      XSD.gMonthDay,
      XSD.gYear,
      XSD.gYearMonth,
      XSD.hexBinary,
      XSD.ID,
      XSD.IDREF,
      XSD.IDREFS,
      XSD.integer,
      XSD.language,
      XSD.Name,
      XSD.NCName,
      XSD.negativeInteger,
      XSD.NMTOKEN,
      XSD.NMTOKENS,
      XSD.nonNegativeInteger,
      XSD.nonPositiveInteger,
      XSD.normalizedString,
      XSD.NOTATION,
      XSD.positiveInteger,
      XSD.QName,
      XSD.time,
      XSD.token,
      XSD.unsignedByte,
      XSD.unsignedInt,
      XSD.unsignedLong,
      XSD.unsignedShort,
      XSD.xboolean,
      XSD.xbyte,
      XSD.xdouble,
      XSD.xfloat,
      XSD.xint,
      XSD.xlong,
      XSD.xshort,
      XSD.xstring
    };
  
  /**
   * A list of terms that can be used to generate metadata for a resource.
   */
  protected static final Property[] METADATA_TERMS = {
      DCTerms.available,
      DCTerms.conformsTo,
      DCTerms.contributor,
      DCTerms.created,
      DCTerms.creator,
      DCTerms.date,
      DCTerms.issued,
      DCTerms.license,
      DCTerms.modified,
      DCTerms.publisher,
      DCTerms.rights,
      DCTerms.rightsHolder,
      DCTerms.valid,
      DC.contributor,
      DC.creator,
      DC.date,
      DC.publisher,
      DC.rights,
      OWL.versionInfo
    };
  
  /** The family name for graphviz node properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_NODE = "graphviz-node";
  /** The family name for anonymouys graphviz node properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_NODE_ANONYMOUS = "graphviz-node-anonymous";
  /** The family name for graphviz class properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_NODE_CLASS = "graphviz-node-class";
  /** The family name for graphviz edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE = "graphviz-edge";
  /** The family name for graphviz incoming edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE_INCOMING = "graphviz-edge-incoming";
  /** The family name for graphviz outgoing edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE_OUTGOING = "graphviz-edge-outgoing";
  /** The family name for graphviz super-class edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE_SUPER_CLASS = "graphviz-edge-super-class";
  /** The family name for graphviz super-property edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE_SUPER_PROPERTY = "graphviz-edge-super-property";
  /** The family name for graphviz import edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE_IMPORT = "graphviz-edge-import";
  /** The family name for graphviz graph properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_GRAPH = "graphviz-graph";
  /** The family name for html properties: {@value} */
  public static final String FAMILY_HTML = "html";

  /** The node preferences */
  protected static final Property[] NODE_ATTRIBUTES = {
    Dossier.color,
    Dossier.fontColor,
    Dossier.fontName,
    Dossier.fontSize,
    Dossier.nodeShape,
    Dossier.nodeStyle
  };
  /** The edge preferences */
  protected static final Property[] EDGE_ATTRIBUTES = {
    Dossier.arrowHead,
    Dossier.arrowStyle,
    Dossier.arrowTail,
    Dossier.clipHead,
    Dossier.clipTail,
    Dossier.color,
    Dossier.fontColor,
    Dossier.fontName,
    Dossier.fontSize,
    Dossier.weight
  };

  /** The graph/subgraph preferences */
  protected static final Property[] GRAPH_ATTRIBUTES = {
    Dossier.backgroundColor,
    Dossier.fontColor,
    Dossier.fontName,
    Dossier.fontSize
  };

  /** The html preferences */
  protected static final Property[] HTML_ATTRIBUTES = {
  };
 
  /** The configuration that is used to handle cross-ontology links */
  protected Configuration configuration;
  /** The map of resources onto internal ids */
  protected BiMap<Resource, String> ids;
  /** The display preferences for this generator */
  protected DisplayPreferences preferences;
  /** The language order to use */
  protected List<String> languageOrder;

  /**
   * Create a generator for a configuration.
   *
   * @param configuration The configuration
   */
  public Generator(Configuration configuration) {
    super();
    this.configuration = configuration;
    this.languageOrder = this.chooseLanguageOrder();
    this.ids = HashBiMap.create();
  }

  /**
   * Choose the order in which to process language-specific elements.
   * <p>
   * Language specifiers are xml:lang compatible.
   * If the locale has a country-based variant then that variant is tried first (eg. "en-gb").
   * The plain language is then tried (eg. "en").
   * Then no language, then English ("en") if not already present and lastly any language. 
   *
   * @return The language order
   */
  protected List<String> chooseLanguageOrder() {
    List<String> order = new ArrayList<String>(5);
    Locale locale = this.configuration.getLocale();
  
    if (!locale.getCountry().isEmpty())
      order.add((locale.getLanguage() + "-" + locale.getCountry()).toLowerCase());
    order.add(locale.getLanguage().toLowerCase());
    order.add("");
    if (!order.contains("en"))
      order.add("en");
    order.add(null);
    return order;
  }

  /**
   * Create an id for a resource.
   *
   * @param resource The resource
   * 
   * @return The matching ID for the resource
   */
  protected String createId(Resource resource) {
    String id = this.ids.get(resource);
    String baseId;
    int extension;
  
    if (id != null)
      return id;
    baseId = this.getLabel(resource);
    id = baseId;
    extension = 1;
    while (this.ids.inverse().containsKey(id))
      id = baseId + "_" + extension++;
    this.ids.put(resource, id);
    return id;
  }
  
  /**
   * Is this a local resource?
   * <p>
   * A local resource is a resource that should be found within
   * the document being generated.
   *
   * @param resource The resource to test
   * 
   * @return True if the resource is local to the generated document
   */
  abstract protected boolean isLocal(Resource resource);

  /**
   * Get the prefix for a namespace.
   * <p>
   * By default, this returns the prefix as defined by the configuration.
   * Superclasses can override this to be more sophisticated.
   *
   * @param ns The namespace
   * 
   * @return The prefix
   */
  protected String getPrefix(String ns) {
    if (ns == null)
      return null;
    return this.configuration.getPrefixes().getNsURIPrefix(ns);
  }

  /**
   * Create a cross-reference for a resource.
   * <p>
   * If the resource is local, use the local ID.
   * If the resource has an entry in the namespace map,
   * then use the entry.
   * Otherwise, use the resource URI.
   *
   * @param resource The resource
   * @param full Try to map to a file name, even if this is local
   * 
   * @return The HREF
   * 
   * @see #getNamespaceMap()
   */
  protected String getHref(Resource resource, boolean full) {
    String file;
  
    if (!resource.isURIResource())
      return "#";
    if (!full && this.isLocal(resource)) 
      return "#" + this.createId(resource);
    file = this.configuration.getHtmlFile(resource);
    if (file != null)
      return file + "#" + resource.getLocalName();
    return resource.getURI();
  }

  /**
   * Get a locale-specific human-readable label for this resource.
   * <p>
   * If possible, this is the prefixed name of the resource.
   *
   * @param resource The resource
   * 
   * @return The label
   */
  protected String getLabel(Resource resource) {
    StringBuilder builder;
    String ns, prefix;
  
    if (resource.isAnon())
      return "";
    builder = new StringBuilder();
    ns = resource.getNameSpace();
    prefix = this.getPrefix(ns);
    if (prefix != null && !prefix.isEmpty()) {
      builder.append(prefix);
      builder.append(":");
    }
    builder.append(resource.getLocalName());
    return builder.toString();
  }

  /**
   * See if we can append a property of the appropriate language.
   *
   * @param model The source model
   * @param resource The resource to examine
   * @param lang The language (null for not specified)
   * @param sb The target builder
   * @param property The property to use
   * 
   * @return True if one has been found
   */
  protected boolean appendProperty(Model model, Resource resource, String lang, StringBuilder sb, Property property) {
        StmtIterator si = model.listStatements(resource, property, null, lang);
      
        if (!si.hasNext())
          return false;
        if (sb.length() > 0)
          sb.append("&#10;");
        sb.append(si.next().getString().replace("\"", "'").replace("\n", "&#10;"));
        return true;
      }

  /**
   * Append a possible property value.
   * <p>
   * Properties are tried until one is found.
   * The locale is used to try for fist language-country, then language, then not specified, then any.
   *
   * @param model The source model
   * @param resource The resource
   * @param locale The locale
   * @param sb The string builder
   * @param properties The properties to try
   */
  protected void appendProperty(Model model, Resource resource, StringBuilder sb, Property...properties) {
    Locale locale = this.configuration.getLocale();
  
    for (Property property: properties) {
      if (!locale.getCountry().isEmpty() && this.appendProperty(model, resource, (locale.getLanguage() + "-" + locale.getCountry()).toLowerCase(), sb, property))
        return;
      if (this.appendProperty(model, resource, locale.getLanguage().toLowerCase(), sb, property))
        return;
      if (this.appendProperty(model, resource, "", sb, property))
        return;
      if (this.appendProperty(model, resource, null, sb, property))
        return;
    }
  }

  /**
   * Construct a tool tip from the information available.
   * <p>
   * This uses the generator locale.
   * The label, title, comment and/or description are added, as available.
   * If nothing can be found, the label is used.
   *
   * @param model The model to use
   * @param resource The resource to examine
   * 
   * @return
   */
  protected String getTooltip(Model model, Resource resource) {
    StringBuilder sb = new StringBuilder();
  
    this.appendProperty(model, resource, sb, DCTerms.title, DC.title, RDFS.label);
    this.appendProperty(model, resource, sb, DCTerms.description, DC.description, RDFS.comment);
    if (sb.length() == 0)
      sb.append(this.getLabel(resource));
    return sb.toString();
  }

  /**
   * Construct a title from the information available.
   * <p>
   * This uses the generator locale and either uses a specifially
   * stated title or label or uses the resource label.
   *
   * @param model The model to use
   * @param resource The resource to examine
   * 
   * @return A suitable title
   */
  protected String getTitle(Model model, Resource resource) {
    StringBuilder sb = new StringBuilder();
  
    this.appendProperty(model, resource, sb, DCTerms.title, DC.title, RDFS.label);
    if (sb.length() == 0)
      sb.append(resource.getLocalName());
    return sb.toString();
  }

  /**
   * Generate style information, if available.
   *
   * @param document The source document
   * @param prefs The preferences
   * @param properties The properties to include
   * 
   * @return A style element or null for none
   */
  protected Element generateStyle(Document document, DisplayPreference prefs, String family, Property... properties) {
    StyleRenderer renderer = new StyleRenderer(document, family);
    boolean showLabel = prefs.getBooleanPref(Dossier.showLabel, true);
    boolean reverseHierarchy = prefs.getBooleanPref(Dossier.reverseHierarchy, false);

    renderer.begin();
    if (!showLabel)
      renderer.render("showLabel", false);
    if (reverseHierarchy)
      renderer.render("reverseHierarchy", true);
    prefs.render(renderer, properties);
    renderer.end();
    return renderer.getStyle();
  }

  /**
   * Generate style information, if available.
   * <p>
   * The properties uses are chosen from the style family.
   *
   * @param document The source document
   * @param resource The resource
   * @param family The style family
   * 
   * @return A style element or null for none
   */
  protected Element generateStyle(Document document, Resource resource, String family) {
    Property[] props;
    DisplayPreference prefs = this.preferences.getPreference(resource);

    if (family.equals(this.FAMILY_GRAPHVIZ_EDGE))
      props = this.EDGE_ATTRIBUTES;
    else if (family.equals(this.FAMILY_HTML))
      props = this.HTML_ATTRIBUTES;
    else if (family.equals(this.FAMILY_GRAPHVIZ_GRAPH))
      props = this.GRAPH_ATTRIBUTES;
    else if (family.equals(this.FAMILY_GRAPHVIZ_EDGE_INCOMING)) {
      props = this.EDGE_ATTRIBUTES;
      prefs = prefs.asIncoming();
    } else if (family.equals(this.FAMILY_GRAPHVIZ_EDGE_OUTGOING)) {
      props = this.EDGE_ATTRIBUTES;
      prefs = prefs.asOutgoing();
    } else if (family.equals(this.FAMILY_GRAPHVIZ_EDGE_SUPER_CLASS))
      props = this.EDGE_ATTRIBUTES;
    else if (family.equals(this.FAMILY_GRAPHVIZ_EDGE_SUPER_PROPERTY))
      props = this.EDGE_ATTRIBUTES;
    else if (family.equals(this.FAMILY_GRAPHVIZ_EDGE_IMPORT))
      props = this.EDGE_ATTRIBUTES;
    else
      props = this.NODE_ATTRIBUTES;
    return this.generateStyle(document, prefs, family, props);
  }

  /**
   * Add descriptive data such as labels, etc.
   * <p>
   * Elements are organised first by language, taken from the
   * {@link ModelGenerator#languageOrder} list, and then by property.
   *
   * @param model The model to use
   * @param document The document to add to
   * @param parent The parent element
   * @param name The XML element name of the descrptive data
   * @param resource The source resource
   * @param properties The properties that should be interrogated for descriptive data.
   */
  protected void addDescriptiveData(Model model, Document document, Element parent, String name, Resource resource, Property... properties) {
    Set<Statement> seen = new HashSet<Statement>();
    StmtIterator si;
    Statement statement;
    Element elt;

    for (String lang: this.languageOrder) {
      for (Property property: properties) {
        si = model.listStatements(resource, property, null, lang);
        while (si.hasNext()) {
          statement = si.next();
          if (seen.contains(statement))
            continue;
          seen.add(statement);
          elt = document.createElementNS(this.DOSSIER_URI, name);
          if (lang != null && !lang.isEmpty())
            elt.setAttribute("lang", lang);
          elt.setTextContent(statement.getString());
          parent.appendChild(elt);
        }
      }
    }
  }

  /**
   * Add reference data such as see-alsos, etc.
   * <p>
   * Elements are organised by property.
   *
   * @param model The model to use
   * @param document The document to add to
   * @param parent The parent element
   * @param name The XML element name of the descrptive data
   * @param resource The source resource
   * @param ignoreSelf Ignore self references
   * @param properties The properties that should be interrogated for descriptive data.
   */
  protected void addReferenceURIs(Model model, Document document, Element parent, String name, Resource resource, boolean ignoreSelf, Property... properties) {
    StmtIterator si;
    Statement statement;
    Resource ref;
    Element elt;
    String id;

    for (Property property: properties) {
      si = model.listStatements(resource, property, (Resource) null);
      while (si.hasNext()) {
        statement = si.next();
        if (!statement.getObject().isURIResource())
          continue;
        if (ignoreSelf && resource.equals(statement.getObject()))
          continue;
        ref = statement.getResource();
        elt = document.createElementNS(this.DOSSIER_URI, name);
        id = this.ids.get(ref);
        if (id != null)
          elt.setAttribute("ref", id);
        elt.setAttribute("name", this.getLabel(ref)); // Transform can't look up on id() for some reason
        elt.setAttribute("uri", ref.getURI());
        elt.setAttribute("href", this.getHref(ref, false));
        parent.appendChild(elt);
      }
    }
  }

  /**
   * Generate metadata information, if available.
   *
   * @param model The model to 
   * @param document The source document
   * @param resource The resource
   * 
   * @return A metadata element or null for none
   */
  protected Element generateMetadata(Model model, Document document, Resource resource) {
    Element metadata = null, term;
    Set<String> entries = new HashSet<String>();
    Statement statement;
    String mdterm;

    for (Property property: this.METADATA_TERMS) {
      mdterm = this.getLabel(property);

      if (entries.contains(mdterm))
        continue;
      statement = resource.getProperty(property);
      if (statement != null) {
        if (metadata == null)
          metadata = document.createElementNS(this.DOSSIER_URI, "metadata");
        term = document.createElementNS(this.DOSSIER_URI, "term");
        term.setAttribute("term", mdterm);
        if (statement.getObject().isURIResource()) {
          term.setAttribute("href", this.getHref(statement.getResource(), false));
          term.setAttribute("value", statement.getResource().getURI());
        } else if (statement.getObject().isLiteral()) {
          Object value = statement.getLiteral().getValue();

          term.setAttribute("value", value.toString());
        }
        metadata.appendChild(term);
      }
    }
    return metadata;
  }

  /**
   * Generate information about a resource.
   * <p>
   * Add identifiers and basic descriptive information.
   *
   * @param document The document
   * @param name The name of the XML element
   * @param resource The resource to interrogate
   * 
   * @return The basic element
   */
  protected Element generateResourceType(Model model, Document document, String name, Resource resource) {
    String id = this.createId(resource);
    Element re = document.createElementNS(this.DOSSIER_URI, name);
    Element metadata;

    re.setAttribute("id", id);
    if (resource.isURIResource())
      re.setAttribute("uri", resource.getURI());
    re.setAttribute("name", this.getLabel(resource));
    re.setAttribute("href", this.getHref(resource, true));
    this.addDescriptiveData(model, document, re, "label", resource, Skos.prefLabel, RDFS.label, Skos.altLabel, DCTerms.title, DC.title);
    this.addDescriptiveData(model, document, re, "description", resource, RDFS.comment, DCTerms.description, DC.description);
    this.addReferenceURIs(model, document, re, "definition", resource, true, RDFS.isDefinedBy, RDFS.seeAlso);
    metadata = this.generateMetadata(model, document, resource);
    if (metadata != null)
      re.appendChild(metadata);
    return re;
  }

  /**
   * Add datatype information to domains and ranges that happen to fall into
   * a model but which have not been specified.
   * 
   * @param model The model
   */
  protected void addDatatypes(Model model) {
    Resource rdfsDatatype = model.createResource(RDFS.Datatype.getURI());
  
    for (Resource datatype: this.XSD_DATATYPES) {
      if (model.contains(null, null, datatype) && !model.contains(datatype, RDF.type, rdfsDatatype))
        model.add(datatype, RDF.type, rdfsDatatype);
    }
  }
  
  /**
   * Create a new document, using the dossier XML schema.
   *
   * @return An empty docuemnt
   * 
   * @throws ParserConfigurationException If unable to create a suitable document
   */
  protected Document newDocument() throws ParserConfigurationException {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;

    builderFactory.setNamespaceAware(true);
    builder = builderFactory.newDocumentBuilder();
    return builder.newDocument();
  }

  /**
   * Generate a document describing the thing that we
   * want to generate from.
   * <p>
   * The document generated re-orders the model(s) and
   * re-structures things so that {@link Documenter} objects
   * can generate suitable representations.
   *
   * @return An XML document describing the data supplied to the generator
   * 
   * @throws Exception if there is an error in the generation
   */
  public abstract Document generate() throws Exception;



  /**
   * Render style information as an XML element.
   */
  protected class StyleRenderer implements PreferenceRenderer {
    private Document document;
    private String family;
    private Element style;

    public StyleRenderer(Document document, String family) {
      super();
      this.document = document;
      this.family = family;
    }

    public Element getStyle() {
      return this.style;
    }

    private void ensureStyle() {
      if (this.style == null) {
        this.style = this.document.createElementNS(Generator.this.DOSSIER_URI, "style");   
        if (this.family != null)
          this.style.setAttribute("family", this.family);
      }
    }

    public void renderAttribute(String property, Object value) {
      this.ensureStyle();
      this.style.setAttribute(property, value.toString());
    }

    public void renderElement(String property, Object value) {
      Element se;

      this.ensureStyle();
      se = this.document.createElementNS(Generator.this.DOSSIER_URI, "attribute");
      se.setAttribute("style", property);
      se.setAttribute("value", value.toString());
      this.style.appendChild(se);
    }

    @Override
    public void render(String property, Object value) {
      if (property.equals("showLabel") || property.equals("reverseHierarchy"))
        this.renderAttribute(property, value);
      else
        this.renderElement(property, value);
    }

    @Override
    public void begin() {
      this.style = null;
    }

    @Override
    public void end() {
    } 
  }

}