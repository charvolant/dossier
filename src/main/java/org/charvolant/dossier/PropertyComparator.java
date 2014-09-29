/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.Comparator;

import com.hp.hpl.jena.ontology.OntProperty;

/**
 * Compare properties and put them into sub-property order, with the topmost
 * property first.
 * <p>
 * If properties are not directly ordered, the comparator orders by {@link OntProperty#getLocalName()}.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class PropertyComparator implements Comparator<OntProperty> {
  @Override
  public int compare(OntProperty o1, OntProperty o2) {
    if (o1.hasSubProperty(o2, false))
      return -1;
    if (o2.hasSubProperty(o1, false))
      return 1;
    return o1.getLocalName().compareTo(o2.getLocalName());
  }
}
