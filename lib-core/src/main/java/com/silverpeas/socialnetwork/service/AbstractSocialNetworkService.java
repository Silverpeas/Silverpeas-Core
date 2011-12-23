package com.silverpeas.socialnetwork.service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.social.connect.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.silverpeas.socialnetwork.dao.ExternalAccountDao;
import com.silverpeas.socialnetwork.model.AccountId;
import com.silverpeas.socialnetwork.model.ExternalAccount;
import com.silverpeas.socialnetwork.model.SocialNetworkID;
import com.silverpeas.util.StringUtil;

@Service
public abstract class AbstractSocialNetworkService implements SocialNetworkService{

	static {
		// Fix - empty proxy port and proxy host not supported by Spring Social
		if ( !StringUtil.isDefined(System.getProperty("http.proxyPort")) ) {
			System.getProperties().remove("http.proxyPort");
		}
		if ( !StringUtil.isDefined(System.getProperty("http.proxyHost")) ) {
			System.getProperties().remove("http.proxyHost");
		}
	}

	@Inject
	private ExternalAccountDao dao = null;

	public ExternalAccountDao getDao() {
		return dao;
	}

	public void setDao(ExternalAccountDao dao) {
		this.dao = dao;
	}

	@Override
	abstract public String buildAuthenticateUrl(String callBackURL);

	@Override
	abstract public AccessToken exchangeForAccessToken(HttpServletRequest request, String callBackURL) throws SocialNetworkAuthorizationException;

	@Override
	abstract public UserProfile getUserProfile(AccessToken authorizationToken);

	@Override
	abstract public String getUserProfileId(AccessToken authorizationToken);

	@Override
	public ExternalAccount getExternalAccount(SocialNetworkID networkId,
			String profileId) {
		return dao.readByPrimaryKey(new AccountId(networkId, profileId));
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void createExternalAccount(SocialNetworkID networkId, String userId, String profileId) {
		ExternalAccount account = new ExternalAccount();
		account.setNetworkId(networkId);
		account.setSilverpeasUserId(userId);
		account.setProfileId(profileId);

		dao.save(account);
	}

}
