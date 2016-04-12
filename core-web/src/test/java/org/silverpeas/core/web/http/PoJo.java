/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.web.http;

import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Date;

/**
 * @author: Yohann Chastagnier
 */
public class PoJo {

  private String aStringWithoutAnnotation;

  @FormParam("")
  private RequestFile aRequestFile;

  @FormParam("")
  private String aStringNotInParameter;

  @FormParam("")
  private String aString;

  @FormParam("")
  @UnescapeHtml
  private String aStringNotInParameterToUnescape;

  @FormParam("")
  @UnescapeHtml
  private String aStringToUnescape;

  @FormParam("anIntegerNotInParameter")
  private Integer anIntegerNotInParameter;

  @FormParam("")
  private Integer anInteger;

  @FormParam("")
  private int aPrimitiveIntegerNotInParameter = -1;

  @XmlElement(name = "anIntegerFromAnnotation")
  private int aPrimitiveInteger;

  @FormParam("")
  private Long aLongNotInParameter;

  @FormParam("")
  private Long aLong;

  @FormParam("aLongFromAnnotation")
  private long aPrimitiveLong;

  @FormParam("")
  private Boolean aBooleanNotInParameter;

  @XmlElement
  private Boolean aBoolean;

  @FormParam("aBooleanFromAnnotation")
  private boolean aPrimitiveBoolean;

  @FormParam("")
  private Date aDateNotInParameter;

  @FormParam("")
  private Date aDate;

  @FormParam("")
  private EnumWithoutCreationAnnotation anEnumNotInParameter;

  @FormParam("")
  private EnumWithoutCreationAnnotation anEnum;

  @FormParam("")
  private URI anUriNotInParameter;

  @FormParam("")
  private URI anUri;

  private PoJo() {
    // To be instantiated
  }

  public RequestFile getaRequestFile() {
    return aRequestFile;
  }

  public String getaStringWithoutAnnotation() {
    return aStringWithoutAnnotation;
  }

  public String getaStringNotInParameter() {
    return aStringNotInParameter;
  }

  public String getaString() {
    return aString;
  }

  public String getaStringNotInParameterToUnescape() {
    return aStringNotInParameterToUnescape;
  }

  public String getaStringToUnescape() {
    return aStringToUnescape;
  }

  public Integer getAnIntegerNotInParameter() {
    return anIntegerNotInParameter;
  }

  public Integer getAnInteger() {
    return anInteger;
  }

  public int getaPrimitiveIntegerNotInParameter() {
    return aPrimitiveIntegerNotInParameter;
  }

  public int getaPrimitiveInteger() {
    return aPrimitiveInteger;
  }

  public Long getaLongNotInParameter() {
    return aLongNotInParameter;
  }

  public Long getaLong() {
    return aLong;
  }

  public long getaPrimitiveLong() {
    return aPrimitiveLong;
  }

  public Boolean getaBooleanNotInParameter() {
    return aBooleanNotInParameter;
  }

  public Boolean getaBoolean() {
    return aBoolean;
  }

  public boolean isaPrimitiveBoolean() {
    return aPrimitiveBoolean;
  }

  public Date getaDateNotInParameter() {
    return aDateNotInParameter;
  }

  public Date getaDate() {
    return aDate;
  }

  public EnumWithoutCreationAnnotation getAnEnumNotInParameter() {
    return anEnumNotInParameter;
  }

  public EnumWithoutCreationAnnotation getAnEnum() {
    return anEnum;
  }

  public URI getAnUri() {
    return anUri;
  }

  public URI getAnUriNotInParameter() {
    return anUriNotInParameter;
  }
}
