/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.contribution.publication.subscription;

import org.silverpeas.core.subscription.SubscriptionContributionType;

/**
 * @author silveryocha
 */
public class PublicationSubscriptionConstants {

  private PublicationSubscriptionConstants() {
    // Constant class
  }

  /**
   * The resource is a publication. Used by component instances handling publications.
   */
  public static final SubscriptionContributionType PUBLICATION = new SubscriptionContributionType() {
    private static final long serialVersionUID = 1202631086138582114L;

    @Override
    public int priority() {
      return 20;
    }

    @Override
    public String getName() {
      return "PUBLICATION";
    }
  };

  /**
   * The resource is a publication alias. Used by component instances handling publications.
   */
  public static final SubscriptionContributionType PUBLICATION_ALIAS = new SubscriptionContributionType() {
    private static final long serialVersionUID = -5367357111587094711L;

    @Override
    public int priority() {
      return 20;
    }

    @Override
    public String getName() {
      return "PUBLICATION_ALIAS";
    }
  };
}
