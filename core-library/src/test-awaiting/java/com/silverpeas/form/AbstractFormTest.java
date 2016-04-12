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
package org.silverpeas.core.contribution.content.form;

import java.util.Arrays;
import java.util.List;
import org.silverpeas.core.contribution.content.form.dummy.DummyRecordTemplate;
import javax.servlet.jsp.JspWriter;
import org.apache.commons.fileupload.FileItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import static org.silverpeas.core.contribution.content.form.AbstractForm.REPEATED_FIELD_CSS_HIDE;
import static org.silverpeas.core.contribution.content.form.AbstractForm.REPEATED_FIELD_CSS_SHOW;

/**
 * Unit tests on the AbstractForm implemented methods.
 *
 */
public class AbstractFormTest {

  public static final String FIELD_NAME1 = "name";
  public static final String FIELD_NAME2 = "surname";
  public static final String FIELD_TYPE = "text";
  public static final String FIELD_LABEL1 = "name";
  public static final String FIELD_LABEL2 = "surname";
  public static final String FIELD_WYSIWYG = "wysiwyg";
  public static final String FIELD_WYSIWYG_TYPE = "textarea";

  public AbstractFormTest() {
  }

  @Before
  public void setUp() throws Exception {
    TypeManager typeManager = TypeManager.getInstance();
    typeManager.setDisplayer(MyFieldDisplayer.class.getName(),
        FIELD_TYPE, FIELD_NAME1, true);
    typeManager.setDisplayer(MyFieldDisplayer.class.getName(),
        FIELD_TYPE, FIELD_NAME2, true);
    typeManager.setDisplayer(MyFieldDisplayer.class.getName(),
        FIELD_WYSIWYG_TYPE, FIELD_WYSIWYG, true);
  }

  @After
  public void tearDown() {
  }

  /**
   * Empty test to check the unit test passes with its resources set up.
   */
  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  @Test
  public void testDisplayScriptsWithAnEmptyRecordTemplate() throws Exception {
    MyFormImpl myform = new MyFormImpl(new DummyRecordTemplate());
    JspWriter jspWriter = mock(JspWriter.class);
    PagesContext pageContext = mock(PagesContext.class);
    when(pageContext.getLanguage()).thenReturn("fr");
    myform.displayScripts(jspWriter, pageContext);
    verify(jspWriter).write(myform.toScript());
  }

  @Test
  public void testDisplayScriptWithANonEmptyRecordTemplate() throws Exception {
    MyFormImpl myForm = new MyFormImpl(new MyRecordTemplate(
        new MyFieldTemplate(FIELD_NAME1, FIELD_TYPE, FIELD_LABEL1),
        new MyFieldTemplate(FIELD_NAME2, FIELD_TYPE, FIELD_LABEL2)));
    JspWriter jspWriter = mock(JspWriter.class);
    PagesContext pageContext = mock(PagesContext.class);
    when(pageContext.getLanguage()).thenReturn("fr");
    myForm.displayScripts(jspWriter, pageContext);
    //verify(jspWriter).write(myForm.toScript());
  }

  @Test
  public void testIsEmptyWithNoFileItemsFound() throws Exception {
    MyFormImpl myForm = new MyFormImpl(new MyRecordTemplate(
        new MyFieldTemplate(FIELD_NAME1, FIELD_TYPE, FIELD_LABEL1),
        new MyFieldTemplate(FIELD_NAME2, FIELD_TYPE, FIELD_LABEL2)));
    DataRecord dataRecord = mock(DataRecord.class);
    PagesContext pagesContext = mock(PagesContext.class);
    FileItem fileItem = mock(FileItem.class);
    when(fileItem.getFieldName()).thenReturn("");
    when(fileItem.getName()).thenReturn("");
    when(fileItem.isFormField()).thenReturn(true);
    boolean isEmpty = myForm.isEmpty(Arrays.asList(fileItem, fileItem), dataRecord, pagesContext);
    assertTrue(isEmpty);
  }

