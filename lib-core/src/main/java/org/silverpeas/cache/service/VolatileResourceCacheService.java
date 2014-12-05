/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.cache.service;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.session.SessionInfo;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.attachment.AttachmentServiceFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This cache permits to reference some volatile resources (an entity that is being registered into
 * persistence, but not yet validated by the user)
 * @author Yohann Chastagnier
 */
public class VolatileResourceCacheService {

  private Map<ForeignPK, Object> componentResources = new HashMap<ForeignPK, Object>();

  /**
   * Creates a new volatile identifier of integer type.
   * @return a new volatile identifier of integer type.
   */
  public int newVolatileIntegerIdentifier() {
    String volatileId = newVolatileIntegerIdentifierAsString();
    return Integer.parseInt(volatileId);
  }

  /**
   * Creates a new volatile identifier of integer type converted as string value.
   * @return a new volatile identifier of integer type converted as string value.
   */
  public synchronized String newVolatileIntegerIdentifierAsString() {
    try {
      Thread.sleep(1);
    } catch (InterruptedException ignored) {
    }
    String currentTime = String.valueOf(System.currentTimeMillis());
    return "-" + currentTime.substring(currentTime.length() - 9, currentTime.length());
  }

  /**
   * Creates a new volatile identifier of long type.
   * @return a new volatile identifier of long type.
   */
  public long newVolatileLongIdentifier() {
    String volatileId = newVolatileLongIdentifierAsString();
    return Long.parseLong(volatileId);
  }

  /**
   * Creates a new volatile identifier of long type converted as string value.
   * @return a new volatile identifier of long type converted as string value.
   */
  public synchronized String newVolatileLongIdentifierAsString() {
    try {
      Thread.sleep(1);
    } catch (InterruptedException ignored) {
    }
    String currentTime = String.valueOf(System.currentTimeMillis());
    return "-" + currentTime.substring(1);
  }

  /**
   * Creates a new volatile identifier of string type.
   * @return a new volatile identifier of string type.
   */
  public String newVolatileStringIdentifier() {
    return "volatile-" + UUID.randomUUID().toString();
  }

  /**
   * Adds a contribution into the cache.
   * @param contribution the contribution to add into the cache.
   * @return the previous value associated with <tt>key</tt>, or
   * <tt>null</tt> if there was no mapping for <tt>key</tt>.
   * (A <tt>null</tt> return can also indicate that the map
   * previously associated <tt>null</tt> with <tt>key</tt>,
   * if the implementation supports <tt>null</tt> values.)
   */
  public Object addComponentResource(SilverpeasContent contribution) {
    return componentResources
        .put(new ForeignPK(contribution.getId(), contribution.getComponentInstanceId()),
            contribution);
  }

  /**
   * Removes from the cache a component resource.
   * @param resourceId the identifier of the resource into the component instance.
   * @param componentInstanceId the identifier of the component which holds the resource.
   * @return the previous value associated with <tt>key</tt>, or
   * <tt>null</tt> if there was no mapping for <tt>key</tt>.
   */
  public Object removeComponentResource(String resourceId, String componentInstanceId) {
    return componentResources.remove(new ForeignPK(resourceId, componentInstanceId));
  }

  /**
   * Clears all the resources referenced into this instance of volatile cache.
   */
  public void clear() {
    final VolatileResourceCacheService current = this;
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        current.deleteAllAttachments();
      }
    });
    try {
      thread.run();
    } catch (Throwable throwable) {
      // This treatment must not disturb the server in any case, so nothing is thrown here.
      SilverTrace.warn("cache", VolatileResourceCacheService.class.getName(),
          "The clear of volatile cache did not end successfully...");
    }
  }

  /**
   * Removes attachments created on volatile resources that have not been persisted.
   */
  private void deleteAllAttachments() {
    for (Map.Entry<ForeignPK, Object> componentResource : componentResources.entrySet()) {
      AttachmentServiceFactory.getAttachmentService()
          .deleteAllAttachments(componentResource.getKey().getId(),
              componentResource.getKey().getInstanceId());
    }
  }

  /**
   * Clears the volatile cache attached to a user session.
   * @param sessionInfo the session of a user.
   */
  public static void clearFrom(SessionInfo sessionInfo) {
    VolatileResourceCacheService cache = sessionInfo.getCache()
        .get(VolatileResourceCacheService.class.getName(), VolatileResourceCacheService.class);
    if (cache != null) {
      cache.clear();
    }
  }
}
