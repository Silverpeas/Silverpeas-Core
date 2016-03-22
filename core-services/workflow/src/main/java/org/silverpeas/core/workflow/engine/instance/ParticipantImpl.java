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

package org.silverpeas.core.workflow.engine.instance;

import org.silverpeas.core.workflow.api.instance.Participant;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;

public class ParticipantImpl extends ActorImpl implements Participant {
  final private String action;

  /**
   * Constructor Definitivly set the user, userRoleName, state and the action
   * @param user user aka the actor
   * @param userRoleName name of the role under which the user was/may be an actor
   * @param state state for which the user was/may be an actor
   * @param action name of the action in which has acted the participant
   */
  public ParticipantImpl(User user, String userRoleName, State state,
      String action) {
    super(user, userRoleName, state);
    this.action = action;
  }

  /**
   * Get the action in which has acted the participant
   * @return Action name
   */
  public String getAction() {
    return action;
  }
}