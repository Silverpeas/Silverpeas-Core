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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

/**
 * <p>Java class for profile of spaceMapping element.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 *  &lt;xs:simpleType name=&quot;SpaceProfileType&quot; final=&quot;restriction&quot;&gt;
 *     &lt;xs:annotation&gt;
 *       &lt;xs:documentation&gt;
 *         A space has its own pre-defined user profiles. When a component instance is added within a
 *         given space, the roles of the users in this instance can then be inherited from the parent
 *         space. Because the user profiles of the application can be different from those of a space,
 *         it is necessary to define a mapping between the profiles of the space with those of the
 *         application.
 *       &lt;/xs:documentation&gt;
 *       &lt;xs:documentation&gt;
 *         By default, a space is visible to all users defined at least in a given user profile of an
 *         application instance in that space.
 *       &lt;/xs:documentation&gt;
 *     &lt;/xs:annotation&gt;
 *     &lt;xs:restriction base=&quot;xs:string&quot;&gt;
 *       &lt;xs:enumeration value=&quot;admin&quot;&gt;
 *         &lt;xs:annotation&gt;
 *           &lt;xs:documentation&gt;
 *             The space administrator. He can manage the details of a space and as such he can add or
 *             remove any application instances.
 *           &lt;/xs:documentation&gt;
 *         &lt;/xs:annotation&gt;
 *       &lt;/xs:enumeration&gt;
 *       &lt;xs:enumeration value=&quot;publisher&quot;&gt;
 *         &lt;xs:annotation&gt;
 *           &lt;xs:documentation&gt;
 *             The publisher. He can add/remove/edit/move contributions in the application instances in
 *             the space. When supported, he&#39;s in charge also of the validation of the contributions
 *             proposed by users in lower user profile.
 *           &lt;/xs:documentation&gt;
 *         &lt;/xs:annotation&gt;
 *       &lt;/xs:enumeration&gt;
 *       &lt;xs:enumeration value=&quot;writer&quot;&gt;
 *         &lt;xs:annotation&gt;
 *           &lt;xs:documentation&gt;
 *             The writer. He can add/remove/edit his own contributions in the application instances.
 *             With some instances, he can also participate in the edition of the contributions of
 *             others users.
 *           &lt;/xs:documentation&gt;
 *         &lt;/xs:annotation&gt;
 *       &lt;/xs:enumeration&gt;
 *       &lt;xs:enumeration value=&quot;reader&quot;&gt;
 *         &lt;xs:annotation&gt;
 *           &lt;xs:documentation&gt;
 *             The reader. He has only read-only access to the application instances of the space.
 *           &lt;/xs:documentation&gt;
 *         &lt;/xs:annotation&gt;
 *       &lt;/xs:enumeration&gt;
 *     &lt;/xs:restriction&gt;
 *   &lt;/xs:simpleType&gt;
 * </pre>
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpaceProfileType")
public class SpaceProfile implements Serializable {
  private static final long serialVersionUID = -8829429520356692137L;

  @XmlValue
  private String value;

  public String getValue() {
    return value;
  }
}
