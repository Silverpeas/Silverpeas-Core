/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.quickcheck.generators.SimpleStringGenerator.SpSimpleString;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author silveryocha
 */
@RunWith(JUnitQuickcheck.class)
public class WebEncodeHelperTest {

  @Test
  public void empty() {
    assertThat(true, is(true));
  }

  @Test
  public void javaStringToJsStringWhenNullOrEmpty() {
    assertThat(WebEncodeHelper.javaStringToJsString(null), isEmptyString());
    assertThat(WebEncodeHelper.javaStringToJsString(""), isEmptyString());
  }

  @Test
  public void javaStringToJsStringWithJson() {
    assertThat(WebEncodeHelper.javaStringToJsString(JSONCodec.encodeObject(o ->
        o.put("toto", "t'ti - \n le fou dingue ?!")
         .put("href", "https://www.silverpeas.org"))),
        is("{\\\"toto\\\":\\\"t\\'ti - \\\\n le fou dingue ?!\\\",\\\"href\\\":\\\"https:\\/\\/www.silverpeas.org\\\"}"));
  }

  @Property(trials = 1000)
  public void javaStringToJsStringQuickCheck(
      @When(seed = 0) @SpSimpleString(includes = "5..2000") String string) {
    final String actual = WebEncodeHelper.javaStringToJsString(string);
    assertThat(actual.length(), greaterThanOrEqualTo(string.length()));
  }

  @Test
  public void javaStringToHtmlStringWhenNullOrEmpty() {
    assertThat(WebEncodeHelper.javaStringToHtmlString(null), isEmptyString());
    assertThat(WebEncodeHelper.javaStringToHtmlString(""), isEmptyString());
  }

  @Test
  public void javaStringToHtmlStringWithRealHtml() {
    assertThat(WebEncodeHelper.javaStringToHtmlString(
        "<div><a href=\"https://www.silverpeas.org\"><span>Visit our community</span></a></div>"),
        is("&lt;div&gt;&lt;a href=&quot;https://www.silverpeas.org&quot;&gt;&lt;span&gt;Visit our community&lt;/span&gt;&lt;/a&gt;&lt;/div&gt;"));
  }

  @Property(trials = 1000)
  public void javaStringToHtmlStringQuickCheck(
      @When(seed = 0) @SpSimpleString(includes = "5..2000") String string) {
    final String actual = WebEncodeHelper.javaStringToHtmlString(string);
    assertThat(actual.length(), greaterThanOrEqualTo(string.length()));
  }

  @Test
  public void escapeXmlWhenNullOrEmpty() {
    assertThat(WebEncodeHelper.escapeXml(null), isEmptyString());
    assertThat(WebEncodeHelper.escapeXml(""), isEmptyString());
  }

  @Test
  public void escapeXmlWithRealHtml() {
    assertThat(WebEncodeHelper.escapeXml(
        "<div><a href=\"https://www.silverpeas.org\"><span>Visit our community</span></a></div>"),
        is("&lt;div&gt;&lt;a href=&quot;https://www.silverpeas.org&quot;&gt;&lt;span&gt;Visit our community&lt;/span&gt;&lt;/a&gt;&lt;/div&gt;"));
  }

  @Property(trials = 1000)
  public void escapeXmlQuickCheck(
      @When(seed = 0) @SpSimpleString(includes = "5..2000") String string) {
    final String actual = WebEncodeHelper.escapeXml(string);
    assertThat(actual.length(), greaterThanOrEqualTo(string.length()));
  }

  @Test
  public void convertWhiteSpacesForHTMLDisplayWhenNullOrEmpty() {
    assertThat(WebEncodeHelper.convertWhiteSpacesForHTMLDisplay(null), isEmptyString());
    assertThat(WebEncodeHelper.convertWhiteSpacesForHTMLDisplay(""), isEmptyString());
  }

