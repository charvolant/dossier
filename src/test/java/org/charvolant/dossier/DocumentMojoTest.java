/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

//import org.junit.Test;

/**
 * Test cases for the {@link DocumentMojo} mojo
 * <p>
 * TODO To be returned to when maven gets their test harness sorted out and  any attempt to get it running isn't mired in dependency hell.
 * <p>
 * Basically, guava interferes with guice which interferes with sisu
 * with interferes with everything.
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class DocumentMojoTest {  
  /**
   * Clean up the test directory.
   *
   * @param dir
   */
  private void clean(File dir) {
    if (!dir.exists())
      return;
    for (File file: dir.listFiles()) {
      if (file.isDirectory())
        this.clean(file);
      file.delete();
    }
  }
  
  /**
   * Placeholder until we can implement this property.
   *
   * @param string
   * @param pom
   * @return
   */
  private DocumentMojo lookupMojo(String string, File pom) {
    DocumentMojo mojo = new DocumentMojo();
    
    return mojo;
  }


  /**
   * Test method for {@link org.charvolant.dossier.DocumentMojo#execute()}.
   */
  //@Test
  public void testExecute1() throws Exception {
    File dir = new File("target/test-harness/dot-generator-mojo-1");
    File pom = new File("src/test/resources/org/charvolant/dossier/dot-generator-mojo-1.pom");
    DocumentMojo mojo = (DocumentMojo) this.lookupMojo("document", pom);
    File html, svg, css;
    
    this.clean(dir);
    assertNotNull(mojo);  
    mojo.execute();
    html = new File(dir, "test1.html");
    assertTrue(html.exists());
    assertTrue(html.length() > 0);
    svg = new File(dir, "test1.svg");
    assertTrue(svg.exists());
    assertTrue(svg.length() > 0);
    css = new File(dir, "css");
    assertTrue(css.exists());
    assertTrue(css.isDirectory());
  }

  /**
   * Test method for {@link org.charvolant.dossier.DocumentMojo#execute()}.
   */
  //@Test
  public void testExecute2() throws Exception {
    File dir = new File("target/test-harness/dot-generator-mojo-2");
    File pom = new File("src/test/resources/org/charvolant/dossier/dot-generator-mojo-2.pom");
    DocumentMojo mojo = (DocumentMojo) this.lookupMojo("dot", pom);
    File dot;
    
    this.clean(dir);
    assertNotNull(mojo);  
    mojo.execute();
    dot = new File(dir, "test2-test1.dot");
    assertTrue(dot.exists());
    assertTrue(dot.length() > 0);
  }

  /**
   * Test method for {@link org.charvolant.dossier.DocumentMojo#execute()}.
   */
  //@Test
  public void testExecute3() throws Exception {
    File dir = new File("target/test-harness/dot-generator-mojo-3");
    File pom = new File("src/test/resources/org/charvolant/dossier/dot-generator-mojo-3.pom");
    DocumentMojo mojo = (DocumentMojo) this.lookupMojo("dot", pom);
    File dot;
    
    this.clean(dir);
    assertNotNull(mojo);  
    mojo.execute();
    dot = new File(dir, "test1.dot");
    assertTrue(dot.exists());
    assertTrue(dot.length() > 0);
    dot = new File(dir, "test2.dot");
    assertTrue(dot.exists());
    assertTrue(dot.length() > 0);
    dot = new File(dir, "display-prefs.dot");
    assertTrue(dot.exists());
    assertTrue(dot.length() > 0);
  }
}
