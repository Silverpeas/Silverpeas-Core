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
package org.silverpeas.core.notification.sse;

import org.silverpeas.core.notification.sse.behavior.IgnoreStoring;

import java.util.List;

import static org.silverpeas.core.notification.sse.AbstractServerEventDispatcherTaskTest.EVENT_SOURCE_REQUEST_URI;
import static org.silverpeas.core.util.CollectionUtil.asList;

/**
 * @author Yohann Chastagnier
 */
class TestServerEventDEventSourceURI extends AbstractServerEventTest implements IgnoreStoring {

  private static final List<String> EVENT_SOURCE_URIS =
      asList(EVENT_SOURCE_REQUEST_URI, "/other/uri");
  private static final ServerEventName EVENT_NAME = () -> "EVENT_D";

  @Override
  public ServerEventName getName() {
    return EVENT_NAME;
  }

  @Override
  public List<String> getEventSourceURIs() {
    return EVENT_SOURCE_URIS;
  }
}
