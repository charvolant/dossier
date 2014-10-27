/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.io.Writer;
import java.util.ResourceBundle;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Generate documentation from an intermediate XML representation.
 * <p>
 * This class is generally set up to permit XSLT translation.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public abstract class Documenter {
  /** The configuration information */
  protected Configuration configuration;
  /** The document that describes the ontology */
  protected Document document;
  
  /**
   * Construct a documenter.
   * 
   * @param configuration The document configuration
   * @param document The XML document to use
   */
  public Documenter(Configuration configuration, Document document) {
    this.configuration = configuration;
    this.document = document;
  }
  
  /**
   * Get the resource bundle to use for this documenter.
   * <p>
   * By default, this looks up the bundle defined by the
   * class name without the package.
   *
   * @return The resource bundle
   */
  protected ResourceBundle getResourceBundle() {
    return ResourceBundle.getBundle(this.getClass().getName(), this.configuration.getLocale());
  }
  
  /**
   * Perform a transformation.
   * <p>
   * Before transformation, any parameters that are specified
   * by the resource bundle are set.
   *
   * @param transformer The transformer
   * @param result The output result
   */
  protected void transform(Transformer transformer, Result result) throws TransformerException {
    ResourceBundle bundle = this.getResourceBundle();
    DOMSource source = new DOMSource(this.document);
     
    for (String param: bundle.keySet())
      transformer.setParameter(param, bundle.getString(param));
    transformer.transform(source, result);
  }

  /**
   * Transform this document into another document.
   *
   * @param template The transformation template
   * @return
   * @throws TransformerException
   */
  protected Document transform(Templates template) throws TransformerException {
    Transformer transformer = template.newTransformer();
    DOMResult result = new DOMResult();
    
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    this.transform(transformer, result);
    return result.getNode().getOwnerDocument();
  }
  /**
   * Transform this document for output.
   *
   * @param transformer The transformer
   * @param writer What to write to
   * 
   * @throws TransformerException
   */
  protected void transform(Transformer transformer, Writer writer) throws TransformerException {
    StreamResult result = new StreamResult(writer);
    
    this.transform(transformer, result);
  }

  /**
   * Transform this document for output.
   *
   * @param template The transformation template
   * @param writer What to write to
   * 
   * @throws TransformerException
   */
  protected void transform(Templates template, Writer writer) throws TransformerException {
    Transformer transformer = template.newTransformer();
    
    this.transform(transformer, writer);
  }


}
