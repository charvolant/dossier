package org.charvolant.dossier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Abstract generator for complex descriptions of ontologies.
 * <p>
 * By the time this is finished, there should be some suitable
 * intermediate representation of the ontology that can be used to build
 * something useful to the outside world.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
abstract public class ModelGenerator extends Generator {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(ModelGenerator.class);

  /** The ontology that we intend to graph. This is built over the  */
  protected OntModel ontology;
  /** The model that we intend to graph. This is built over the base ontology  */
  protected OntModel model;
  /** The primary ontology to use */
  protected Ontology primary;
  /** The classes referenced by this generation */
  protected Set<OntClass> referencedClasses;
  /** The properties referenced by this generation */
  protected Set<OntProperty> referencedProperties;
  /**
   * Construct a Generator.
   *
   * @param configuration The generation configuration
   * @param ontology The ontology to generate from
   * @param displayModel The displayModel to use
   */
  public ModelGenerator(Configuration configuration, OntModel ontology) {
    super(configuration);
    this.ontology = ontology;
    this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, this.ontology);
    this.model.setNsPrefixes(PrefixMapping.Extended);
    this.preferences = new DisplayPreferences(this.ontology, this.configuration.getDisplayModel());
    this.referencedClasses = new HashSet<OntClass>();
    this.referencedProperties = new HashSet<OntProperty>();
    this.primary = this.configuration.choosePrimaryOntology(this.model);
    this.languageOrder = this.chooseLanguageOrder();
    this.addDatatypes(this.model);
  }

  /**
   * Get the ontology.
   *
   * @return the ontology
   */
  public OntModel getOntology() {
    return this.ontology;
  }

  /**
   * Get the model.
   *
   * @return the model
   */
  public OntModel getModel() {
    return this.model;
  }

  /**
   * Get the primary.
   *
   * @return the primary
   */
  public Ontology getPrimary() {
    return this.primary;
  }

  /**
   * See if a resource is local to the model.
   *
   * @param resource The resource
   * 
   * @return True if the resource is local
   */
  protected boolean isLocal(Resource resource) {
    String prefix;

    if (!resource.isURIResource() || resource.getNameSpace() == null)
      return true;
    prefix = this.getPrefix(resource.getNameSpace());
    return prefix != null && prefix.isEmpty();
  }


  /**
   * Reduce a list of candidiate elements to things
   * the local model regards as part of its namespace.
   *
   * @param candidates The possible candidiates
   * 
   * @return A set of local items
   */
  protected <T extends Resource> Set<T> collectLocal(ExtendedIterator<T> candidates) {
    Set<T> chosen = new HashSet<T>();

    while (candidates.hasNext()) {
      T candidate = candidates.next();

      if (this.isLocal(candidate))
        chosen.add(candidate);
    }
    return chosen;  
  }

  /**
   * Get the prefix for a namespace.
   * <p>
   * If the prefix doesn't yet exist, a new one is made up
   * if the namespace corresponds to one of the ontologies in the
   * configuration.
   *
   * @param ns The namespace
   * 
   * @return The prefix
   */
  @Override
  protected String getPrefix(String ns) {
    String prefix;
    
    if (ns == null)
      return null;
    if ((prefix = this.model.getNsURIPrefix(ns)) != null)
      return prefix;
    return super.getPrefix(ns);
  }

  /**
   * Collect elements that match a specific prefix.
   *
   * @param candidates The possible candidiates
   * @param prefix Only collect candidates where the namespace resolves to this prefix
   * 
   * @return A set of local items
   */
  protected <T extends Resource> Set<T> collectPrefix(Collection<T> candidates, String prefix) {
    Set<T> chosen = new HashSet<T>();
    String ns, pf;

    if (prefix == null)
      prefix = "";
    for (T candidate: candidates) {
      ns = candidate.getNameSpace();
      pf = this.getPrefix(ns);
      if (prefix.equals(pf))
        chosen.add(candidate);
    }
    return chosen;  
  }

  /**
   * Collect the namespaces used by the classes and properties.
   * <p>
   * Make sure that all namespaces have an
   *
   * @return The collected namespace set
   */
  protected Set<String> collectNamespaces() {
    Set<String> namespaces = new HashSet<String>();

    for (OntClass clazz: this.referencedClasses)
      namespaces.add(clazz.getNameSpace());
    for (OntProperty property: this.referencedProperties)
      namespaces.add(property.getNameSpace());
    for (String ns: namespaces)
      this.getPrefix(ns);
    return namespaces;
  }
}