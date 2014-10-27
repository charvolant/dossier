/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;

/**
 * A documenter that just produces a copy of the input document.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class XmlDocumenter extends Documenter {
  /**
   * Construct an XML documenter.
   *
   * @param configuration The suite configuration
   * @param document The document to generate from
   */
  public XmlDocumenter(Configuration configuration, Document document) {
    super(configuration, document);
  }

  /**
   * Generate an XML document and write it out.
   * 
   * @param writer The destination
   * 
   * @throws Exception if unable to process the result
   */
  public void generate(Writer writer) throws Exception {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    this.transform(transformer, writer);
  }
}
