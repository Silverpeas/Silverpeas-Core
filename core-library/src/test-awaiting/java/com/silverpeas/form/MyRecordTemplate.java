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

import java.util.List;

import org.silverpeas.util.CollectionUtil;

/**
 * A record template implementation for testing purpose.
 */
public class MyRecordTemplate implements RecordTemplate {

  private List<FieldTemplate> templates;

  public MyRecordTemplate(final FieldTemplate... fieldTemplates) {
    this.templates = CollectionUtil.asList(fieldTemplates);
  }

  @Override
  public String[] getFieldNames() {
    String[] names = new String[templates.size()];
    for (int i = 0; i < templates.size(); i++) {
      names[i] = templates.get(i).getFieldName();
    }
    return names;
  }

  @Override
  public FieldTemplate[] getFieldTemplates() throws FormException {
    return templates.toArray(new FieldTemplate[templates.size()]);
  }

  @Override
  public FieldTemplate getFieldTemplate(final String fieldName) throws FormException {
    FieldTemplate template = null;
    for (FieldTemplate fieldTemplate : templates) {
      if (fieldTemplate.getFieldName().equals(fieldName)) {
        template = fieldTemplate;
        break;
      }
    }
    return template;
  }

  @Override
  public int getFieldIndex(final String fieldName) throws FormException {
    int index = -1;
    for (int i = 0; i < templates.size(); i++) {
      FieldTemplate fieldTemplate = templates.get(i);
      if (fieldTemplate.getFieldName().equals(fieldName)) {
        index = i;
        break;
      }
    }
    return index;
  }

  @Override
  public DataRecord getEmptyRecord() throws FormException {
    return null;
  }

  @Override
  public boolean checkDataRecord(DataRecord record) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
