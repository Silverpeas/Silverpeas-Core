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
package org.silverpeas.core.pdc.subscription.test;

import org.silverpeas.core.pdc.classification.ClassifyEngine;
import org.silverpeas.core.pdc.pdc.model.AxisHeaderI18N;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.pdc.model.SearchCriteria;
import org.silverpeas.core.test.BasicWarBuilder;

/**
 * A ShrinkWrap War builder for the subscriptions on the PdC dedicated to the integration tests.
 * <p>
 * Only the classes of the PdC that the subscriptions require are embedded into the archive. The
 * whole PdC engine isn't: it counts a lot of managed beans whose injection points would all have to
 * be satisfied at deployment whereas the subscriptions don't use them.
 * </p>
 * @author mmoquillon
 */
public class WarBuilder4Pdc extends BasicWarBuilder {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param classOfTest the class of the test for which a war archive will be built.
   */
  protected <T> WarBuilder4Pdc(final Class<T> classOfTest) {
    super(classOfTest);
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the common
   * dependencies required by the subscriptions on the PdC.
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4Pdc onWarForTestClass(Class<T> test) {
    WarBuilder4Pdc warBuilder = new WarBuilder4Pdc(test);
    warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .addAsResource(
            "org/silverpeas/jobStartPagePeas/settings/jobStartPagePeasSettings.properties")
        .addAsResource("org/silverpeas/util/logging")
        .addAsResource("org/silverpeas/lookAndFeel/generalLook.properties")
        .testFocusedOn(war -> war
            .addPackages(true, "org.silverpeas.core.pdc.subscription")
            // the classification model carries the criteria on the axis of the PdC; its engine is
            // the only managed bean of the package and isn't used by the subscriptions
            .addPackages(false, "org.silverpeas.core.pdc.classification")
            .deleteClasses(ClassifyEngine.class)
            // the tree model carries no managed bean and is required by the values of the axis
            .addPackages(false, "org.silverpeas.core.pdc.tree.model")
            .addClasses(SearchCriteria.class, AxisValueCriterion.class, AxisHeaderI18N.class,
                org.silverpeas.core.pdc.pdc.model.Value.class)
            .addAsResource("create-database.sql"));
    return warBuilder;
  }
}
