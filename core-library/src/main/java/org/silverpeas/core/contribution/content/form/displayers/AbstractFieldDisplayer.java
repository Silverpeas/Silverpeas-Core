
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
package org.silverpeas.core.contribution.content.form.displayers;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ehugonnet
 * @param <T>
 */
public abstract class AbstractFieldDisplayer<T extends Field> implements FieldDisplayer<T> {

  @Override
  public List<String> update(List<FileItem> items, T field, FieldTemplate template,
          PagesContext pageContext) throws FormException {
    String fieldName = Util.getFieldOccurrenceName(template.getFieldName(), field.getOccurrence());
    String value = FileUploadUtil.getParameter(items, fieldName, null, pageContext.getEncoding());
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES
            && !StringUtil.isDefined(value)) {
      return new ArrayList<>(0);
    }
    return update(value, field, template, pageContext);
  }

  @Override
  public void index(FullIndexEntry indexEntry, String key, String fieldName, T field,
      String language, boolean stored) {
    if (field != null) {
      String value = field.getStringValue();
      if (value != null) {
        value = value.trim().replaceAll("##", " ");
        indexEntry.addField(key, value, language, stored); // add data in dedicated field
        indexEntry.addTextContent(value, language); // add data in global field
      }
    }
  }
}