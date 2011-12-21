/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.util.i18n;

import com.google.common.collect.Lists;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class I18NHelperTest {

  public I18NHelperTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

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
    List<String> languages = Lists.newArrayList(result);
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
    assertThat(result, is("<a href=\"http://www.google.fr?SwitchLanguage=fr\" class=\"ArrayNavigationOn\" id=\"translation_fr\">FR</a>" +
        "&nbsp;<a href=\"http://www.google.fr?SwitchLanguage=en\" class=\"\" id=\"translation_en\">EN</a>" +
        "&nbsp;<a href=\"http://www.google.fr?SwitchLanguage=de\" class=\"\" id=\"translation_de\">DE</a>"));
    
    url = "http://www.google.com/search?client=ubuntu&ie=utf-8&oe=utf-8";
    currentLanguage = "en";
    result = I18NHelper.getHTMLLinks(url, currentLanguage);
    assertThat(result, is("<a href=\"http://www.google.com/search?client=ubuntu&ie=utf-8&oe=utf-8&SwitchLanguage=fr\" class=\"\" id=\"translation_fr\">FR</a>" +
        "&nbsp;<a href=\"http://www.google.com/search?client=ubuntu&ie=utf-8&oe=utf-8&SwitchLanguage=en\" class=\"ArrayNavigationOn\" id=\"translation_en\">EN</a>" +
        "&nbsp;<a href=\"http://www.google.com/search?client=ubuntu&ie=utf-8&oe=utf-8&SwitchLanguage=de\" class=\"\" id=\"translation_de\">DE</a>"));
  }

  /**
   * Test of getHTMLLinks method, of class I18NHelper.
   */
  @Test
  public void testGetHTMLLinksForLanguages() {
    List<String> languages = Lists.newArrayList("fr", "en");
    String currentLanguage = "fr";
    String result = I18NHelper.getHTMLLinks(languages, currentLanguage);
    assertThat(result, is("<a href=\"javaScript:showTranslation('fr');\" class=\"ArrayNavigationOn\" id=\"translation_fr\">FR</a>" +
        "&nbsp;<a href=\"javaScript:showTranslation('en');\" class=\"\" id=\"translation_en\">EN</a>"));
  }

//  /**
//   * Test of getHTMLLinks method, of class I18NHelper.
//   */
//  @Test
//  public void testGetHTMLLinks_I18NBean_String() {
//    System.out.println("getHTMLLinks");
//    I18NBean bean = null;
//    String currentLanguage = "";
//    String expResult = "";
//    String result = I18NHelper.getHTMLLinks(bean, currentLanguage);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getFormLine method, of class I18NHelper.
//   */
//  @Test
//  public void testGetFormLine_ResourcesWrapper() {
//    System.out.println("getFormLine");
//    ResourcesWrapper resources = null;
//    String expResult = "";
//    String result = I18NHelper.getFormLine(resources);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
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
//    System.out.println("getHTMLSelectObject");
//    String userLanguage = "";
//    I18NBean bean = null;
//    String selectedTranslation = "";
//    String expResult = "";
//    String result = I18NHelper.getHTMLSelectObject(userLanguage, bean, selectedTranslation);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
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
//   * Test of getSelectedLanguage method, of class I18NHelper.
//   */
//  @Test
//  public void testGetSelectedLanguage() {
//    System.out.println("getSelectedLanguage");
//    HttpServletRequest request = null;
//    String expResult = "";
//    String result = I18NHelper.getSelectedLanguage(request);
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
