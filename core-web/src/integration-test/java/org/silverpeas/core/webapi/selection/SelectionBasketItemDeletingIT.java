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
import org.silverpeas.web.AuthId;
import org.silverpeas.web.ResourceDeletionTest;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertTrue;
import static org.silverpeas.core.webapi.selection.SelectionBasketItemITContext.uriOf;
import static org.silverpeas.core.webapi.selection.SelectionBasketItemITContext.uriPathOf;

/**
 * Integration tests on deleting items in the selection basket through its REST-based web API.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class SelectionBasketItemDeletingIT extends ResourceDeletionTest {

  private String authToken;
  private User user;

  @Deployment
  public static Archive<?> createTestArchive() {
    return SelectionBasketItemITContext.createTestArchiveForTest(SelectionBasketItemDeletingIT.class);
  }

  @Before
  public void prepareUser() {
    user = User.getById("1");
    authToken = getTokenKeyOf(user);
  }

  @Override
  @Ignore("A user can access the web API once authenticated whatever its privilege")
  public void deletionOfAResourceByANonAuthorizedUser() {
    assertTrue(true);
  }

  @Test
  public void popAnEmptyBasket() {
    Response response = deleteAt(aResourceURI());
    assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void popSingleItemFromANonEmptyBasket() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    PublicationDetail publication = getPublication(1);
    SelectionBasket.get().put(publication);

    Response response = deleteAt(aResourceURI(), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.isEmpty(), is(true));
  }

  @Test
  public void popAnItemFromANonEmptyBasket() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    SelectionBasket basket = SelectionBasket.get();
    PublicationDetail publication = getPublication(0);
    basket.put(publication);
    basket.put(getPublication(1));

    Response response = deleteAt(aResourceURI(), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    BasketItem item = BasketItem.from(publication);
    var expected = new SelectionBasketEntry(item);
    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.size(), is(1));
    assertThat(content.get(0).getURI(), is(uriOf(publication)));
    assertThat(content.get(0), is(expected));
  }

  @Test
  public void deleteAnItemInAnEmptyBasket() {
    Response response = deleteAt(uriPathOf(getPublication(0)));
    assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void deleteANonExistingItemInANonEmptyBasket() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    PublicationDetail publication = getPublication(1);
    SelectionBasket.get().put(publication);

    Response response = deleteAt(uriPathOf(getPublication(0)), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void deleteAnItemInABasketWithASingleItem() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    PublicationDetail publication = getPublication(1);
    SelectionBasket.get().put(publication);

    Response response = deleteAt(uriPathOf(publication), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.isEmpty(), is(true));
  }

  @Test
  public void deleteAnItemInABasketWithSeveralItems() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    SelectionBasket basket = SelectionBasket.get();
    PublicationDetail publication1 = getPublication(0);
    PublicationDetail publication2 = getPublication(1);
    basket.put(publication1);
    basket.put(publication2);

    Response response = deleteAt(uriPathOf(publication1), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    BasketItem item = BasketItem.from(publication2);
    var expected = new SelectionBasketEntry(item);
    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.size(), is(1));
    assertThat(content.get(0).getURI(), is(uriOf(publication2)));
    assertThat(content.get(0), is(expected));
  }

  @Test
  public void deleteAtAGivenIndexInAnEmptyBasket() {
    Response response = deleteAt(uriPathOf(0));
    assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void deleteAtAnInvalidIndexInANonEmptyBasket() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    PublicationDetail publication = getPublication(1);
    SelectionBasket.get().put(publication);

    Response response = deleteAt(uriPathOf(1), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void deleteAtAGivenIndexInABasketWithASingleItem() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    PublicationDetail publication = getPublication(1);
    SelectionBasket.get().put(publication);

    Response response = deleteAt(uriPathOf(0), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.isEmpty(), is(true));
  }

  @Test
  public void deleteAtAGivenIndexABasketWithSeveralItems() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    SelectionBasket basket = SelectionBasket.get();
    PublicationDetail publication1 = getPublication(0);
    PublicationDetail publication2 = getPublication(1);
    basket.put(publication1);
    basket.put(publication2);

    Response response = deleteAt(uriPathOf(1), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    BasketItem item = BasketItem.from(publication2);
    var expected = new SelectionBasketEntry(item);
    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.size(), is(1));
    assertThat(content.get(0).getURI(), is(uriOf(publication2)));
    assertThat(content.get(0), is(expected));
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
  @SuppressWarnings("unchecked")
  public SelectionBasketEntry aResource() {
    PublicationDetail publication = getPublication(0);
    BasketItem item = BasketItem.from(publication);
    return new SelectionBasketEntry(item);
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
