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

package org.silverpeas.core.socialnetwork.service;

import org.silverpeas.core.socialnetwork.connectors.SocialNetworkConnector;
import org.silverpeas.core.socialnetwork.dao.ExternalAccountManager;
import org.silverpeas.core.socialnetwork.model.ExternalAccount;
import org.silverpeas.core.socialnetwork.model.ExternalAccountIdentifier;
import org.silverpeas.core.socialnetwork.model.SocialNetworkID;
import org.silverpeas.core.socialnetwork.qualifiers.Facebook;
import org.silverpeas.core.socialnetwork.qualifiers.LinkedIn;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SocialNetworkService {
  static private String AUTHORIZATION_TOKEN_SESSION_ATTR = "socialnetwork_authorization_token_";
  static private String SOCIALNETWORK_ID_SESSION_ATTR = "socialnetwork_id";

  @Inject
  @Facebook
  private SocialNetworkConnector facebook;

  @Inject
  @LinkedIn
  private SocialNetworkConnector linkedIn;

  @Inject
  private ExternalAccountManager dao;

  protected SocialNetworkService() {
  }

  public static SocialNetworkService getInstance() {
    return ServiceProvider.getService(SocialNetworkService.class);
  }

  /**
   * Get social network service implementation specific to given social network
   * @param networkId enum representing network id
   * @return
   */
  public SocialNetworkConnector getSocialNetworkConnector(SocialNetworkID networkId) {
    switch (networkId) {
      case FACEBOOK:
        return facebook;

      case LINKEDIN:
        return linkedIn;
    }

    return null;
  }

  /**
   * Get social network service implementation specific to given social network
   * @param networkIdAsString network id as String
   * @return
   */
  public SocialNetworkConnector getSocialNetworkConnector(String networkIdAsString) {
    SocialNetworkID networkId = SocialNetworkID.valueOf(networkIdAsString);
    switch (networkId) {
      case FACEBOOK:
        return facebook;

      case LINKEDIN:
        return linkedIn;
    }

    return null;
  }

  public ExternalAccount getExternalAccount(SocialNetworkID networkId, String profileId) {
    ExternalAccount externalAccount =
        dao.getById(new ExternalAccountIdentifier(networkId, profileId).asString());
    if (externalAccount != null) {
      UserDetail user = UserDetail.getById(externalAccount.getSilverpeasUserId());
      if (user == null || user.isDeletedState()) {
        removeExternalAccount(externalAccount.getSilverpeasUserId(), networkId);
        externalAccount = null;
      }
    }
    return externalAccount;
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public void createExternalAccount(SocialNetworkID networkId, String userId, String profileId) {
    ExternalAccount account = new ExternalAccount();
    account.setId(networkId.name() + ExternalAccountIdentifier.COMPOSITE_SEPARATOR + profileId);
    account.setSilverpeasUserId(userId);

    dao.saveAndFlush(account);
  }

  public List<ExternalAccount> getUserExternalAccounts(String userId) {
    List<ExternalAccount> accounts = dao.findBySilverpeasUserId(userId);

    if (accounts == null) {
      return new ArrayList<>();
    }

    return accounts;
  }

  public void removeAuthorizationToken(HttpSession session) {
    if (session != null) {
      SocialNetworkID networkId =
          (SocialNetworkID) session.getAttribute(SOCIALNETWORK_ID_SESSION_ATTR);
      if (networkId != null) {
        session.setAttribute(SOCIALNETWORK_ID_SESSION_ATTR, null);
        session.setAttribute(AUTHORIZATION_TOKEN_SESSION_ATTR + networkId, null);
      }
    }
  }

  public void storeAuthorizationToken(HttpSession session, SocialNetworkID networkId,
      AccessToken authorizationToken) {
    session.setAttribute(AUTHORIZATION_TOKEN_SESSION_ATTR + networkId, authorizationToken);
    session.setAttribute(SOCIALNETWORK_ID_SESSION_ATTR, networkId);
  }

  public AccessToken getStoredAuthorizationToken(HttpSession session, SocialNetworkID networkId) {
    AccessToken token =
        (AccessToken) session.getAttribute(AUTHORIZATION_TOKEN_SESSION_ATTR + networkId);
    return token;
  }

  public SocialNetworkID getSocialNetworkIDUsedForLogin(HttpSession session) {
    SocialNetworkID networkId =
        (SocialNetworkID) session.getAttribute(SOCIALNETWORK_ID_SESSION_ATTR);
    return networkId;
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public void removeExternalAccount(String userId, SocialNetworkID networkId) {
    List<ExternalAccount> accounts = dao.findBySilverpeasUserId(userId);

    if (accounts != null) {
      for (ExternalAccount account : accounts) {
        if (account.getNetworkId() == networkId) {
          dao.delete(account);
          break;
        }
      }
    }
  }
}
