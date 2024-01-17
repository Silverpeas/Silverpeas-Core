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
package org.silverpeas.core.contribution.content.form;

import org.silverpeas.core.ResourceReference;

import java.io.Serializable;
import java.util.Map;

/**
 * A DataRecord is the interface used by all the form components to exchange, display and save a set
 * of named and typed fields which are unknown at compile time but defined by a silverpeas end user
 * in a workflow process model or a publication model. A DataRecord is modelled by a RecordTemplate
 * that defined the fields by their name and type. A DataRecord is built, managed and saved by a
 * RecordSet.
 * @see Field
 * @see RecordSet
 * @see RecordTemplate
 */
public interface DataRecord extends Serializable {
  /**
   * Returns the data record identifier. This id is unique within the {@link RecordSet} from witch
   * this data record has been get. This id is null when it has been built from a
   * {@link RecordTemplate} but not yet inserted into a {@link RecordSet}.
   */
  String getId();

  /**
   * Gives an id to the data record. This id must be set before the record is inserted in a
   * {@link RecordSet}.
   * @param externalId the identifier to set to this data record.
   */
  void setId(String externalId);

  /**
   * Is this data record a new one?
   * @return true if this record has not been inserted in a {@link RecordSet}. False otherwise.
   */
  boolean isNew();

  /**
   * Gets the specified named field. If there is several fields with the given name, then only the
   * first one is returned.
   * @param fieldName the name of a field.
   * @return the named field or, in the case there is no such field, either null or an empty field.
   * @throws FormException if the field cannot be got.
   * @apiNote the {@link FormException} can be thrown if the named field cannot be found instead of
   * returning null or an empty field. It depends of the implementation of the concrete class.
   */
  Field getField(String fieldName) throws FormException;

  /**
   * Gets the nth occurrence of the specified named field.
   * @param fieldName the name of a field.
   * @param occurrence the nth occurrence of such a field.
   * @return the named field or, in the case there is no such field, either null or an empty field.
   */
  Field getField(String fieldName, int occurrence);

  /**
   * Gets the field at the index position in this data record.
   * @param fieldIndex the index of the field in the data record.
   * @return the field or, in the case there is no such field, either null or an empty field.
   * @throws FormException when the index is invalid.
   */
  Field getField(int fieldIndex) throws FormException;

  /**
   * Gets the name of all the fields set in this data record.
   * @return an array with the name of the fields.
   */
  String[] getFieldNames();

  /**
   * Gets the size of this data record or -1 whether this operation isn't supported.
   * @return the total number of fields making this record, whatever their name. If this method
   * isn't supported, -1 is returned.
   */
  int size();

  /**
   * Gets the language in which any text valued some of the fields are.
   * @return the ISO-631 code of a supported language.
   */
  String getLanguage();

  /**
   * Sets the language in which the text fields are valued.
   * @param language the ISO-631 code of a supported language.
   */
  void setLanguage(String language);

  /**
   * Gets a dictionary of all field values, each of them mapped to the name of their corresponding
   * field.
   * @param language the language in which are the valued the text fields.
   * @return a {@link Map} of field values mapped to their corresponding field name.
   */
  Map<String, String> getValues(String language);

  /**
   * Gets the reference to the resource in Silverpeas to which this data record belongs.
   * @return a reference to a resource in Silverpeas.
   */
  ResourceReference getResourceReference();

}
