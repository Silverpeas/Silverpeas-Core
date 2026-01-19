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

package org.silverpeas.core.webapi.admin.scim;

import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.server.exception.*;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.silverpeas.core.annotation.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.webapi.admin.scim.ScimLogger.logger;

/**
 * <p>
 * The service in charge of handling the Silverpeas's groups against those sent by SCIM client.<br/>
 * It is finally kind of CRUD service.
 * </p>
 * <p>
 * All provider methods are called by WEB services which are decoding the HTTP requests in front.
 * </p>
 * <p>
 * But for now, groups coming from SCIM clients are not handled...
 * </p>
 * @author silveryocha
 */
@Service
public class ScimGroupAdminService extends AbstractScimAdminService implements Repository<ScimGroup> {

  @Override
  public Class<ScimGroup> getResourceClass() {
    return ScimGroup.class;
  }

  @Override
  public ScimGroup create(final ScimGroup resource) throws UnableToCreateResourceException {
    logger().warn("SCIM create group not handled");
    validateDomainExists();
    return null;
  }

  @Override
  public ScimGroup update(String id, String version, ScimGroup resource,
      Set<AttributeReference> includedAttributeReferences,
      Set<AttributeReference> excludedAttributeReferences)
      throws UnableToUpdateResourceException {
    logger().warn("SCIM update group not handled");
    validateDomainExists();
    return null;
  }

  @Override
  public ScimGroup patch(String id, String version, List<PatchOperation> patchOperations,
      Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes)
      throws UnableToUpdateResourceException {
    logger().warn("SCIM patch group not handled");
    validateDomainExists();
    return null;
  }

  @Override
  public ScimGroup get(final String id) throws UnableToRetrieveResourceException {
    logger().warn("SCIM get group not handled");
    validateDomainExists();
    return null;
  }

  @Override
  public FilterResponse<ScimGroup> find(final Filter filter, final PageRequest pageRequest,
      final SortRequest sortRequest) throws UnableToRetrieveResourceException {
    logger().warn("SCIM find group not handled");
    validateDomainExists();
    return null;
  }

  @Override
  public void delete(final String id) throws UnableToDeleteResourceException {
    logger().warn("SCIM delete group not handled");
    validateDomainExists();
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    logger().debug(() -> "getting group SCIM extensions");
    return Collections.emptyList();
  }
}
