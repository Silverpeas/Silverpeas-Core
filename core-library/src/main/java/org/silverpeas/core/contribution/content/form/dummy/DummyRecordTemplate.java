/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.form.dummy;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericDataRecord;
import org.silverpeas.core.util.ArrayUtil;

/**
 * A dummy record template.
 */
public class DummyRecordTemplate implements RecordTemplate {

  private final DataRecord dataRecord;
  private final FieldTemplate fieldTemplate;

  /**
   * A DummyRecordTemplate is empty.
   */
  public DummyRecordTemplate() {
    fieldTemplate = new DummyFieldTemplate();
    dataRecord = new DummyDataRecord();
  }

  /**
   * Constructs a {@link DummyRecordTemplate} from the specified other {@link RecordTemplate}.
   * @param template another {@link RecordTemplate} object.
   * @throws FormException if an error occurs while initializing this {@link DummyRecordTemplate}
   * object.
   */
  public DummyRecordTemplate(RecordTemplate template) throws FormException {
    fieldTemplate = new DummyFieldTemplate();
    dataRecord = new GenericDataRecord(template);
  }

  @Override
  public String[] getFieldNames() {
    return ArrayUtil.emptyStringArray();
  }

  @Override
  public FieldTemplate[] getFieldTemplates() {
    return new FieldTemplate[0];
  }

  @Override
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    return fieldTemplate;
  }

  @Override
  public int getFieldIndex(String fieldName) throws FormException {
    return 0;
  }

  @Override
  public DataRecord getEmptyRecord() throws FormException {
    return dataRecord;
  }

  @Override
  public boolean checkDataRecord(DataRecord record) {
    return true;
  }
}
