/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.util;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests on the way the beans are (un)marshalled in JSON by the mapper of Silverpeas.
 * @author mmoquillon
 */
class JSONCodecTest {

  @Test
  void unknownPropertiesAreIgnoredWhenDecoding() {
    String json = "{\"id\":\"42\",\"name\":\"Toto\",\"readOnly\":true,\"uri\":\"/beans/42\"}";
    Bean bean = JSONCodec.decode(json, Bean.class);
    assertThat(bean.getId(), is("42"));
    assertThat(bean.getName(), is("Toto"));
  }

  @Test
  void onlyJaxbExposedPropertiesAreEncoded() {
    Bean bean = new Bean("42", "Toto");
    String json = JSONCodec.encode(bean);
    assertThat(json, containsString("\"id\":\"42\""));
    assertThat(json, containsString("\"name\":\"Toto\""));
    assertThat(json, containsString("\"readOnly\":true"));
    assertThat(json, not(containsString("hidden")));
  }

  @Test
  void nullPropertiesAreOmitted() {
    String json = JSONCodec.encode(new Bean("42", null));
    assertThat(json, not(containsString("name")));
  }

  @Test
  void jacksonAnnotationsAreTakenIntoAccount() {
    String json = JSONCodec.encode(new Bean("42", "Toto", Level.HIGH));
    assertThat(json, containsString("\"level\":\"high\""));
    Bean bean = JSONCodec.decode(json, Bean.class);
    assertThat(bean.getLevel(), is(Level.HIGH));
  }

  @Test
  void newObjectMapperIsConfiguredAsTheCodec() throws Exception {
    ObjectMapper mapper = JSONCodec.newObjectMapper();
    Bean bean = mapper.readValue("{\"id\":\"42\",\"unknown\":1}", Bean.class);
    assertThat(bean.getId(), is("42"));
    assertThat(mapper.writeValueAsString(bean), is("{\"id\":\"42\",\"readOnly\":true}"));
  }

  enum Level {
    LOW, HIGH;

    @JsonValue
    public String asJson() {
      return name().toLowerCase();
    }
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.NONE)
  static class Bean {
    @XmlElement
    private String id;
    @XmlElement
    private String name;
    @XmlElement
    private Level level;
    private String hidden = "secret";

    Bean() {
    }

    Bean(String id, String name) {
      this(id, name, null);
    }

    Bean(String id, String name, Level level) {
      this.id = id;
      this.name = name;
      this.level = level;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public Level getLevel() {
      return level;
    }

    public String getHidden() {
      return hidden;
    }

    @XmlElement
    public boolean isReadOnly() {
      return true;
    }
  }
}
