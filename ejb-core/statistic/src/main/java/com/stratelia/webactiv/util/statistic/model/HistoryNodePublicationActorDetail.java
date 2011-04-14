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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.statistic.model;

import java.io.Serializable;
import java.util.Date;

import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * Class declaration
 * @author
 */
public class HistoryNodePublicationActorDetail implements Serializable {

  private static final long serialVersionUID = 1L;

  private Date date;
  private String userId;
  private NodePK node;
  private PublicationPK pub;

  /**
   * Constructor declaration
   * @param date
   * @param userId
   * @param node
   * @param pub
   * @see
   */
  public HistoryNodePublicationActorDetail(Date date, String userId,
      NodePK node, PublicationPK pub) {
    this.date = date;
    this.userId = userId;
    this.node = node;
    this.pub = pub;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Date getDate() {
    return date;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public PublicationPK getPublicationPK() {
    return pub;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public NodePK getNodePK() {
    return node;
  }

}
