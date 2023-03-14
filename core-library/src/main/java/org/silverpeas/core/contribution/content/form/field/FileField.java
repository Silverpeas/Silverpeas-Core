/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.contribution.content.form.field;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;

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
  public static final String TYPE = TYPE_FILE;
  public static final String PARAM_ID_SUFFIX = "_id";

  @Override
  public String getTypeName() {
    return TYPE;
  }


  public String getAttachmentId() {
    return attachmentId;
  }

  public void setAttachmentId(String attachmentId) {
    this.attachmentId = attachmentId;
  }

  @Override
  public String getStringValue() {
    return getAttachmentId();
  }

  @Override
  public void setStringValue(String value) {
    this.attachmentId = value;
  }

  /**
   * The referenced attachment ids.
   */
  private String attachmentId;

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}