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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.webapi.selection;

import org.jboss.shrinkwrap.api.Archive;
import org.silverpeas.core.SilverpeasResource;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.core.webapi.profile.AuthenticationResource;
import org.silverpeas.core.webapi.profile.ProfileResourceBaseURIs;
import org.silverpeas.core.webapi.profile.UserProfileEntity;
import org.silverpeas.core.webapi.util.UserEntity;
import org.silverpeas.web.RESTWebServiceTest;

import java.net.URI;

/**
 * Base class for all integration test classes on the SelectionBasket Web resource.
 * @author mmoquillon
 */
public abstract class SelectionBasketItemITContext {

  public static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/webapi/selection/create-table.sql";
  public static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/webapi/selection/create-dataset.sql";

  private static final String URI_BASE =
      "http://localhost:8080/silverpeas/services/selection/item/%s";

  private static final String ITEM_URI_PATH = SelectionBasketResource.BASE_URI_PATH + "/item/%s";
  private static final String INDEX_URI_PATH = SelectionBasketResource.BASE_URI_PATH + "/index/%d";

  /**
   * Creates an archive with the integration tests to run within a Wildfly instance.
   * @param test the test class in which are defined the integration tests to run.
   * @return a Web archive.
   */
  public static Archive<?> createTestArchiveForTest(Class<? extends RESTWebServiceTest> test) {
    return WarBuilder4WebCore.onWarForTestClass(test)
        .addRESTWebServiceEnvironment()
        .addClasses(UserEntity.class, UserProfileEntity.class, AuthenticationResource.class,
            ProfileResourceBaseURIs.class)
        .testFocusedOn(w -> w.addPackages(true, "org.silverpeas.core.webapi.selection"))
        .build();
  }

  /**
   * Computes the URI of the specified Silverpeas resource as provided by the Web resource
   * representing the selection basket of the current user.
   * @param resource a resource in Silverpeas. It can be an organisational object or a
   * contribution.
   * @return the URI of the resource related to the selection basket web resource.
   */
  public static URI uriOf(final SilverpeasResource resource) {
    return URI.create(String.format(URI_BASE, resource.getIdentifier().asString()));
  }

  /**
   * Computes the path of the URI of the specified resource relative to the path at which is exposed
   * the selection basket web resource.
   * @param resource a resource in Silverpeas. It can be an organisational object or a
   * contribution.
   * @return the URI of the resource relative to the selection basket web resource URI path.
   */
  public static String uriPathOf(final SilverpeasResource resource) {
    return String.format(ITEM_URI_PATH, resource.getIdentifier().asString());
  }

  /**
   * Computes the path of the URI of the item at the specified index relative to the path at which
   * is exposed the selection basket web resource.
   * @param index index of a resource in the selection basket
   * @return the URI of the resource relative to the selection basket web resource URI path.
   */
  public static String uriPathOf(final int index) {
    return String.format(INDEX_URI_PATH, index);
  }
}
