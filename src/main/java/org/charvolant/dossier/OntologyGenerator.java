package org.charvolant.dossier;

import java.util.Collection;
import java.util.Collections;

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
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OntologyGenerator extends ModelGenerator {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(OntologyGenerator.class);

  /** The sub-class property */
  private Property subClassOf;
  /** The sub-property label property */
  private Property subPropertyOf;
  /** The class label property */
  private OntClass owlClass;

  /**
   * Construct an Xml Generator.
   *
   * @param configuration The generator configuration and shared data
   * @param ontology The ontology to generate from
   */
  public OntologyGenerator(Configuration configuration, OntModel ontology) {
    super(configuration, ontology);
    this.subClassOf = this.model.createProperty(RDFS.subClassOf.getURI());
    this.subPropertyOf = this.model.createProperty(RDFS.subPropertyOf.getURI());
    this.owlClass = this.model.createClass(OWL.Class.getURI());
    this.ids = HashBiMap.create();
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
   * Add reference data such as class relationships, etc.
   * <p>
   * Elements are organised by property.
   *
   * @param model The model to use
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
   * Generate a class description.
   *
   * @param document The source document
   * @param resource The class
   * 
   * @return The class element
   */
  public Element generateClass(Document document, OntClass resource) {
    Element cls = this.generateResourceType(this.model, document, "class", resource);
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
    Element property = this.generateResourceType(this.model, document, "property", resource);
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
    Element ontology = this.generateResourceType(this.model, document, "ontology", ont);
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
      if ((style = this.generateStyle(document, this.owlClass, this.FAMILY_GRAPHVIZ_NODE_CLASS)) != null)
        ontology.appendChild(style);
    }
    this.addReferenceURIs(this.model, document, ontology, "version", this.primary, false, OWL.versionInfo);
    this.addReferenceURIs(this.model, document, ontology, "prior-version", this.primary, false, OWL.priorVersion);
    this.addReferenceURIs(this.model, document, ontology, "backward-compatible-with", this.primary, false, OWL.backwardCompatibleWith);
    this.addReferenceURIs(this.model, document, ontology, "incompatible-with", this.primary, false, OWL.incompatibleWith);
    this.addReferenceURIs(this.model, document, ontology, "imports", this.primary, false, OWL.imports);
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
    Document document;
    Element ontology, subo;
    Ontology sub;

    if (this.configuration.getPrefixes() == null) // If not already done
      this.configuration.buildPrefixMap(Collections.singleton(this.model));
    document = this.newDocument();
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

        this.parent = this.document.createElementNS(OntologyGenerator.this.DOSSIER_URI, "union");
        previous.appendChild(this.parent);
        union.getOperands().apply(this);
        this.parent = previous;
      } else if (clazz.isIntersectionClass()) {
        IntersectionClass intersection = clazz.asIntersectionClass();

        this.parent = this.document.createElementNS(OntologyGenerator.this.DOSSIER_URI, "intersection");
        previous.appendChild(this.parent);
        intersection.getOperands().apply(this);
        this.parent = previous;
      } else if (clazz.isEnumeratedClass()) {
        EnumeratedClass enumeration = clazz.asEnumeratedClass();

        this.parent = this.document.createElementNS(OntologyGenerator.this.DOSSIER_URI, "enumeration");
        previous.appendChild(this.parent);
        enumeration.getOneOf().apply(this);
        this.parent = previous;
      } else if (clazz.isComplementClass()) {
        ComplementClass complement = clazz.asComplementClass();

        this.parent = this.document.createElementNS(OntologyGenerator.this.DOSSIER_URI, "complement");
        previous.appendChild(this.parent);
        complement.getOperands().apply(this);
        this.parent = previous;
      } else {
        String id = OntologyGenerator.this.ids.get(clazz);
        Element element = this.document.createElementNS(OntologyGenerator.this.DOSSIER_URI, "class-ref");

        if (id != null)
          element.setAttribute("ref", id);
        element.setAttribute("uri", clazz.getURI());
        element.setAttribute("name", OntologyGenerator.this.getLabel(clazz));
        element.setAttribute("href", OntologyGenerator.this.getHref(clazz, false));
        this.parent.appendChild(element);
      }
    }

  }

}
