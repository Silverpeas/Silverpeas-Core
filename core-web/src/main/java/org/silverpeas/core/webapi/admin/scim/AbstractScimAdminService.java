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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.admin.scim;

import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.spec.protocol.filter.AttributeComparisonExpression;
import edu.psu.swe.scim.spec.protocol.filter.FilterExpression;
import edu.psu.swe.scim.spec.protocol.filter.LogicalExpression;
import edu.psu.swe.scim.spec.protocol.filter.LogicalOperator;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.SearchCriteria;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import java.text.MessageFormat;

import static javax.ws.rs.core.Response.Status.*;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * Base of all SCIM services which handles the resources between a SCIM client and Silverpeas
 * SCIM server.
 * @author silveryocha
 */
abstract class AbstractScimAdminService {

  @Inject
  protected ScimRequestContext scimRequestContext;

  @Inject
  protected Administration admin;

  protected void validateDomainExists() {
    if (isNotDefined(scimRequestContext.getDomainId())) {
      throw new WebApplicationException(FORBIDDEN);
    }
  }

  /**
   * Centralizing the {@link SearchCriteria} instance creation from {@link FilterExpression}
   * given by SCIM client.
   * @param expression the filters given by the SCIM client.
   * @param searchCriteria the silverpeas {@link SearchCriteria} instance.
   * @param <T> the type of {@link SearchCriteria}
   * @return the completed {@link SearchCriteria} instance.
   * @throws UnableToRetrieveResourceException
   */
  protected <T extends SearchCriteria> T processExpression(FilterExpression expression,
      T searchCriteria) throws UnableToRetrieveResourceException {
    if (expression instanceof LogicalExpression) {
      final LogicalExpression le = (LogicalExpression) expression;
      processExpression(le.getLeft(), searchCriteria);
      processExpression(le.getRight(), searchCriteria);

      if (le.getOperator().equals(LogicalOperator.OR)) {
        throw new UnableToRetrieveResourceException(NOT_IMPLEMENTED,
            "impossible to perform OR logical operation");
      }
    } else if (expression instanceof AttributeComparisonExpression) {
      final AttributeComparisonExpression ace = (AttributeComparisonExpression) expression;
      final String attributeBase = ace.getAttributePath().getFullAttributeName();

      if (ace.getOperation() == edu.psu.swe.scim.spec.protocol.filter.CompareOperator.EQ) {
        if ("id".equals(attributeBase)) {
          searchCriteria.onUserIds((String) ace.getCompareValue());
          return searchCriteria;
        } else if ("externalId".equals(attributeBase)) {
          searchCriteria.onUserSpecificIds((String) ace.getCompareValue());
          return searchCriteria;
        }
      }
      throw new UnableToRetrieveResourceException(BAD_REQUEST, MessageFormat
          .format("Unable to apply operator {0} on attribute {1}", ace.getOperation(),
              ace.getAttributePath().getFullAttributeName()));
    }
    return searchCriteria;
  }
}
