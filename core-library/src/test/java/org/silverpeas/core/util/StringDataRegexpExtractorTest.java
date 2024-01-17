/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.UnitTest;

import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.silverpeas.core.util.CollectionUtil.asList;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexps;

@UnitTest
class StringDataRegexpExtractorTest {

  private final static String COMMON_STRING =
      "common/string/0256/?param=ab-04\ncommon/StRing/abcd/toto";

  private final static Pattern VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE =
      Pattern.compile("(?i)string/([^/]+)/([a-z]{1,2})?");
  private final static int[] VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX = new int[]{1, 2};

  @Test
  void extractFromNullWithNoDirective() {
    List<String> result = StringDataExtractor.from(null).extract();
    assertThat(result, hasSize(0));
  }

  @Test
  void extractFromEmptyWithNoDirective() {
    List<String> result = StringDataExtractor.from("").extract();
    assertThat(result, hasSize(0));
  }

  @Test
  void extractFromCommonStringWithNoDirective() {
    List<String> result = StringDataExtractor.from(COMMON_STRING).extract();
    assertThat(result, hasSize(0));
  }

  @Test
  void extractFromNullWithOneDirective() {
    List<String> result = StringDataExtractor.from(null).withDirective(
        regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
            VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX)).extract();
    assertThat(result, hasSize(0));
  }

  @Test
  void extractFromEmptyWithOneDirective() {
    List<String> result = StringDataExtractor.from("").withDirective(
        regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
            VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX)).extract();
    assertThat(result, hasSize(0));
  }

  @Test
  void extractFromCommonStringWithOneDirective() {
    List<String> result = StringDataExtractor.from(COMMON_STRING).withDirective(
        regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
            VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX)).extract();
    assertThat(result, contains("0256", "abcd", "to"));
  }

  @Test
  void extractFromCommonStringWithOneDirectiveButNotGroupIndex() {
    List<String> result = StringDataExtractor.from(COMMON_STRING)
        .withDirective(regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE)).extract();
    assertThat(result, contains("string/0256/", "StRing/abcd/to"));
  }

  @Test
  void extractFromCommonStringWithOneDirectiveButBadGroupIndex() {
    assertThrows(IndexOutOfBoundsException.class, () ->
    StringDataExtractor.from(COMMON_STRING)
        .withDirective(regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE, 3)).extract());
  }

  @Test
  void extractFromCommonStringWithTwoDirectives() {
    List<String> result = StringDataExtractor.from(COMMON_STRING).withDirective(
        regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
            VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX))
        .withDirective(regexp(".*string/(.+)/(.{1,3})?.*", 2)).extract();
    assertThat(result, contains("0256", "abcd", "to", "?pa"));
  }

  @Test
  void extractFromCommonStringWithTwoDirectivesSpecifiedInOneCall() {
    List<String> result = StringDataExtractor.from(COMMON_STRING).withDirectives(regexps(
        asList(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
            Pattern.compile(".*string/(.+)/(.{1,3})?.*")),
        VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX)).extract();
    assertThat(result, contains("0256", "abcd", "to", "?pa"));
  }

  @Test
  void
  extractFromCommonStringWithTwoDirectivesInOtherOrderAndDemonstratesThatReturnedValuesAreUnique() {
    List<String> result = StringDataExtractor.from(COMMON_STRING)
        .withDirective(regexp(".*string/(.+)/(.{1,3})?.*", 2)).withDirective(
            regexp(VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE,
                VALID_COMMON_EXTRACT_PATTERN_DIRECTIVE_GROUP_INDEX)).extract();
    assertThat(result, contains("?pa", "0256", "abcd", "to"));
  }

  @Test
  void extractUniqueFromCommonStringWithOneDirective() {
    String result = StringDataExtractor.from(COMMON_STRING).withDirective(regexp(".*(string).*", 1))
        .extractUnique();
    assertThat(result, is("string"));
  }

  @Test
  void extractUniqueFromCommonStringWithOneDirectiveOtherTry() {
    String result =
        StringDataExtractor.from(COMMON_STRING).withDirective(regexp("string", 0)).extractUnique();
    assertThat(result, is("string"));
  }

  @Test
  void extractUniqueFromCommonStringWithOneDirectiveAndNoValue() {
    String result =
        StringDataExtractor.from(COMMON_STRING).withDirective(regexp(".*(novalue).*", 1))
            .extractUnique();
    assertThat(result, is(emptyString()));
  }

  @Test
  void extractUniqueFromCommonStringWithOneDirectiveButSeveralValues() {
    assertThrows(IllegalStateException.class, () ->
    StringDataExtractor.from(COMMON_STRING).withDirective(regexp("(?i).*(string).*", 1))
        .extractUnique());

  }
}