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

import org.silverpeas.core.notification.system.AbstractResourceEvent;

/**
 * A form-record creation, update or deletion. Observers that read persistent data should use
 * {@code @Observes(during = TransactionPhase.AFTER_SUCCESS)}. An update carries the identity
 * before and after the operation, rather than snapshots of the form's field values.
 * Delivery order at transaction completion is not guaranteed: consumers should reread the
 * authoritative state rather than replaying the events as ordered commands.
 */
public class FormRecordEvent extends AbstractResourceEvent<FormRecordReference> {

  private static final long serialVersionUID = 1L;

  public FormRecordEvent(Type type, FormRecordReference... references) {
    super(type, references);
  }
}
