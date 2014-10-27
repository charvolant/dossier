/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * The generator configuration.
 * <p>
 * This contains global information about the range and type of documents to
 * be generated, the display preferences and so on.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class Configuration {
  private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
  
  /** The list of structural ontologies that should generally be overridden */
  private static final Set<String> STRUCTURE_URIS = Sets.newHashSet(XSD.getURI(), OWL.getURI(), RDF.getURI(), RDFS.getURI());

  /** The display preferences model */
  private Model displayModel;
  /** The locale for internationalisation */
  private Locale locale;
  /** The map of namespaces onto file name roots */
  private BiMap<String, String> roots;
  /** The map of resource URIs onto default namespaces */ 
  private BiMap<String, String> namespaces;
  /** The map of namespaces to prefixes */
  private PrefixMapping prefixes;

  /**
   * Construct an empty configuration.
   */
  public Configuration() {
    this.displayModel = ModelFactory.createDefaultModel();
    this.roots = HashBiMap.create();
    this.namespaces = HashBiMap.create();
    this.locale = Locale.getDefault();
    this.prefixes = null;
  }

  /**
   * Set the display model.
   *
   * @param displayModel the new display model 
   */
  public void setDisplayModel(Model displayModel) {
    this.displayModel = displayModel;
  }

  /**
   * Get the display model.
   *
   * @return the display model
   */
  public Model getDisplayModel() {
    return this.displayModel;
  }

  /**
   * Get the locale.
   *
   * @return the locale
   */
  public Locale getLocale() {
    return this.locale;
  }

  /**
   * Set the locale.
   *
   * @param locale the new locale 
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * Get the root for a file name.
   * <p>
   * The root is normally derived from the 
   *
   * @param resource The resource
   * 
   * @return The root file name for the thing documenting the resource
   */
  private String getNamespaceRoot(Resource resource) {
    String uri, ns;
    
    if (!resource.isURIResource())
      return null;
    uri = resource.getURI();
    ns = this.namespaces.get(uri);
    if (ns == null)
      ns = resource.getNameSpace();
    return this.roots.get(ns);
  }

  /**
   * Create a namespace root for a model.
   * <p>
   * The primary ontology is used.
   *
   * @param model The model
   * 
   * @see #choosePrimaryOntology(OntModel)
   */
  public void createNamespaceRoot(OntModel model, String base) {
    Ontology ontology = this.choosePrimaryOntology(model);
    String ns = model.getNsPrefixURI("");
    int current = 0;
    String candidate;

    this.logger.debug("Primary ontology " + ontology.getURI() + " has namespace " + ns);
    if (!this.roots.containsKey(ns)) {
      candidate = base;
      while (this.roots.inverse().containsKey(candidate)) {
        candidate = base + "_" + ++current;
      }
      this.logger.debug("Adding root for " + ns + " as " + candidate);
      this.roots.put(ns, candidate);
    }
    if (!this.namespaces.containsKey(ontology.getURI()))
      this.namespaces.put(ontology.getURI(), ns);
  }
  
  /**
   * Build the prefix map from a collection of ontological models.
   * <p>
   * The prefix map is constructed from the models, using
   * the import hierarchy as a guide.
   * <p>
   * For any unmapped namespaces that we know about,
   * if the namespace has a root, then return the root as a prefix.
   * 
   * @param models The models
   *
   */
  public void buildPrefixMap(Collection<OntModel> models) {
    List<OntModel> sorted = Util.sort(models, this);
    
    this.prefixes = PrefixMapping.Factory.create();
    this.prefixes.setNsPrefixes(PrefixMapping.Extended);
    for (OntModel model: sorted)
      this.prefixes.setNsPrefixes(model.getNsPrefixMap());
    for(Entry<String, String> entry: this.roots.entrySet())
      if (this.prefixes.getNsURIPrefix(entry.getKey()) == null)
        this.prefixes.setNsPrefix(entry.getValue(), entry.getKey());
  }
  
  
  /**
   * Get the prefix mapping for all ontologies
   * <p>
   * The prefix map must have been intialised before calling this method.
   * 
   * @return The prefix mapping
   * 
   * @see #buildPrefixMap(Collection)
   */
  public PrefixMapping getPrefixes() {
    return this.prefixes;
  }

  /**
   * Choose the primary ontology from a model.
   * <p>
   * Preferentially, this is the ontology that has a URI that matches the default namespace
   * (possibly without a trailing "#" or "/")
   * Otherwise, by default, this is the ontology not imported.
   * 
   * TODO I'm sick of guessing. Next job is to make this explicit
   * 
   * @param model The ontology model
   * 
   * @return The primary ontology
   */
  public Ontology choosePrimaryOntology(OntModel model) {
    ExtendedIterator<Ontology> oi = model.listOntologies();
    Ontology primary = null, candidate;
    String base = model.getNsPrefixURI("");
    String guess = base != null && (base.endsWith("#") || base.endsWith("/")) ? base.substring(0, base.length() - 1) : base;
    
    while (oi.hasNext()) {
      candidate = oi.next();
      
      if (primary == null)
        primary = candidate;
      else if (candidate.imports(primary))
        primary = candidate;
      else if (candidate.getURI().equals(base) || candidate.getURI().equals(guess))
        primary = candidate;
      else if (this.STRUCTURE_URIS.contains(primary.getURI()))
        primary = candidate;
    }
    return primary;
  }


  /**
   * Get the HTML file name for a resource.
   *
   * @param resource The resource
   * 
   * @return The location of the HTML file where the resource resides
   */
  public String getHtmlFile(Resource resource) {
    String root = this.getNamespaceRoot(resource);

    return root == null ? null : root + ".html";
  }

  /**
   * Get the main diagram file name for a resource.
   *
   * @param resource The resource
   * 
   * @return The location of the HTML file where the resource resides
   */
  public String getDiagramFile(Resource resource) {
    String root = this.getNamespaceRoot(resource);

    return root == null ? null : root + ".svg";
  }



  /**
   * Get the XML file name for a resource.
   *
   * @param resource The resource
   * 
   * @return The location of the XML file where the resource resides
   */
  public String getXmlFile(Resource resource) {
    String root = this.getNamespaceRoot(resource);

    return root == null ? null : root + ".xml";
  }


}
