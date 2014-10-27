/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Test cases for XML generator
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class SuiteGeneratorTest extends AbstractTest {
  private Configuration configuration;
  private List<OntModel> models;
  private Model dossier;
  private SuiteGenerator generator;

  private String convertDom(Document doc) throws Exception {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    StreamResult result = new StreamResult(new StringWriter());
    DOMSource source = new DOMSource(doc);
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.transform(source, result);
    return this.trim(result.getWriter().toString());
  }
  
  private void addModel(String name) {
    OntModel model;

    model = ModelFactory.createOntologyModel();
    model.getDocumentManager().setProcessImports(false);
    model.read(this.getClass().getResource(name).toString());
    this.models.add(model);
  }
  
  @Before
  public void setUp() throws Exception {
    this.dossier = ModelFactory.createDefaultModel();
    this.dossier.read(this.getClass().getResource("dossier.rdf").toString());
    this.dossier.read(this.getClass().getResource("standard.rdf").toString());
    this.configuration = new Configuration();
    this.configuration.setDisplayModel(this.dossier);
    this.configuration.setLocale(Locale.ENGLISH);
    this.models = new ArrayList<OntModel>();
  }

  @Test
  public void testgemerateXml1() throws Exception {
    Document document;
    
    this.addModel("test1.rdf");
    this.generator = new SuiteGenerator(this.configuration, this.models);
    document = this.generator.generate();
    //System.out.println(this.convertDom(document));
    assertEquals(this.loadResource("suite-output-1.xml"), this.convertDom(document));
  }


  @Test
  public void testgemerateXml2() throws Exception {
    Document document;
    
    this.addModel("test1.rdf");
    this.addModel("test2.rdf");
    this.generator = new SuiteGenerator(this.configuration, this.models);
    document = this.generator.generate();
    //System.out.println(this.convertDom(document));
    assertEquals(this.loadResource("suite-output-2.xml"), this.convertDom(document));
  }

  @Test
  public void testgemerateXml3() throws Exception {
    Document document;
    
    this.addModel("test5.rdf");
    this.addModel("test6.rdf");
    this.generator = new SuiteGenerator(this.configuration, this.models);
    document = this.generator.generate();
    System.out.println(this.convertDom(document));
    assertEquals(this.loadResource("suite-output-3.xml"), this.convertDom(document));
  }
}
