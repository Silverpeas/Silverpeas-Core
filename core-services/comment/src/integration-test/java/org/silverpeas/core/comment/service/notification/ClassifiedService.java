/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.comment.service.notification;

import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.HashMap;
import java.util.Map;

/**
 * A Silverpeas component service to use in tests.
 */
public class ClassifiedService implements ApplicationService<Classified> {

  public static final String COMPONENT_NAME = "classifieds";

  @Override
  public Classified getContentById(String contentId) {
    if (!classifieds.containsKey(contentId)) {
      throw new RuntimeException("classified of id " + contentId + " not found");
    }
    return classifieds.get(contentId);
  }

  @Override
  public SettingBundle getComponentSettings() {
    return ResourceLocator.getSettingBundle(
        "org.silverpeas.classifieds.settings.classifiedsSettings");
  }

  @Override
  public LocalizationBundle getComponentMessages(String language) {
    return ResourceLocator.getLocalizationBundle(
        "org.silverpeas.classifieds.multilang.classifiedsBundle", language);
  }

  /**
   * Is this service related to the specified component instance. The service is related to the
   * specified instance if it is a service defined by the application from which the instance
   * was spawned.
   * @param instanceId the unique instance identifier of the component.
   * @return true if the instance is spawn from the application to which the service is related.
   * False otherwise.
   */
  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("classified");
  }

  public void putContent(final Classified classified) {
    classifieds.put(classified.getId(), classified);
  }

  private Map<String, Classified> classifieds = new HashMap<String, Classified>();
}
