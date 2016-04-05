/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.i18n;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.silverpeas.core.util.MultiSilverpeasBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author ehugonnet
 */
public class I18NHelperTest {

  /**
   * Test of getLanguageLabel method, of class I18NHelper.
   */
  @Test
  public void testGetLanguageLabel() {
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
   * Test of getLanguages method, of class I18NHelper.
   */
  @Test
  public void testGetLanguages() {
    Iterator<String> result = I18NHelper.getLanguages();
    List<String> languages = new ArrayList<String>(3);
    CollectionUtils.addAll(languages, result);
    assertThat(languages, containsInAnyOrder("en", "fr", "de"));
  }

  /**
   * Test of getAllSupportedLanguages method, of class I18NHelper.
   */
  @Test
  public void testGetAllSupportedLanguages() {
    Set<String> supportedLanguages = I18NHelper.getAllSupportedLanguages();
    assertThat(supportedLanguages, containsInAnyOrder("en", "fr", "de"));
  }

  /**
   * Test of getNumberOfLanguages method, of class I18NHelper.
   */
  @Test
  public void testGetNumberOfLanguages() {
    int nbLanguage = I18NHelper.getNumberOfLanguages();
    assertThat(nbLanguage, is(3));
  }

  /**
   * Test of isDefaultLanguage method, of class I18NHelper.
   */
  @Test
  public void testIsDefaultLanguage() {
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
  public void testCheckLanguage() {
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
  public void testGetHTMLLinksForCurrentLanguageByUrl() {
    String url = "http://www.google.fr";
    String currentLanguage = "fr";
    String result = I18NHelper.getHTMLLinks(url, currentLanguage);
    assertThat(result,
        is(
        "<a href=\"http://www.google.fr?SwitchLanguage=fr\" class=\"ArrayNavigationOn\" id=\"translation_fr\">FR</a>"
        + "&nbsp;<a href=\"http://www.google.fr?SwitchLanguage=en\" class=\"\" id=\"translation_en\">EN</a>"
        + "&nbsp;<a href=\"http://www.google.fr?SwitchLanguage=de\" class=\"\" id=\"translation_de\">DE</a>"));

    url = "http://www.google.com/search?client=ubuntu&ie=utf-8&oe=utf-8";
    currentLanguage = "en";
    result = I18NHelper.getHTMLLinks(url, currentLanguage);
    assertThat(result,
        is(
        "<a href=\"http://www.google.com/search?client=ubuntu&ie=utf-8&oe=utf-8&SwitchLanguage=fr\" class=\"\" id=\"translation_fr\">FR</a>"
        + "&nbsp;<a href=\"http://www.google.com/search?client=ubuntu&ie=utf-8&oe=utf-8&SwitchLanguage=en\" class=\"ArrayNavigationOn\" id=\"translation_en\">EN</a>"
        + "&nbsp;<a href=\"http://www.google.com/search?client=ubuntu&ie=utf-8&oe=utf-8&SwitchLanguage=de\" class=\"\" id=\"translation_de\">DE</a>"));
  }

  /**
   * Test of getHTMLLinks method, of class I18NHelper.
   */
  @Test
  public void testGetHTMLLinksForLanguages() {
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
  public void testGetHTMLLinksForI18NBeanAndCurrentLanguage() {
    I18NBean bean = mock(I18NBean.class);
    Translation tradFR = new Translation();
    tradFR.setId(1);
    tradFR.setLanguage("fr");
    tradFR.setObjectId("18");
    Translation tradEN = new Translation();
    tradEN.setId(2);
    tradEN.setLanguage("en");
    tradEN.setObjectId("28");
    Map<String, Translation> translations = new Hashtable<String, Translation>(2);
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
  public void testGetFormLine() {
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
//
//  /**
//   * Test of getFormLine method, of class I18NHelper.
//   */
//  @Test
//  public void testGetFormLine_3args() {
//    System.out.println("getFormLine");
//    ResourcesWrapper resources = null;
//    I18NBean bean = null;
//    String translation = "";
//    String expResult = "";
//    String result = I18NHelper.getFormLine(resources, bean, translation);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getHTMLSelectObject method, of class I18NHelper.
//   */
//  @Test
//  public void testGetHTMLSelectObject() {
//    String userLanguage = "fr";
//    I18NBean bean = mock(I18NBean.class);
//    Translation tradFR = new Translation();
//    tradFR.setId(1);
//    tradFR.setLanguage("fr");
//    tradFR.setObjectId("18");
//    Translation tradEN = new Translation();
//    tradEN.setId(2);
//    tradEN.setLanguage("en");
//    tradEN.setObjectId("28");
//    String selectedTranslation = "rn";
//    String expResult = "";
//    String result = I18NHelper.getHTMLSelectObject(userLanguage, bean, selectedTranslation);
//    assertThat(result, is("<SELECT name=\"I18NLanguage\" >\n" +
//        "<option value=\"fr_-1\" >Français</option>\n" +
//        "<option value=\"en_-1\" >Anglais</option>\n" +
//        "<option value=\"de_-1\" >Allemand</option>\n</SELECT>"));
//  }

//  /**
//   * Test of updateHTMLLinks method, of class I18NHelper.
//   */
//  @Test
//  public void testUpdateHTMLLinks() {
//    System.out.println("updateHTMLLinks");
//    I18NBean bean = null;
//    String expResult = "";
//    String result = I18NHelper.updateHTMLLinks(bean);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getLanguageAndTranslationId method, of class I18NHelper.
//   */
//  @Test
//  public void testGetLanguageAndTranslationId() {
//    System.out.println("getLanguageAndTranslationId");
//    HttpServletRequest request = null;
//    String[] expResult = null;
//    String[] result = I18NHelper.getLanguageAndTranslationId(request);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getSelectedContentLanguage method, of class I18NHelper.
//   */
//  @Test
//  public void testGetSelectedLanguage() {
//    System.out.println("getSelectedContentLanguage");
//    HttpServletRequest request = null;
//    String expResult = "";
//    String result = I18NHelper.getSelectedContentLanguage(request);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of setI18NInfo method, of class I18NHelper.
//   */
//  @Test
//  public void testSetI18NInfo_I18NBean_HttpServletRequest() {
//    System.out.println("setI18NInfo");
//    I18NBean bean = null;
//    HttpServletRequest request = null;
//    I18NHelper.setI18NInfo(bean, request);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of setI18NInfo method, of class I18NHelper.
//   */
//  @Test
//  public void testSetI18NInfo_I18NBean_List() {
//    System.out.println("setI18NInfo");
//    I18NBean bean = null;
//    List<FileItem> parameters = null;
//    I18NHelper.setI18NInfo(bean, parameters);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
}
