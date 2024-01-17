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
package org.silverpeas.core.contribution.content.form.record;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;

import java.io.Serializable;

/**
 * A {@link IdentifiedRecordTemplate} decorates a {@link RecordTemplate} with a database identifier
 * and an external identifier.
 */
public class IdentifiedRecordTemplate implements RecordTemplate, Serializable {

  private static final long serialVersionUID = 1L;
  private int id = -1;
  private String externalId;
  private final RecordTemplate wrappedTemplate;
  private String templateName;
  private boolean encrypted;

  /**
   * A IdentifiedRecordTemplate is built upon a wrapped template.
   * @param wrappedTemplate another {@link RecordTemplate} instance.
   */
  public IdentifiedRecordTemplate(RecordTemplate wrappedTemplate) {
    this.wrappedTemplate = wrappedTemplate;
  }

  /**
   * Gets the wrapped template.
   * @return the wrapped {@link RecordTemplate} instance.
   */
  public RecordTemplate getWrappedTemplate() {
    return wrappedTemplate;
  }

  public String[] getFieldNames() {
    return wrappedTemplate.getFieldNames();
  }

  public FieldTemplate[] getFieldTemplates() throws FormException {
    return wrappedTemplate.getFieldTemplates();
  }

  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    return wrappedTemplate.getFieldTemplate(fieldName);
  }

 public int getFieldIndex(String fieldName) throws FormException {
    return wrappedTemplate.getFieldIndex(fieldName);
  }

 public DataRecord getEmptyRecord() throws FormException {
    return wrappedTemplate.getEmptyRecord();
  }

  public boolean checkDataRecord(DataRecord record) {
    return wrappedTemplate.checkDataRecord(record);
  }

  /**
   * Gets the external template id.
   * @return the external identifier set.
   */
  public String getExternalId() {
    return externalId;
  }

  /**
   * Gives an external id to the template.
   * @param externalId an identifier.
   */
  public void setExternalId(String externalId) {
    if (this.externalId == null) {
      this.externalId = externalId;
    }
  }

  /**
   * Gets the internal identifier. It is for internal mechanism.
   * @return an identifier.
   */
  public int getInternalId() {
    return id;
  }

  /**
   * Sets the internal identifier to this template. It is for internal mechanism.
   * @param id an internal identifier.
   */
  public void setInternalId(int id) {
    if (this.id == -1) {
      this.id = id;
    }
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public void setEncrypted(boolean encrypted) {
    this.encrypted = encrypted;
  }

  public boolean isEncrypted() {
    return encrypted;
  }

  public String getInstanceId() {
    return getExternalId().split(":")[0];
  }
}