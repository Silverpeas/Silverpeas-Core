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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PasswordPolicyEntity {

  /* List of password rules indexed by rule names */
  @XmlElement(defaultValue = "")
  private Map<String, PasswordRuleEntity> rules = new LinkedHashMap<String, PasswordRuleEntity>();

  /* Number of rules that have to match within a combination of rules */
  @XmlElement
  private int nbMatchingCombinedRules = 0;

  /* List of password combined rules indexed by rule names */
  @XmlElement(defaultValue = "")
  private Map<String, PasswordRuleEntity> combinedRules =
      new LinkedHashMap<String, PasswordRuleEntity>();

  /* Message contains additional rules that are not verifiable within Silverpeas services */
  @XmlElement(defaultValue = "")
  private String extraRuleMessage = "";

  /**
   * Creates a new password policy entity
   * @param nbMatchingCombinedRules
   * @param extraRuleMessage
   * @return the entity representing the specified rule.
   */
  public static PasswordPolicyEntity createFrom(int nbMatchingCombinedRules,
      final String extraRuleMessage) {
    return new PasswordPolicyEntity(nbMatchingCombinedRules, extraRuleMessage);
  }

  /**
   * Default hidden constructor.
   * @param extraRuleMessage
   */
  private PasswordPolicyEntity(int nbMatchingCombinedRules, final String extraRuleMessage) {
    this.nbMatchingCombinedRules = nbMatchingCombinedRules;
    this.extraRuleMessage = extraRuleMessage;
  }

  protected PasswordPolicyEntity() {
  }

  public PasswordPolicyEntity addRule(PasswordRuleEntity rule) {
    rules.put(rule.getType(), rule);
    return this;
  }

  public PasswordPolicyEntity addCombinedRule(PasswordRuleEntity rule) {
    combinedRules.put(rule.getType(), rule);
    return this;
  }

  public Map<String, PasswordRuleEntity> getRules() {
    return rules;
  }

  public int getNbMatchingCombinedRules() {
    return nbMatchingCombinedRules;
  }

  public Map<String, PasswordRuleEntity> getCombinedRules() {
    return combinedRules;
  }

  public String getExtraRuleMessage() {
    return extraRuleMessage;
  }
}
