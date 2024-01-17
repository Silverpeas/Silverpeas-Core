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

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A dummy record set.
 */
public class DummyRecordSet implements RecordSet {
  private final DummyRecordTemplate recordTemplate;

  /**
   * The no parameters constructor.
   */
  public DummyRecordSet() {
    this.recordTemplate = new DummyRecordTemplate();
  }

  public DummyRecordSet(RecordTemplate template) throws FormException {
    this.recordTemplate = new DummyRecordTemplate(template);
  }

  /**
   * Returns the RecordTemplate shared by all the DataRecord of this RecordSet.
   */
  @Override
  public RecordTemplate getRecordTemplate() {
    return recordTemplate;
  }

  /**
   * Returns an empty DataRecord built on the RecordTemplate.
   * @throws FormException if an error occurs
   */
  @Override
  public DataRecord getEmptyRecord() throws FormException {
    return recordTemplate.getEmptyRecord();
  }

  /**
   * This dummy record set always return a dummy record.
   * @throws FormException if an error occurs
   */
  @Override
  public DataRecord getRecord(String recordId) throws FormException {
    return recordTemplate.getEmptyRecord();
  }

  @Override
  public DataRecord getRecord(String recordId, String language) throws FormException {
    return recordTemplate.getEmptyRecord();
  }

  public List<DataRecord> getRecords(String fieldName, String fieldValue) {
    return Collections.emptyList();
  }

  @Override
  public void save(DataRecord record) throws FormException {
    // dummy
  }

  @Override
  public void delete(String objectId) throws FormException {
    // dummy
  }

  @Override
  public void delete(String objectId, String language) throws FormException {
    // dummy
  }

  @Override
  public void clone(String originalExternalId, String originalComponentId, String cloneExternalId,
      String cloneComponentId, Map<String, String> attachmentIds) throws FormException {
    // dummy
  }

  @Override
  public void merge(String fromExternalId, String fromComponentId, String toExternalId,
      String toComponentId, Map<String, String> attachmentIds) throws FormException {
    // dummy
  }

  @Override
  public void indexRecord(String recordId, String formName,
      FullIndexEntry indexEntry) throws FormException {
    // dummy
  }

  @Override
  public void copy(ResourceReference fromPK, ResourceReference toPK, RecordTemplate toRecordTemplate,
      Map<String, String> attachmentIds) throws FormException {
    // dummy
  }

  @Override
  public void move(ResourceReference fromPK, ResourceReference toPK, RecordTemplate toRecordTemplate)
      throws FormException {
    // dummy
  }
}