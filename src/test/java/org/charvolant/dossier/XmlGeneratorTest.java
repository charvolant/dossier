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
public class XmlGeneratorTest extends AbstractTest {
  private Configuration configuration;
  private OntModel model;
  private Model dossier;
  private XmlGenerator generator;

  private String convertDom(Document doc) throws Exception {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    StreamResult result = new StreamResult(new StringWriter());
    DOMSource source = new DOMSource(doc);
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.transform(source, result);
    return this.trim(result.getWriter().toString());
  }
  
  @Before
  public void setUp() throws Exception {
    this.dossier = ModelFactory.createDefaultModel();
    this.dossier.read(this.getClass().getResource("dossier.rdf").toString());
    this.dossier.read(this.getClass().getResource("standard.rdf").toString());
    this.configuration = new Configuration();
    this.configuration.setDisplayModel(this.dossier);
    this.configuration.setLocale(Locale.ENGLISH);
  }

  @Test
  public void testgemerateXml1() throws Exception {
    Document document;

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test1.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    document = this.generator.generate();
    assertEquals(this.loadResource("xml-output-1.xml"), this.convertDom(document));
    //System.out.println(this.convertDom(document));
  }

  @Test
  public void testgemerateXml2() throws Exception {
    Document document;

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test2.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    document = this.generator.generate();
    //System.out.println(this.convertDom(document));
    assertEquals(this.loadResource("xml-output-2.xml"), this.convertDom(document));
  }

  @Test
  public void testgemerateXml3() throws Exception {
    Document document;

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test3.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    document = this.generator.generate();
    //System.out.println(this.convertDom(document));
    assertEquals(this.loadResource("xml-output-3.xml"), this.convertDom(document));
  }

  @Test
  public void testgemerateXml4() throws Exception {
    Document document;

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test4.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    document = this.generator.generate();
    //System.out.println(this.convertDom(document));
    assertEquals(this.loadResource("xml-output-4.xml"), this.convertDom(document));
  }

  @Test
  public void testgemerateXml5() throws Exception {
    Document document;

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test5.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    document = this.generator.generate();
    assertEquals(this.loadResource("xml-output-5.xml"), this.convertDom(document));
    //System.out.println(this.convertDom(document));
  }

  /**
   * Just tests to see whether can eat our own dogfood.
   *
   * @throws Exception
   */
  @Test
  public void testgemerateXmlSelf() throws Exception {
    @SuppressWarnings("unused")
    Document document;

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("dossier.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    document = this.generator.generate();
    //System.out.println(this.convertDom(document));
  }
}
