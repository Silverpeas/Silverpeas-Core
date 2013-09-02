/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.form.displayers;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A PdcPositionsFieldDisplayer is an object that prints out pdc positions for given axis.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class PdcPositionsFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  private PdcBm pdcBm = null;
  private ContentManager contentManager = null;

  /**
   * Constructor uses for test purpose only
   */
  public PdcPositionsFieldDisplayer(PdcBm pdcBm, ContentManager contentManager) {
    this.pdcBm = pdcBm;
    this.contentManager = contentManager;
  }

  /**
   * Constructeur
   */
  public PdcPositionsFieldDisplayer() {
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    String[] s = new String[1];
    s[0] = TextField.TYPE;
    return s;
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the fieldName is unknown by the template.
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws java.io.IOException {
    // no javascript as this displayer is for readonly purpose.
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext context) throws FormException {

    String language = context.getLanguage();
    Map<String, String> parameters = template.getParameters(language);

    String axisId = parameters.get("axisId");
    if (!StringUtil.isDefined(axisId) || !StringUtil.isInteger(axisId)) {
      out.println("??axisId??");
    }

    else {
      // get PDC positions
      try {
        ContentManager contentManager = getContentManager();
        int silverContentId =
            contentManager.getSilverContentId(context.getObjectId(), context.getComponentId());
        List<ClassifyPosition> positions =
            getPdcBm().getPositions(silverContentId, context.getComponentId());

        ElementContainer result = new ElementContainer();
        Table positionsTables = new Table();
        positionsTables.setClass("pdcPositionsField");
        for (ClassifyPosition position : positions) {
          String valueId = position.getValueOnAxis(Integer.parseInt(axisId));
          if (StringUtil.isDefined(valueId)) {
            valueId = valueId.substring(0, valueId.length() - 1);
            valueId = valueId.substring(valueId.lastIndexOf('/') + 1, valueId.length());
            TR row = new TR();
            TD cell = new TD();
            Value value = getPdcBm().getValue(axisId, valueId);
            cell.addElement(value.getName(language));
            row.addElement(cell);
            positionsTables.addElement(row);
          }
        }
        result.addElement(positionsTables);
        out.println(positionsTables.toString());
      } catch (ContentManagerException e) {
        SilverTrace.debug("form", "PdcPositionsDisplayer.getParameterValues",
            "form.EX_CANT_READ_VALUE",
            "axisId = " + axisId + ", objectId=" + context.getObjectId(), e);
      } catch (PdcException pe) {
        SilverTrace.debug("form", "PdcPositionsDisplayer.getParameterValues",
            "form.EX_CANT_READ_VALUE",
            "axisId = " + axisId + ", objectId=" + context.getObjectId(), pe);
      }
    }

  }

  private ContentManager getContentManager() throws ContentManagerException {
    if (contentManager == null) {
      contentManager = new ContentManager();
    }

    return contentManager;
  }

  @Override
  public List<String> update(List<FileItem> items, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    // nothing to do as this displayer is for readonly purpose.
    return new ArrayList<String>();
  }

  @Override
  public List<String> update(String values, TextField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {

    // nothing to do as this displayer is for readonly purpose.
    return new ArrayList<String>();
  }

  @Override
  public boolean isDisplayedMandatory() {
    return false;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }

  private PdcBm getPdcBm() {
    if (pdcBm == null) {
      pdcBm = (PdcBm) new PdcBmImpl();
    }
    return pdcBm;
  }

}
