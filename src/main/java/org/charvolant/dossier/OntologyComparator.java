/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.Comparator;

import com.hp.hpl.jena.ontology.Ontology;

/**
 * Compare ontologies and put them into import order, with the imported ontology first.
 * <p>
 * If ontologies are not directly ordered, the comparator orders by {@link Ontology#getURI()}.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class OntologyComparator implements Comparator<Ontology> {
  @Override
  public int compare(Ontology o1, Ontology o2) {
    if (o1.imports(o2))
      return -1;
    if (o2.imports(o1))
      return 1;
    return o1.getLocalName().compareTo(o2.getLocalName());
  }
}
