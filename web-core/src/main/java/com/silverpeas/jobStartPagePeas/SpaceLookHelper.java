/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.jobStartPagePeas;

import java.io.File;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

public class SpaceLookHelper implements Serializable {

  private static final long serialVersionUID = 1L;

  private String spaceId = null;
  private Hashtable<String, SpaceLookItem> items = new Hashtable<String, SpaceLookItem>();

  public SpaceLookHelper(String spaceId) {
    this.spaceId = spaceId;
  }

  public void setFiles(List<File> files) {
    if (files != null) {
      for (File file : files) {
        SpaceLookItem item = new SpaceLookItem(file, spaceId);

        if (item != null) {
          items.put(item.getName()
              .substring(0, item.getName().lastIndexOf(".")), item);
        }
      }
    }
  }

  public SpaceLookItem getItem(String name) {
    return items.get(name);
  }
}
