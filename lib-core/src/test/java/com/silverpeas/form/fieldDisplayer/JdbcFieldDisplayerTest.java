/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.form.fieldDisplayer;

import com.google.common.base.Charsets;
import com.silverpeas.form.fieldType.JdbcField;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.PagesContext;
import com.silverpeas.jcrutil.RandomGenerator;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class JdbcFieldDisplayerTest {

  public JdbcFieldDisplayerTest() {
  }
  
  /**
   * Test of getManagedTypes method, of class JdbcFieldDisplayer.
   */
  @Test
  public void testGetManagedTypes() {
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    String[] result = instance.getManagedTypes();
    assertNotNull(result);
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
    String lineSeparator = System.getProperty("line.separator");
    assertEquals("if (isWhitespace(stripInitialWhitespace(field.value))) {" + lineSeparator
        + "\t\terrorMsg+=\"  - 'Mon champs JDBC' doit être renseigné\\n \";" + lineSeparator
        + "\t\terrorNb++;" + lineSeparator
        + "\t}" + lineSeparator
        + " try { " + lineSeparator
        + "if (typeof(checkmonChamps) == 'function')" + lineSeparator
        + " 	checkmonChamps('fr');" + lineSeparator
        + " } catch (e) { " + lineSeparator
        + " 	//catch all exceptions" + lineSeparator
        + " }", new String(out.toByteArray(), Charsets.UTF_8).trim());
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
    for(int i = 0; i< size; i++) {
      resList.add(String.valueOf(i));
    }
    when(field.selectSql(null, null, "0")).thenReturn(resList);
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    instance.display(printer, field, template, pagesContext);
    String display = new String(out.toByteArray(), Charsets.UTF_8).trim();
    assertNotNull(display);
    assertEquals(1390, display.length());
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
    JdbcField field =  mock(JdbcField.class);
    when(field.getTypeName()).thenReturn(JdbcField.TYPE);
    when(field.isReadOnly()).thenReturn(false);
    when(field.acceptValue("newValue", "fr")).thenReturn(true);    
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    instance.update(newValue, field, null, pagesContext);
    verify(field).setValue("newValue", "fr");
  }
  
  @Test(expected=com.silverpeas.form.FormException.class)
  public void testUpdateIncorrectField() throws Exception {
    String newValue = "";
    Field field = mock(Field.class);
    when(field.getTypeName()).thenReturn(RandomGenerator.getRandomString());
    JdbcFieldDisplayer instance = new JdbcFieldDisplayer();
    instance.update(newValue, field, null, null);
  }
  
  @Test(expected=com.silverpeas.form.FormException.class)
  public void testUpdateIncorrectValue() throws Exception {
    String newValue = "";
    PagesContext pagesContext = new PagesContext();
    pagesContext.setUseMandatory(true);
    pagesContext.setCurrentFieldIndex("10");
    pagesContext.setLastFieldIndex(20);
    pagesContext.setLanguage("fr");
    pagesContext.setEncoding("UTF-8");
    pagesContext.setUserId("0");
    JdbcField field =  mock(JdbcField.class);
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
    assertTrue(instance.isDisplayedMandatory());
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
    assertEquals(expResult, result);
  }
}