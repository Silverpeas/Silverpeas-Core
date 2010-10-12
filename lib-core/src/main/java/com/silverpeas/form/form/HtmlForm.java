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
 * FLOSS exception.  You should have received a copy of the text describing
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
  private PagesContext pagesContext;
  private String m_FileName = null;
  private BufferedReader m_HtmlFile = null;
  private String currentLine = "";

  private static String TAG_BEGIN = "<%=";
  private static String TAG_END = "%>";

  /**
   * Creates a new HTML form from the specified template of records.
   * @param template the record template from which the form is built.
   * @throws FormException if an error occurs while setting up the form.
   */
  public HtmlForm(RecordTemplate template) throws FormException {
    super(template);
  }

  /**
   * Sets the HTML file into which the form should be printed.
   * @param fileName the HTML file.
   */
  public void setFileName(String fileName) {
    m_FileName = fileName;
  }

  /**
   * Closes the HTML file.
   * If the file is already closed, does nothing.
   * @throws IOException if an error occurs while closing the file.
   */
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

  /**
   * Opens the underlying HTML file.
   * If the file is already opened, it is then closed before opening it again.
   * @throws FileNotFoundException if the file doesn't exist.
   * @throws IOException if an error occurs while opening the file.
   */
  private void openHtmlFile() throws FileNotFoundException, IOException {
    closeHtmlFile();
    m_HtmlFile = new BufferedReader(new FileReader(new File(m_FileName)));

  }

  /**
   * Parses the content of the HTML file in order to insert the form at the correct position.
   * @param out the writer into which the content will be printed.
   * @throws IOException if an error occurs while working with the HTML file.
   * @throws FileNotFoundException if the underlying HTML file doesn't exist.
   */
  private void parseFile(PrintWriter out) throws IOException,
      FileNotFoundException {
    openHtmlFile();
    PagesContext pc = new PagesContext(pagesContext);
    boolean endOfFile = false;
    pc.incCurrentFieldIndex(1);
    do {
      endOfFile = printBeforeTag(out);
      if (!endOfFile)
        processTag(out, pc);

    } while (!endOfFile);
    closeHtmlFile();
  }

  /**
   * Prints the content of the HTML file before the position at which the form has to be written.
   * @param out the writer into which the content of the HTML file is printed with the form.
   * @return true if the position at which the form should be printed is found. Actually the position
   * is at the end of the underlying HTML file.
   * @throws IOException if an error occurs while working with the HTML file or the writer.
   */
  private boolean printBeforeTag(PrintWriter out) throws IOException {
    if (currentLine == null || currentLine.isEmpty())
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

  /**
   * Prints the content of the form into the writer.
   * @param out the writer.
   * @param pc the page context
   * @throws IOException if an error while printing the content of the form.
   */
  private void processTag(PrintWriter out, PagesContext pc) throws IOException {
    if (currentLine == null || currentLine.isEmpty())
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

  /**
   * Prints the field identified by its name.
   * @param out the writer into which the field is printed.
   * @param fieldName the name of the field.
   * @param pc the page context.
   * @throws IOException if an error occurs while printing the field.
   */
  private void printField(PrintWriter out, String fieldName, PagesContext pc)
          throws IOException {
    try {
      Field field = record.getField(fieldName);
      if (field != null) {
        boolean fieldFound = false;
        for (FieldTemplate fieldTemplate : getFieldTemplates()) {
          String fieldType;
          String fieldDisplayerName;
          fieldName = fieldName.substring(fieldName.indexOf(".") + 1,
                  fieldName.length());
          if (fieldTemplate != null
                  && fieldTemplate.getFieldName().equalsIgnoreCase(fieldName)) {
            fieldType = fieldTemplate.getTypeName();
            fieldDisplayerName = fieldTemplate.getDisplayerName();

            FieldDisplayer fieldDisplayer = TypeManager.getInstance().getDisplayer(
                    fieldType, fieldDisplayerName);

            if (fieldDisplayer != null) {
              fieldDisplayer.display(out, field, fieldTemplate, pc);
            }
            fieldFound = true;
            break;
          }
        }
        if (!fieldFound) {
          out.print(field.getValue(pc.getLanguage()));
        }
      }
    } catch (FormException fe) {
      SilverTrace.error("form", "HtmlForm.display", "form.EXP_UNKNOWN_FIELD",
              fieldName);
    }
  }

  /**
   * Prints the label of the field identified by its name.
   * @param out the writer into which the field should be written.
   * @param fieldName the name of the field.
   * @param pc the page context.
   * @throws IOException if an error occurs while printing the field label.
   */
  private void printFieldLabel(PrintWriter out, String fieldName,
      PagesContext pc) throws IOException {
    for (FieldTemplate fieldTemplate : getFieldTemplates()) {
      if (fieldTemplate != null
              && fieldTemplate.getFieldName().equalsIgnoreCase(fieldName)) {
        out.print(fieldTemplate.getLabel(pc.getLanguage()));
        break;
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
  @Override
  public void display(JspWriter jw, PagesContext PagesContext, DataRecord record) {
    this.record = record;
    this.pagesContext = PagesContext;
    try {
      StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter(sw, true);

      out.println("<input type=\"hidden\" name=\"id\" value=\"" + record.getId()
          + "\"/>");
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
  @Override
  public String toString(PagesContext PagesContext, DataRecord record) {
    this.record = record;
    this.pagesContext = PagesContext;
    StringWriter sw = new StringWriter();
    try {
      // StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter(sw, true);

      out.println("<input type=\"hidden\" name=\"id\" value=\"" + record.getId()
          + "\"/>");
      parseFile(out);
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
  @Override
  public String getTitle() {
    return "";
  }
}
