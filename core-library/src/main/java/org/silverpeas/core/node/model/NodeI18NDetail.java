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

package org.silverpeas.core.node.model;

import org.silverpeas.core.i18n.Translation;

public class NodeI18NDetail extends Translation implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  public NodeI18NDetail(String lang, String nodeName, String nodeDescription) {
    super(lang, nodeName, nodeDescription);
  }

  public NodeI18NDetail(int id, String lang, String nodeName,
      String nodeDescription) {
    super(id, lang, nodeName, nodeDescription);
  }

  public int getNodeId() {
    return new Integer(super.getObjectId());
  }

  public void setNodeId(String id) {
    super.setObjectId(id);
  }

  /**
   * Return the object table name
   * @return the table name of the object
   * @since 1.0
   */
  public String getTableName() {
    return "SB_Node_NodeI18N";
  }

}