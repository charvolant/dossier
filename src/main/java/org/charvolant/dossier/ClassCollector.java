/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.ComplementClass;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Collect the classes that make up a domain or range.
 * <p>
 * The class collector isn't particularly fussy about how the classes are referenced.
 * Union, intersection, complement, whatever; if there's a reference to a concrete class,
 * it gets collected.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class ClassCollector implements RDFList.ApplyFn {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(ClassCollector.class);

  /** The collection of classes. */
  private Set<OntClass> classes;
  /**
   * Construct a class collector.
   */
  public ClassCollector() {
    this.classes = new HashSet<OntClass>();
  }

  /**
   * Get the collected classes.
   *
   * @return the collected classes
   */
  public Set<OntClass> getClasses() {
    return this.classes;
  }

  public void apply(RDFNode node) {
    OntClass clazz;

    if (!node.isResource() || !node.canAs(OntClass.class))
      return;
    clazz = node.as(OntClass.class);
    if (clazz.isUnionClass()) {
      UnionClass union = clazz.asUnionClass();

      union.getOperands().apply(this);
    } else if (clazz.isIntersectionClass()) {
      IntersectionClass intersection = clazz.asIntersectionClass();

      intersection.getOperands().apply(this);
    } else if (clazz.isComplementClass()) {
      ComplementClass complement = clazz.asComplementClass();

      complement.getOperands().apply(this);
    } else 
      this.classes.add(clazz);
  }

  private void collect(ExtendedIterator<? extends OntResource> sources) {
    while (sources.hasNext())
      this.apply(sources.next());
  }

  /**
   * Collect the domain classes for a property
   *
   * @param property The property
   */
  public void collectDomain(OntProperty property) {
    this.collect(property.listDomain());
  }

  /**
   * Collect the range classes for a property
   *
   * @param property The property
   */
  public void collectRange(OntProperty property) {
    this.collect(property.listRange());
  }

  /**
   * Collect the direct super-classes for a class
   *
   * @param clazz The class
   */
  public void collectSupers(OntClass clazz) {
    this.collect(clazz.listSuperClasses(true));
  }
}
