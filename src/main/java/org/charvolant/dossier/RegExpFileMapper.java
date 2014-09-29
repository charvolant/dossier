/*
 *  $Id$
 *
 * Copyright (c) 2014 pal155
 *
 * See LICENSE for licensing details
 */
package org.charvolant.dossier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.shared.model.fileset.mappers.FileNameMapper;

/**
 * A file mapper that uses a regular expression to 
 *
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class RegExpFileMapper implements FileNameMapper {
  /**
   * The regular expression mappers role-hint: {@value}.
   */
  public static final String ROLE_HINT = "regexp";

  private Pattern from;
  private String to;
  
  /**
   * Construct a RegExpFileMapper.
   *
   */
  public RegExpFileMapper() {
  }


  /** 
   * Set the regular expression that the file is mapped from.
   * 
   * @throws java.util.regexp.PatternSyntaxException if unable to compile the from pattern
   * 
   * @see org.apache.maven.shared.model.fileset.mappers.FileNameMapper#setFrom(java.lang.String)
   */
  @Override
  public void setFrom(String from) {
    this.from = Pattern.compile(from);
  }


  /** 
   * Set the replacement string.
   * <p>
   * This can match from patterns by using the $n syntax.
   * See {@link Matcher#replaceFirst(String)}
   *
   * @see org.apache.maven.shared.model.fileset.mappers.FileNameMapper#setTo(java.lang.String)
   */
  @Override
  public void setTo(String to) {
    this.to = to;
  }


  /** 
   * Map the input file name onto the output file name.
   * <p>
   * The mapping is performed by replacing the first instance of the matching from pattern
   * with the to string, with 
   * If a match is not found, then the original file name is returned.
   *
   * @see org.apache.maven.shared.model.fileset.mappers.FileNameMapper#mapFileName(java.lang.String)
   */
  @Override
  public String mapFileName(String sourceFileName) {
    Matcher matcher = this.from.matcher(sourceFileName);
    
    return matcher.replaceFirst(this.to);
  }

}
