/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.jcrutil.servlets;

import javax.jcr.Repository;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jackrabbit.server.CredentialsProvider;
import org.silverpeas.util.crypto.CryptMD5;

import com.silverpeas.jcrutil.security.impl.SilverpeasDigestCredentialsProvider;
import com.stratelia.webactiv.util.ResourceLocator;

public class SimpleDigestWebdavServlet extends org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet {


  private static final long serialVersionUID = -1609493516113921269L;
  private static final int DIGEST_KEY_SIZE = 16;
  private static final ResourceLocator resources = new ResourceLocator(
      "org.silverpeas.util.jcr", "");

  @Override
  public String getAuthenticateHeaderValue() {
    String nOnce = generateNOnce();
    return "Digest realm=\"" + resources.getString("jcr.authentication.realm") + "\", "
        + "qop=\"auth\", nonce=\"" + nOnce + "\", " + "opaque=\"" + CryptMD5.encrypt(nOnce) + "\"";
  }

  protected String generateNOnce() {
    String nOnceValue = RandomStringUtils.random(DIGEST_KEY_SIZE) + ":"
        + System.currentTimeMillis() + ":" + resources.getString("jcr.authentication.realm");
    return CryptMD5.encrypt(nOnceValue);
  }
  /**
   * the jcr repository
   */
  private Repository repository;

  /**
   * {@inheritDoc}
   */
  @Override
  public Repository getRepository() {
    if (repository == null) {
      repository = RepositoryAccessServlet.getRepository(getServletContext());
    }
    return repository;
  }

  /**
   * {@inheritDoc}
   */
  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected CredentialsProvider getCredentialsProvider() {
    return new SilverpeasDigestCredentialsProvider();
  }
}