  @Test
  public void convertWhiteSpacesForHTMLDisplayWithRealWhiteCharacters() {
    assertThat(WebEncodeHelper.convertWhiteSpacesForHTMLDisplay("\r"), is(""));
    assertThat(WebEncodeHelper.convertWhiteSpacesForHTMLDisplay("\r\r"), is(""));
    assertThat(WebEncodeHelper.convertWhiteSpacesForHTMLDisplay("\n"), is("<br/>"));
    assertThat(WebEncodeHelper.convertWhiteSpacesForHTMLDisplay("\n\n"), is("<br/><br/>"));
    assertThat(WebEncodeHelper.convertWhiteSpacesForHTMLDisplay("\t"), is("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
    assertThat(WebEncodeHelper.convertWhiteSpacesForHTMLDisplay("\t\t"), is("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
    assertThat(WebEncodeHelper.convertWhiteSpacesForHTMLDisplay("a"), is("a"));
  }

  @Property(trials = 1000)
  public void convertWhiteSpacesForHTMLDisplayQuickCheck(
      @When(seed = 0) @SpSimpleString(includes = "5..2000") String string) {
    final String actual = WebEncodeHelper.convertWhiteSpacesForHTMLDisplay(string);
    final int rCount = (int) string.chars().filter(c -> c == '\r').count();
    final int nCount = (int) string.chars().filter(c -> c == '\n').count();
    final int tCount = (int) string.chars().filter(c -> c == '\t').count();
    final int totalOffset = (-1 * rCount) + (nCount * 4) + ((tCount * 6 * 6) - tCount);
    assertThat(actual.length(), is(string.length() + totalOffset));
  }

  @Test
  public void javaStringToHtmlParagrapheWhenNullOrEmpty() {
    assertThat(WebEncodeHelper.javaStringToHtmlParagraphe(null), isEmptyString());
    assertThat(WebEncodeHelper.javaStringToHtmlParagraphe(""), isEmptyString());
  }

  @Test
  public void javaStringToHtmlParagrapheWithRealHtml() {
    assertThat(WebEncodeHelper.javaStringToHtmlParagraphe(
        "<div><a href=\"https://www.silverpeas.org\"><span>Visit our community</span></a></div>"),
        is("&lt;div&gt;&lt;a href=&quot;https://www.silverpeas.org&quot;&gt;&lt;span&gt;Visit our community&lt;/span&gt;&lt;/a&gt;&lt;/div&gt;"));
  }

  @Property(trials = 1000)
  public void javaStringToHtmlParagrapheQuickCheck(
      @When(seed = 0) @SpSimpleString(includes = "5..2000") String string) {
    final String actual = WebEncodeHelper.javaStringToHtmlParagraphe(string);
    final int rCount = (int) string.chars().filter(c -> c == '\r').count();
    final int nCount = (int) string.chars().filter(c -> c == '\n').count();
    final int tCount = (int) string.chars().filter(c -> c == '\t').count();
    final int totalOffset = (-1 * rCount) + (nCount * 4) + ((tCount * 6 * 6) - tCount);
    assertThat(actual.length(), greaterThanOrEqualTo(string.length() + totalOffset));
  }

  public void htmlStringToJavaString() {
  }

  @Test
  public void htmlStringToJavaStringWhenNullOrEmpty() {
    assertThat(WebEncodeHelper.htmlStringToJavaString(null), isEmptyString());
    assertThat(WebEncodeHelper.htmlStringToJavaString(""), isEmptyString());
  }

  @Test
  public void htmlStringToJavaStringWithRealHtml() {
    assertThat(WebEncodeHelper.htmlStringToJavaString(
        "&lt;div&gt;&lt;a href=&quot;https://www.silverpeas.org&quot;&gt;&lt;span&gt;Visit our community&lt;/span&gt;&lt;/a&gt;&lt;/div&gt;"),
        is("<div><a href=\"https://www.silverpeas.org\"><span>Visit our community</span></a></div>"));
  }

  @Property(trials = 1000)
  public void htmlStringToJavaStringQuickCheck(
      @When(seed = 0) @SpSimpleString(includes = "5..2000") String string) {
    final String actual = WebEncodeHelper.htmlStringToJavaString(string);
    assertThat(actual.length(), lessThanOrEqualTo(string.length()));
  }

  @Test
  public void encodeFilename() {
    assertThat(WebEncodeHelper.encodeFilename("toto.txt"), is("=?UTF-8?B?dG90by50eHQ=?="));
  }
}