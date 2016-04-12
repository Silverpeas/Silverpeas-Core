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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.silverpeas.search.indexEngine.model.FullIndexEntry;

import org.silverpeas.core.contribution.content.form.displayers.AbstractFieldDisplayer;
import org.silverpeas.util.CollectionUtil;

import org.apache.commons.fileupload.FileItem;

/**
 * A displayer of a MyField object dedicated to tests.
 */
public class MyFieldDisplayer extends AbstractFieldDisplayer<Field> {

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext PagesContext)
      throws IOException {
    out.append(toScript(template));
  }

  @Override
  public void display(PrintWriter out, Field field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<String> update(List<FileItem> items, Field field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    return CollectionUtil.asList(template.getFieldName());
  }

  @Override
  public List<String> update(String value, Field field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isDisplayedMandatory() {
    return false;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }

  @Override
  public void index(FullIndexEntry indexEntry, String key, String fieldName, Field field,
      String language, boolean stored) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public String toScript(final FieldTemplate template) {
    StringBuilder builder = new StringBuilder();
    builder.append(template.getFieldName())
        .append(".name=")
        .append(template.getLabel())
        .append(";\n")
        .append(template.getFieldName())
        .append(".value='';\n");
    return builder.toString();
  }

}
