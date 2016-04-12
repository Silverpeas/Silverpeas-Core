/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
public class StringDataExtractor {

  private final String source;
  private final List<ExtractorDirective> directives = new ArrayList<ExtractorDirective>();

  /**
   * Gets an instance of {@link StringDataExtractor}
   * @param source the string from which data must be extracted.
   * @return an instance of {@link StringDataExtractor}
   */
  public static StringDataExtractor from(String source) {
    return new StringDataExtractor(source);
  }

  /**
   * Hidden constructor.
   * @param source
   */
  private StringDataExtractor(final String source) {
    this.source = source;
  }

  /**
   * Indicates a directive of data extraction.
   * @param directive the directive that will handle the data extraction.
   * @return the current instance of {@link StringDataExtractor}.
   */
  public StringDataExtractor withDirective(final ExtractorDirective directive) {
    directives.add(directive);
    return this;
  }

  /**
   * Indicates a directive of data extraction.
   * @param directives the directives that will handle the data extraction.
   * @return the current instance of {@link StringDataExtractor}.
   */
  public StringDataExtractor withDirectives(final List<? extends ExtractorDirective> directives) {
    for (ExtractorDirective directive : directives) {
      withDirective(directive);
    }
    return this;
  }

  /**
   * Extracts data from a strict by applying the specified extract pattern directives.
   * @return the list of data.
   */
  public List<String> extract() {
    Set<String> data = new LinkedHashSet<String>();
    for (ExtractorDirective directive : directives) {
      data.addAll(directive.extract(source));
    }
    return new ArrayList<String>(data);
  }

  /**
   * Calls first {@link #extract()} and then return the only contained value from the result, null
   * if no value extracted.
   * @return the unique value aimed by the extraction.
   * @throws java.lang.IllegalStateException if it exists several values behind the extraction
   * directive.
   */
  public String extractUnique() throws IllegalStateException {
    List<String> data = extract();
    if (data.size() > 1) {
      throw new IllegalStateException(
          "Several values behind the extraction directive whereas an unique value is expected.");
    }
    return data.isEmpty() ? "" : data.get(0);
  }

  /**
   * This interface permits to implement several way to extract data from a string.
   */
  public interface ExtractorDirective {

    /**
     * Extracts the data according to the directive.
     * All returned data are unique, so the result container contains no value doubloon.
     * @param source the string source.
     * @return the extracted unique data.
     */
    Set<String> extract(String source);
  }

  /**
   * Class that permits to specify the directives in order to extract data.
   */
  public static class RegexpPatternDirective implements ExtractorDirective {
    private final Pattern pattern;
    private final int[] groupIndexesToExtract;

    /**
     * Gets a new instance of {@link RegexpPatternDirective}.
     * @param pattern the regexp pattern to identify the data.
     * @param groupIndexesToExtract the group indexes to extract. If no group defined, 0 by
     * default.
     * @return an instance of {@link RegexpPatternDirective}.
     */
    public static RegexpPatternDirective regexp(final Pattern pattern,
        final int... groupIndexesToExtract) {
      return new RegexpPatternDirective(pattern, groupIndexesToExtract);
    }

    /**
     * Gets a new instance of {@link RegexpPatternDirective}.
     * @param patterns the regexp patterns to identify the data.
     * @param groupIndexesToExtract the group indexes to extract. If no group defined, 0 by
     * default.
     * @return an instance of {@link RegexpPatternDirective}.
     */
    public static List<RegexpPatternDirective> regexps(final List<Pattern> patterns,
        final int... groupIndexesToExtract) {
      List<RegexpPatternDirective> regexpPatternDirectives =
          new ArrayList<RegexpPatternDirective>();
      for (Pattern pattern : patterns) {
        regexpPatternDirectives.add(regexp(pattern, groupIndexesToExtract));
      }
      return regexpPatternDirectives;
    }

    /**
     * Gets a new instance of {@link RegexpPatternDirective}.
     * @param pattern the regexp pattern to identify the data.
     * @param groupIndexesToExtract the group indexes to extract. If no group defined, 0 by
     * default.
     * @return an instance of {@link RegexpPatternDirective}.
     */
    public static RegexpPatternDirective regexp(final String pattern,
        final int... groupIndexesToExtract) {
      return regexp(Pattern.compile(pattern), groupIndexesToExtract);
    }

    private RegexpPatternDirective(final Pattern pattern, final int... groupIndexesToExtract) {
      this.pattern = pattern;
      this.groupIndexesToExtract = groupIndexesToExtract;
    }

    @Override
    public Set<String> extract(String source) {
      Set<String> data = new LinkedHashSet<String>();
      if (isDefined(source)) {
        int[] groupIndexesToExtract =
            (this.groupIndexesToExtract.length == 0) ? new int[]{0} : this.groupIndexesToExtract;
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
          for (Integer groupIndex : groupIndexesToExtract) {
            String extractData = matcher.group(groupIndex);
            if (isDefined(extractData)) {
              data.add(extractData);
            }
          }
        }
      }
      return data;
    }
  }
}
