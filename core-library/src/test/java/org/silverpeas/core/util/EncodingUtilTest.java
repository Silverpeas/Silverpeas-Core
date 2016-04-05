package org.silverpeas.core.util;

import org.junit.Test;
import org.silverpeas.core.util.EncodingUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Yohann Chastagnier
 */
public class EncodingUtilTest {

  @Test
  public void asHex() {
    String sourceOfBytes = "Pousse pas mémé dans les orties";
    String hex = EncodingUtil.asHex(sourceOfBytes.getBytes());
    assertThat(hex, is(otherWayToGetHexa(sourceOfBytes.getBytes())));
  }

  private static String otherWayToGetHexa(byte[] binaryData) {
    StringBuilder sb = new StringBuilder();
    if (binaryData != null) {
      for (byte oneByte : binaryData) {
        sb.append(String.format("%02x", oneByte));
      }
    }
    return sb.toString();
  }
}