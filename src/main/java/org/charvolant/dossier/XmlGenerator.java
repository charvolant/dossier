package org.charvolant.dossier;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.charvolant.dossier.vocabulary.Dossier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.HashBiMap;
import com.hp.hpl.jena.ontology.ComplementClass;
import com.hp.hpl.jena.ontology.EnumeratedClass;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class XmlGenerator extends Generator {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(XmlGenerator.class);
  
  /** The location of the dossier schema */
  @SuppressWarnings("unused")
  private static final URL SCHEMA_LOCATION = XmlGenerator.class.getResource("dossier.xsd");

  /** The URI for the dossier schema: {@value} */
  public static final String DOSSIER_URI = "http://www.charvolant.org/dossier#";

  /** The family name for graphviz node properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_NODE = "graphviz-node";
  /** The family name for anonymouys graphviz node properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_NODE_ANONYMOUS = "graphviz-node-anonymous";
  /** The family name for graphviz edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE = "graphviz-edge";
  /** The family name for graphviz incoming edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE_INCOMING = "graphviz-edge-incoming";
  /** The family name for graphviz outgoing edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE_OUTGOING = "graphviz-edge-outgoing";
  /** The family name for graphviz super-class edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE_SUPER_CLASS = "graphviz-edge-super-class";
  /** The family name for graphviz edge properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_EDGE_SUPER_PROPERTY = "graphviz-edge-super-property";
  /** The family name for graphviz graph properties: {@value} */
  public static final String FAMILY_GRAPHVIZ_GRAPH = "graphviz-graph";
  /** The family name for html properties: {@value} */
  public static final String FAMILY_HTML = "html";

  /** The node preferences */
  private static final Property[] NODE_ATTRIBUTES = {
    Dossier.color,
    Dossier.fontColor,
    Dossier.fontName,
    Dossier.fontSize,
    Dossier.nodeShape,
    Dossier.nodeStyle
  };
  /** The edge preferences */
  private static final Property[] EDGE_ATTRIBUTES = {
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
  private static final Property[] GRAPH_ATTRIBUTES = {
    Dossier.backgroundColor,
    Dossier.fontColor,
    Dossier.fontName,
    Dossier.fontSize
  };

  /** The html preferences */
  private static final Property[] HTML_ATTRIBUTES = {
  };

  /** The SKOS preferred label property */
  private Property prefLabel;
  /** The SKOS altername label property */
  private Property altLabel;
  /** The sub-class property */
  private Property subClassOf;
  /** The sub-property label property */
  private Property subPropertyOf;

  /**
   * Construct an Xml Generator.
   *
   * @param configuration The generator configuration and shared data
   * @param ontology The ontology to generate from
   */
  public XmlGenerator(Configuration configuration, OntModel ontology) {
    super(configuration, ontology);
    this.prefLabel = this.model.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel");
    this.altLabel = this.model.createProperty("http://www.w3.org/2004/02/skos/core#altLabel");
    this.subClassOf = this.model.createProperty(RDFS.subClassOf.getURI());
    this.subPropertyOf = this.model.createProperty(RDFS.subPropertyOf.getURI());
    this.ids = HashBiMap.create();
  }

  /**
   * Add descriptive data such as labels, etc.
   * <p>
   * Elements are organised first by language, taken from the
   * {@link Generator#languageOrder} list, and then by property.
   *
   * @param document The document to add to
   * @param parent The parent element
   * @param name The XML element name of the descrptive data
   * @param resource The source resource
   * @param properties The properties that should be interrogated for descriptive data.
   */
  private void addDescriptiveData(Document document, Element parent, String name, Resource resource, Property... properties) {
    Set<Statement> seen = new HashSet<Statement>();
    StmtIterator si;
    Statement statement;
    Element elt;

    for (String lang: this.languageOrder) {
      for (Property property: properties) {
        si = this.model.listStatements(resource, property, null, lang);
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
   * @param document The document to add to
   * @param parent The parent element
   * @param name The XML element name of the descrptive data
   * @param resource The source resource
   * @param ignoreSelf Ignore self references
   * @param properties The properties that should be interrogated for descriptive data.
   */
  private void addReferenceURIs(Document document, Element parent, String name, Resource resource, boolean ignoreSelf, Property... properties) {
    StmtIterator si;
    Statement statement;
    Resource ref;
    Element elt;
    String id;

    for (Property property: properties) {
      si = this.model.listStatements(resource, property, (Resource) null);
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
   * Add reference data such as class relationships, etc.
   * <p>
   * Elements are organised by property.
   *
   * @param document The document to add to
   * @param parent The parent element
   * @param name The XML element name of the descrptive data
   * @param resource The source resource
   * @param ignoreSelf Ignore self-references
   * @param properties The properties that should be interrogated for descriptive data.
   */
  private void addReferences(Document document, Element parent, String name, Resource resource, DisplayPreference prefs, Collection<? extends Resource> references) {
    Element elt, style;
    String id;

    for (Resource ref: references) {
      if (!ref.isURIResource())
        continue;
      id = this.ids.get(ref);
      elt = document.createElementNS(this.DOSSIER_URI, name);
      if (id != null)
        elt.setAttribute("ref", id);
      if (ref.isURIResource())
        elt.setAttribute("uri", ref.getURI());
      elt.setAttribute("name", this.getLabel(ref)); // Transform can't look up on id() for some reason
      elt.setAttribute("href", this.getHref(ref, false));
      style = prefs == null ? null : this.generateStyle(document, prefs, "graphviz-edge", this.EDGE_ATTRIBUTES);
      if (style != null)
        elt.appendChild(style);
      parent.appendChild(elt);
    }
  }

  /**
   * Generate metadata information, if available.
   *
   * @param document The source document
   * @param resource The resource
   * 
   * @return A metadata element or null for none
   */
  private Element generateMetadata(Document document, Resource resource) {
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
   * Generate style information, if available.
   *
   * @param document The source document
   * @param prefs The preferences
   * @param properties The properties to include
   * 
   * @return A style element or null for none
   */
  private Element generateStyle(Document document, DisplayPreference prefs, String family, Property... properties) {
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
  private Element generateStyle(Document document, Resource resource, String family) {
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
    else
      props = this.NODE_ATTRIBUTES;
    return this.generateStyle(document, prefs, family, props);
  }
  
  /**
   * Generate a diagram reference for an ontology.
   *
   * @param document The source document
   * @param ontology The ontology
   * 
   * @return A diagram reference, or null for none
   */
  private Element generateDiagram(Document document, Ontology ontology) {
    String href = this.configuration.getDiagramFile(ontology);
    Element diagram;
    
    if (href == null)
      return null;
    diagram = document.createElementNS(this.DOSSIER_URI, "diagram");
    diagram.setAttribute("href", href);
    diagram.setAttribute("name", "ontology");
    return diagram;
  }

  /**
   * Add a classifier to an element.
   *
   * @param document The document being built
   * @param element The element ot add to
   * @param classifier The classifier string
   */
  private void addClassifier(Document document, Element element, String classifier) {
    Element cf = document.createElementNS(this.DOSSIER_URI, "classifier");

    cf.setTextContent(classifier);
    element.appendChild(cf);
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
  private Element generateResourceType(Document document, String name, Resource resource) {
    String id = this.createId(resource);
    Element re = document.createElementNS(this.DOSSIER_URI, name);
    Element metadata;

    re.setAttribute("id", id);
    if (resource.isURIResource())
      re.setAttribute("uri", resource.getURI());
    re.setAttribute("name", this.getLabel(resource));
    re.setAttribute("href", this.getHref(resource, true));
    this.addDescriptiveData(document, re, "label", resource, this.prefLabel, RDFS.label, this.altLabel, DCTerms.title, DC.title);
    this.addDescriptiveData(document, re, "description", resource, RDFS.comment, DCTerms.description, DC.description);
    this.addReferenceURIs(document, re, "definition", resource, true, RDFS.isDefinedBy, RDFS.seeAlso);
    metadata = this.generateMetadata(document, resource);
    if (metadata != null)
      re.appendChild(metadata);
    return re;
  }

  /**
   * Generate a class description.
   *
   * @param document The source document
   * @param resource The class
   * 
   * @return The class element
   */
  public Element generateClass(Document document, OntClass resource) {
    Element cls = this.generateResourceType(document, "class", resource);
    DisplayPreference subPrefs = this.preferences.getPreference(this.subClassOf);
    Element style;

    if ((style = this.generateStyle(document, resource, this.FAMILY_GRAPHVIZ_NODE)) != null)
      cls.appendChild(style);
    if ((style = this.generateStyle(document, resource, this.FAMILY_HTML)) != null)
      cls.appendChild(style);
    if (resource.isDataRange())
      this.addClassifier(document, cls, "datatype");
    this.addReferences(document, cls, "sub-class-of", resource, subPrefs, Util.reduce(resource.listSuperClasses().toSet(), true, true, true));
    return cls;
  }

  private void generateRange(Document document, OntResource range, Element parent) {
    RangeGenerator generator = new RangeGenerator(document, parent);

    generator.apply(range);
  }

  /**
   * Generate a property description.
   *
   * @param document The source document
   * @param resource The property
   * 
   * @return The property element
   */
  public Element generateProperty(Document document, OntProperty resource) {
    Element property = this.generateResourceType(document, "property", resource);
    Element r, style;
    DisplayPreference subPrefs = this.preferences.getPreference(this.subPropertyOf);

    if (resource.isDatatypeProperty())
      this.addClassifier(document, property, "datatype");
    if ((style = this.generateStyle(document, resource, this.FAMILY_GRAPHVIZ_NODE)) != null)
      property.appendChild(style);
    if ((style = this.generateStyle(document, resource, this.FAMILY_GRAPHVIZ_EDGE_INCOMING)) != null)
      property.appendChild(style);
    if ((style = this.generateStyle(document, resource, this.FAMILY_GRAPHVIZ_EDGE_OUTGOING)) != null)
      property.appendChild(style);
    if ((style = this.generateStyle(document, resource, this.FAMILY_HTML)) != null)
      property.appendChild(style);
    if (resource.isFunctionalProperty())
      this.addClassifier(document, property, "functional");
    if (resource.isInverseFunctionalProperty())
      this.addClassifier(document, property, "inverse-functional");
    if (resource.isSymmetricProperty())
      this.addClassifier(document, property, "symmetric");
    if (resource.isTransitiveProperty())
      this.addClassifier(document, property, "transitive");
    if (resource.isAnnotationProperty())
      this.addClassifier(document, property, "annotation");
    this.addReferences(document, property, "sub-property-of", resource, subPrefs, Util.reduceProperties(resource.listSuperProperties().toSet(), true, true));
    for (OntResource domain: resource.listDomain().toSet()) {
      r = document.createElementNS(this.DOSSIER_URI, "domain");
      property.appendChild(r);
      this.generateRange(document, domain, r);
    }
    for (OntResource range: resource.listRange().toSet()) {
      r = document.createElementNS(this.DOSSIER_URI, "range");
      property.appendChild(r);
      this.generateRange(document, range, r);
    }
    return property;
  }

  /**
   * Create an ontology element.
   *
   * @param document The parent document
   * @param ont The ontology
   * 
   * @return The ontology element
   */
  public Element generateOntology(Document document, Ontology ont) {
    Resource resource = this.model.createResource(RDFS.Resource.getURI());
    Resource anonymous = this.model.createResource(Dossier.CollectingNode);
    Element ontology = this.generateResourceType(document, "ontology", ont);
    String prefix = this.getPrefix(ont.getNameSpace());
    Element style, diagram;

    if ((style = this.generateStyle(document, ont, this.FAMILY_GRAPHVIZ_GRAPH)) != null)
      ontology.appendChild(style);
    if ((style = this.generateStyle(document, ont, this.FAMILY_HTML)) != null)
      ontology.appendChild(style);
    if (ont.equals(this.primary)) {
      if ((style = this.generateStyle(document, this.preferences.getClassPreference(resource), this.FAMILY_GRAPHVIZ_NODE, this.NODE_ATTRIBUTES)) != null)
        ontology.appendChild(style);
      if ((style = this.generateStyle(document, this.preferences.getClassPreference(resource), this.FAMILY_GRAPHVIZ_EDGE, this.EDGE_ATTRIBUTES)) != null)
        ontology.appendChild(style);
      if ((style = this.generateStyle(document, anonymous, this.FAMILY_GRAPHVIZ_NODE_ANONYMOUS)) != null)
        ontology.appendChild(style);
      if ((style = this.generateStyle(document, this.subClassOf, this.FAMILY_GRAPHVIZ_EDGE_SUPER_CLASS)) != null)
        ontology.appendChild(style);
      if ((style = this.generateStyle(document, this.subPropertyOf, this.FAMILY_GRAPHVIZ_EDGE_SUPER_PROPERTY)) != null)
        ontology.appendChild(style);
    }
    this.addReferenceURIs(document, ontology, "version", this.primary, false, OWL.versionInfo);
    this.addReferenceURIs(document, ontology, "prior-version", this.primary, false, OWL.priorVersion);
    this.addReferenceURIs(document, ontology, "backward-compatible-with", this.primary, false, OWL.backwardCompatibleWith);
    this.addReferenceURIs(document, ontology, "incompatible-with", this.primary, false, OWL.incompatibleWith);
    this.addReferenceURIs(document, ontology, "imports", this.primary, false, OWL.imports);
    if ((diagram = this.generateDiagram(document, ont)) != null)
      ontology.appendChild(diagram);
    for (OntClass clazz: Util.sort(this.collectPrefix(this.referencedClasses, prefix), this.configuration.getDisplayModel(), false))
      ontology.appendChild(this.generateClass(document, clazz));
    for (OntProperty property: Util.sort(this.collectPrefix(this.referencedProperties, prefix)))
      ontology.appendChild(this.generateProperty(document, property));
    return ontology;
  }

  /**
   * Collect all the properties that might be used.
   * <p>
   * The ontology properties are added, as are the immediate super-properties,
   * even if they are from other ontologies.
   * <p>
   * The immedidate domain and range classes are 
   *
   */
  private void collectProperties() {
    ExtendedIterator<? extends OntProperty> si;
    ClassCollector domain, range;

    for (OntProperty prop: this.collectLocal(this.model.listOntProperties())) {
      this.referencedProperties.add(prop);
      si = prop.listSuperProperties(true);
      while (si.hasNext()) {
        this.referencedProperties.add(si.next());
      }
      domain = new ClassCollector();
      domain.collectDomain(prop);
      this.referencedClasses.addAll(Util.reduce(domain.getClasses(), true, false, true));
      range = new ClassCollector();
      range.collectRange(prop);
      this.referencedClasses.addAll(Util.reduce(range.getClasses(), true, false, true));
    }

  }

  /**
   * Collect the classes that need to be used.
   * <p>
   * These are the local ontology classes.
   * The {@link #collectProperties()} method also adds classes used by the properties.
   * We also collect any super-classes of the local classes.
   *
   */
  private void collectClasses() {
    for (OntClass clazz: this.collectLocal(this.model.listNamedClasses())) {
      this.referencedClasses.add(clazz);
      for (OntClass superClass: Util.reduce(clazz.listSuperClasses(true).toSet(), true, true, true))
        this.referencedClasses.add(superClass);
    }  
  }

  /**
   * Generate identifiers for all the classes and properties that we have.
   */
  private void collectIds() {
    for (OntClass clazz: this.referencedClasses)
      this.createId(clazz);
    for (OntProperty property: this.referencedProperties)
      this.createId(property);
  }


  public Document generate() throws Exception {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document;
    Element ontology, subo;
    Ontology sub;

    builderFactory.setNamespaceAware(true);
    builder = builderFactory.newDocumentBuilder();
    document = builder.newDocument();
    this.collectProperties();
    this.collectClasses();
    this.collectIds();
    ontology = this.generateOntology(document, this.primary);
    for (String namespace: this.collectNamespaces()) {
      String prefix = this.getPrefix(namespace);

      if (prefix != null && !prefix.isEmpty()) {
        sub = this.model.createOntology(namespace);
        subo = this.generateOntology(document, sub);
        ontology.appendChild(subo);
      }
    }
    document.appendChild(ontology);
    return document;
  }

  private class RangeGenerator implements RDFList.ApplyFn {
    private Document document;
    private Element parent;

    public RangeGenerator(Document document, Element parent) {
      super();
      this.document = document;
      this.parent = parent;
    }    

    public void apply(RDFNode node) {
      Element previous = this.parent;
      OntClass clazz;

      if (!node.isResource() || !node.canAs(OntClass.class))
        return;
      clazz = node.as(OntClass.class);
      if (clazz.isUnionClass()) {
        UnionClass union = clazz.asUnionClass();

        this.parent = this.document.createElementNS(XmlGenerator.this.DOSSIER_URI, "union");
        previous.appendChild(this.parent);
        union.getOperands().apply(this);
        this.parent = previous;
      } else if (clazz.isIntersectionClass()) {
        IntersectionClass intersection = clazz.asIntersectionClass();

        this.parent = this.document.createElementNS(XmlGenerator.this.DOSSIER_URI, "intersection");
        previous.appendChild(this.parent);
        intersection.getOperands().apply(this);
        this.parent = previous;
      } else if (clazz.isEnumeratedClass()) {
        EnumeratedClass enumeration = clazz.asEnumeratedClass();

        this.parent = this.document.createElementNS(XmlGenerator.this.DOSSIER_URI, "enumeration");
        previous.appendChild(this.parent);
        enumeration.getOneOf().apply(this);
        this.parent = previous;
      } else if (clazz.isComplementClass()) {
        ComplementClass complement = clazz.asComplementClass();

        this.parent = this.document.createElementNS(XmlGenerator.this.DOSSIER_URI, "complement");
        previous.appendChild(this.parent);
        complement.getOperands().apply(this);
        this.parent = previous;
      } else {
        String id = XmlGenerator.this.ids.get(clazz);
        Element element = this.document.createElementNS(XmlGenerator.this.DOSSIER_URI, "class-ref");

        if (id != null)
          element.setAttribute("ref", id);
        element.setAttribute("uri", clazz.getURI());
        element.setAttribute("name", XmlGenerator.this.getLabel(clazz));
        element.setAttribute("href", XmlGenerator.this.getHref(clazz, false));
        this.parent.appendChild(element);
      }
    }

  }


  /**
   * Render style information as a .
   */
  private class StyleRenderer implements PreferenceRenderer {
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
        this.style = this.document.createElementNS(XmlGenerator.this.DOSSIER_URI, "style");   
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
      se = this.document.createElementNS(XmlGenerator.this.DOSSIER_URI, "attribute");
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
