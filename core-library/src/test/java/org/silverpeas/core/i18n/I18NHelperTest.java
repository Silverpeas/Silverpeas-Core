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
package org.silverpeas.core.i18n;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.kernel.TestManagedBeanFeeder;
import org.silverpeas.kernel.test.UnitTest;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author ehugonnet
 */
@UnitTest
class I18NHelperTest {

  @BeforeAll
  static void setUpI18n() {
    I18n i18n = new I18n();
    TestManagedBeanFeeder feeder = new TestManagedBeanFeeder();
    feeder.removeAllManagedBeans();
    feeder.manageBean(i18n, I18n.class);
  }

  @AfterAll
  static void releaseI18n() {
    TestManagedBeanFeeder feeder = new TestManagedBeanFeeder();
    feeder.removeAllManagedBeans();
  }

  /**
   * Test of getLanguageLabel method, of class I18NHelper.
   */
  @Test
  void testGetLanguageLabel() {
    String code = "fr";
    String userLanguage = "en";
    String label = I18NHelper.getLanguageLabel(code, userLanguage);
    assertThat(label, is("French"));
    userLanguage = "fr";
    label = I18NHelper.getLanguageLabel(code, userLanguage);
    assertThat(label, is("Français"));
    code = "de";
    label = I18NHelper.getLanguageLabel(code, userLanguage);
    assertThat(label, is("Allemand"));
  }

  /**
   * Test of getAllSupportedLanguages method, of class I18NHelper.
   */
  @Test
  void testGetAllSupportedLanguages() {
    List<String> supportedLanguages = I18NHelper.getAllSupportedLanguages();
    assertThat(supportedLanguages, containsInAnyOrder("en", "fr", "de"));
  }

  /**
   * Test of isDefaultLanguage method, of class I18NHelper.
   */
  @Test
  void testIsDefaultLanguage() {
    String language = "en";
    boolean result = I18NHelper.isDefaultLanguage(language);
    assertThat(result, is(false));
    language = "fr";
    result = I18NHelper.isDefaultLanguage(language);
    assertThat(result, is(true));
    language = "de";
    result = I18NHelper.isDefaultLanguage(language);
    assertThat(result, is(false));
  }

  /**
   * Test of checkLanguage method, of class I18NHelper.
   */
  @Test
  void testCheckLanguage() {
    String language = "";
    String result = I18NHelper.checkLanguage(language);
    assertThat(result, is("fr"));
    language = "fr";
    result = I18NHelper.checkLanguage(language);
    assertThat(result, is("fr"));
    language = "en";
    result = I18NHelper.checkLanguage(language);
    assertThat(result, is("en"));
  }

  /**
   * Test of getHTMLLinks method, of class I18NHelper.
   */
  @Test
  void testGetHTMLLinksForCurrentLanguageByUrl() {
    String url = "https://www.google.fr";
    String currentLanguage = "fr";
    String result = I18NHelper.getHTMLLinks(url, currentLanguage);
    assertThat(result,
        is(
        "<a href=\"https://www.google.fr?SwitchLanguage=fr\" class=\"ArrayNavigationOn\" " +
            "id=\"translation_fr\">FR</a>"
        + "&nbsp;<a href=\"https://www.google.fr?SwitchLanguage=en\" class=\"\" " +
            "id=\"translation_en\">EN</a>"
        + "&nbsp;<a href=\"https://www.google.fr?SwitchLanguage=de\" class=\"\" " +
            "id=\"translation_de\">DE</a>"));

    url = "https://www.google.com/search?client=ubuntu&ie=utf-8&oe=utf-8";
    currentLanguage = "en";
    result = I18NHelper.getHTMLLinks(url, currentLanguage);
    assertThat(result,
        is(
        "<a href=\"https://www.google.com/search?client=ubuntu&ie=utf-8&oe=utf-8&SwitchLanguage" +
            "=fr\" class=\"\" id=\"translation_fr\">FR</a>"
        + "&nbsp;<a href=\"https://www.google" +
            ".com/search?client=ubuntu&ie=utf-8&oe=utf-8&SwitchLanguage=en\" class=\"ArrayNavigationOn\" id=\"translation_en\">EN</a>"
        + "&nbsp;<a href=\"https://www.google" +
            ".com/search?client=ubuntu&ie=utf-8&oe=utf-8&SwitchLanguage=de\" class=\"\" id=\"translation_de\">DE</a>"));
  }

  /**
   * Test of getHTMLLinks method, of class I18NHelper.
   */
  @Test
  void testGetHTMLLinksForLanguages() {
    List<String> languages = Arrays.asList("fr", "en");
    String currentLanguage = "fr";
    String result = I18NHelper.getHTMLLinks(languages, currentLanguage);
    assertThat(result,
        is(
        "<a href=\"javaScript:showTranslation('fr');\" class=\"ArrayNavigationOn\" id=\"translation_fr\">FR</a>"
        + "&nbsp;<a href=\"javaScript:showTranslation('en');\" class=\"\" id=\"translation_en\">EN</a>"));
  }

  /**
   * Test of getHTMLLinks method, of class I18NHelper.
   */
  @Test
  void testGetHTMLLinksForI18NBeanAndCurrentLanguage() {
    @SuppressWarnings("rawtypes") I18NBean bean = mock(I18NBean.class);
    BeanTranslation tradFR = new BeanTranslation();
    tradFR.setId("1");
    tradFR.setLanguage("fr");
    tradFR.setObjectId("18");
    BeanTranslation tradEN = new BeanTranslation();
    tradEN.setId("2");
    tradEN.setLanguage("en");
    tradEN.setObjectId("28");
    Map<String, BeanTranslation> translations = new Hashtable<>(2);
    translations.put("fr", tradFR);
    translations.put("en", tradEN);
    when(bean.getTranslation("fr")).thenReturn(tradFR);
    when(bean.getTranslation("en")).thenReturn(tradEN);
    when(bean.getTranslations()).thenReturn(translations);
    String currentLanguage = "fr";
    String result = I18NHelper.getHTMLLinks(bean, currentLanguage);
    assertThat(result,
        is(
        "<a href=\"javaScript:showTranslation('fr');\" class=\"ArrayNavigationOn\" id=\"translation_fr\">FR</a>"
        + "&nbsp;<a href=\"javaScript:showTranslation('en');\" class=\"\" id=\"translation_en\">EN</a>"));
  }

  /**
   * Test of getFormLine method, of class I18NHelper.
   */
  @Test
  void testGetFormLine() {
    MultiSilverpeasBundle resources = mock(MultiSilverpeasBundle.class);
    when(resources.getString("GML.language")).thenReturn("Langue");
    when(resources.getLanguage()).thenReturn("fr");
    String result = I18NHelper.getFormLine(resources);
    assertThat(result, is(
        "<tr>\n<td class=\"txtlibform\">Langue :</td>\n<td><SELECT name=\"I18NLanguage\" >\n"
        + "<option value=\"fr_-1\" >Français</option>\n"
        + "<option value=\"en_-1\" >Anglais</option>\n"
        + "<option value=\"de_-1\" >Allemand</option>\n</SELECT></td></tr>\n"));
  }
}
