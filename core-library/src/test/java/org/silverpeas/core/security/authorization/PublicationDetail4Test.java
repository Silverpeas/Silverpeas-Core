/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.core.security.authorization;

import org.jodconverter.office.utils.Lo;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

import java.util.ArrayList;
import java.util.List;

/**
 * @author silveryocha
 */
class PublicationDetail4Test extends PublicationDetail {
  private static final long serialVersionUID = 4151460677735398441L;

  private List<Location> locations = new ArrayList<>();

  PublicationDetail4Test(final String id, final NodeDetail node) {
    final NodePK nodePK = node.getNodePK();
    setPk(new PublicationPK(id, nodePK.getInstanceId()));
    setStatus(PublicationDetail.VALID_STATUS);
    locations.add(new Location(nodePK.getId(), nodePK.getInstanceId()));
    clearAliases();
  }

  PublicationDetail4Test(final String id, final NodeDetail node, final String cloneId) {
    this(id, node);
    setCloneId(cloneId);
  }

  PublicationDetail4Test(final String id, final NodeDetail node, final String cloneId, final boolean isTheClone) {
    this(id, node, cloneId);
    setStatus(CLONE_STATUS);
  }

  PublicationDetail4Test clearAliases() {
    final Location main = locations.get(0);
    locations.clear();
    locations.add(main);
    return this;
  }

  PublicationDetail4Test addAliasLocation(final NodeDetail node) {
    final NodePK nodePK = node.getNodePK();
    final Location location = new Location(nodePK.getId(), nodePK.getInstanceId());
    location.setAsAlias("userId");
    locations.add(location);
    return this;
  }

  List<Location> getLocations() {
    return locations;
  }
}
