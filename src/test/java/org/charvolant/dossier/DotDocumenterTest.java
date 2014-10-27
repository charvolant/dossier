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

import org.charvolant.dossier.DotDocumenter;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Test cases for display preferences
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class DotDocumenterTest extends AbstractTest {
  /** The dossier ontology */
  private Model dossier;
  /* The configuration */
  private Configuration configuration;
  /** The combined model */
  private OntModel model;
  /** The XML generator */
  private OntologyGenerator generator;
  /** The display preferences */
  private DotDocumenter documenter;
  
  /**
   * Set up display ontology
   *
   * @throws java.lang.Exception
   */
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
  public void testGenerateDot1() throws Exception {
    StringWriter writer = new StringWriter();
    
    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test1.rdf").toString());
    this.generator = new OntologyGenerator(this.configuration, this.model);
    this.documenter = new DotDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("dot-output-1.dot"), this.trim(writer.toString()));
  }

  @Test
  public void testGenerateDot2() throws Exception {
    StringWriter writer = new StringWriter();
    
    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test2.rdf").toString());
    this.generator = new OntologyGenerator(this.configuration, this.model);
    this.documenter = new DotDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("dot-output-2.dot"), this.trim(writer.toString()));
  }

  @Test
  public void testGenerateDot3() throws Exception {
    StringWriter writer = new StringWriter();
    
    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test3.rdf").toString());
    this.generator = new OntologyGenerator(this.configuration, this.model);
    this.documenter = new DotDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("dot-output-3.dot"), this.trim(writer.toString()));
  }

  @Test
  public void testGenerateDot4() throws Exception {
    StringWriter writer = new StringWriter();
    
    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test4.rdf").toString());
    this.generator = new OntologyGenerator(this.configuration, this.model);
    this.documenter = new DotDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("dot-output-4.dot"), this.trim(writer.toString()));
  }

  @Test
  public void testGenerateDot5() throws Exception {
    StringWriter writer = new StringWriter();
    
    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test5.rdf").toString());
    this.generator = new OntologyGenerator(this.configuration, this.model);
    this.documenter = new DotDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("dot-output-5.dot"), this.trim(writer.toString()));
  }

  @Test
  public void testGenerateDot6() throws Exception {
    StringWriter writer = new StringWriter();
    
    this.model = ModelFactory.createOntologyModel();
    this.model.getDocumentManager().setProcessImports(false);
    this.model.read(this.getClass().getResource("test6.rdf").toString());
    this.generator = new OntologyGenerator(this.configuration, this.model);
    this.documenter = new DotDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("dot-output-6.dot"), this.trim(writer.toString()));
  }

  @Test
  public void testGenerateDot7() throws Exception {
    StringWriter writer = new StringWriter();
    
    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("test7.rdf").toString());
    this.generator = new OntologyGenerator(this.configuration, this.model);
    this.documenter = new DotDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("dot-output-7.dot"), this.trim(writer.toString()));
  }

  /**
   * Test auto dogfood consumption.
   *
   * @throws Exception
   */
  @Test
  public void testGenerateDotSelf() throws Exception {
    StringWriter writer = new StringWriter();
    
    this.model = ModelFactory.createOntologyModel();
    this.model.read(this.getClass().getResource("dossier.rdf").toString());
    this.generator = new OntologyGenerator(this.configuration, this.model);
    this.documenter = new DotDocumenter(this.configuration, this.generator.generate());
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
  }
}
