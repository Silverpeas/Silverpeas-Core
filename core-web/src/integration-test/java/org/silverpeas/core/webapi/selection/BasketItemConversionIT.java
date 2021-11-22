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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.web.RESTWebServiceTest;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Integration tests on the conversion of a {@link BasketItem} instance to its counterpart
 * Silverpeas resource.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class BasketItemConversionIT extends RESTWebServiceTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return SelectionBasketItemITContext.createTestArchiveForTest(BasketItemConversionIT.class);
  }

  @Test
  public void aBasketItemMapsAContribution() {
    PublicationDetail publication = getPublication(0);
    BasketItem basketItem = BasketItem.from(publication);
    assertThat(basketItem.isContribution(), is(true));
    assertThat(basketItem, is(publication));
  }

  @Test
  public void aBasketItemMapsAResource() {
    ComponentInst app = OrganizationController.get().getComponentInst("toto1");
    BasketItem basketItem = BasketItem.from(app);
    assertThat(basketItem.isComponentInstance(), is(true));
    assertThat(basketItem, is(app));
  }

  @Test
  public void aContributionCanBeObtainedByItsMappedBasketItem() {
    PublicationDetail publication = getPublication(1);

    BasketItem basketItem = BasketItem.from(publication);
    assertThat(basketItem.isContribution(), is(true));

    PublicationDetail contribution = basketItem.toContribution();
    assertThat(contribution, notNullValue());
    assertThat(contribution, is(publication));
  }

  @Test
  public void aResourceCanBeObtainedByItsMappedBasketItem() {
    ComponentInst app = OrganizationController.get().getComponentInst("toto1");

    BasketItem basketItem = BasketItem.from(app);
    assertThat(basketItem.isComponentInstance(), is(true));

    ComponentInst resource = basketItem.toResource();
    assertThat(resource, notNullValue());
    assertThat(resource, is(app));
  }

  @Test
  public void aContributionCanBeObtainedAsAResourceByItsMappedBasketItem() {
    PublicationDetail publication = getPublication(0);

    BasketItem basketItem = BasketItem.from(publication);
    assertThat(basketItem.isContribution(), is(true));

    PublicationDetail resource = basketItem.toResource();
    assertThat(resource, notNullValue());
    assertThat(resource, is(resource));
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[0];
  }

  @Override
  protected String getTableCreationScript() {
    return SelectionBasketItemITContext.TABLE_CREATION_SCRIPT;
  }

  @Override
  protected String getDataSetScript() {
    return SelectionBasketItemITContext.DATA_SET_SCRIPT;
  }

  private PublicationDetail getPublication(int index) {
    Collection<PublicationDetail> publications = PublicationService.get()
        .getAllPublications("toto1");
    assertThat(publications.size(), greaterThanOrEqualTo(index + 1));

    Iterator<PublicationDetail> iterator = publications.iterator();
    for (int i = 0; i < index; i++) {
      iterator.next();
    }
    return iterator.next();
  }
}
