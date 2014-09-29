package org.charvolant.dossier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Abstract generator for complex descriptions of ontologies.
 * <p>
 * By the time this is finished, there should be some suitable
 * intermediate representation of the ontology that can be used to build
 * something useful to the outside world.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
abstract public class Generator {
  /** The list of XSD datatypes that automatically make the resource a datatype and the class present */
  private static final Resource[] XSD_DATATYPES = {
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
    DC.rights
  };

  /** The configuration that is used to handle cross-ontology links */
  protected Configuration configuration;
  /** The ontology that we intend to graph. This is built over the  */
  protected OntModel ontology;
  /** The model that we intend to graph. This is built over the base ontology  */
  protected OntModel model;
  /** The primary ontology to use */
  protected Ontology primary;
  /** The map of resources onto internal ids */
  protected
  BiMap<Resource, String> ids;
  /** The display preferences for this generator */
  protected DisplayPreferences preferences;
  /** The classes referenced by this generation */
  protected Set<OntClass> referencedClasses;
  /** The properties referenced by this generation */
  protected Set<OntProperty> referencedProperties;
  /** The language order to use */
  protected List<String> languageOrder;

  

  /**
   * Construct a Generator.
   *
   * @param configuration The generation configuration
   * @param ontology The ontology to generate from
   * @param displayModel The displayModel to use
   */
  public Generator(Configuration configuration, OntModel ontology) {
    super();
    this.configuration = configuration;
    this.ontology = ontology;
    this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, this.ontology);
    this.model.setNsPrefixes(PrefixMapping.Extended);
    this.ids = HashBiMap.create();
    this.preferences = new DisplayPreferences(this.ontology, this.configuration.getDisplayModel());
    this.referencedClasses = new HashSet<OntClass>();
    this.referencedProperties = new HashSet<OntProperty>();
    this.primary = this.configuration.choosePrimaryOntology(this.model);
    this.languageOrder = this.chooseLanguageOrder();
    this.addDatatypes();
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
   * Get the ontology.
   *
   * @return the ontology
   */
  public OntModel getOntology() {
    return this.ontology;
  }

  /**
   * Get the model.
   *
   * @return the model
   */
  public OntModel getModel() {
    return this.model;
  }

  /**
   * Get the primary.
   *
   * @return the primary
   */
  public Ontology getPrimary() {
    return this.primary;
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
    String prefix;
  
    if (resource.isAnon())
      return "";
    builder = new StringBuilder();
    prefix = this.model.getNsURIPrefix(resource.getNameSpace());
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
   * @param resource The resource to examine
   * @param lang The language (null for not specified)
   * @param sb The target builder
   * @param property The property to use
   * 
   * @return True if one has been found
   */
  protected boolean appendProperty(Resource resource, String lang, StringBuilder sb, Property property) {
    StmtIterator si = this.model.listStatements(resource, property, null, lang);

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
   * @param resource The resource
   * @param locale The locale
   * @param sb The string builder
   * @param properties The properties to try
   */
  protected void appendProperty(Resource resource, StringBuilder sb, Property...properties) {
    Locale locale = this.configuration.getLocale();
    
    for (Property property: properties) {
      if (!locale.getCountry().isEmpty() && this.appendProperty(resource, (locale.getLanguage() + "-" + locale.getCountry()).toLowerCase(), sb, property))
        return;
      if (this.appendProperty(resource, locale.getLanguage().toLowerCase(), sb, property))
        return;
      if (this.appendProperty(resource, "", sb, property))
        return;
      if (this.appendProperty(resource, null, sb, property))
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
   * @param resource The resource to examine
   * 
   * @return
   */
  protected String getTooltip(Resource resource) {
    StringBuilder sb = new StringBuilder();

    this.appendProperty(resource, sb, DCTerms.title, DC.title, RDFS.label);
    this.appendProperty(resource, sb, DCTerms.description, DC.description, RDFS.comment);
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
   * @param resource The resource to examine
   * 
   * @return
   */
  protected String getTitle(Resource resource) {
    StringBuilder sb = new StringBuilder();

    this.appendProperty(resource, sb, DCTerms.title, DC.title, RDFS.label);
    if (sb.length() == 0)
      sb.append(resource.getLocalName());
    return sb.toString();
  }
  
  /**
   * See if a resource is local to the model.
   *
   * @param resource The resource
   * 
   * @return True if the resource is local
   */
  protected boolean isLocal(Resource resource) {
    String prefix;
    
    if (!resource.isURIResource() || resource.getNameSpace() == null)
      return true;
    prefix = this.model.getNsURIPrefix(resource.getNameSpace());
    return prefix == null || prefix.isEmpty();
  }

  
  /**
   * Add datatype information to domains and ranges that happen to fall into
   * the model but which have not been specified.
   */
  protected void addDatatypes() {
    Resource rdfsDatatype = this.model.createResource(RDFS.Datatype.getURI());

    for (Resource datatype: this.XSD_DATATYPES) {
      if (this.model.contains(null, null, datatype) && !this.model.contains(datatype, RDF.type, rdfsDatatype))
        this.model.add(datatype, RDF.type, rdfsDatatype);
    }
  }
  

  /**
   * Reduce a list of candidiate elements to things
   * the local model regards as part of its namespace.
   *
   * @param candidates The possible candidiates
   * 
   * @return A set of local items
   */
  protected <T extends Resource> Set<T> collectLocal(ExtendedIterator<T> candidates) {
    Set<T> chosen = new HashSet<T>();

    while (candidates.hasNext()) {
      T candidate = candidates.next();
      
      if (this.isLocal(candidate))
        chosen.add(candidate);
    }
    return chosen;  
  }

  /**
   * Collect elements that match a specific prefix.
   *
   * @param candidates The possible candidiates
   * @param prefix Only collect candidates where the namespace resolves to this prefix
   * 
   * @return A set of local items
   */
  protected <T extends Resource> Set<T> collectPrefix(Collection<T> candidates, String prefix) {
    Set<T> chosen = new HashSet<T>();
    String pf;

    if (prefix == null)
      prefix = "";
    for (T candidate: candidates) {
      pf = this.model.getNsURIPrefix(candidate.getNameSpace());
      if (prefix.equals(pf))
          chosen.add(candidate);
    }
    return chosen;  
  }
  
  /**
   * Collect the namespaces used by the classes and properties.
   *
   * @return The collected namespace set
   */
  protected Set<String> collectNamespaces() {
    Set<String> namespaces = new HashSet<String>();
    
    for (OntClass clazz: this.referencedClasses)
      namespaces.add(clazz.getNameSpace());
    for (OntProperty property: this.referencedProperties)
      namespaces.add(property.getNameSpace());
    return namespaces;
  }

}