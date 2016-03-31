/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.notification.user.server.channel.popup;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.JpaBasicEntityManager;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

/**
 * JPA repository of <code>POPUPMessageBean</code> instances.
 * @author mmoquillon
 */
public class POPUPMessageBeanRepository
    extends JpaBasicEntityManager<POPUPMessageBean, UniqueLongIdentifier> {

  public POPUPMessageBean findFirstMessageByUserId(String userId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("userId", Long.valueOf(userId));
    return findFirstByNamedQuery("findByUserId", parameters);
  }

  public void deleteMessagesByUserIdAndSenderId(String userId, String senderId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("userId", Long.parseLong(userId)).add("senderId", senderId);
    deleteFromNamedQuery("deleteByUserIdAndSenderId", parameters);
  }
}
