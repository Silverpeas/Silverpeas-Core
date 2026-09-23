/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.twofactor;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.enterprise.context.ApplicationScoped;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Generates QR code images for two-factor authentication enrollment.
 */
@ApplicationScoped
public class QrCodeGenerator {

  public byte[] generate(final String content, final int size) {
    try {
      final BitMatrix matrix = new MultiFormatWriter().encode(
          content, BarcodeFormat.QR_CODE, size, size);
      final BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
      try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
        ImageIO.write(image, "PNG", output);
        return output.toByteArray();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Unable to generate TOTP QR code", e);
    }
  }
}