  @Test
  public void testIsEmptyWithAllFileItemsWithoutContent() throws Exception {
    MyFormImpl myForm = new MyFormImpl(new MyRecordTemplate(
        new MyFieldTemplate(FIELD_NAME1, FIELD_TYPE, FIELD_LABEL1),
        new MyFieldTemplate(FIELD_NAME2, FIELD_TYPE, FIELD_LABEL2)));
    DataRecord dataRecord = mock(DataRecord.class);
    PagesContext pagesContext = mock(PagesContext.class);
    FileItem fileItem1 = mock(FileItem.class);
    when(fileItem1.getFieldName()).thenReturn(FIELD_NAME1);
    when(fileItem1.getName()).thenReturn(FIELD_NAME1);
    when(fileItem1.isFormField()).thenReturn(true);
    when(fileItem1.getString(anyString())).thenReturn("");
    FileItem fileItem2 = mock(FileItem.class);
    when(fileItem2.getFieldName()).thenReturn(FIELD_NAME2);
    when(fileItem2.getName()).thenReturn(FIELD_NAME2);
    when(fileItem2.isFormField()).thenReturn(true);
    when(fileItem2.getString(anyString())).thenReturn(null);
    boolean isEmpty = myForm.isEmpty(Arrays.asList(fileItem1, fileItem2), dataRecord, pagesContext);
    assertTrue(isEmpty);
  }

  @Test
  public void testIsNotEmptyWithOnlyOneFileItemWithoutContent() throws Exception {
    MyFormImpl myForm = new MyFormImpl(new MyRecordTemplate(
        new MyFieldTemplate(FIELD_NAME1, FIELD_TYPE, FIELD_LABEL1),
        new MyFieldTemplate(FIELD_NAME2, FIELD_TYPE, FIELD_LABEL2)));
    DataRecord dataRecord = mock(DataRecord.class);
    PagesContext pagesContext = mock(PagesContext.class);
    FileItem fileItem1 = mock(FileItem.class);
    when(fileItem1.getFieldName()).thenReturn(FIELD_NAME1);
    when(fileItem1.getName()).thenReturn(FIELD_NAME1);
    when(fileItem1.isFormField()).thenReturn(true);
    when(fileItem1.getString(anyString())).thenReturn("tartempion");
    FileItem fileItem2 = mock(FileItem.class);
    when(fileItem2.getFieldName()).thenReturn(FIELD_NAME2);
    when(fileItem2.getName()).thenReturn(FIELD_NAME2);
    when(fileItem2.isFormField()).thenReturn(true);
    when(fileItem2.getString(anyString())).thenReturn(null);
    boolean isEmpty = myForm.isEmpty(Arrays.asList(fileItem1, fileItem2), dataRecord, pagesContext);
    assertFalse(isEmpty);
  }

  @Test
  public void testUpdateOfEmptyFormDoesNothing() throws Exception {
    MyFormImpl myForm = new MyFormImpl(new DummyRecordTemplate());
    DataRecord dataRecord = mock(DataRecord.class);
    PagesContext pagesContext = mock(PagesContext.class);
    FileItem fileItem = mock(FileItem.class);
    when(dataRecord.getField(anyString())).thenReturn(null);
    List<String> attachments = myForm.update(Arrays.asList(fileItem), dataRecord,
        pagesContext, true);
    assertTrue(attachments.isEmpty());
  }

  @Test
  public void testUpdateWithWysiwygFields() throws Exception {
    MyFormImpl myForm = new MyFormImpl(new MyRecordTemplate(
        new MyFieldTemplate(FIELD_NAME1, FIELD_TYPE, FIELD_LABEL1),
        new MyFieldTemplate(FIELD_NAME2, FIELD_TYPE, FIELD_LABEL2),
        new MyFieldTemplate(FIELD_WYSIWYG, FIELD_WYSIWYG_TYPE, FIELD_WYSIWYG)));
    DataRecord dataRecord = mock(DataRecord.class);
    PagesContext pagesContext = mock(PagesContext.class);
    FileItem fileItem = mock(FileItem.class);
    when(dataRecord.getField(anyString())).thenReturn(null);
    List<String> attachments = myForm.update(Arrays.asList(fileItem), dataRecord, pagesContext,
        true);
    assertEquals(3, attachments.size());
    assertTrue(attachments.contains(FIELD_NAME1));
    assertTrue(attachments.contains(FIELD_NAME2));
    assertTrue(attachments.contains(FIELD_WYSIWYG));
  }

