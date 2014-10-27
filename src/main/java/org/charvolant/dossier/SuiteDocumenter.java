/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Generate documentation for a suite of ontologies.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class SuiteDocumenter extends Documenter {
  /** The standard template for transformation */
  private static Templates XSLT;

  {
    TransformerFactory factory = TransformerFactory.newInstance();

    try {
      XSLT = factory.newTemplates(
          new StreamSource(
              SuiteDocumenter.class.getResourceAsStream("dossier-suite-html.xsl")
              )
          );
    } catch (TransformerConfigurationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  /**
   * Construct a suite documenter.
   *
   * @param configuration The suite configuration
   * @param document The document to generate from
   */
  public SuiteDocumenter(Configuration configuration, Document document) {
    super(configuration, document);
  }

  /**
   * Generate an HTML document and write it out.
   * 
   * @param writer The destination
   * 
   * @throws Exception if unable to process the result
   */
  public void generate(Writer writer) throws Exception {
    this.transform(this.XSLT, writer);
  }

  /**
   * Run for a specific ontology.
   * <p>
   * Takes a single argument, the ontology URL
   *
   * @param args The arguments
   */
  public static void main(String[] args) throws Exception {
    List<OntModel> models = new ArrayList<OntModel>();
    OntModel model;
    Model displayModel;
    SuiteDocumenter documenter;
    SuiteGenerator generator;
    File out = new File(args[args.length - 1]);
    FileWriter ow = new FileWriter(out);
    Configuration config = new Configuration();

    displayModel = ModelFactory.createDefaultModel();
    displayModel.read(HtmlDocumenter.class.getResource("dossier.rdf").toString());
    displayModel.read(HtmlDocumenter.class.getResource("standard.rdf").toString());    
    for (int i = 0; i < args.length - 1; i++) {
      model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
      model.getDocumentManager().setProcessImports(false);
      model.read(args[i]);
      config.createNamespaceRoot(model, Util.getBaseName(new URI(args[i])));
      models.add(model);
    }
    config.setDisplayModel(displayModel);
    generator = new SuiteGenerator(config, models);
    documenter = new SuiteDocumenter(config, generator.generate());
    documenter.generate(ow);
    ow.close();
  }

}
