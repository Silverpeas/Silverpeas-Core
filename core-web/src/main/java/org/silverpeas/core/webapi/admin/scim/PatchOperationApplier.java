/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import edu.psu.swe.scim.spec.protocol.data.PatchOperation;
import edu.psu.swe.scim.spec.resources.Email;
import edu.psu.swe.scim.spec.resources.Name;
import edu.psu.swe.scim.spec.resources.ScimUser;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.Mutable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;
import static org.silverpeas.core.web.http.RequestParameterDecoder.*;
import static org.silverpeas.core.webapi.admin.scim.ScimLogger.logger;

/**
 * <p>
 * This tool is able to update an {@link ScimUser} from a {@link PatchOperation}.
 * </p>
 * <p>
 * The {@link ScimUser} instance must have been built from Silverpeas's data and must not come from
 * an external SCIM client.
 * </p>
 * @author silveryocha
 */
public class PatchOperationApplier {

  private static final String VALUE_ATTR_NAME = "value";
  private static final String PRIMARY_ATTR_NAME = "primary";
  private final ScimUser scimUser;

  PatchOperationApplier(final ScimUser scimUser) {
    this.scimUser = scimUser;
  }

  /**
   * Applies the patch operation on the current {@link ScimUser} (built with silverpeas's data).
   * @param operation the operation to perform.
   */
  void apply(PatchOperation operation) {
    final String attrName =
        operation.getPath().getValuePathExpression().getAttributePath().getFullAttributeName();
    if (attrName.equals("active")) {
      applyActiveData(operation);
    } else if (attrName.equals("externalId")) {
      applyExternalIdData(operation);
    } else if (attrName.contains("emails")) {
      applyMailData(operation);
    } else if (attrName.contains("name.")) {
      applyNameData(operation, attrName);
    } else if (attrName.equals("userName")) {
      applyLoginData(operation);
    } else {
      logger().debug(() -> {
        final String value = getValue(operation, VALUE_ATTR_NAME, String.class);
        return format(
            "attribute {0} with value {1} has been skipped as it is not handled by Silverpeas",
            attrName, value);
      });
    }
  }

  private void applyExternalIdData(final PatchOperation operation) {
    final PatchOperation.Type o = operation.getOperation();
    if (o == PatchOperation.Type.ADD || o == PatchOperation.Type.REPLACE) {
      scimUser.setExternalId(getValue(operation, VALUE_ATTR_NAME, String.class));
    } else if (o == PatchOperation.Type.REMOVE) {
      throw new SilverpeasRuntimeException("cannot remove external id?!!");
    }
  }

  private void applyLoginData(final PatchOperation operation) {
    final PatchOperation.Type o = operation.getOperation();
    if (o == PatchOperation.Type.ADD || o == PatchOperation.Type.REPLACE) {
      scimUser.setUserName(getValue(operation, VALUE_ATTR_NAME, String.class));
    } else if (o == PatchOperation.Type.REMOVE) {
      throw new SilverpeasRuntimeException("cannot remove login!");
    }
  }

  private void applyNameData(final PatchOperation operation, final String attrName) {
    final PatchOperation.Type o = operation.getOperation();
    Name name = scimUser.getName();
    if (name == null) {
      name = new Name();
      scimUser.setName(name);
    }
    if (o == PatchOperation.Type.ADD || o == PatchOperation.Type.REPLACE) {
      final String value = getValue(operation, VALUE_ATTR_NAME, String.class);
      if (attrName.endsWith(".givenName")) {
        name.setGivenName(value);
      } else if (attrName.endsWith(".familyName")) {
        name.setFamilyName(value);
      }
    } else if (o == PatchOperation.Type.REMOVE) {
      if (attrName.endsWith(".givenName")) {
        name.setGivenName(null);
      } else if (attrName.endsWith(".familyName")) {
        name.setFamilyName(null);
      }
    }
  }

  private void applyMailData(final PatchOperation operation) {
    final boolean isPrimary = getValue(operation, PRIMARY_ATTR_NAME, Boolean.class);
    if (isPrimary) {
      List<Email> emails = scimUser.getEmails();
      if (emails == null) {
        emails = new ArrayList<>();
        scimUser.setEmails(emails);
      }
      emails.clear();
      final PatchOperation.Type o = operation.getOperation();
      if (o == PatchOperation.Type.ADD || o == PatchOperation.Type.REPLACE) {
        final String value = getValue(operation, VALUE_ATTR_NAME, String.class);
        final Email email = new Email();
        email.setValue(value);
        email.setPrimary(true);
        emails.add(email);
      }
    }
  }

  private void applyActiveData(final PatchOperation operation) {
    final PatchOperation.Type o = operation.getOperation();
    if (o == PatchOperation.Type.ADD || o == PatchOperation.Type.REPLACE) {
      scimUser.setActive(getValue(operation, VALUE_ATTR_NAME, Boolean.class));
    } else if (o == PatchOperation.Type.REMOVE) {
      throw new SilverpeasRuntimeException("cannot remove active state...");
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T getValue(final PatchOperation operation, final String attributeName,
      final Class<T> clazz) {
    final Mutable<T> valueToPatch = Mutable.empty();
    final Object value = operation.getValue();
    if (value instanceof List) {
      ((List<Map<String, Object>>) value).forEach(i -> i.forEach((k, v) -> {
        if (attributeName.equals(k)) {
          if (clazz.isAssignableFrom(String.class)) {
            valueToPatch.set((T) v.toString());
          } else if (clazz.isAssignableFrom(Long.class)) {
            valueToPatch.set((T) asLong(v));
          } else if (clazz.isAssignableFrom(Integer.class)) {
            valueToPatch.set((T) asInteger(v));
          } else if (clazz.isAssignableFrom(Boolean.class)) {
            valueToPatch.set((T) Boolean.valueOf(asBoolean(v)));
          } else {
            throw new UnsupportedOperationException(
                format("The type {0} is not handled for attribute {1}...", clazz.getName(),
                    attributeName));
          }
        }
      }));
    }
    return valueToPatch.get();
  }
}
