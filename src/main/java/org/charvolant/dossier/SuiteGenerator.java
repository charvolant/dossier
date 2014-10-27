/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Generate a description of a suite of ontologies.
 * <p>
 * The suite is generally intended as an index document of some
 * sort
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class SuiteGenerator extends Generator {
  /** The ontologies to diagram */
  private List<OntModel> models;
  /** The primary ontology URIs */
  private Map<String, Ontology> primaries;
  /** The list of ontologies */
  private List<Ontology> ontologies;
  
  /**
   * Construct a suite generator.
   *
   * @param configuration The configuration
   * @param models The models to generate for
   */
  public SuiteGenerator(Configuration configuration, List<OntModel> models) {
    super(configuration);
    this.models = models;
  }

  /** 
   * @{inheritDoc}
   * <p>
   * There are no local ontolgies in this generator.
   *
   * @see org.charvolant.dossier.Generator#isLocal(com.hp.hpl.jena.rdf.model.Resource)
   */
  @Override
  protected boolean isLocal(Resource resource) {
    return false;
  }

  /**
   * Build the ontology list.
   * <p>
   * The primary ontology map is built first, using the chosen
   * primary ontologies. Then everything else is piled in.
   */
  protected void buildOntologies() {
    Set<String> seen;
    
    this.primaries = new HashMap<String, Ontology>(this.models.size());
    for (OntModel om: this.models) {
      Ontology primary = this.configuration.choosePrimaryOntology(om);
      
      if (primary != null)
        this.primaries.put(primary.getURI(), primary);
    }
    seen = new HashSet<String>(this.primaries.keySet());
    // Standard ontologies that we shouldn't be bothered about
    seen.add(OWL.getURI()); 
    seen.add(RDF.getURI());
    seen.add(RDFS.getURI());
    // Variations on the primary ontology names caused by inaccurate imports
    for (String uri: this.primaries.keySet())
      if (!uri.endsWith("#") && !uri.endsWith("/")) {
        seen.add(uri + "#");
        seen.add(uri + "/");
      }
    this.ontologies = new ArrayList<Ontology>(this.primaries.values());
    Collections.sort(this.ontologies, new OntologyComparator());
    for (OntModel om: this.models) {
      ExtendedIterator<Ontology> oi = om.listOntologies();
      
      while (oi.hasNext()) {
        Ontology ont = oi.next();

        if (!seen.contains(ont.getURI())) {
          this.ontologies.add(ont);
          seen.add(ont.getURI());
        }
      }
    }
  }
 
  /**
   * Generate an ontology definition.
   *
   * @param document The document
   * @param ontology The ontology
   * 
   * @return An XML element describing the ontology
   */
  private Element generateOntology(Document document, Ontology ontology) {
    OntModel model = (OntModel) ontology.getModel();
    Element ont = this.generateResourceType(model, document, "ontology", ontology);
    Element style;

    this.preferences = new DisplayPreferences(model, this.configuration.getDisplayModel());
    if ((style = this.generateStyle(document, this.preferences.getClassPreference(OWL.Ontology), this.FAMILY_GRAPHVIZ_NODE, this.NODE_ATTRIBUTES)) != null)
      ont.appendChild(style);
    if ((style = this.generateStyle(document, this.preferences.getClassPreference(OWL.imports), this.FAMILY_GRAPHVIZ_EDGE_IMPORT, this.EDGE_ATTRIBUTES)) != null)
      ont.appendChild(style);
    this.addReferenceURIs(model, document, ont, "version", ontology, false, OWL.versionInfo);
    this.addReferenceURIs(model, document, ont, "prior-version", ontology, false, OWL.priorVersion);
    this.addReferenceURIs(model, document, ont, "backward-compatible-with", ontology, false, OWL.backwardCompatibleWith);
    this.addReferenceURIs(model, document, ont, "incompatible-with", ontology, false, OWL.incompatibleWith);
    this.addReferenceURIs(model, document, ont, "imports", ontology, false, OWL.imports);
    return ont;
  }

  /** 
   * @{inheritDoc}
   *
   * @see org.charvolant.dossier.Generator#generate()
   */
  @Override
  public Document generate() throws Exception {
    Document document;
    Element ontologies, ont, diagram;
 
    if (this.configuration.getPrefixes() == null) // If not already done
      this.configuration.buildPrefixMap(this.models);
    this.buildOntologies();
    document = this.newDocument();
    ontologies = document.createElementNS(this.DOSSIER_URI, "ontologies");
    document.appendChild(ontologies);
    for (Ontology ontology: this.ontologies)
      this.createId(ontology);
    diagram = document.createElementNS(this.DOSSIER_URI, "diagram");
    diagram.setAttribute("href", "index.svg");
    diagram.setAttribute("name", "imports");
    ontologies.appendChild(diagram);
    for (Ontology ontology: this.ontologies) {
      ont = this.generateOntology(document, ontology);
      ontologies.appendChild(ont);
    }
      
    return document;
  }
}
