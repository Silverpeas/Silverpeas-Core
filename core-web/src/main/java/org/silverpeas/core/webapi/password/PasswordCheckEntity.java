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
import org.silverpeas.core.security.authentication.password.service.PasswordCheck;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PasswordCheckEntity {

  /* Indicates if it exists at least one error */
  @XmlElement
  private boolean isCorrect;

  /* Indicates if combination of rules is respected */
  @XmlElement
  private boolean isRuleCombinationRespected;

  /* List of password required rule ids that are not verified */
  @XmlElement
  private Collection<String> requiredRuleIdsInError = new ArrayList<String>();

  /* List of password combined rule ids that are not verified */
  @XmlElement
  private Collection<String> combinedRuleIdsInError = new ArrayList<String>();

  /**
   * Creates a new password check entity
   * @return the entity representing the password rule checking.
   */
  public static PasswordCheckEntity createFrom(final PasswordCheck passwordCheck) {
    return new PasswordCheckEntity(passwordCheck);
  }

  /**
   * Defulat hidden constructor
   * @param passwordCheck
   */
  private PasswordCheckEntity(final PasswordCheck passwordCheck) {
    isCorrect = passwordCheck.isCorrect();
    isRuleCombinationRespected = passwordCheck.isRuleCombinationRespected();
    for (PasswordRule rule : passwordCheck.getRequiredRulesInError()) {
      requiredRuleIdsInError.add(rule.getType().name());
    }
    for (PasswordRule rule : passwordCheck.getCombinedRulesInError()) {
      combinedRuleIdsInError.add(rule.getType().name());
    }
  }

  protected PasswordCheckEntity() {
  }

  public boolean isCorrect() {
    return isCorrect;
  }

  public boolean isRuleCombinationRespected() {
    return isRuleCombinationRespected;
  }

  public Collection<String> getRequiredRuleIdsInError() {
    return requiredRuleIdsInError;
  }

  public Collection<String> getCombinedRuleIdsInError() {
    return combinedRuleIdsInError;
  }
}
