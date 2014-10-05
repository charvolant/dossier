/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Generate an graphviz DOT description describing an ontology.
 * <p>
 * This uses a {@link XmlGenerator} to generate an internal description
 * and then a transform to generate documentation.
 * 
 * @see http://www.graphviz.org
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class DotDocumenter extends Documenter {
  /** The standard template for transformation */
  private static Templates XSLT;
  
  {
    TransformerFactory factory = TransformerFactory.newInstance();
    
    XSLT = factory.newTemplates(
        new StreamSource(
            DotDocumenter.class.getResourceAsStream("dossier-dot.xsl")
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
  public DotDocumenter(Configuration configuration, Document document) throws Exception {
    super(configuration, document);    
  }
  
  
  /**
   * Generate grapviz dot document and write it out.
   * <p>
   * If the format is not {@link Format#DOT} then dot is called to
   * generate the appropriate output form.
   * 
   * @param writer The destination
   * @param format The output format
   * 
   * @throws Exception if unable to process the result
   */
  public void generate(Writer writer, Format format) throws Exception {
    switch (format) {
    case HTML:
      throw new IllegalArgumentException("Format " + format + " is not a valid output format");
    case DOT:
      this.transform(this.XSLT, writer);
      break;
    case SVG:
      ProcessBuilder pb = new ProcessBuilder("dot", "-Tsvg");
      Process pr = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
      Writer input = new OutputStreamWriter(pr.getOutputStream());
      char[] buffer = new char[1024];
      int n;
      
      this.transform(this.XSLT, input);
      input.close();
      while ((n = reader.read(buffer)) > 0)
        writer.write(buffer, 0, n);
      reader.close();
    }
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
    DotDocumenter generator;
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
    config.createNamespaceRoot(model, Util.getBaseName(new URI(args[0])));
    config.setDisplayModel(displayModel);
    xmlGenerator = new XmlGenerator(config, model);
    generator = new DotDocumenter(config, xmlGenerator.generate());
    generator.generate(ow, Format.SVG);
    ow.close();
  }

}
