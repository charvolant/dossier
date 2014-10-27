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
public class SuiteDotDocumenterTest extends AbstractTest {
  private Configuration configuration;
  private List<OntModel> models;
  private Model dossier;
  private SuiteGenerator generator;
  private SuiteDotDocumenter documenter;
  
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
  public void testgenerateDot1() throws Exception {
    Document document;
    StringWriter writer = new StringWriter();
    
    this.addModel("test1.rdf");
    this.generator = new SuiteGenerator(this.configuration, this.models);
    document = this.generator.generate();
    this.documenter = new SuiteDotDocumenter(this.configuration, document);
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("suite-dot-output-1.dot"), this.trim(writer.toString()));
  }


  @Test
  public void testgenerateDot2() throws Exception {
    Document document;
    StringWriter writer = new StringWriter();
    
    this.addModel("test1.rdf");
    this.addModel("test2.rdf");
    this.generator = new SuiteGenerator(this.configuration, this.models);
    document = this.generator.generate();
    this.documenter = new SuiteDotDocumenter(this.configuration, document);
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("suite-dot-output-2.dot"), this.trim(writer.toString()));
  }

  @Test
  public void testgenerateDot3() throws Exception {
    Document document;
    StringWriter writer = new StringWriter();
    
    this.addModel("test4.rdf");
    this.addModel("test5.rdf");
    this.addModel("test6.rdf");
    this.generator = new SuiteGenerator(this.configuration, this.models);
    document = this.generator.generate();
    this.documenter = new SuiteDotDocumenter(this.configuration, document);
    this.documenter.generate(writer, Format.DOT);
    //System.out.println(writer.toString());
    assertEquals(this.loadResource("suite-dot-output-3.dot"), this.trim(writer.toString()));
  }
}
