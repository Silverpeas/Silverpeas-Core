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

import org.junit.jupiter.api.Test;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.junit.jupiter.api.Assertions.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class FormRecordEventTest {

  private final FormRecordReference reference =
      new FormRecordReference("webPages42:example", "0", "fr");

  @Test
  void creationExposesOnlyTheCreatedIdentity() {
    var event = new FormRecordEvent(ResourceEvent.Type.CREATION, reference);
    assertNull(event.getTransition().getBefore());
    assertEquals(reference, event.getTransition().getAfter());
    assertEquals("webPages42", reference.componentInstanceId());
  }

  @Test
  void updateRetainsBothIdentitiesWithoutDocumentaryValues() {
    var event = new FormRecordEvent(ResourceEvent.Type.UPDATE, reference, reference);
    assertEquals(reference, event.getTransition().getBefore());
    assertEquals(reference, event.getTransition().getAfter());
  }

  @Test
  void deletionKeepsTheIdentityAfterTheRecordHasGone() {
    var event = new FormRecordEvent(ResourceEvent.Type.DELETION, reference);
    assertEquals("webPages42:example", event.getTransition().getBefore().templateId());
    assertEquals("0", event.getTransition().getBefore().recordId());
    assertEquals("fr", event.getTransition().getBefore().language());
    assertNull(event.getTransition().getAfter());
  }

  @Test
  void unscopedTemplateHasNoComponent() {
    assertNull(new FormRecordReference("example", "0", null).componentInstanceId());
  }
}
