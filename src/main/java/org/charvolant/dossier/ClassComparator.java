/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.Comparator;

import org.charvolant.dossier.vocabulary.Dossier;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Compare a classes and put them into subclass order, with the topmost
 * class first.
 * <p>
 * If classes are not directly ordered, then they are ordered by
 * {@link dossier#priority}, with unspecified priorities
 * being 0.
 * If there is no priority order, then the {@link OntClass#getLocalName()}  local name is used
 * to give a stable order.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 */
public class ClassComparator implements Comparator<OntClass> {
  /** The display model to use for priorities */
  private Model displayModel;
  /** Reverse ordering */
  private int reverse;
  
  /**
   * Construct a class comparator.
   * <p>
   * By default, classes are sorted in order from highest superclass to
   * lowest subclass. If reversed
   *
   * @param displayModel The model to use for priorities (null for no priority ordering)
   * @param reverse Reverse direction (from subclass to superclass)
   */
  public ClassComparator(Model displayModel, boolean reverse) {
    super();
    this.displayModel = displayModel;
    this.reverse = reverse ? -1 : 1;
  }
  
  /**
   * Construct a class comparator that sorts from least specific (superclass)
   * to most specific (subclass).
   * 
   * @param displayModel The display model to use for ordering
   */
  public ClassComparator(Model displayModel) {
    this(displayModel, false);
  }

  @Override
  public int compare(OntClass o1, OntClass o2) {
    int p1 = 0, p2 = 0;
    
    if (o1.hasSubClass(o2))
      return - this.reverse;
    if (o2.hasSubClass(o1))
      return this.reverse;
    if (this.displayModel != null && this.displayModel.contains(o1, Dossier.priority))
      p1 = this.displayModel.getProperty(o1, Dossier.priority).getInt();
    if (this.displayModel != null && this.displayModel.contains(o2, Dossier.priority))
      p2 = this.displayModel.getProperty(o2, Dossier.priority).getInt();
    if (p1 != p2)
      return this.reverse * (p1 - p2);
    return this.reverse * o1.getLocalName().compareTo(o2.getLocalName());
  }
}
