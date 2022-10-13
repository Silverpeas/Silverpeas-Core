/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

import java.io.Serializable;
import java.util.Date;

/**
 * Location of a publication. The location of a publication is the node in a tree of nodes in which
 * is located the publication. This is for Silverpeas components
 * that use nodes to organize the publications they manage into a tree view.
 * <p>
 * The location concept here comes from the filesystem: a publication can be located only at a given
 * location and any others locations from which the publication can be accessed is said to be
 * virtual, that is to say to be an alias, a shortcut to the original location. This is useful to
 * restrict the node concept to the folder or directory one and to reified this constrain.
 * </p>
 */
public class Location extends NodePK {

  private static final long serialVersionUID = 6640210341762444143L;

  private Alias alias = null;
  private int pubOrder = 0;

  /**
   * Constructs a new location for a publication.
   * @param nodeId the unique identifier of a node.
   * @param instanceId the unique identifier of the component instance that uses the node to
   * organize the publications.
   */
  public Location(String nodeId, String instanceId) {
    super(nodeId, instanceId);
  }

  public Alias getAlias() {
    return alias;
  }

  public void setAsAlias(final String userId, final Date date) {
    if (alias == null) {
      alias = new Alias();
    }
    alias.setUserId(userId).setDate(date == null ? new Date() : date);
  }

  public void setAsAlias(final String userId) {
    setAsAlias(userId, new Date());
  }

  /**
   * Is this location matches the specified node in a given component instance?
   * @param nodeDetail a node in a component instance.
   * @return true if this location matches exactly the specified node. False otherwise.
   */
  public boolean isNode(final NodeDetail nodeDetail) {
    return nodeDetail.getNodePK().equals(this);
  }

  /**
   * Is this location actually an alias to the publication.
   * @return true if this location is in fact a shortcut to the publication in its true location.
   * If false, then this location represents the true position of the publication. Then only move
   * can be used to relocate the publication.
   */
  public boolean isAlias() {
    return getAlias() != null;
  }

  /**
   * Gets the order of this publication among other publications in the same location.
   * @return the order of the publication in this location.
   */
  public int getPubOrder() {
    return pubOrder;
  }

  /**
   * Sets the order of the publication among others ones in this location.
   * @param pubOrder a new order of the publication in this location.
   */
  public void setPubOrder(int pubOrder) {
    this.pubOrder = pubOrder;
  }

  @Override
  public boolean equals(Object other) {
    return super.equals(other);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public class Alias implements Serializable {
    private String userId;
    private transient String userName;
    private Date date;

    private Alias() {
    }

    public String getUserId() {
      return userId;
    }

    public Alias setUserId(final String userId) {
      this.userId = userId;
      this.userName = User.getById(getUserId()).getDisplayedName();
      return this;
    }

    public String getUserName() {
      return userName;
    }

    public Date getDate() {
      return date;
    }

    public Alias setDate(final Date date) {
      this.date = date;
      return this;
    }
  }
}
