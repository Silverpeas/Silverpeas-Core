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
package org.silverpeas.core.webapi.password;

import org.silverpeas.core.security.authentication.password.rule.PasswordRule;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PasswordRuleEntity {

  @XmlElement(defaultValue = "")
  private String type;

  @XmlElement(defaultValue = "")
  private String description;

  /**
   * Creates a new rule entity from the specified rule.
   * @param rule
   * @param language
   * @return the entity representing the specified rule.
   */
  public static PasswordRuleEntity createFrom(final PasswordRule rule, final String language) {
    return new PasswordRuleEntity(rule, language);
  }

  /**
   * Default hidden constructor.
   */
  private PasswordRuleEntity(final PasswordRule rule, final String language) {
    type = rule.getType().name();
    description = rule.getDescription(language);
  }

  protected PasswordRuleEntity() {

  }

  public String getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }
}
