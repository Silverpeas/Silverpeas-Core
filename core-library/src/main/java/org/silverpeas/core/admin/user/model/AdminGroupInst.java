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

/**
 * @author Norbert CHAIX
 * @version 1.0
 * 13/10/2000
 */

package org.silverpeas.core.admin.user.model;

import java.io.Serializable;
import java.util.ArrayList;

public class AdminGroupInst implements Serializable {

  private static final long serialVersionUID = -4321791324660707809L;
  private Group m_Group; // Admin group detail of this node
  private ArrayList<AdminGroupInst> m_alChildrenAdminGroupInst; // Children Admin group inst of

  // this node

  /** Creates a new Space */
  public AdminGroupInst() {
    m_Group = null;
    m_alChildrenAdminGroupInst = new ArrayList<AdminGroupInst>();
  }

  public Group getGroup() {
    return m_Group;
  }

  public void setGroup(Group group) {
    m_Group = group;
  }

  public void setChildrenAdminGroupInst(ArrayList<AdminGroupInst> alChildrenAdminGroupInst) {
    m_alChildrenAdminGroupInst = alChildrenAdminGroupInst;
  }

  public ArrayList<AdminGroupInst> getAllChildrenAdminGroupInst() {
    return m_alChildrenAdminGroupInst;
  }
}
