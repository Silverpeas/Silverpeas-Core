/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.persistence.datasource.repository.jpa;

import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.persistence.datasource.repository.Parameters;

import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class permits to handle as a friendly way the named parameters of a query.
 * <p/>
 * Use {@link #add} method to add a named parameter (its name, its value and
 * optionaly a temporal type).
 * <p/>
 * Use {@link #applyTo} to apply the named parameter to the query. This method returns the query
 * passed, so that query methods can be called directly in one line of code.
 * <p/>
 * @author Yohann Chastagnier
 */
public class NamedParameters implements Parameters {

  private String lastParameterName;
  final Map<String, NamedParameter<?, ?>> namedParameters;

  /**
   * Default constructor.
   */
  NamedParameters() {
    namedParameters = new LinkedHashMap<String, NamedParameter<?, ?>>();
  }

  /**
   * Gets the last parameter.
   * @return
   */
  public String getLastParameterName() {
    return lastParameterName;
  }

  /**
   * Adding a new named parameter.
   * @param name
   * @param value
   * @return
   */
  @Override
  public NamedParameters add(final String name, final Object value) {
    return add(name, value, null);
  }

  /**
   * Adding a new named parameter.
   * @param name
   * @param value
   * @param temporalType
   * @return
   */
  @SuppressWarnings("unchecked")
  public NamedParameters add(final String name, final Object value,
      final TemporalType temporalType) {
    if (value instanceof Object[] && ((Object[]) value)[0] instanceof Date && temporalType != null) {
      namedParameters.put(name,
          new DateCollectionNamedParameter(name, CollectionUtil.asSet((Date[]) value),
              temporalType));
    } else if (value instanceof Collection && ((Collection) value).iterator().next() instanceof Date
        && temporalType != null) {
      namedParameters.put(name,
          new DateCollectionNamedParameter(name, new HashSet<Date>((Collection) value),
              temporalType)
      );
    } else if (value instanceof Date && temporalType != null) {
      namedParameters.put(name, new DateNamedParameter(name, (Date) value, temporalType));
    } else if (value instanceof Object[] && ((Object[]) value)[0] instanceof Enum) {
      namedParameters
          .put(name, new EnumCollectionNamedParameter(name, CollectionUtil.asSet((Enum[]) value)));
    } else if (value instanceof Collection && ((Collection) value).iterator().next() instanceof Enum) {
      namedParameters
          .put(name, new EnumCollectionNamedParameter(name, new HashSet<Enum>((Collection) value)));
    } else if (value instanceof Enum) {
      namedParameters.put(name, new EnumNamedParameter(name, (Enum) value));
    } else {
      namedParameters.put(name, new ObjectNamedParameter(name, value));
    }
    lastParameterName = name;
    return this;
  }

  /**
   * Applies the named parameters to the given query.
   * @param <E> the type of the JPQL query.
   * @param query the query on which the parameters have to be applied.
   * @return the JPQL query enriched with the parameters.
   */
  public <E extends Query> E applyTo(final E query) {
    for (final NamedParameter<?, ?> namedParameter : namedParameters.values()) {
      if (namedParameter instanceof DateNamedParameter) {
        final DateNamedParameter dateParameter = (DateNamedParameter) namedParameter;
        query.setParameter(dateParameter.getName(), dateParameter.getValue(),
            dateParameter.getTemporalType());
      } else {
        query.setParameter(namedParameter.getName(), namedParameter.getValue());
      }
    }
    return query;
  }
}
