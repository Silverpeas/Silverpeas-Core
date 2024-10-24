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

package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import java.util.Objects;

/**
 * Unique identifier of a publication. As a publication can be located in different locations in
 * different component instances, it is always uniquely identified by its alone local identifier and
 * not by the pair local identifier/component instance identifier like with any others
 * contributions. This is why, the equality and the hash computation is done only on its local
 * unique identifier.
 *
 * @author silveryocha
 */
class PublicationIdentifier extends ContributionIdentifier {
  private static final long serialVersionUID = 6311113318238469259L;

  protected PublicationIdentifier(final String instanceId, final String localId,
      final String type) {
    super(instanceId, localId, type);
  }

  @Override
  public ResourceReference toReference() {
    return new PublicationPK(getLocalId(), getComponentInstanceId());
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof PublicationIdentifier) {
      PublicationIdentifier that = (PublicationIdentifier) o;
      return Objects.equals(that.getLocalId(), getLocalId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getLocalId());
  }
}
