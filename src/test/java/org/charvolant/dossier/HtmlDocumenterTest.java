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

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Test cases for XML generator
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class HtmlDocumenterTest extends AbstractTest {
  private Model dossier;
  private Configuration configuration;
  private OntModel model;
  private XmlGenerator generator;
  private HtmlDocumenter documenter;
  
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
  public void testgemerateHtml1() throws Exception {
    StringWriter writer  = new StringWriter();

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test1.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    this.documenter = new HtmlDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("html-output-1.html"), this.trim(writer.toString()));
  }

  @Test
  public void testgemerateHtml2() throws Exception {
    StringWriter writer  = new StringWriter();

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test2.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    this.documenter = new HtmlDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("html-output-2.html"), this.trim(writer.toString()));
  }

  @Test
  public void testgemerateHtml3() throws Exception {
    StringWriter writer  = new StringWriter();

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test3.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    this.documenter = new HtmlDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("html-output-3.html"), this.trim(writer.toString()));
  }

  @Test
  public void testgemerateHtml4() throws Exception {
    StringWriter writer  = new StringWriter();

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test4.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    this.documenter = new HtmlDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("html-output-4.html"), this.trim(writer.toString()));
  }

  @Test
  public void testgemerateHtml5() throws Exception {
    StringWriter writer  = new StringWriter();

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test5.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    this.documenter = new HtmlDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("html-output-5.html"), this.trim(writer.toString()));
  }

  /**
   * See if we can eat our own dogfood.
   *
   * @throws Exception
   */
  @Test
  public void testgemerateHtmlSelf() throws Exception {
    StringWriter writer  = new StringWriter();

    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("dossier.rdf").toString());
    this.generator = new XmlGenerator(this.configuration, this.model);
    this.documenter = new HtmlDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer);
    //System.out.println(writer.toString());
  }
}
