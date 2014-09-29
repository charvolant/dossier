/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Assorted utility methods.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class Util {
  /** Namespaces to remove if we don't want generics */
  private static Set<String> GENERIC_NAMESPACES = new HashSet<String>(Arrays.asList(OWL.getURI(), RDFS.getURI(), RDF.getURI()));

  /**
   * Dump a list of statements about a resource.
   *
   * @param resource The resource
   * @param model The model to take the statements from
   */
  public static void dump(Resource resource, Model model) {
    StmtIterator si = model != null ? model.listStatements(resource, null, (RDFNode) null) : resource.listProperties();
  
    System.out.println(resource);
    while (si.hasNext()) {
      System.out.print("  ");
      System.out.println(si.next());
    }
    System.out.println();
  }
  
  /**
   * Perform a topological sort of classes into subclass/priority order.
   *
   * @param classes The list of classes
   * @param displayModel The display model for priorities
   * @param reverse True for subclass -> superclass order, false for superclass -> subclass
   * 
   * @return The sorted list
   */
  public static List<OntClass> sort(Collection<OntClass> classes, Model displayModel, boolean reverse) {
    boolean[] used = new boolean[classes.size()];
    List<OntClass> sorted = new ArrayList<OntClass>(classes.size());
    ClassComparator comparator = new ClassComparator(displayModel, reverse);
    Iterator<OntClass> ci;
    OntClass candidate, current;
    int i, index;
    
    do {
      current = null;
      index = -1;
      ci = classes.iterator();
      for (i = 0; i < used.length; i++) {
        candidate = ci.next();
        if (used[i])
          continue;
        if (current == null || comparator.compare(current, candidate) > 0) {
          index = i;
          current = candidate;
        }
      }
      if (current != null) {
        used[index] = true;
        sorted.add(current);
      }
    } while (current != null);
    return sorted;
  }
  
  /**
   * Perform a topological sort of properties into subproperty order.
   *
   * @param properties The list of properties
   * 
   * @return The sorted list
   */
  public static List<OntProperty> sort(Collection<? extends OntProperty> properties) {
    boolean[] used = new boolean[properties.size()];
    List<OntProperty> sorted = new ArrayList<OntProperty>(properties.size());
    PropertyComparator comparator = new PropertyComparator();
    Iterator<? extends OntProperty> pi;
    OntProperty candidate, current;
    int i, index;
    
    do {
      current = null;
      index = -1;
      pi = properties.iterator();
      for (i = 0; i < used.length; i++) {
        candidate = pi.next();
        if (used[i])
          continue;
        if (current == null || comparator.compare(current, candidate) > 0) {
          index = i;
          current = candidate;
        }
      }
      if (current != null) {
        used[index] = true;
        sorted.add(current);
      }
    } while (current != null);
    return sorted;
  }
   
  /**
   * Reduce a list of classes to most-specific or least specific order.
   * <p>
   * Most specific order means that a subclass is used instead of a superclass.
   * Otherwise a superclass is used in preference to a subclass.
   *
   * @param classes The classes to reduce
   * @param mostSpecific The order to reduce them by
   * @param removeGenerics Remove things from the RDF, RDFS and OWL namespaces
   * @param removeTop Remove simple top classes, such as owl:Thing and owl:Resource
   * 
   * @return
   */
  public static List<OntClass> reduce(Collection<OntClass> classes, boolean mostSpecific, boolean removeGenerics, boolean removeTop) {
    List<OntClass> reduced = new ArrayList<OntClass>(classes.size());
    List<OntClass> sorted = sort(classes, null, mostSpecific);
    boolean[] ignore = new boolean[sorted.size()];
    OntClass current, candidate;
    int i, j;
    
    for (i = 0; i < ignore.length; i++) {
      if (ignore[i])
        continue;
      current = sorted.get(i);
      if (removeTop && (current.equals(OWL.Thing) || current.equals(RDFS.Resource)))
        continue;
      if (removeGenerics && current.getNameSpace() != null && GENERIC_NAMESPACES.contains(current.getNameSpace()))
        continue;
      reduced.add(current);
      for (j = i + 1; j < ignore.length; j++) {
        candidate = sorted.get(j);
        if ((mostSpecific && current.hasSuperClass(candidate)) || (!mostSpecific && candidate.hasSuperClass(current)))
          ignore[j] = true;
      }
    }
    return reduced;
  }
  
 /**
  * Reduce a list of properties to most-specific or least specific order.
  * <p>
  * Most specific order means that a subproperty is used instead of a superproperty.
  * Otherwise a superproperty is used in preference to a subproperty.
  *
  * @param properties The properties to reduce
  * @param mostSpecific The order to reduce them by
  * @param removeGenerics Remove things like rdfs:Resource and owl;Thing
  * 
  * @return
  */
 public static List<OntProperty> reduceProperties(Collection<? extends OntProperty> properties, boolean mostSpecific, boolean removeGenerics) {
   List<OntProperty> reduced = new ArrayList<OntProperty>(properties.size());
   List<OntProperty> sorted = sort(properties);
   boolean[] ignore = new boolean[sorted.size()];
   OntProperty current, candidate;
   int i, j;
   
   for (i = 0; i < ignore.length; i++) {
     if (ignore[i])
       continue;
     current = sorted.get(i);
     if (removeGenerics && current.getNameSpace() != null && GENERIC_NAMESPACES.contains(current.getNameSpace()))
       continue;
     reduced.add(current);
     for (j = i + 1; j < ignore.length; j++) {
       candidate = sorted.get(j);
       if ((mostSpecific && current.hasSuperProperty(candidate, false)) || (!mostSpecific && candidate.hasSuperProperty(current, false)))
         ignore[j] = true;
     }
   }
   return reduced;
 }
}
