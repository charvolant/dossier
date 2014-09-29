/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.charvolant.dossier.vocabulary.Dossier;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Information about how to render a particular resource.
 * <p>
 * Preferences are chosen in the following way:
 * <p>
 * Instances inherit preference information from their classes.
 * Note that a class is an instance of {@link RDFS#Class}, {@link OWL#Class} etc,
 * which means that the preferences that describe how to display a class comes
 * from the class of the class, rather than from the class itself.
 * <p>
 * Class preferences are inherited from superclasses.
 * Superclasses are sorted into inheritance order and then URI order.
 * The preferences are then built by applying the preferences in turn, ending with 
 * the preferences specified in the class itself.
 * The preferences in {@link RDFS#Resource} are not directly inherited.
 * Instead, they are used to define the default information for the
 * graph.
 * <p>
 * Property preferences are first built from the property classes (eg. {@link OWL#DatatypeProperty}).
 * They then inherit property information in sub-property order, in a similar way to classes.
 * They then have their own properties overlaid over that lot.
 * 
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class DisplayPreference {
  public static final Property[] PREFERENCE_LIST = {
    Dossier.arrowHead,
    Dossier.arrowStyle,
    Dossier.arrowTail,
    Dossier.backgroundColor,
    Dossier.clipHead,
    Dossier.clipTail,
    Dossier.color,
    Dossier.extendPropertyEdge,
    Dossier.fontColor,
    Dossier.fontName,
    Dossier.fontSize,
    Dossier.isConstraint,
    Dossier.nodeShape,
    Dossier.nodeStyle,
    Dossier.reverseHierarchy,
    Dossier.showLabel,
    Dossier.weight
  };

  /** The parent display preferences */
  private DisplayPreferences parent;
  /** The preferences dictionary */
  private Map<Property, Object> preferences;

  /**
   * Construct an empty display preference
   *
   * @param parent The owning preferences system
   */
  public DisplayPreference(DisplayPreferences parent) {
    this.parent = parent;
    this.preferences = new HashMap<Property, Object>();
  }

  /**
   * Construct a display preference for a resource.
   *
   * @param parent The owning preferences system
   * @param resource The resource
   * @param asClass Use the class definition for preferences
   */
  public DisplayPreference(DisplayPreferences parent, Resource resource, boolean asClass) {
    this(parent);
    this.buildFrom(resource, asClass);
  }
  
  /**
   * Make a copy display preference.
   * <p>
   * Used for modified versions
   *
   * @param source The oridinal preference
   */
  protected DisplayPreference(DisplayPreference source) {
    this(source.parent);
    this.preferences = new HashMap<Property, Object>(source.preferences);
  }

  /**
   * Get a string preference.
   *
   * @param property The property that defines this preference
   * @param dflt The default value if not present
   * 
   * @return The preference, if present, otherwise the default
   */
  public String getStringPref(Property property, String dflt) {
    Object pref = this.preferences.get(property);

    if (pref != null)
      return pref.toString();
    return dflt;
  }

  /**
   * Get a boolean preference.
   *
   * @param property The property that defines this preference
   * @param dflt The default value if not present
   * 
   * @return The preference, if present and a boolean otherwise the default
   */
  public boolean getBooleanPref(Property property, boolean dflt) {
    Object pref = this.preferences.get(property);

    if (pref != null && (pref instanceof Boolean))
      return ((Boolean) pref).booleanValue();
    return dflt;
  }
  
  private void buildFromType(Resource resource) {
    OntModel model = this.parent.getModel();
    List<OntClass> classes = new ArrayList<OntClass>();
    NodeIterator ni = model.listObjectsOfProperty(resource, RDF.type);
    
    while (ni.hasNext()) {
      Resource cls = ni.next().asResource();
      if (cls.isURIResource())
        classes.add(model.getOntClass(cls.getURI()));
    }
    classes = Util.sort(classes, this.parent.getDisplayModel(), false);
    for (OntClass cls: classes)
      if (!cls.equals(RDFS.Resource))
        this.addPreferences(this.parent.getClassPreference(cls));
  }

  /**
   * Build the preferences for a resource, working up the type and
   * sub-class/sub-property hierarchy.
   *
   * @param resource The resource
   */
  private void buildFrom(Resource resource, boolean asClass) {
    OntModel model = this.parent.getModel();
    List<OntProperty> properties;
    NodeIterator ni;

    if (asClass && resource.hasProperty(RDF.type, RDFS.Class)) {
      if (resource.hasProperty(Dossier.displayPreferences))
        this.buildFromResource(resource.getPropertyResourceValue(Dossier.displayPreferences));
      this.buildFromResource(resource);
    } else if (resource.hasProperty(RDF.type, RDF.Property)) {
      this.buildFromType(resource);
      properties = new ArrayList<OntProperty>();
      ni = model.listObjectsOfProperty(resource, RDFS.subPropertyOf);
      while (ni.hasNext()) properties.add(model.getOntProperty(ni.next().asResource().getURI()));
      Collections.sort(properties, new PropertyComparator());
      for (OntProperty prop: properties)
        if (!prop.equals(resource))
          this.addPreferences(this.parent.getPreference(prop));
      if (resource.hasProperty(Dossier.displayPreferences))
        this.buildFromResource(resource.getPropertyResourceValue(Dossier.displayPreferences));
      this.buildFromResource(resource);
    } else {
      this.buildFromType(resource);
    }
  }

  /**
   * Parse an RGB colour component statement.
   * <p>
   * Colours can either be hex strings (interpreted as integers),
   * integers from 0-255 or doubles from 0-1.0. The type of the 
   * literal is used as a guide.
   *
   * @param statement The component statement
   * @param dflt The default value
   * 
   * @return The component value, if found or the default.
   */
  private int parseRGBComponent(Statement statement, int dflt) {
    Literal component;
    RDFDatatype type;
    int value;

    if (statement == null || !statement.getObject().isLiteral())
      return dflt;
    component = statement.getLiteral();
    type = component.getDatatype();
    if (type == null || type.equals(XSDDatatype.XSDstring))
      value = Integer.parseInt(component.getString(), 16);
    else if (type.equals(XSDDatatype.XSDdouble) || type.equals(XSDDatatype.XSDfloat))
      value = (int) Math.round(component.getDouble() * 255.0);
    else if (type.equals(XSDDatatype.XSDunsignedByte) || type.equals(XSDDatatype.XSDint)  || type.equals(XSDDatatype.XSDinteger))
      value = component.getInt();
    else
      value = dflt;
    return value;
  }

  /**
   * Parse an RGB colour resource.
   * <p>
   * This will only attempt to build an RGB or RGBA colour specification if
   *
   * @param resource
   * @return
   */
  private String parseRGBColor(Resource resource) {
    int red, green, blue, alpha;
    StringBuilder builder;

    if (!resource.hasProperty(RDF.type, Dossier.RGBColor))
      return null;
    red = this.parseRGBComponent(resource.getProperty(Dossier.red), 255);
    green = this.parseRGBComponent(resource.getProperty(Dossier.green), 255);
    blue = this.parseRGBComponent(resource.getProperty(Dossier.blue), 255);
    alpha = this.parseRGBComponent(resource.getProperty(Dossier.alpha), 255);
    builder = new StringBuilder();
    builder.append("#");
    if (red < 16) builder.append("0");
    builder.append(Integer.toHexString(red));
    if (green < 16) builder.append("0");
    builder.append(Integer.toHexString(green));
    if (blue < 16) builder.append("0");
    builder.append(Integer.toHexString(blue));
    if (alpha != 255) {
      if (alpha < 16) builder.append("0");
      builder.append(Integer.toHexString(alpha));
    }
    return builder.toString();
  }

  /**
   * Get all the information that a resource has.
   *
   * @param resource The resource
   */
  private void buildFromResource(Resource resource) {
    StmtIterator si;
    Statement s;
    Resource r;
    Object value, val;

    resource = this.parent.getDisplayModel().createResource(resource.getURI());
    for (Property prop: this.PREFERENCE_LIST) {
      si = resource.listProperties(prop);
      if (si.hasNext()) {
        value = null;
        while (si.hasNext()) {
          s = si.next();
          val = null;
          if (s != null && s.getObject().isResource()) {
            r = s.getResource();
            val = this.parseRGBColor(r);
            if (val == null)
              s = r.getProperty(Dossier.graphvizName);
          }
          if (val == null && s != null)
            val = s.getLiteral().getValue();
          if (val != null)
            value = value == null ? val : value.toString() + "," + val.toString();
        }
        if (value != null)
          this.preferences.put(prop, value);
      }   
    }
  }

  /**
   * Copy any non-null preferences
   *
   * @param preference The preference
   */
  private void addPreferences(DisplayPreference preference) {
    for (Property prop: this.PREFERENCE_LIST)
      if (preference.preferences.containsKey(prop))
        this.preferences.put(prop, preference.preferences.get(prop));
  }
  
  /**
   * Render a list of preferences.
   *
   * @param renderer The renderer to convert the properties into text
   * @param properties The list of properties to render
   */
  public void render(PreferenceRenderer renderer, Property[] properties) {
    for (Property prop: properties) {
      Object value = this.preferences.get(prop);
      if (value != null) {
        Statement name = this.parent.getDisplayModel().getProperty(prop, Dossier.graphvizName);

        renderer.render(name != null ? name.getString() : prop.getLocalName(), value);
      }
    }   
  }
  
  /**
   * Build a display preference for an intermediate property arc.
   * <p>
   * This is the same as the property display preference, 
   * but without an arrow head or tail and no layout contstraint.
   *
   * @return The incoming version
   */
  public DisplayPreference asIntermediate() {
    DisplayPreference pref = new DisplayPreference(this);
    
    pref.preferences.put(Dossier.arrowHead, "none");
    pref.preferences.put(Dossier.arrowTail, "none");
    return pref;
  }

  /**
   * Build a display preference for an incoming arc to a property.
   * <p>
   * This is the same as the property display preference, 
   * but without an arrow head and no label.
   * If the shape of the property is none, then extend the head of
   * the arc into the node.
   *
   * @return The incoming version
   */
  public DisplayPreference asIncoming() {
    DisplayPreference pref = new DisplayPreference(this);
    
    pref.preferences.put(Dossier.arrowHead, "none");
    if (pref.getBooleanPref(Dossier.extendPropertyEdge, false))
      pref.preferences.put(Dossier.clipHead, false);
    pref.preferences.put(Dossier.showLabel, false);
    return pref;
  }
  
 /**
  * Build a display preference for an outgoing arc to a property.
  * <p>
  * This is the same as the property display preference, 
  * but without an arrow tail, no label and no layout constraint.
  * If the shape of the property is none, then extend the tail of
  * the arc into the node.
  *
  * @return The outgoing version
  */
 public DisplayPreference asOutgoing() {
   DisplayPreference pref = new DisplayPreference(this);
   
   pref.preferences.put(Dossier.arrowTail, "none");
   if (pref.getBooleanPref(Dossier.extendPropertyEdge, false))
     pref.preferences.put(Dossier.clipTail, false);
   pref.preferences.put(Dossier.showLabel, false);
   return pref;
 }
}
