/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.pdc.form.displayers;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.displayers.AbstractFieldDisplayer;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.apache.commons.fileupload.FileItem;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A PdcPositionsFieldDisplayer is an object that prints out pdc positions for given axis.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class PdcPositionsFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  private PdcManager pdcManager = null;
  private ContentManager contentManager = null;

  /**
   * Constructor uses for test purpose only
   */
  public PdcPositionsFieldDisplayer(PdcManager pdcManager, ContentManager contentManager) {
    this.pdcManager = pdcManager;
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
            getPdcManager().getPositions(silverContentId, context.getComponentId());

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
            Value value = getPdcManager().getValue(axisId, valueId);
            cell.addElement(value.getName(language));
            row.addElement(cell);
            positionsTables.addElement(row);
          }
        }
        result.addElement(positionsTables);
        out.println(positionsTables.toString());
      } catch (ContentManagerException | PdcException e) {
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
    return new ArrayList<>();
  }

  @Override
  public List<String> update(String values, TextField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {

    // nothing to do as this displayer is for readonly purpose.
    return new ArrayList<>();
  }

  @Override
  public boolean isDisplayedMandatory() {
    return false;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }

  private PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = new GlobalPdcManager();
    }
    return pdcManager;
  }

}
