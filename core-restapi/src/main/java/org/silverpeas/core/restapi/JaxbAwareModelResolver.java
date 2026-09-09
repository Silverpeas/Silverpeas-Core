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
package org.silverpeas.core.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import io.swagger.v3.core.jackson.ModelResolver;

/**
 * Makes the schema resolver of Swagger aware of the JAXB annotations of the web entities.
 * <p>
 * By default that resolver figures the properties of an entity out with a plain Jackson
 * {@link ObjectMapper}, whereas Silverpeas serializes them with the introspector of the JAXB
 * annotations. Without this resolver the generated schemas describe properties that never reach
 * the wire, those excluded by {@code @XmlTransient} or by an access set to
 * {@code XmlAccessType.FIELD}, and miss the fields that do reach it. The
 * {@code JAXBAnnotationsHelper} of Swagger doesn't help here: it reads {@code @XmlElement},
 * {@code @XmlElementWrapper} and {@code @XmlAttribute} only, and only to feed the XML metadata of
 * a schema, never its visibility.
 * </p>
 * <p>
 * The introspector is set on a mapper of its own, the very way it is set by the JSON codec of
 * Silverpeas, so that it applies to the resolution of the schemas only and not to the whole of
 * the generation.
 * </p>
 * <p>
 * The resolver takes part in nothing but the generation of the documentation; it plays no role at
 * runtime. It is declared by the modelConverterClasses parameter of the Swagger Maven plugin.
 * </p>
 * @author Miguel Moquillon
 */
public class JaxbAwareModelResolver extends ModelResolver {

  public JaxbAwareModelResolver() {
    super(jaxbAwareMapper());
  }

  private static ObjectMapper jaxbAwareMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setAnnotationIntrospector(
        new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance()));
    return mapper;
  }
}