  @Test
  public void testUpdateWithoutWysiwygFields() throws Exception {
    MyFormImpl myForm = new MyFormImpl(new MyRecordTemplate(
        new MyFieldTemplate(FIELD_NAME1, FIELD_TYPE, FIELD_LABEL1),
        new MyFieldTemplate(FIELD_NAME2, FIELD_TYPE, FIELD_LABEL2),
        new MyFieldTemplate(FIELD_WYSIWYG, FIELD_WYSIWYG_TYPE, FIELD_WYSIWYG)));
    DataRecord dataRecord = mock(DataRecord.class);
    PagesContext pagesContext = mock(PagesContext.class);
    FileItem fileItem = mock(FileItem.class);
    when(dataRecord.getField(anyString())).thenReturn(null);
    List<String> attachments = myForm.update(Arrays.asList(fileItem), dataRecord, pagesContext,
        false);
    assertEquals(2, attachments.size());
    assertTrue(attachments.contains(FIELD_NAME1));
    assertTrue(attachments.contains(FIELD_NAME2));
  }

  @Test
  public void testUpdateWysiwygOfEmptyFormDoesNothing() throws Exception {
    MyFormImpl myForm = new MyFormImpl(new DummyRecordTemplate());
    DataRecord dataRecord = mock(DataRecord.class);
    PagesContext pagesContext = mock(PagesContext.class);
    FileItem fileItem = mock(FileItem.class);
    when(dataRecord.getField(anyString())).thenReturn(null);
    List<String> attachments = myForm.updateWysiwyg(Arrays.asList(fileItem), dataRecord,
        pagesContext);
    assertTrue(attachments.isEmpty());
  }

  @Test
  public void testUpdateWysiwyg() throws Exception {
    MyFormImpl myForm = new MyFormImpl(new MyRecordTemplate(
        new MyFieldTemplate(FIELD_NAME1, FIELD_TYPE, FIELD_LABEL1),
        new MyFieldTemplate(FIELD_NAME2, FIELD_TYPE, FIELD_LABEL2),
        new MyFieldTemplate(FIELD_WYSIWYG, FIELD_WYSIWYG_TYPE, FIELD_WYSIWYG)));
    DataRecord dataRecord = mock(DataRecord.class);
    PagesContext pagesContext = mock(PagesContext.class);
    FileItem fileItem = mock(FileItem.class);
    when(dataRecord.getField(anyString())).thenReturn(null);
    List<String> attachments = myForm.updateWysiwyg(Arrays.asList(fileItem), dataRecord,
        pagesContext);
    assertEquals(1, attachments.size());
    assertTrue(attachments.contains(FIELD_WYSIWYG));
  }

  @Test
  public void testUpdateWysiwygWithNoWysiwygFieldsDoesNothing() throws Exception {
    MyFormImpl myForm = new MyFormImpl(new MyRecordTemplate(
        new MyFieldTemplate(FIELD_NAME1, FIELD_TYPE, FIELD_LABEL1),
        new MyFieldTemplate(FIELD_NAME2, FIELD_TYPE, FIELD_LABEL2)));
    DataRecord dataRecord = mock(DataRecord.class);
    PagesContext pagesContext = mock(PagesContext.class);
    FileItem fileItem = mock(FileItem.class);
    when(dataRecord.getField(anyString())).thenReturn(null);
    List<String> attachments = myForm.updateWysiwyg(Arrays.asList(fileItem), dataRecord,
        pagesContext);
    assertTrue(attachments.isEmpty());
  }

