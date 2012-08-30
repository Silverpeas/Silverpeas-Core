/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.admin.space.quota;

import org.silverpeas.quota.QuotaKey;
import org.silverpeas.quota.contant.QuotaType;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.OrganizationControllerFactory;
import com.stratelia.webactiv.beans.admin.PersonalSpaceController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * @author Yohann Chastagnier
 */
public class DataStorageSpaceQuotaKey implements QuotaKey {

  private final SpaceInst space;
  private final UserDetail user;
  private final boolean traverseSpacePath;

  public static DataStorageSpaceQuotaKey from(final UserDetail user) {
    return new DataStorageSpaceQuotaKey(
        new PersonalSpaceController().getPersonalSpace(user.getId()), user, true);
  }

  public static DataStorageSpaceQuotaKey from(final SpaceInst space) {
    return from(space, false);
  }

  public static DataStorageSpaceQuotaKey from(final SpaceInst space, final boolean traverseSpacePath) {
    return new DataStorageSpaceQuotaKey(space, (space.isPersonalSpace()) ? space.getCreator()
        : null, traverseSpacePath);
  }

  public static DataStorageSpaceQuotaKey from(final String componentInstanceId) {
    final SpaceInst space =
        OrganizationControllerFactory
            .getFactory()
            .getOrganizationController()
            .getSpaceInstById(
                OrganizationControllerFactory.getFactory().getOrganizationController()
                    .getComponentInst(componentInstanceId).getDomainFatherId());
    return new DataStorageSpaceQuotaKey(space, (space.isPersonalSpace()) ? space.getCreator()
        : null, true);
  }

  /**
   * Default constructor
   * @param space
   * @param user
   * @param traverseSpacePath TODO
   */
  private DataStorageSpaceQuotaKey(final SpaceInst space, final UserDetail user,
      final boolean traverseSpacePath) {
    this.space = space;
    this.user = user;
    this.traverseSpacePath = traverseSpacePath;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.QuotaKey#isValid()
   */
  @Override
  public boolean isValid() {
    return (user != null && StringUtil.isDefined(user.getId())) ||
        (space != null && StringUtil.isDefined(space.getId()));
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.QuotaKey#getQuotaType()
   */
  @Override
  public QuotaType getQuotaType() {
    return (user != null) ? QuotaType.DATA_STORAGE_IN_USER_SPACE : QuotaType.DATA_STORAGE_IN_SPACE;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.QuotaKey#getResourceId()
   */
  @Override
  public String getResourceId() {
    return (user != null) ? user.getId() : space.getId().replaceFirst(Admin.SPACE_KEY_PREFIX, "");
  }

  /**
   * @return the space
   */
  public SpaceInst getSpace() {
    return space;
  }

  /**
   * @return the traverseSpacePath
   */
  protected boolean isTraverseSpacePath() {
    return traverseSpacePath;
  }
}
