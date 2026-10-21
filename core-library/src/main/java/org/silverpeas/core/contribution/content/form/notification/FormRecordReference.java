/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.contribution.content.form.notification;

import java.io.Serializable;

/**
 * Immutable identity of a form record. No field values are exposed: consumers must read the
 * committed record if they need its contents.
 * @param templateId the external record-set identifier, such as {@code webPages42:example}.
 * @param recordId the external record identifier, such as {@code 0} for a web page.
 * @param language the stored language, or null for the default language.
 */
public record FormRecordReference(String templateId, String recordId, String language)
    implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Gets the component prefix when the template identifier is scoped to a component.
   * @return the component identifier, or null for an unscoped template.
   */
  public String componentInstanceId() {
    int separator = templateId.indexOf(':');
    return separator > 0 ? templateId.substring(0, separator) : null;
  }
}
