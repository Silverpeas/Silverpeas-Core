/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.core.date;

import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPIRule;

import java.time.ZoneId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author silveryocha
 */
public class TimeZoneUtilTest {

  @Rule
  public CommonAPIRule commonAPIRule = new CommonAPIRule();

  @Test
  public void toZoneId() {
    assertThat(TimeZoneUtil.toZoneId("Pacific Standard Time (Mexico)"), is(ZoneId.of("America/Tijuana")));
    assertThat(TimeZoneUtil.toZoneId("Pacific Standard Time"), is(ZoneId.of("America/Los_Angeles")));
    assertThat(TimeZoneUtil.toZoneId("Romance Standard Time"), is(ZoneId.of("Europe/Paris")));
    assertThat(TimeZoneUtil.toZoneId("GMT_-11"), is(ZoneId.of("Etc/GMT+11")));
    assertThat(TimeZoneUtil.toZoneId("GMT_Standard_Time"), is(ZoneId.of("Europe/London")));
    assertThat(TimeZoneUtil.toZoneId("UTC"), is(ZoneId.of("UTC")));
    assertThat(TimeZoneUtil.toZoneId("Asia/Muscat"), is(ZoneId.of("Asia/Muscat")));
  }
}