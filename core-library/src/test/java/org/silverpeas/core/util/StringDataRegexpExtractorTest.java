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

import org.junit.Test;
import org.silverpeas.core.util.StringDataExtractor;

import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.util.CollectionUtil.asList;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexps;


public class StringDataRegexpExtractorTest {

  private final static String COMMON_STRING =
      "common/string/0256/?param=ab-04\ncommon/StRing/abcd/toto";

  private final static Pattern VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE =
      Pattern.compile("(?i)string/([^/]+)/([a-z]{1,2})?");
  private final static int[] VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX = new int[]{1, 2};

  @Test
  public void extractFromNullWithNoDirective() {
    List<String> result = StringDataExtractor.from(null).extract();
    assertThat(result, hasSize(0));
  }

  @Test
  public void extractFromEmptyWithNoDirective() {
    List<String> result = StringDataExtractor.from("").extract();
    assertThat(result, hasSize(0));
  }

  @Test
  public void extractFromCommonStringWithNoDirective() {
    List<String> result = StringDataExtractor.from(COMMON_STRING).extract();
    assertThat(result, hasSize(0));
  }

  @Test
  public void extractFromNullWithOneDirective() {
    List<String> result = StringDataExtractor.from(null).withDirective(
        regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
            VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX)).extract();
    assertThat(result, hasSize(0));
  }

  @Test
  public void extractFromEmptyWithOneDirective() {
    List<String> result = StringDataExtractor.from("").withDirective(
        regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
            VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX)).extract();
    assertThat(result, hasSize(0));
  }

  @Test
  public void extractFromCommonStringWithOneDirective() {
    List<String> result = StringDataExtractor.from(COMMON_STRING).withDirective(
        regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
            VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX)).extract();
    assertThat(result, contains("0256", "abcd", "to"));
  }

  @Test
  public void extractFromCommonStringWithOneDirectiveButNotGroupIndex() {
    List<String> result = StringDataExtractor.from(COMMON_STRING)
        .withDirective(regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE)).extract();
    assertThat(result, contains("string/0256/", "StRing/abcd/to"));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void extractFromCommonStringWithOneDirectiveButBadGroupIndex() {
    StringDataExtractor.from(COMMON_STRING)
        .withDirective(regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE, 3)).extract();
  }

  @Test
  public void extractFromCommonStringWithTwoDirectives() {
    List<String> result = StringDataExtractor.from(COMMON_STRING).withDirective(
        regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
            VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX))
        .withDirective(regexp(".*string/(.+)/(.{1,3})?.*", 2)).extract();
    assertThat(result, contains("0256", "abcd", "to", "?pa"));
  }

  @Test
  public void extractFromCommonStringWithTwoDirectivesSpecifiedInOneCall() {
    List<String> result = StringDataExtractor.from(COMMON_STRING).withDirectives(regexps(
        asList(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
            Pattern.compile(".*string/(.+)/(.{1,3})?.*")),
        VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX)).extract();
    assertThat(result, contains("0256", "abcd", "to", "?pa"));
  }

  @Test
  public void
  extractFromCommonStringWithTwoDirectivesInOtherOrderAndDemonstratesThatReturnedValuesAreUnique() {
    List<String> result = StringDataExtractor.from(COMMON_STRING)
        .withDirective(regexp(".*string/(.+)/(.{1,3})?.*", 2)).withDirective(
            regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
                VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX)).extract();
    assertThat(result, contains("?pa", "0256", "abcd", "to"));
  }

  @Test
  public void extractUniqueFromCommonStringWithOneDirective() {
    String result = StringDataExtractor.from(COMMON_STRING).withDirective(regexp(".*(string).*", 1))
        .extractUnique();
    assertThat(result, is("string"));
  }

  @Test
  public void extractUniqueFromCommonStringWithOneDirectiveOtherTry() {
    String result =
        StringDataExtractor.from(COMMON_STRING).withDirective(regexp("string", 0)).extractUnique();
    assertThat(result, is("string"));
  }

  @Test
  public void extractUniqueFromCommonStringWithOneDirectiveAndNoValue() {
    String result =
        StringDataExtractor.from(COMMON_STRING).withDirective(regexp(".*(novalue).*", 1))
            .extractUnique();
    assertThat(result, isEmptyString());
  }

  @Test(expected = IllegalStateException.class)
  public void extractUniqueFromCommonStringWithOneDirectiveButSeveralValues() {
    StringDataExtractor.from(COMMON_STRING).withDirective(regexp("(?i).*(string).*", 1))
        .extractUnique();

  }
}