/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.Locale;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
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

  /**
   * Construct an empty configuration.
   */
  public Configuration() {
    this.displayModel = ModelFactory.createDefaultModel();
    this.roots = HashBiMap.create();
    this.namespaces = HashBiMap.create();
    this.locale = Locale.getDefault();
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
  public void createNamespaceRoot(OntModel model) {
    Ontology ontology = this.choosePrimaryOntology(model);
    String ns = model.getNsPrefixURI("");
    int current = 0;
    String base, candidiate;

    if (!this.roots.containsKey(ns)) {
      base = ontology.getLocalName().toLowerCase();
      candidiate = base;
      while (this.roots.inverse().containsKey(candidiate)) {
        candidiate = base + "_" + ++current;
      }
      this.roots.put(ns, candidiate);
    }
    if (!this.namespaces.containsKey(ontology.getURI()))
      this.namespaces.put(ontology.getURI(), ns);
  }

  /**
   * Choose the primary ontology from a model.
   * <p>
   * By default, this is the ontology not imported.
   * 
   * @param model The ontology model
   * 
   * @return The primary ontology
   */
  public Ontology choosePrimaryOntology(OntModel model) {
    ExtendedIterator<Ontology> oi = model.listOntologies();
    Ontology primary = null, candidate;

    while (oi.hasNext()) {
      candidate = oi.next();
      if (primary == null)
        primary = candidate;
      else if (candidate.imports(primary))
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



}
