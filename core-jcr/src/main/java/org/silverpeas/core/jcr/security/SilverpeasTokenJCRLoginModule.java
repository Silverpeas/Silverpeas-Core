/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.security;

import org.apache.jackrabbit.api.security.authentication.token.TokenCredentials;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.StringUtil;

import javax.annotation.Nonnull;
import javax.jcr.Credentials;
import javax.security.auth.login.LoginException;
import java.util.Set;

/**
 * A login module to authenticate the users who access the JCR repository used by Silverpeas. This
 * login module accepts only {@link TokenCredentials} carrying the API token of the user.
 * <p>
 * The login module verifies the token by asking to Silverpeas the user having such a token. If no
 * such a user exists, then the authentication is considered as a failure.
 * </p>
 * @author mmoquillon
 */
public class SilverpeasTokenJCRLoginModule extends SilverpeasJCRLoginModule {

  @SuppressWarnings("rawtypes")
  static final Set<Class> SUPPORTED_CREDENTIALS = Set.of(TokenCredentials.class);

  @SuppressWarnings("rawtypes")
  @Override
  @Nonnull
  protected Set<Class> getSupportedCredentials() {
    return SUPPORTED_CREDENTIALS;
  }

  @Override
  protected User authenticateUser(final Credentials credentials)
      throws LoginException {
    String token = ((TokenCredentials) credentials).getToken();
    User user = User.provider().getUserByToken(token);
    if (user == null) {
      throw new LoginException("User API Token '" + token + "' non valid!");
    }
    return user;
  }

  @Override
  @Nonnull
  protected AccessContext getAccessContext(final Credentials credentials) {
    final String grantedDocPath = ((TokenCredentials) credentials).getAttribute(
        WebDavAccessContext.AUTHORIZED_DOCUMENT_PATH_ATTRIBUTE);
    return StringUtil.isDefined(grantedDocPath) ?
        new WebDavAccessContext(grantedDocPath) :
        AccessContext.EMPTY;
  }
}
