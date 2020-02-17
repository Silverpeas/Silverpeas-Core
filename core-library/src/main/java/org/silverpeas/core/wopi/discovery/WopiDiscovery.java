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

package org.silverpeas.core.wopi.discovery;

import org.silverpeas.core.SilverpeasRuntimeException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * WOPI discovery is the process by which a WOPI host identifies Office for the web capabilities
 * and how to initialize Office for the web applications within a site. WOPI hosts use the
 * discovery XML to determine how to interact with Office for the web.
 * <p>
 *   This class represents the root element of a discovery XML description file.
 * </p>
 * <a href="https://wopi.readthedocs.io/en/latest/discovery.html#">See the explanations of WOPI discovery from official doc.</a>
 * @author silveryocha
 */
@XmlRootElement(name = "wopi-discovery")
@XmlAccessorType(XmlAccessType.FIELD)
public class WopiDiscovery implements Serializable {
  private static final long serialVersionUID = -4834847364477784378L;

  @XmlElement(name = "net-zone")
  private NetZone netZone;

  protected NetZone getNetZone() {
    return netZone;
  }

  public void consumeBaseUrlMimeType(BiConsumer<String, Action> consumer) {
    netZone.getApps().forEach(ap ->
        ap.getActions().stream()
            .filter(ac -> ac.getName().contains("edit"))
            .forEach(ac -> consumer.accept(ap.getName(), ac)));
  }

  public static WopiDiscovery load(InputStream in) {
    try {
      final JAXBContext context = JAXBContext.newInstance(WopiDiscovery.class);
      final Unmarshaller unmarshaller = context.createUnmarshaller();
      return (WopiDiscovery) unmarshaller.unmarshal(in);
    } catch (JAXBException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }
}
