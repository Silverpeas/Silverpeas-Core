/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.cmis;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.silverpeas.core.webapi.base.SilverpeasRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigInteger;

/**
 * The context of a request targeting a CMIS service in Silverpeas.
 * @author mmoquillon
 */
public class CmisRequestContext extends SilverpeasRequestContext implements CallContext {

  private final CallContext callContext;

  public CmisRequestContext(final CallContext callContext) {
    this.callContext = callContext;
    final HttpServletRequest request =
        (HttpServletRequest) callContext.get(CallContext.HTTP_SERVLET_REQUEST);
    final HttpServletResponse response =
        (HttpServletResponse) callContext.get(CallContext.HTTP_SERVLET_RESPONSE);
    init(request, response);
  }

  @Override
  public String getBinding() {
    return callContext.getBinding();
  }

  @Override
  public boolean isObjectInfoRequired() {
    return callContext.isObjectInfoRequired();
  }

  @Override
  public Object get(final String key) {
    return callContext.get(key);
  }

  @Override
  public CmisVersion getCmisVersion() {
    return callContext.getCmisVersion();
  }

  @Override
  public String getRepositoryId() {
    return callContext.getRepositoryId();
  }

  @Override
  public String getUsername() {
    return callContext.getUsername();
  }

  @Override
  public String getPassword() {
    return callContext.getPassword();
  }

  @Override
  public String getLocale() {
    return callContext.getLocale();
  }

  @Override
  public BigInteger getOffset() {
    return callContext.getOffset();
  }

  @Override
  public BigInteger getLength() {
    return callContext.getLength();
  }

  @Override
  public File getTempDirectory() {
    return callContext.getTempDirectory();
  }

  @Override
  public boolean encryptTempFiles() {
    return callContext.encryptTempFiles();
  }

  @Override
  public int getMemoryThreshold() {
    return callContext.getMemoryThreshold();
  }

  @Override
  public long getMaxContentSize() {
    return callContext.getMaxContentSize();
  }
}
  