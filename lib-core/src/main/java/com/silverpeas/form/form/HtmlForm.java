/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.form.form;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import javax.servlet.jsp.JspWriter;

import com.silverpeas.form.AbstractForm;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.TypeManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A Form is an object which can display in HTML the content of a DataRecord to a end user and can
 * retrieve via HTTP any updated values.
 * @see DataRecord
 * @see RecordTemplate
 * @see FieldDisplayer
 */
public class HtmlForm extends AbstractForm {
  private DataRecord record;
  private PagesContext PagesContext;
  private String m_FileName = null;
  private BufferedReader m_HtmlFile = null;
  private String currentLine = "";

  private static String TAG_BEGIN = "<%=";
  private static String TAG_END = "%>";

  public HtmlForm(RecordTemplate template) throws FormException {
    super(template);
  }

  public void setFileName(String fileName) {
    m_FileName = fileName;
  }

  private void closeHtmlFile() throws IOException {
    if (m_HtmlFile != null) {
      try {
        m_HtmlFile.close();
        m_HtmlFile = null;
      } catch (IOException e) {
        SilverTrace.error("form", "HtmlForm.closeHtmlFile",
            "form.EX_CANT_CLOSE_FILE", "fileName = [" + m_FileName + "]", e);
        throw e;
      }
    }
  }

  private void openHtmlFile() throws FileNotFoundException, IOException {
    closeHtmlFile();

    m_HtmlFile = new BufferedReader(new FileReader(new File(m_FileName)));

  }

  private void parseFile(PrintWriter out) throws IOException,
      FileNotFoundException {
    openHtmlFile();

    PagesContext pc = new PagesContext(PagesContext);
    boolean endOfFile = false;
    pc.incCurrentFieldIndex(1);

    do {
      endOfFile = printBeforeTag(out);
      if (!endOfFile)
        processTag(out, pc);

    } while (!endOfFile);
    closeHtmlFile();
  }

  private boolean printBeforeTag(PrintWriter out) throws IOException {
    if (currentLine == null || currentLine.length() == 0)
      currentLine = m_HtmlFile.readLine();

    // Testing end of file
    if (currentLine == null)
      return true;

    int pos = currentLine.indexOf(TAG_BEGIN);
    if (pos == -1) {
      // tag not found in the read line
      out.println(currentLine);
      currentLine = "";
      return printBeforeTag(out);
    } else {
      // tag found in the read line
      out.println(currentLine.substring(0, pos));
      currentLine = currentLine.substring(pos, currentLine.length());
      return false;
    }
  }

  private void processTag(PrintWriter out, PagesContext pc) throws IOException {
    if (currentLine == null || currentLine.length() == 0)
      currentLine = m_HtmlFile.readLine();

    // Testing end of file
    if (currentLine == null) {
      SilverTrace.error("form", "HtmlForm.processTag", "form.EX_END_OF_FILE");
      return;
    }

    // Testing begin tag
    int beginPos = currentLine.indexOf(TAG_BEGIN);
    if (beginPos != 0) {
      SilverTrace.error("form", "HtmlForm.processTag", "form.EX_TAG_NOT_FOUND");
      return;
    }

    // Search for end tag
    int endPos = currentLine.indexOf(TAG_END);
    while (endPos == -1) {
      String newLine = m_HtmlFile.readLine();
      if (newLine == null) {
        SilverTrace.error("form", "HtmlForm.processTag", "form.EX_END_OF_FILE");
        return;
      }
      currentLine += newLine;
      endPos = currentLine.indexOf(TAG_END);
    }

    String fieldName = currentLine.substring(beginPos + TAG_BEGIN.length(),
        endPos);

    if (fieldName.endsWith(".label")) {
      fieldName = fieldName.substring(0, fieldName.lastIndexOf("."));
      printFieldLabel(out, fieldName, pc);
    } else {
      printField(out, fieldName, pc);
    }
    currentLine = currentLine.substring(endPos + TAG_END.length(), currentLine
        .length());
  }

