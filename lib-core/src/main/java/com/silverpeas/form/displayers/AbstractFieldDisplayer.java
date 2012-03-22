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

package com.silverpeas.form.displayers;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.fileupload.FileItem;

/**
 * @author ehugonnet
 * @param <T>
 */
public abstract class AbstractFieldDisplayer<T extends Field> implements FieldDisplayer<T> {

  @Override
  public List<String> update(List<FileItem> items, T field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String value = FileUploadUtil.getParameter(items, template.getFieldName(), null, pageContext.
        getEncoding());
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES
        && !StringUtil.isDefined(value)) {
      return new ArrayList<String>(0);
    }
    return update(value, field, template, pageContext);
  }

  @Override
  public void index(FullIndexEntry indexEntry, String key, String fieldName, T field,
      String language) {
    if (field != null) {
      String value = field.getStringValue();
      if (value != null) {
        value = value.trim().replaceAll("##", " ");
        indexEntry.addField(key, value, language); // add data in dedicated field
        indexEntry.addTextContent(value, language); // add data in global field
      }
    }
  }
}
