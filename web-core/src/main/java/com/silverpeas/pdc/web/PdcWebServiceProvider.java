/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.pdc.web;

import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONUnmarshaller;
import com.sun.jersey.json.impl.JSONUnmarshallerImpl;
import java.io.StringReader;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

/**
 * The PdC web service provider is a provider of services on the PdC that are usually performed
 * by the REST-based web resources. Theses services are publicly available to the Silverpeas business
 * objects in order to perform some explicit operations that cannot be done by REST invocation in
 * some circumstances; for example, to classify a new content in conjunction with its creation that
 * is not taken in charge by a web service (the classification can be done only on existing resource
 * content).
 */
public class PdcWebServiceProvider {

  private static final PdcWebServiceProvider instance = new PdcWebServiceProvider();
  @Inject
  private PdcServiceProvider serviceProvider;

  public static PdcWebServiceProvider aWebServiceProvider() {
    return instance;
  }

  /**
   * Classify the specified Silverpeas resource content onto the PdC with the specified positions.
   * The PdC is the one configured for the Silverpeas component instance that owns the resource.
   * @param content the key identifying uniquely the resource content.
   * @param positions the JSON representation of an array of PdC positions.
   * @throws JAXBException if an error occurs while deserializing the positions to Java objects.
   * @throws ContentManagerException if the content or the component instance doesn't exist or
   * cannot be fetched.
   * @throws PdcException if at least one position cannot be created onto the PdC.
   */
  public void classifyContent(final WAPrimaryKey content, String positions) throws JAXBException,
          ContentManagerException, PdcException {
    PdcClassificationEntity classification = fromJSON(positions);
    for (PdcPositionEntity position : classification.getClassificationPositions()) {
      getPdcServiceProvider().addPosition(position.toClassifyPosition(), content.getId(), content.
              getInstanceId());
    }
  }

  private PdcWebServiceProvider() {
  }

  private PdcServiceProvider getPdcServiceProvider() {
    return serviceProvider;
  }

  private PdcClassificationEntity fromJSON(String classification) throws JAXBException {
    JSONJAXBContext context = new JSONJAXBContext(PdcClassificationEntity.class,
            PdcPositionEntity.class, PdcPositionValue.class);
    JSONUnmarshaller unmarshaller = new JSONUnmarshallerImpl(context, JSONConfiguration.DEFAULT);
    try {
    return unmarshaller.unmarshalFromJSON(new StringReader(classification),
            PdcClassificationEntity.class);
    } catch (Error ex) {
      throw new JAXBException(ex.getMessage(), ex);
    }
  }
}