  private void printField(PrintWriter out, String fieldName, PagesContext pc)
      throws IOException {
    try {
      Field field = record.getField(fieldName);
      if (field != null) {
        boolean find = false;
        Iterator<FieldTemplate> itFields = null;
        if (getFieldTemplates() != null) {
          itFields = getFieldTemplates().iterator();
        }
        if (itFields != null && itFields.hasNext()) {
          FieldTemplate fieldTemplate;
          String fieldType;
          String fieldDisplayerName;

          while (!find && itFields.hasNext()) {
            fieldTemplate = itFields.next();
            fieldName = fieldName.substring(fieldName.indexOf(".") + 1,
                fieldName.length());
            if (fieldTemplate != null
                && fieldTemplate.getFieldName().equalsIgnoreCase(fieldName)) {
              fieldType = fieldTemplate.getTypeName();
              fieldDisplayerName = fieldTemplate.getDisplayerName();

              FieldDisplayer fieldDisplayer = TypeManager.getDisplayer(
                  fieldType, fieldDisplayerName);

              if (fieldDisplayer != null) {
                fieldDisplayer.display(out, field, fieldTemplate, pc);
              }
              find = true;
            }
          }
        }
        if (!find)
          out.print(field.getValue(pc.getLanguage()));
      }
    } catch (FormException fe) {
      SilverTrace.error("form", "HtmlForm.display", "form.EXP_UNKNOWN_FIELD",
          fieldName);
    }
  }

  private void printFieldLabel(PrintWriter out, String fieldName,
      PagesContext pc) throws IOException {
    boolean find = false;
    Iterator<FieldTemplate> itFields = null;
    if (getFieldTemplates() != null) {
      itFields = getFieldTemplates().iterator();
    }
    if (itFields != null && itFields.hasNext()) {
      FieldTemplate fieldTemplate;

      while (!find && itFields.hasNext()) {
        fieldTemplate = itFields.next();
        if (fieldTemplate != null
            && fieldTemplate.getFieldName().equalsIgnoreCase(fieldName)) {
          out.print(fieldTemplate.getLabel(pc.getLanguage()));
          find = true;
        }
      }
    }
  }

  /**
   * Prints the HTML layout of the dataRecord using the RecordTemplate to extract labels and extra
   * informations. The value formats may be adapted to a local language. Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>a field is unknown by the template.
   * <LI>a field has not the required type.
   * </UL>
   */
  public void display(JspWriter jw, PagesContext PagesContext, DataRecord record) {
    this.record = record;
    this.PagesContext = PagesContext;
    try {
      StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter(sw, true);

      out.println("<INPUT TYPE=\"hidden\" NAME=id VALUE=\"" + record.getId()
          + "\">");
      parseFile(out);

      out.flush();
      jw.write(sw.toString());
    } catch (FileNotFoundException fe) {
      SilverTrace.error("form", "HtmlForm.display", "form.EX_CANT_OPEN_FILE",
          null, fe);
    } catch (IOException fe) {
      SilverTrace.error("form", "HtmlForm.display", "form.EXP_CANT_WRITE",
          null, fe);
    }
  }

  /**
   * Prints the HTML layout of the dataRecord using the RecordTemplate to extract labels and extra
   * informations. The value formats may be adapted to a local language. Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>a field is unknown by the template.
   * <LI>a field has not the required type.
   * </UL>
   */
  public String toString(PagesContext PagesContext, DataRecord record) {
    this.record = record;
    this.PagesContext = PagesContext;
    StringWriter sw = new StringWriter();
    try {
      // StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter(sw, true);

      out.println("<INPUT TYPE=\"hidden\" NAME=id VALUE=\"" + record.getId()
          + "\">");
      parseFile(out);

      // out.flush();
      // jw.write(sw.toString());
    } catch (FileNotFoundException fe) {
      SilverTrace.error("form", "HtmlForm.toString", "form.EX_CANT_OPEN_FILE",
          null, fe);
    } catch (IOException fe) {
      SilverTrace.error("form", "HtmlForm.toString", "form.EXP_CANT_WRITE",
          null, fe);
    }
    return sw.getBuffer().toString();
  }

  /**
   * Get the form title No title for HTML form
   */
  public String getTitle() {
    return "";
  }
}
