/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.importexport.ical.ical4j;

import org.silverpeas.core.importexport.EncodingException;
import org.silverpeas.core.date.Datable;
import org.silverpeas.core.date.Date;
import org.silverpeas.core.date.DateTime;
import java.text.ParseException;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import javax.inject.Singleton;

/**
 * A decoder/encoder of iCal4J dates with Silverpeas dates.
 */
@Singleton
public class ICal4JDateCodec {

  /**
   * Encodes a Silverpeas date into an iCal4J date.
   * @param aDate the date to encode.
   * @return an iCal4J date.
   * @throws EncodingException if the encoding fails.
   */
  public net.fortuna.ical4j.model.Date encode(final Datable<?> aDate) throws EncodingException {
    return encode(aDate, false);
  }

  /**
   * Encodes the specified Silverpeas date into an iCal4J date set in UTC.
   * @param aDate the date to encode.
   * @return an iCal4J date.
   * @throws EncodingException if the encoding fails.
   */
  public net.fortuna.ical4j.model.Date encodeInUTC(final Datable<?> aDate) throws EncodingException {
    return encode(aDate, true);
  }

  /**
   * Encodes the specified Silverpeas date into an iCal4J date set or not in UTC according to the
   * specified UTC flag. If the UTC flag is positioned at false, then the encoded date is set in the
   * same timezone than the specified date to encode.
   * @param aDate the date to encode.
   * @param inUTC the UTC flag indicating whether the iCal4J date must be set in UTC. If false, the
   * encoded date will be in the same timezone than the specified date.
   * @return an iCal4J date.
   * @throws EncodingException if the encoding fails.
   */
  public net.fortuna.ical4j.model.Date encode(final Datable<?> aDate, boolean inUTC) throws
      EncodingException {
    net.fortuna.ical4j.model.Date iCal4JDate = null;
    try {
      if (aDate instanceof DateTime) {
        if (inUTC) {
          iCal4JDate = new net.fortuna.ical4j.model.DateTime(aDate.toICalInUTC());
        } else {
          iCal4JDate = new net.fortuna.ical4j.model.DateTime(aDate.toICal());
          ((net.fortuna.ical4j.model.DateTime)iCal4JDate).setTimeZone(getTimeZone(aDate));
        }
      } else if (aDate instanceof Date) {
        iCal4JDate = new net.fortuna.ical4j.model.Date(aDate.toICal());
      }
    } catch (ParseException ex) {
      throw new EncodingException(ex.getMessage(), ex);
    }
    return iCal4JDate;
  }

  private TimeZone getTimeZone(final Datable<?> date) {
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    return registry.getTimeZone(date.getTimeZone().getID());
  }
}
