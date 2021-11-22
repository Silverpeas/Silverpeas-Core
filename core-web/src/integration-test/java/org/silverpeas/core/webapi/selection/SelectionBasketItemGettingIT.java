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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.selection.SelectionBasket;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.web.AuthId;
import org.silverpeas.web.ResourceGettingTest;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.silverpeas.core.webapi.selection.SelectionBasketItemITContext.uriOf;

/**
 * Integration tests on getting items in the selection basket through its REST-based web API.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class SelectionBasketItemGettingIT extends ResourceGettingTest {

  private String authToken;
  private User user;

  @Deployment
  public static Archive<?> createTestArchive() {
    return SelectionBasketItemITContext.createTestArchiveForTest(SelectionBasketItemGettingIT.class);
  }

  @Before
  public void prepareUser() {
    user = User.getById("1");
    authToken = getTokenKeyOf(user);
  }

  @Override
  @Ignore("A user can access the web API once authenticated whatever its privilege")
  public void gettingAResourceByAnUnauthorizedUser() {
    assertTrue(true);
  }

  @Test
  public void gettingEmptyBasketContent() {
    Response response = getAt(aResourceURI(), Response.class);
    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.isEmpty(), is(true));
  }

  @Test
  public void gettingBasketContentWithOneSingleItem() {
    String sessionKey = authenticate(user);

    Collection<PublicationDetail> publications = PublicationService.get()
        .getAllPublications("toto1");
    assertThat(publications.isEmpty(), is(false));
    PublicationDetail publication = publications.iterator().next();
    SelectionBasket.get().put(publication);

    BasketItem item = BasketItem.from(publication);
    var expected = new SelectionBasketEntry(item);
    Response response =
        getAt(aResourceURI(), withAsAuthId(AuthId.sessionKey(sessionKey)), Response.class);
    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.size(), is(1));
    assertThat(content.get(0).getURI(), is(uriOf(publication)));
    assertThat(content.get(0), is(expected));
  }

  @Test
  public void gettingBasketContent() {
    String sessionKey = authenticate(user);

    Collection<PublicationDetail> publications = PublicationService.get()
        .getAllPublications("toto1");
    assertThat(publications.isEmpty(), is(false));
    publications.forEach(p -> SelectionBasket.get().put(p));

    Response response =
        getAt(aResourceURI(), withAsAuthId(AuthId.sessionKey(sessionKey)), Response.class);
    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.size(), is(publications.size()));
    Mutable<Integer> counter = Mutable.of(content.size() - 1);
    publications.forEach(p -> {
      BasketItem item = BasketItem.from(p);
      var expected = new SelectionBasketEntry(item);
      assertThat(content.get(counter.get()).getURI(), is(uriOf(p)));
      assertThat(content.get(counter.get()), is(expected));
      counter.set(counter.get() - 1);
    });
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[0];
  }

  @Override
  public String aResourceURI() {
    return SelectionBasketResource.BASE_URI_PATH;
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI() + "/id/tartempion";
  }

  @Override
  public <T> T aResource() {
    return null;
  }

  @Override
  public String getAPITokenValue() {
    return authToken;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return SelectionBasketEntry.class;
  }

  @Override
  protected String getTableCreationScript() {
    return SelectionBasketItemITContext.TABLE_CREATION_SCRIPT;
  }

  @Override
  protected String getDataSetScript() {
    return SelectionBasketItemITContext.DATA_SET_SCRIPT;
  }
}
