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
import org.silverpeas.web.ResourceCreationTest;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests on posting items in the selection basket through its REST-based web API.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class SelectionBasketItemPostingIT extends ResourceCreationTest {

  private String authToken;
  private User user;

  @Deployment
  public static Archive<?> createTestArchive() {
    return SelectionBasketItemITContext.createTestArchiveForTest(SelectionBasketItemPostingIT.class);
  }

  @Before
  public void prepareUser() {
    user = User.getById("1");
    authToken = getTokenKeyOf(user);
  }

  @Override
  @Ignore("A user can access the web API once authenticated whatever its privilege")
  public void creationOfANewResourceByANonAuthorizedUser() {
    assertTrue(true);
  }

  @Test
  public void postAnItemIntoAnEmptyBasket() {
    SelectionBasketEntry expected = aResource();
    Response response = post(expected, at(aResourceURI()));
    assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));

    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.size(), is(1));
    assertThat(content.get(0).getItem(), is(expected.getItem()));
  }

  @Test
  public void postTwoItemsIntoAnEmptyBasket() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    SelectionBasketEntry expected1 = aResource();
    Response response = post(expected1, at(aResourceURI()), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));

    SelectionBasketEntry expected2 = anotherResource();
    response = post(expected2, at(aResourceURI()), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));

    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.size(), is(2));
    assertThat(content.get(0).getItem(), is(expected2.getItem()));
    assertThat(content.get(1).getItem(), is(expected1.getItem()));
  }

  @Test
  public void postTwoIdenticalItemsIntoAnEmptyBasket() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    SelectionBasketEntry expected = aResource();
    Response response = post(expected, at(aResourceURI()), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));

    response = post(expected, at(aResourceURI()), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));

    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.size(), is(1));
    assertThat(content.get(0).getItem(), is(expected.getItem()));
  }

  @Test
  public void postAnItemIntoANonEmptyBasket() {
    String sessionKey = authenticate(user);
    AuthId authId = AuthId.sessionKey(sessionKey);

    PublicationDetail publication = getPublication(1);
    SelectionBasket.get().put(publication);

    SelectionBasketEntry expected = aResource();
    Response response = post(expected, at(aResourceURI()), withAsAuthId(authId));
    assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));

    GenericType<List<SelectionBasketEntry>> contentType = new GenericType<>() {};
    List<SelectionBasketEntry> content = response.readEntity(contentType);
    assertThat(content.size(), is(2));
    assertThat(content.get(0).getItem(), is(expected.getItem()));
    assertThat(content.get(1).getItem().getIdentifier(), is(publication.getIdentifier()));
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

  public SelectionBasketEntry anotherResource() {
    PublicationDetail publication = getPublication(1);
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
