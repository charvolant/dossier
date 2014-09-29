/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * Useful utilities for test cases.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
abstract public class AbstractTest {

  /**
   * Construct a AbstractTest.
   *
   */
  public AbstractTest() {
  }
  
  /**
   * Trim a string by collapsing whitespace.
   * 
   *
   * @param trimable
   * @return
   */
  public String trim(String trimable) {
    return trimable.trim().replaceAll("[ \t]+", " ").replaceAll("\n[\r\n]*", "\n");
  }
  
  /**
   * Load a resource.
   *
   * @param name The resource name (relative to the class)
   * 
   * @return The loaded resource
   * 
   * @throws IOException if unable to load
   */
  public String loadResource(String name) throws IOException {
    InputStreamReader reader = new InputStreamReader(this.getClass().getResourceAsStream(name));
    StringWriter writer = new StringWriter(2048);
    char[] buffer = new char[1024];
    int n;
    
    while ((n = reader.read(buffer)) >= 0)
      writer.write(buffer, 0, n);
    reader.close();
    writer.close();
    return this.trim(writer.toString());
  }

}
