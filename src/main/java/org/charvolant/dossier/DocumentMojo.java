/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Goal which generates documentation for a collection of ontologies.
 * <p>
 * Both HTML documentation, SVG diagrams and style sheets are generated into
 * a directory.
 */
@Mojo(name = "document", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class DocumentMojo extends AbstractMojo
{  
  /** The enclosing maven project */
  @Component
  private MavenProject project;
  
  /** Location of the ontologies. */
  @Parameter(property="ontologies", required = true)
  private FileSet ontologies;

  /**
   * Location of the display preferences to use.
   * <p>
   * Defaults to the internal standard 
   */
  @Parameter(property="preferences", required=true)
  private File preferencesFile;

  /**
   * Run the goal.
   *
   * @see org.apache.maven.plugin.AbstractMojo#execute()
   */
  public void execute() throws MojoExecutionException
  {
    try {
      FileSetManager fsManager;
      File base, df, od, of;
      Model display;
      OntModel model;
      Configuration configuration;
      CollectionCreator creator;

      fsManager = new FileSetManager();
      base = this.project == null ? new File(".").getAbsoluteFile().getCanonicalFile() : this.project.getBasedir();
      od = new File(base, this.ontologies.getOutputDirectory());
      df = new File(base, this.ontologies.getDirectory());
      if (!od.exists())
        if (!od.mkdirs())
          throw new MojoExecutionException("Unable to create output direcrtory " + od);
      display = ModelFactory.createDefaultModel();
      display.read(this.getClass().getResource("dossier.rdf").toString());
      if (this.preferencesFile == null)
        display.read(this.getClass().getResource("standard.rdf").toString());
      else {
        if (this.preferencesFile == null || !this.preferencesFile.exists())
          throw new MojoExecutionException("No preferences file " + this.preferencesFile);
        display.read(this.preferencesFile.toURI().toString());
      }
      configuration = new Configuration();
      configuration.setDisplayModel(display);
      creator = new CollectionCreator(configuration);
      for (String ontology: fsManager.getIncludedFiles(this.ontologies)) {
        of = new File(df, ontology).getCanonicalFile();
        if (!of.exists())
          throw new MojoExecutionException("No ontology file " + of);
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
        model.getDocumentManager().setProcessImports(false);
        model.read(of.toURI().toString());
        creator.addOntology(model);
      }
      creator.generate(od);
    } catch (MojoExecutionException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new MojoExecutionException("Unable to create documentation files", ex);
    }
  }
}