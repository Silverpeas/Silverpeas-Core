/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

package org.silverpeas.core.contribution.attachment.process.huge;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * This manager allows to register huge treatment processing running at an instant on the
 * platform over attachments.
 * <p>
 *   The aim of a such manager is to monitoring some critical treatments, like switching all
 *   documents of a component instance from simple to versioned type. Indeed, some treatments can
 *   take a long time to perform and can be difficult to handle into a concurrency context. As
 *   this manager is able to give information on current huge treatments, the UI of some of
 *   attachment features can be adapted, for example, according to them.
 * </p>
 * @author silveryocha
 */
@Service
@Singleton
public class AttachmentHugeProcessManager {

  private final Set<String> allInstanceIds = Collections.synchronizedSet(new HashSet<>());
  private final ThreadLocal<Set<String>> instanceIdsOfThread = new ThreadLocal<>();

  public static AttachmentHugeProcessManager get() {
    return ServiceProvider.getSingleton(AttachmentHugeProcessManager.class);
  }

  /**
   * Is a huge processing running about the attachments hosted by the component instances
   * represented by the given identifier?
   * @param instanceId unique identifier of a component instance.
   * @return true if a huge processing is currently running, false otherwise.
   */
  public boolean isOneRunningOnInstance(final String instanceId) {
    return allInstanceIds.contains(instanceId);
  }

  /**
   * Is a huge processing running about the attachments hosted by the component instances
   * represented by the given identifier?
   * @param instanceId unique identifier of a component instance.
   * @throw AttachmentException if a huge processing is currently running, false otherwise.
   */
  public void checkNoOneIsRunningOnInstance(final String instanceId) {
    if (isOneRunningOnInstance(instanceId)) {
      throw new AttachmentException(MessageFormat.format(
          "Huge process over attachments of instance {0} is currently running.", instanceId));
    }
  }

  void startForInstances(final Set<String> instanceIds) {
    instanceIds.forEach(this::checkNoOneIsRunningOnInstance);
    final Set<String> newInstanceIdsOfThread = ofNullable(instanceIdsOfThread.get())
        .orElseGet(HashSet::new);
    this.allInstanceIds.addAll(instanceIds);
    newInstanceIdsOfThread.addAll(instanceIds);
    instanceIdsOfThread.set(newInstanceIdsOfThread);
  }

  void endForInstances(final Set<String> instanceIds) {
    this.allInstanceIds.removeAll(instanceIds);
    ofNullable(instanceIdsOfThread.get()).ifPresent(s -> {
      s.removeAll(instanceIds);
      if (s.isEmpty()) {
        instanceIdsOfThread.remove();
      }
    });
  }
}
