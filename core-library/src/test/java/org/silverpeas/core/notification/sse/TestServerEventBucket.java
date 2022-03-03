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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.sse;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A bucket to receive the different {@link ServerEvent} events coming from server event
 * notification.
 * @author Yohann Chastagnier
 */
@Singleton
public class TestServerEventBucket {

  private List<Entry> events = new ArrayList<>();

  public void listened(String fromListener, final ServerEvent event) {
    SilverLogger.getLogger(this)
        .info("{0} - receive event with name ''{1}'' and data ''{2}''", fromListener,
            event.getName().asString(), event.getData("RECEIVER_SESSION_KEY", new UserDetail()));
    events.add(new Entry(fromListener, event));
  }

  public List<ServerEvent> getServerEvents() {
    List<ServerEvent> serverEvents =
        events.stream().map(Entry::getEvent).collect(Collectors.toList());
    return Collections.unmodifiableList(serverEvents);
  }

  public boolean isEmpty() {
    return events.isEmpty();
  }

  public void empty() {
    events.clear();
  }

  static class Entry {
    private final String fromListener;
    private final ServerEvent event;

    Entry(final String fromListener, final ServerEvent event) {
      this.fromListener = fromListener;
      this.event = event;
    }

    public String getFromListener() {
      return fromListener;
    }

    public ServerEvent getEvent() {
      return event;
    }
  }
}
