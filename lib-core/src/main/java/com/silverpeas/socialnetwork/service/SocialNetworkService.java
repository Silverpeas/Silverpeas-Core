package com.silverpeas.socialnetwork.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.silverpeas.socialnetwork.connectors.SocialNetworkConnector;
import com.silverpeas.socialnetwork.dao.ExternalAccountDao;
import com.silverpeas.socialnetwork.model.AccountId;
import com.silverpeas.socialnetwork.model.ExternalAccount;
import com.silverpeas.socialnetwork.model.SocialNetworkID;

public class SocialNetworkService {
  static private String AUTHORIZATION_TOKEN_SESSION_ATTR = "socialnetwork_authorization_token_";
  static private String SOCIALNETWORK_ID_SESSION_ATTR = "socialnetwork_id";

	@Inject @Named("facebookConnector")
	private SocialNetworkConnector facebook = null;

	@Inject @Named("linkedInConnector")
	private SocialNetworkConnector linkedIn = null;

	@Inject
  private ExternalAccountDao dao = null;

	private static SocialNetworkService instance = null;

  public SocialNetworkService() {}

	static public SocialNetworkService getInstance() {
		if (instance == null) {
			instance = new SocialNetworkService();
		}
		return instance;
	}

	/**
	 * Get social network service implementation specific to given social network
	 *
	 * @param networkid		enum representing network id
	 *
	 * @return
	 */
	public SocialNetworkConnector getSocialNetworkConnector(SocialNetworkID networkId) {
		switch(networkId) {
		case FACEBOOK:
			return facebook;

		case LINKEDIN:
			return linkedIn;
		}

		return null;
	}

	/**
	 * Get social network service implementation specific to given social network
	 *
	 * @param networkIdAsString		network id as String
	 *
	 * @return
	 */
	public SocialNetworkConnector getSocialNetworkConnector(String networkIdAsString) {
		SocialNetworkID networkId = SocialNetworkID.valueOf(networkIdAsString);
		switch(networkId) {
		case FACEBOOK:
			return facebook;

		case LINKEDIN:
			return linkedIn;
		}

		return null;
	}

  public ExternalAccount getExternalAccount(SocialNetworkID networkId,
      String profileId) {
    return dao.findOne(new AccountId(networkId, profileId));
  }

  @Transactional(propagation=Propagation.REQUIRES_NEW)
  public void createExternalAccount(SocialNetworkID networkId, String userId, String profileId) {
    ExternalAccount account = new ExternalAccount();
    account.setNetworkId(networkId);
    account.setSilverpeasUserId(userId);
    account.setProfileId(profileId);

    dao.saveAndFlush(account);
  }

  public List<ExternalAccount> getUserExternalAccounts(String userId) {
    List<ExternalAccount> accounts = dao.findBySilverpeasUserId(userId);

    if (accounts == null) {
      return new ArrayList<ExternalAccount>();
    }

    return accounts;
  }

  public void storeAuthorizationToken(HttpSession session, SocialNetworkID networkId, AccessToken authorizationToken) {
    session.setAttribute(AUTHORIZATION_TOKEN_SESSION_ATTR+networkId, authorizationToken);
    session.setAttribute(SOCIALNETWORK_ID_SESSION_ATTR, networkId);
  }

  public AccessToken getStoredAuthorizationToken(HttpSession session, SocialNetworkID networkId) {
    AccessToken token = (AccessToken) session.getAttribute(AUTHORIZATION_TOKEN_SESSION_ATTR+networkId);
    return token;
  }

  public SocialNetworkID getSocialNetworkIDUsedForLogin(HttpSession session) {
    SocialNetworkID networkId = (SocialNetworkID) session.getAttribute(SOCIALNETWORK_ID_SESSION_ATTR);
    return networkId;
  }

  @Transactional
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
