/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.web.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import org.silverpeas.core.util.JSONCodec;

/**
 * Provides to the JAX-RS runtime the Jackson mapper with which the web entities of the REST API of
 * Silverpeas are read from and written into JSON.
 * <p>
 * Without this resolver, the JAX-RS runtime falls back to a plain Jackson mapper of its own that
 * rejects any JSON property unknown to the web entity. Yet, the web entities expose read-only
 * properties (an URI, a computed state, ...) that the clients get with the entity and send back
 * as such when they modify it: the request is then rejected with a 400 HTTP status. This resolver
 * provides instead the mapper of {@link JSONCodec#newObjectMapper()}, which ignores such unknown
 * properties, takes into account the Jakarta XML Binding annotations of the entities as well as
 * their Jackson annotations, and omits their null properties.
 * </p>
 * @author mmoquillon
 */
@Provider
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {

  private final ObjectMapper mapper = JSONCodec.newObjectMapper();

  @Override
  public ObjectMapper getContext(final Class<?> type) {
    return mapper;
  }
}
