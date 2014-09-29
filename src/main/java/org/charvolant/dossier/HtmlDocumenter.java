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

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Generate an HTML document describing an ontology.
 * <p>
 * This uses a {@link XmlGenerator} to generate an internal description
 * and then a transform to generate documentation.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class HtmlDocumenter extends Documenter {
  /** The standard template for transformation */
  private static Templates XSLT;
  
  {
    TransformerFactory factory = TransformerFactory.newInstance();
    
    XSLT = factory.newTemplates(
        new StreamSource(
            HtmlDocumenter.class.getResourceAsStream("dossier-html.xsl")
        )
    );
  }
  
  /**
   * Construct a generator for an ontology.
   *
   * @param configuration The doumenter configuration
   * @param document The document describing the ontology
   * 
   * @throws Exception if unable to construct the generator or transformer
   */
  public HtmlDocumenter(Configuration configuration, Document document) throws Exception {
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
    OntModel model;
    Model displayModel;
    HtmlDocumenter generator;
    XmlGenerator xmlGenerator;
    File out = new File(args[1]);
    FileWriter ow = new FileWriter(out);
    Configuration config = new Configuration();

    displayModel = ModelFactory.createDefaultModel();
    displayModel.read(HtmlDocumenter.class.getResource("dossier.rdf").toString());
    displayModel.read(HtmlDocumenter.class.getResource("standard.rdf").toString());    
    model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
    model.getDocumentManager().setProcessImports(false);
    model.read(args[0]);
    config.createNamespaceRoot(model);
    config.setDisplayModel(displayModel);
    xmlGenerator = new XmlGenerator(config, model);
    generator = new HtmlDocumenter(config, xmlGenerator.generate());
    generator.generate(ow);
    ow.close();
  }

}
