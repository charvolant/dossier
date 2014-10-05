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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Goal which generates documentation for a collection of ontologies.
 * <p>
 * Both HTML documentation, SVG diagrams and style sheets are generated into
 * a directory.
 */
@Mojo(name = "document", defaultPhase = LifecyclePhase.SITE)
public class DocumentMojo extends AbstractMojo
{    
  /** The enclosing maven project */
  @Parameter(defaultValue="${project}", readonly=true)
  private MavenProject project;
  
  /** Location of the ontologies. */
  @Parameter(property="ontologies", required = true)
  private FileSet ontologies;

  /**
   * Location of the display preferences to use.
   * <p>
   * If not set then the preferences defaults to the internal standard 
   */
  @Parameter(property="preferences", required=false)
  private File preferencesFile;
  
  /**
   * The directory into which generated files are placed.
   */
  @Parameter(property="outputDirectory", defaultValue="${project.build.directory}/site/dossier")
  private File outputDirectory;

  /**
   * Run the goal.
   *
   * @see org.apache.maven.plugin.AbstractMojo#execute()
   */
  public void execute() throws MojoExecutionException
  {
    try {
      FileSetManager fsManager;
      File base, df, of;
      Model display;
      Configuration configuration;
      CollectionCreator creator;

      fsManager = new FileSetManager(this.getLog(), true);
      base = this.project == null ? new File(".").getAbsoluteFile().getCanonicalFile() : this.project.getBasedir();
      df = this.ontologies.getDirectory() == null ? base : new File(this.ontologies.getDirectory());
      this.getLog().debug("Project base " + base);
      if (!df.isAbsolute())
        df = new File(base, df.getPath());
      this.getLog().debug("Directory " + df);
      if (!this.outputDirectory.exists())
        if (!this.outputDirectory.mkdirs())
          throw new MojoExecutionException("Unable to create output direcrtory " + this.outputDirectory);
      display = ModelFactory.createDefaultModel();
      display.read(this.getClass().getResourceAsStream("dossier.rdf"), null);
      if (this.preferencesFile == null)
        display.read(this.getClass().getResourceAsStream("standard.rdf"), null);
      else {
        if (this.preferencesFile == null || !this.preferencesFile.exists())
          throw new MojoExecutionException("No preferences file " + this.preferencesFile);
        display.read(this.preferencesFile.toURI().toString());
      }
      configuration = new Configuration();
      configuration.setDisplayModel(display);
      creator = new CollectionCreator(configuration);
      for (String ontology: fsManager.getIncludedFiles(this.ontologies)) {
        this.getLog().debug("Loading " + ontology);
        of = new File(df, ontology).getCanonicalFile();
        if (!of.exists())
          throw new MojoExecutionException("No ontology file " + of);
        creator.load(of);
      }
      creator.generate(this.outputDirectory);
    } catch (MojoExecutionException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new MojoExecutionException("Unable to create documentation files", ex);
    }
  }
}