/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.util.persistence;

import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

/**
 * @author Yohann Chastagnier
 */
public class TypedParameterUtil {

  public static String addNamedParameter(final List<TypedParameter<?>> parameters, final String name, final Object value) {
    return addNamedParameter(parameters, name, value, null);
  }

  public static String addNamedParameter(final List<TypedParameter<?>> parameters, final String name,
      final Object value, final TemporalType temporalType) {
    if (value instanceof Date && temporalType != null) {
      parameters.add(new DateParameter(name, (Date) value, temporalType));
    } else {
      parameters.add(new ObjectParameter(name, value));
    }
    return name;
  }

  public static void computeNamedParameters(final TypedQuery<?> typedQuery, final List<TypedParameter<?>> parameters) {
    for (final TypedParameter<?> typeParameter : parameters) {
      if (typeParameter instanceof DateParameter) {
        final DateParameter dateParameter = (DateParameter) typeParameter;
        typedQuery.setParameter(dateParameter.getName(), dateParameter.getValue(), dateParameter.getTemporalType());
      } else {
        typedQuery.setParameter(typeParameter.getName(), typeParameter.getValue());
      }
    }
  }
}
