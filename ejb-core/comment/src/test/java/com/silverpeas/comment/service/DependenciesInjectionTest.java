/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.comment.service;

import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * Unit tests on the dependencies injection of the service on the comments.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-comment.xml")
public class DependenciesInjectionTest {

  @Inject
  private DefaultCommentService service;

  public DependenciesInjectionTest() {
  }

  /**
   * The service on the comments and its dependencies should be injected by the IoC container.
   */
  @Test
  public void theCommentServiceShouldBeInjected() {
    assertNotNull(service);
    assertNotNull(service.getCommentDAO());
  }

  /**
   * The service should have its dependency correctly set by the IoC container when getting by its
   * factory.
   */
  @Test
  public void theCommentServiceShouldBeGetByTheFactory() {
    CommentServiceFactory commentServiceFactory = CommentServiceFactory.getFactory();
    CommentService commentService = commentServiceFactory.getCommentService();
    assertNotNull(commentService);
    assertNotNull(((DefaultCommentService)commentService).getCommentDAO());
  }
}