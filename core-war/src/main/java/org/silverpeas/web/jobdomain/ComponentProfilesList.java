/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.web.jobdomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ComponentProfilesList extends ArrayList<ComponentProfiles> {
  private static final long serialVersionUID = 5012329059821756668L;

  private transient Map<Integer, ComponentProfiles> profilesByLocalComponentInstanceIds = new HashMap<>();

  public ComponentProfilesList() {
    super();
  }

  public ComponentProfiles getByLocalComponentInstanceId(int localComponentInstanceId) {
    return profilesByLocalComponentInstanceIds.get(localComponentInstanceId);
  }

  @Override
  public boolean add(final ComponentProfiles componentProfiles) {
    if (profilesByLocalComponentInstanceIds.putIfAbsent(componentProfiles.getComponent().getLocalId(), componentProfiles) == null) {
     return super.add(componentProfiles);
    }
    return false;
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public void clear() {
    super.clear();
    profilesByLocalComponentInstanceIds.clear();
  }
}