  /**
   * A simple implementation of the AbstractForm class in order to test the method implemented by
   * the abstract class.
   */
  public class MyFormImpl extends AbstractForm {

    private final String language = "fr";

    public MyFormImpl(final RecordTemplate recordTemplate) throws Exception {
      super(recordTemplate);
    }

    @Override
    public void display(JspWriter out, PagesContext pagesContext, DataRecord record) {
    }

    /**
     * The result expected by the displayScripts method of the AbstractForm class.
     *
     * @return the expected result of AbstractForm#displayScripts method
     */
    protected String toScript() {
      List<FieldTemplate> templates = getFieldTemplates();
      MyFieldDisplayer displayer = new MyFieldDisplayer();
      StringBuilder builder = new StringBuilder();
      if (!templates.isEmpty()) {
        builder.append("<script type=\"text/javascript\" src=\"/weblib/xmlForms/").
            append(getFieldTemplates().get(0).getTemplateName()).append(
                ".js\"></script>\n");
      }

      builder.append(Util.getJavascriptIncludes(language))
          .append("\n<script type=\"text/javascript\">\n")
          .append("	var errorNb = 0;\n")
          .append("	var errorMsg = \"\";\n")
          .append("function addXMLError(message) {\n")
          .append("	errorMsg+=\"  - \"+message+\"\\n\";\n")
          .append("	errorNb++;\n")
          .append("}\n")
          .append("function getXMLField(fieldName) {\n")
          .append("	return document.getElementById(fieldName);\n")
          .append("}\n")
          .append("function isCorrectForm() {\n")
          .append("	errorMsg = \"\";\n")
          .append("	errorNb = 0;\n")
          .append("	var field;\n")
          .append("	\n\n");
      for (FieldTemplate fieldTemplate : templates) {
        builder.append("	field = document.getElementById(\"")
            .append(fieldTemplate.getFieldName())
            .append("\");\n")
            .append("	if (field != null) {\n")
            .append(displayer.toScript(fieldTemplate))
            .append("}\n\n");
      }
      builder.append("	\n\n")
          .append("	switch(errorNb)\n")
          .append("	{\n")
          .append("	case 0 :\n")
          .append("		result = true;\n")
          .append("		break;\n")
          .append("	case 1 :\n")
          .append("		errorMsg = \"")
          .append(Util.getString("GML.ThisFormContains", language))
          .append(" 1 ")
          .append(Util.getString("GML.error", language))
          .append(" : \\n \" + errorMsg;\n")
          .append("		window.alert(errorMsg);\n")
          .append("		result = false;\n")
          .append("		break;\n")
          .append("	default :\n")
          .append("		errorMsg = \"")
          .append(Util.getString("GML.ThisFormContains", language))
          .append(" \" + errorNb + \" ")
          .append(Util.getString("GML.errors", language))
          .append(" :\\n \" + errorMsg;\n")
          .append("		window.alert(errorMsg);\n")
          .append("		result = false;\n")
          .append("		break;\n")
          .append("	}\n")
          .append("	return result;\n")
          .append("}\n")
          .append("	\n\n")
          .append("function showOneMoreField(fieldName) {\n")
          .append("$('.field_'+fieldName+' ." + REPEATED_FIELD_CSS_HIDE + ":first').removeClass('"
              + REPEATED_FIELD_CSS_HIDE + "').addClass('" + REPEATED_FIELD_CSS_SHOW + "');\n")
          .append("if ($('.field_'+fieldName+' ." + REPEATED_FIELD_CSS_HIDE + "').length == 0) {\n")
          .append(" $('#form-row-'+fieldName+' #moreField-'+fieldName).hide();\n")
          .append("}\n")
          .append("}\n")
          .append("</script>\n");

      return builder.toString();
    }

    @Override
    public String toString(final PagesContext pageContext, final DataRecord dataRecord) {
      return toScript();
    }

  }

}
