/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.jcrutil.security.impl;

import javax.jcr.Credentials;

/**
 * Credentials used for HTTP Digest authentication
 */
public class DigestCredentials implements Credentials {

  private String username;
  private String clientDigest;
  private String nonce;
  private String nc;
  private String cnonce;
  private String qop;
  private String realm;
  private String md5a2;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getClientDigest() {
    return clientDigest;
  }

  public void setClientDigest(String clientDigest) {
    this.clientDigest = clientDigest;
  }

  public String getNonce() {
    return nonce;
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  public String getNc() {
    return nc;
  }

  public void setNc(String nc) {
    this.nc = nc;
  }

  public String getCnonce() {
    return cnonce;
  }

  public void setCnonce(String cnonce) {
    this.cnonce = cnonce;
  }

  public String getQop() {
    return qop;
  }

  public void setQop(String qop) {
    this.qop = qop;
  }

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public String getMd5a2() {
    return md5a2;
  }

  public void setMd5a2(String md5a2) {
    this.md5a2 = md5a2;
  }
}
