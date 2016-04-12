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
package org.silverpeas.core.contribution.content.form.displayers;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silverpeas.util.Charsets;

import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.fieldType.JdbcField;
import com.silverpeas.jcrutil.RandomGenerator;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class JdbcFieldDisplayerTest {

  String lineSeparator = System.getProperty("line.separator");
  String unixLineSeparator = "\n";

  public JdbcFieldDisplayerTest() {
  }

  /**
   * Test of getManagedTypes method, of class JdbcFieldDisplayer.
   */
  @Test
  public void testGetManagedTypes() {
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    String[] result = instance.getManagedTypes();
    assertThat(result, is(notNullValue()));
    assertThat(result, org.hamcrest.collection.IsArrayWithSize.arrayWithSize(1));
    assertThat(result, org.hamcrest.collection.IsArrayContaining.hasItemInArray(JdbcField.TYPE));
  }

  /**
   * Test of displayScripts method, of class JdbcFieldDisplayer.
   */
  @Test
  public void testDisplayScripts() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    PrintWriter printer = new PrintWriter(new OutputStreamWriter(out, Charsets.UTF_8), true);
    FieldTemplate template = mock(FieldTemplate.class);
    when(template.getTypeName()).thenReturn(JdbcField.TYPE);
    when(template.isMandatory()).thenReturn(true);
    when(template.getLabel("fr")).thenReturn("Mon champs JDBC");
    when(template.getFieldName()).thenReturn("monChamps");
    PagesContext pagesContext = new PagesContext();
    pagesContext.setUseMandatory(true);
    pagesContext.setCurrentFieldIndex("10");
    pagesContext.setLastFieldIndex(20);
    pagesContext.setLanguage("fr");
    pagesContext.setEncoding("UTF-8");
    pagesContext.setUserId("0");
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    instance.displayScripts(printer, template, pagesContext);
    assertThat(new String(out.toByteArray(), Charsets.UTF_8).trim(), is(
        "if (isWhitespace(stripInitialWhitespace(field.value))) {" + lineSeparator
        + "\t\terrorMsg+=\"  - 'Mon champs JDBC' doit être renseigné\\n\";" + lineSeparator
        + "\t\terrorNb++;" + lineSeparator + "\t}" + lineSeparator + " try { " + lineSeparator
        + "if (typeof(checkmonChamps) == 'function')" + lineSeparator + " 	checkmonChamps('fr');"
        + lineSeparator + " } catch (e) { " + lineSeparator + " 	//catch all exceptions"
        + lineSeparator + " }"));
  }

  /**
   * Test of display method, of class JdbcFieldDisplayer.
   */
  @Test
  public void testDisplay() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    PrintWriter printer = new PrintWriter(new OutputStreamWriter(out, Charsets.UTF_8), true);
    FieldTemplate template = mock(FieldTemplate.class);
    when(template.getTypeName()).thenReturn(JdbcField.TYPE);
    when(template.isMandatory()).thenReturn(true);
    when(template.getLabel("fr")).thenReturn("Mon champs JDBC");
    when(template.getFieldName()).thenReturn("monChamps");
    PagesContext pagesContext = new PagesContext();
    pagesContext.setUseMandatory(true);
    pagesContext.setCurrentFieldIndex("10");
    pagesContext.setLastFieldIndex(20);
    pagesContext.setLanguage("fr");
    pagesContext.setEncoding("UTF-8");
    pagesContext.setUserId("0");
    JdbcField field = mock(JdbcField.class);
    when(field.getTypeName()).thenReturn(JdbcField.TYPE);
    int size = 5;
    List<String> resList = new ArrayList<String>(size);
    for (int i = 0; i < size; i++) {
      resList.add(String.valueOf(i));
    }
    when(field.selectSql(null, null, "0")).thenReturn(resList);
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    instance.display(printer, field, template, pagesContext);
    String display = new String(out.toByteArray(), Charsets.UTF_8).trim();
    assertThat(display, is(notNullValue()));
    assertThat(display.length(), is(1381));
  }

  /**
   * Test of display method, of class JdbcFieldDisplayer.
   */
  @Test
  public void testDisplayListBox() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    PrintWriter printer = new PrintWriter(new OutputStreamWriter(out, Charsets.UTF_8), true);
    FieldTemplate template = mock(FieldTemplate.class);
    when(template.getTypeName()).thenReturn(JdbcField.TYPE);
    when(template.isMandatory()).thenReturn(true);
    when(template.getLabel("fr")).thenReturn("Mon champs JDBC");
    when(template.getFieldName()).thenReturn("monChamps");
    Map<String, String> parameters = new HashMap<String, String>(0);
    parameters.put("displayer", "listbox");
    when(template.getParameters("fr")).thenReturn(parameters);
    PagesContext pagesContext = new PagesContext();
    pagesContext.setUseMandatory(true);
    pagesContext.setCurrentFieldIndex("10");
    pagesContext.setLastFieldIndex(20);
    pagesContext.setLanguage("fr");
    pagesContext.setEncoding("UTF-8");
    pagesContext.setUserId("0");
    JdbcField field = mock(JdbcField.class);
    when(field.getTypeName()).thenReturn(JdbcField.TYPE);
    int size = 5;
    List<String> resList = new ArrayList<String>(size);
    for (int i = 0; i < size; i++) {
      resList.add(String.valueOf(i));
    }
    when(field.selectSql(null, null, "0")).thenReturn(resList);
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    instance.display(printer, field, template, pagesContext);
    String display = new String(out.toByteArray(), Charsets.UTF_8).trim();
    assertThat(display, is(notNullValue()));
    assertThat(display.length(), is(323));

    assertThat(display, is(
        "<select name=\"monChamps\" id=\"monChamps\" >" + unixLineSeparator
        + "<option value=\"\"></option><option value=\"0\">0</option>" + unixLineSeparator
        + "<option value=\"1\">1</option>" + unixLineSeparator + "<option value=\"2\">2</option>"
        + unixLineSeparator + "<option value=\"3\">3</option>" + unixLineSeparator
        + "<option value=\"4\">4</option>" + unixLineSeparator
        + "</select>" + unixLineSeparator
        + "&nbsp;<img src=\"/silverpeas//util/icons/mandatoryField.gif\" "
        + "width=\"5\" height=\"5\" alt=\"Obligatoire\"/>"));
  }

  /**
   * Test of update method, of class JdbcFieldDisplayer.
   */
  @Test
  public void testUpdate() throws Exception {
    String newValue = "newValue";
    PagesContext pagesContext = new PagesContext();
    pagesContext.setUseMandatory(true);
    pagesContext.setCurrentFieldIndex("10");
    pagesContext.setLastFieldIndex(20);
    pagesContext.setLanguage("fr");
    pagesContext.setEncoding("UTF-8");
    pagesContext.setUserId("0");
    JdbcField field = mock(JdbcField.class);
    when(field.getTypeName()).thenReturn(JdbcField.TYPE);
    when(field.isReadOnly()).thenReturn(false);
    when(field.acceptValue("newValue", "fr")).thenReturn(true);
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    instance.update(newValue, field, null, pagesContext);
    verify(field).setValue("newValue", "fr");
  }

  @Test(expected = org.silverpeas.core.contribution.content.form.FormException.class)
  public void testUpdateIncorrectField() throws Exception {
    String newValue = "";
    JdbcField field = mock(JdbcField.class);
    when(field.getTypeName()).thenReturn(RandomGenerator.getRandomString());
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    instance.update(newValue, field, null, null);
  }

  @Test(expected = org.silverpeas.core.contribution.content.form.FormException.class)
  public void testUpdateIncorrectValue() throws Exception {
    String newValue = "";
    PagesContext pagesContext = new PagesContext();
    pagesContext.setUseMandatory(true);
    pagesContext.setCurrentFieldIndex("10");
    pagesContext.setLastFieldIndex(20);
    pagesContext.setLanguage("fr");
    pagesContext.setEncoding("UTF-8");
    pagesContext.setUserId("0");
    JdbcField field = mock(JdbcField.class);
    when(field.getTypeName()).thenReturn(JdbcField.TYPE);
    when(field.isReadOnly()).thenReturn(true);
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    instance.update(newValue, field, null, pagesContext);
  }

  /**
   * Test of isDisplayedMandatory method, of class JdbcFieldDisplayer.
   */
  @Test
  public void testIsDisplayedMandatory() {
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    assertThat(instance.isDisplayedMandatory(), is(true));
  }

  /**
   * Test of getNbHtmlObjectsDisplayed method, of class JdbcFieldDisplayer.
   */
  @Test
  public void testGetNbHtmlObjectsDisplayed() {
    FieldTemplate template = null;
    PagesContext pagesContext = null;
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    int expResult = 1;
    int result = instance.getNbHtmlObjectsDisplayed(template, pagesContext);
    assertThat(result, is(expResult));
  }
}
