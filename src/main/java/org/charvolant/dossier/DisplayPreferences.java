/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A repository of preferences associated with a model.
 * <p>
 * The preferences are maintained in a model
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class DisplayPreferences {
  /** The dictionary of class/property display preferences */
  private Map<Resource, DisplayPreference> classPreferences;
  /** The dictionary of instance display preferences */
  private Map<Resource, DisplayPreference> preferences;
  /** The model that holds the preference information */
  private Model displayModel;
  /** The model that holds the ontology that for which preferences are derived from */
  private OntModel model;

  /**
   * Construct a display preferences.
   *
   */
  public DisplayPreferences(OntModel model, Model displayModel) {
    this.model = model;
    this.displayModel = displayModel;
    this.classPreferences = new HashMap<Resource, DisplayPreference>();
    this.preferences = new HashMap<Resource, DisplayPreference>();
  }

  /**
   * Get the model being described by the preferences.
   * <p>
   * This model may be different to the model that describes the ontology
   *
   * @return the model
   */
  public OntModel getModel() {
    return this.model;
  }

  /**
   * Get the model that supplies preferences to the ontology.
   * <p>
   * This model may be different to the model that describes the ontology
   *
   * @return the model
   */
  public Model getDisplayModel() {
    return this.displayModel;
  }
  
  /**
   * Get a display preference for a resource instance.
   *
   * @param uri The resource URI
   * 
   * @return The display preference
   */
  synchronized public DisplayPreference getPreference(Resource resource) {
    DisplayPreference pref = this.preferences.get(resource);
    
    if (pref != null)
      return pref;
    pref = this.buildPreference(resource, false);
    this.preferences.put(resource, pref);
    return pref;
  }
  
  /**
   * Get a display preference for a resource class.
   * <p>
   * This gets the display preference associated with a class
   * (class or property) of things.
   *
   * @param uri The resource URI
   * 
   * @return The display preference
   */
  synchronized public DisplayPreference getClassPreference(Resource resource) {
    DisplayPreference pref = this.classPreferences.get(resource);
    
    if (pref != null)
      return pref;
    pref = this.buildPreference(resource, true);
    this.classPreferences.put(resource, pref);
    return pref;
  }

  /**
   * Build a preference from a specified resource.
   *
   * @param resource The resource
   * 
   * @return The constructed display preference
   */
  protected DisplayPreference buildPreference(Resource resource, boolean asClass) {
    return new DisplayPreference(this, resource, asClass);
  }

}
