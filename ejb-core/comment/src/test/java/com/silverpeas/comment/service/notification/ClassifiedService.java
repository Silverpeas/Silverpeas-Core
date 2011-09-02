/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.comment.service.notification;

import com.silverpeas.SilverpeasComponentService;
import com.stratelia.webactiv.util.ResourceLocator;
import java.util.HashMap;
import java.util.Map;

/**
 * A Silverpeas component service to use in tests.
 */
public class ClassifiedService implements SilverpeasComponentService {
  
  public static final String COMPONENT_NAME = "classifieds";

  @Override
  public Classified getContent(String contentId) {
    if (!classifieds.containsKey(contentId)) {
      throw new RuntimeException("classified of id " + contentId + " not found");
    }
    return classifieds.get(contentId);
  }

  @Override
  public ResourceLocator getComponentSettings() {
    return new ResourceLocator("com.silverpeas.classifieds.settings.classifiedsSettings", "");
  }

  @Override
  public ResourceLocator getComponentMessages(String language) {
    return new ResourceLocator("com.silverpeas.classifieds.multilang.classifiedsBundle", language);
  }
  
  public void putContent(final Classified classified) {
    classifieds.put(classified.getId(), classified);
  }
  
  private Map<String, Classified> classifieds = new HashMap<String, Classified>();
}
