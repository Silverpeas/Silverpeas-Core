/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.core.contribution.attachment.model;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * @author ehugonnet
 */
public class UnlockContextTest {

  /**
   * Test of addOption method, of class UnlockContext.
   */
  @Test
  public void testAddOption() {
    UnlockContext context = new UnlockContext("12", "111", "fr");
    assertThat(context.isForce(), is(false));
    assertThat(context.isPublicVersion(), is(true));
    assertThat(context.isPrivateVersion(), is(false));
    assertThat(context.isUpload(), is(false));
    assertThat(context.isWebdav(), is(false));
    context.addOption(UnlockOption.FORCE);
    assertThat(context.isForce(), is(true));
    assertThat(context.isPublicVersion(), is(true));
    assertThat(context.isPrivateVersion(), is(false));
    context.addOption(UnlockOption.PRIVATE_VERSION);
    assertThat(context.isPublicVersion(), is(false));
    assertThat(context.isPrivateVersion(), is(true));
  }

  /**
   * Test of isPublicVersion method, of class UnlockContext.
   */
  @Test
  public void testIsPublicVersion() {
    UnlockContext context = new UnlockContext("12", "111", "fr");
    assertThat(context.isPublicVersion(), is(true));
  }

  /**
   * Test of isForce method, of class UnlockContext.
   */
  @Test
  public void testIsForce() {
    UnlockContext context = new UnlockContext("12", "111", "fr");
    assertThat(context.isForce(), is(false));
    context.addOption(UnlockOption.FORCE);
    assertThat(context.isForce(), is(true));
    context.removeOption(UnlockOption.FORCE);
    assertThat(context.isForce(), is(false));
  }

  /**
   * Test of isUpload method, of class UnlockContext.
   */
  @Test
  public void testIsUpload() {
    UnlockContext context = new UnlockContext("12", "111", "fr");
    assertThat(context.isUpload(), is(false));
    context.addOption(UnlockOption.UPLOAD);
    assertThat(context.isUpload(), is(true));
    context.removeOption(UnlockOption.UPLOAD);
    assertThat(context.isUpload(), is(false));
  }

  /**
   * Test of isWebdav method, of class UnlockContext.
   */
  @Test
  public void testIsWebdav() {
    UnlockContext context = new UnlockContext("12", "111", "fr");
    assertThat(context.isWebdav(), is(false));
    context.addOption(UnlockOption.WEBDAV);
    assertThat(context.isWebdav(), is(true));
    context.removeOption(UnlockOption.WEBDAV);
    assertThat(context.isWebdav(), is(false));
  }


}
