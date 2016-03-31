/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.notification.system;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A bucket to receive the different {@code org.silverpeas.core.notification.system.TestResourceEvent}
 * events coming from the different way of notifications.
 * @author mmoquillon
 */
@Singleton
public class TestResourceEventBucket {

  private List<TestResourceEvent> events = new ArrayList<>(2);

  public void pour(final TestResourceEvent event) {
    events.add(event);
  }

  public List<TestResourceEvent> getContent() {
    return Collections.unmodifiableList(events);
  }

  public boolean isEmpty() {
    return events.isEmpty();
  }

  public void empty() {
    events.clear();
  }
}
