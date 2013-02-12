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
package org.silverpeas.admin.space.quota;

import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.quota.contant.QuotaType;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.PersonalSpaceController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * @author Yohann Chastagnier
 */
public class DataStorageSpaceQuotaKey extends AbstractSpaceQuotaKey {

  private final UserDetail user;

  public static DataStorageSpaceQuotaKey from(final UserDetail user) {
    return new DataStorageSpaceQuotaKey(
        new PersonalSpaceController().getPersonalSpace(user.getId()), user);
  }

  public static DataStorageSpaceQuotaKey from(final SpaceInst space) {
    return new DataStorageSpaceQuotaKey(space, (space.isPersonalSpace()) ? space.getCreator()
        : null);
  }

  public static DataStorageSpaceQuotaKey from(final String componentInstanceId) {
    final SpaceInst space =
        OrganisationControllerFactory
            .getFactory()
            .getOrganizationController()
            .getSpaceInstById(
                OrganisationControllerFactory.getFactory().getOrganizationController()
                    .getComponentInst(componentInstanceId).getDomainFatherId());
    return from(space);
  }

  /**
   * Default constructor
   * @param space
   * @param user
   */
  private DataStorageSpaceQuotaKey(final SpaceInst space, final UserDetail user) {
    super(space);
    this.user = user;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.QuotaKey#isValid()
   */
  @Override
  public boolean isValid() {
    return (user != null && StringUtil.isDefined(user.getId())) || super.isValid();
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
    return (user != null) ? user.getId() : super.getResourceId();
  }

  /**
   * @return the user
   */
  public UserDetail getUser() {
    return user;
  }
}
