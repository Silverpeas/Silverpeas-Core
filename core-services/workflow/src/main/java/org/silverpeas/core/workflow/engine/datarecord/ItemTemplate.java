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
package org.silverpeas.core.workflow.engine.datarecord;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.Item;

/**
 * A ItemTemplate builds fields giving the title of a process instance.
 */
public class ItemTemplate extends ProcessInstanceFieldTemplate {
  public ItemTemplate(String fieldName, Item item, String role, String lang) {
    super(fieldName, item.getType(), item.getType(), item.getLabel(role, lang));
  }

  /**
   * Returns a field built from this template and filled from the given process instance.
   */
  public Field getField(ProcessInstance instance) throws FormException {
    try {
      String shortFieldName = getFieldName();

      if (shortFieldName.contains("instance.")
          && shortFieldName.startsWith("instance."))
        shortFieldName = shortFieldName.substring(9);

      else if (shortFieldName.contains("folder.")
          && shortFieldName.startsWith("folder."))
        shortFieldName = shortFieldName.substring(7);

      Field returnedField = instance.getField(shortFieldName);

      if (returnedField != null) {
        return returnedField;
      } else {
        throw new FormException("Unexpected field " + getFieldName());
      }
    } catch (WorkflowException e) {
      throw new FormException(e);
    }
  }
}
