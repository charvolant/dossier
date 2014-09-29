/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;


/**
 * Render preference information.
 * <p>
 * Implementers of this interface can be used to 
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public interface PreferenceRenderer {
  /**
   * Begin rendering preferences.
   */
  public void begin();
  /**
   * End rendering preferences
   */
  public void end();
  /**
   * Render a specific property with a given value.
   *
   * @param property The property
   * @param value The value of the property
   */
  public void render(String property, Object value);
}
