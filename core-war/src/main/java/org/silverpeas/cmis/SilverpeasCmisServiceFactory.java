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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.cmis;

import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.CmisServiceWrapperManager;
import org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper;
import org.silverpeas.cmis.security.CmisUserAuthenticator;
import org.silverpeas.core.util.ServiceProvider;

import java.math.BigInteger;
import java.util.Map;

/**
 * Factory to produce and return a CMIS service instance for Silverpeas. It checks the user
 * requesting a CMIS service is authenticated.
 * <p>
 * This factory is used by the OpenCMIS framework to obtain an implementation of {@link CmisService}
 * interface that is specific to Silverpeas.
 * </p>
 * @author mmoquillon
 */
public class SilverpeasCmisServiceFactory extends AbstractServiceFactory {

  /**
   * Default maxItems value for getTypeChildren()
   */
  private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50);

  /**
   * Default depth value for getTypeDescendants()
   */
  private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);

  /**
   * Default maxItems value for getChildren() and other methods returning lists of objects.
   */
  private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200);

  /**
   * Default depth value for getDescendants()
   */
  private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(10);

  private CmisServiceWrapperManager wrapperManager;

  @Override
  public void init(final Map<String, String> parameters) {
    super.init(parameters);
    // we set up a wrapper manager that builds a chain of responsibility within which we define
    // additional CMIS request processors to be executed first before any treatments from
    // a SilverpeasCmisService instance.
    wrapperManager = new CmisServiceWrapperManager();
    wrapperManager.addWrappersFromServiceFactoryParameters(parameters);
    wrapperManager.addOuterWrapper(ConformanceCmisServiceWrapper.class, DEFAULT_MAX_ITEMS_TYPES,
        DEFAULT_DEPTH_TYPES, DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);
    wrapperManager.addInnerWrapper(CmisUserAuthenticator.class);
  }

  @Override
  public CmisService getService(final CallContext callContext) {
    final SilverpeasCmisService service = ServiceProvider.getService(SilverpeasCmisService.class);
    final CmisRequestContext context = new CmisRequestContext(callContext);
    final CallContextAwareCmisService wrapper =
        (CallContextAwareCmisService) wrapperManager.wrap(service);
    wrapper.setCallContext(context);
    return wrapper;
  }

}
  