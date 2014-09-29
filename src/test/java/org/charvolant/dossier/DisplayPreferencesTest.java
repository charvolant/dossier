/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.charvolant.dossier.vocabulary.Dossier;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Test cases for display preferences
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class DisplayPreferencesTest {
  /** The dossier ontology */
  private OntModel dossier;
  /** The test ontology */
  private OntModel test;
  /** The display preferences */
  private DisplayPreferences preferences;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    this.dossier = ModelFactory.createOntologyModel();
    this.dossier.getDocumentManager().setProcessImports(false);
    this.dossier.read(this.getClass().getResource("dossier.rdf").toString());
    this.dossier.read(this.getClass().getResource("display-prefs.rdf").toString());
    this.test = ModelFactory.createOntologyModel();
    this.test.read(this.getClass().getResource("display-prefs.rdf").toString());
    this.preferences = new DisplayPreferences(this.test, this.dossier);
  }

  @Test
  public void testGetPreference1() {
    DisplayPreference pref = this.preferences.getPreference(this.test.createResource(RDFS.Class.getURI()));
    
    assertNotNull(pref);
    assertEquals("box", pref.getStringPref(Dossier.nodeShape, null));
  }

  @Test
  public void testGetPreference2() {
    DisplayPreference pref = this.preferences.getPreference(this.test.createResource("http://localhost/prefs#A"));
    
    assertNotNull(pref);
    assertEquals("box", pref.getStringPref(Dossier.nodeShape, null));
  }

  @Test
  public void testGetPreference3() {
    DisplayPreference pref = this.preferences.getPreference(this.test.createResource("http://localhost/prefs#B"));
    
    assertNotNull(pref);
    assertEquals("box", pref.getStringPref(Dossier.nodeShape, null));
  }

  @Test
  public void testGetPreference4() {
    DisplayPreference pref = this.preferences.getPreference(this.test.createResource("http://localhost/prefs#b1"));
    
    assertNotNull(pref);
    assertEquals("ellipse", pref.getStringPref(Dossier.nodeShape, null));
  }

  @Test
  public void testGetPreference5() {
    DisplayPreference pref = this.preferences.getPreference(this.test.createResource("http://localhost/prefs#C"));
    
    assertNotNull(pref);
    assertEquals("box", pref.getStringPref(Dossier.nodeShape, null));
  }

  @Test
  public void testGetPreference6() {
    DisplayPreference pref = this.preferences.getPreference(this.test.createResource("http://localhost/prefs#D"));
    
    assertNotNull(pref);
    assertEquals("box", pref.getStringPref(Dossier.nodeShape, null));
  }

  @Test
  public void testGetPreference7() {
    DisplayPreference pref = this.preferences.getPreference(this.test.createResource("http://localhost/prefs#d1"));
    
    assertNotNull(pref);
    assertEquals("circle", pref.getStringPref(Dossier.nodeShape, null));
    assertEquals("normal", pref.getStringPref(Dossier.arrowHead, null));
  }

  @Test
  public void testGetPreference8() {
    DisplayPreference pref = this.preferences.getPreference(this.test.createResource("http://localhost/prefs#E"));
    
    assertNotNull(pref);
    assertEquals("box", pref.getStringPref(Dossier.nodeShape, null));
  }

  @Test
  public void testGetPreference9() {
    DisplayPreference pref = this.preferences.getPreference(this.test.createResource("http://localhost/prefs#F"));
    
    assertNotNull(pref);
    assertEquals("box", pref.getStringPref(Dossier.nodeShape, null));
  }

}
