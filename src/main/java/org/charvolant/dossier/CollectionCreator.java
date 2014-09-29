/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Create documentation for a collection of ontologies.
 * <p>
 * Where possible, the ontologies will reference each other.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class CollectionCreator {
  /** The configuration to use */
  private Configuration configuration;
  /** The ontologies */
  private List<OntModel> ontologies;
  
  /**
   * Construct a collection creator.
   *
   */
  public CollectionCreator(Configuration configuration) {
    this.configuration = configuration;
    this.ontologies = new ArrayList<OntModel>();
  }
  
  /**
   * Add an ontology model to the list of things that need to be generated.
   *
   * @param ontology The ontology
   */
  public void addOntology(OntModel ontology) {
    this.ontologies.add(ontology);
    this.configuration.createNamespaceRoot(ontology);
  }
  
  /**
   * Copy a resource into the target directory.
   *
   * @param directory The target directory
   * @param name The name in the directory
   * @param resourcePath The resource path relative to this class
   * 
   * @throws IOException if the copy fails
   */
  private void copyResource(File directory, String name, String resourcePath) throws IOException {
    OutputStream os = new FileOutputStream(new File(directory, name));
    InputStream is = this.getClass().getResourceAsStream(resourcePath);
    byte[] buffer = new byte[1024];
    int n;
    
    while ((n = is.read(buffer)) >=0)
      os.write(buffer, 0, n);
    os.close();
    is.close();
  }
    
  /**
   * Generate documentation for each ontology.
   *
   * @param directory The directory to place the generated documentation into
   * 
   * @throws Exception when something goes horribly wrong
   */
  public void generate(File directory) throws Exception {
    File css = new File(directory, "css");
    
    css.mkdirs();
    this.copyResource(css, "dossier.css", "css/dossier.css");
    this.copyResource(css, "flags.png", "css/flags.png");
    for (OntModel ontology: this.ontologies) {
      XmlGenerator generator = new XmlGenerator(this.configuration, ontology);
      Document document = generator.generate();
      Ontology ont = generator.getPrimary();
      File html = new File(directory, this.configuration.getHtmlFile(ont));
      File svg = new File(directory, this.configuration.getDiagramFile(ont));
      HtmlDocumenter htmlDoc = new HtmlDocumenter(this.configuration, document);
      DotDocumenter dotDoc = new DotDocumenter(this.configuration, document);
      Writer writer;
      
      writer = new FileWriter(html);
      htmlDoc.generate(writer);
      writer.close();
      
      writer = new FileWriter(svg);
      dotDoc.generate(writer, Format.SVG);
      writer.close();
    }
  }
  
  

  /**
   * Run for a list of ontologies.
   * <p>
   * Takes a series of argument, ontology URLs, followed by an output directory
   *
   * @param args The arguments
   */
  public static void main(String[] args) throws Exception {
    OntModel model;
    Model displayModel;
    File directory = new File(args[args.length - 1]);
    Configuration config = new Configuration();
    CollectionCreator creator;
    int i;

    displayModel = ModelFactory.createDefaultModel();
    displayModel.read(HtmlDocumenter.class.getResource("dossier.rdf").toString());
    displayModel.read(HtmlDocumenter.class.getResource("standard.rdf").toString());    
    config.setDisplayModel(displayModel);
    creator = new CollectionCreator(config);
    for (i = 0; i < args.length - 1; i++) {
      model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
      model.getDocumentManager().setProcessImports(false);
      model.read(args[i]);
      creator.addOntology(model);
    }
    creator.generate(directory);
  }

}
