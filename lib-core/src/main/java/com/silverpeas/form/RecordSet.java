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

package com.silverpeas.form;

import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;

/**
 * A RecordSet manages a set of DataRecord built on a same RecordTemplate.
 * @see DataRecord
 * @see RecordTemplate
 */
public interface RecordSet {
  /**
   * Returns the RecordTemplate shared by all the DataRecord of this RecordSet.
   */
  public RecordTemplate getRecordTemplate();

  /**
   * Returns an empty DataRecord built on the RecordTemplate. This record is not yet managed by this
   * RecordSet. This is only an empty record which must be filled and saved in order to become a
   * DataRecord of this RecordSet.
   */
  public DataRecord getEmptyRecord() throws FormException;

  /**
   * Returns the DataRecord with the given id.
   * @throw FormException when the id is unknown.
   */
  public DataRecord getRecord(String id) throws FormException;

  public DataRecord getRecord(String recordId, String language)
      throws FormException;

  /**
   * Inserts the given DataRecord and set its id.
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has a not null id.
   * @throw FormException when the insert fail.
   */
  // public void insert(DataRecord record) throws FormException;

  /**
   * Updates the given DataRecord.
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has a null or unknown id.
   * @throw FormException when the update fail.
   */
  // public void update(DataRecord record) throws FormException;

  /**
   * Save the given DataRecord. If the record id is null then the record is inserted in this
   * RecordSet. Else the record is updated.
   * @see insert
   * @see update
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has an unknown id.
   * @throw FormException when the insert or update fail.
   */
  public void save(DataRecord record) throws FormException;

  /**
   * Index the given DataRecord into the indexEntry. formName looks like allFields (ie template
   * filename allFields.xml without extension)
   * @param data
   * @param formName
   * @param indexEntry
   * @throws FormException
   */
  public void indexRecord(String recordId, String formName,
      FullIndexEntry indexEntry) throws FormException;

  /**
   * Deletes the given DataRecord and set to null its id.
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has an unknown id.
   * @throw FormException when the delete fail.
   */
  public void delete(DataRecord record) throws FormException;

  /**
   * Clones the given DataRecord. Set to cloneExternalId its externalId and insert it.
   */
  public void clone(String originalExternalId, String originalComponentId, String cloneExternalId,
      String cloneComponentId) throws FormException;

  public void merge(String fromExternalId, String fromComponentId, String toExternalId,
      String toComponentId) throws FormException;

}
