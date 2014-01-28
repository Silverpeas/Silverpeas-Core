/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.form.fieldType;

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;

/**
 * A FileField stores an attachment reference.
 * @see Field
 * @see FieldDisplayer
 */
public class FileField extends TextField {

  private static final long serialVersionUID = -6926466281028971482L;
  /**
   * The text field type name.
   */
  static public final String TYPE = TYPE_FILE;
  static public final String PARAM_NAME_SUFFIX = "$$id";
  static public final String PARAM_ID_SUFFIX = "_id";

  /**
   * Returns the type name.
   */
  @Override
  public String getTypeName() {
    return TYPE;
  }

  /**
   * The no parameters constructor
   */
  public FileField() {
  }
  
  public List<String> getAttachmentIds() {
    return attachmentIds;
  }
  
  public void setAttachmentIds(List<String> attachmentIds) {
    this.attachmentIds = attachmentIds;
  }
  
  /**
   * Returns the string value of this field.
   */
  @Override
  public List<String> getStringValues() {
    return attachmentIds;
  }

  /**
   * Set the string value of this field.
   */
  @Override
  public void setStringValues(List<String> values) {
    this.attachmentIds = values;
  }

  /**
   * The referenced attachment ids.
   */
  private List<String> attachmentIds = new ArrayList<String>();

}