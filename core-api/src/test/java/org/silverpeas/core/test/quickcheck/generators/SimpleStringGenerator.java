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

package org.silverpeas.core.test.quickcheck.generators;

import com.pholser.junit.quickcheck.generator.GeneratorConfiguration;
import com.pholser.junit.quickcheck.generator.java.lang.AbstractStringGenerator;
import com.pholser.junit.quickcheck.generator.java.lang.strings.CodePoints;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.Pair;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.emptyList;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author silveryocha
 */
public class SimpleStringGenerator extends AbstractStringGenerator {

  private CodePoints charsetPoints;

  private int minChar = 0;
  private int maxChar = Integer.MAX_VALUE;
  private List<Pair<Integer, Integer>> excludes = emptyList();

  public SimpleStringGenerator() {
    super();
    initialize(Charsets.UTF_8);
  }

  /**
   * Tells this generator to emit simple strings.
   * @param conf the configuration to use
   */
  @SuppressWarnings("unused")
  public void configure(SpSimpleString conf) {
    this.excludes = parseIntervals(conf.excludes());
    final List<Pair<Integer, Integer>> includes = parseIntervals(conf.includes());
    this.minChar = includes.stream().findFirst().orElse(Pair.of(0, 0)).getFirst();
    this.maxChar = includes.isEmpty() ? this.maxChar : 0;
    for (int i = 0; i < includes.size() ; i++) {
      final Pair<Integer, Integer> current = includes.get(i);
      this.maxChar = Math.max(this.maxChar, current.getSecond());
      if (i > 0) {
        final Pair<Integer, Integer> previous = includes.get(i - 1);
        if (previous.getSecond() < current.getFirst()) {
          this.excludes.add(Pair.of(previous.getSecond(), current.getFirst()));
        }
      }
    }
    if (isDefined(conf.charset())) {
      initialize(Charset.forName(conf.charset()));
    }
  }

  private List<Pair<Integer, Integer>> parseIntervals(final String[] intervals) {
    return Stream.of(intervals)
        .map(s -> s.split("[.]{2}"))
        .map(a -> Pair.of(Integer.parseInt(a[0]),Integer.parseInt(a[1])))
        .sorted(Comparator.comparing(Pair::getFirst))
        .collect(Collectors.toList());
  }

  private void initialize(Charset charset) {
    charsetPoints = CodePoints.forCharset(charset);
  }

  @Override
  protected int nextCodePoint(final SourceOfRandomness random) {
    int code = -1;
    while(code == -1 || isExcluded(code)) {
      code = charsetPoints.at(random.nextInt(minChar, Math.min(maxChar, charsetPoints.size()) - 1));
    }
    return code;
  }

  @Override
  protected boolean codePointInRange(final int codePoint) {
    return charsetPoints.contains(codePoint) && !isExcluded(codePoint);
  }

  private boolean isExcluded(final int codePoint) {
    return excludes.stream()
        .anyMatch(i -> i.getFirst() <= codePoint && codePoint <= i.getSecond());
  }

  /**
   * Names a {@link java.nio.charset.Charset}.
   */
  @Target({ PARAMETER, FIELD, ANNOTATION_TYPE, TYPE_USE })
  @Retention(RUNTIME)
  @GeneratorConfiguration
  public @interface SpSimpleString {
    String charset() default "";
    /**
     * @return list of string with pattern "[0-9]+[.]{2}[0-9]+"
     */
    String[] includes() default {};
    /**
     * @return list of string with pattern "[0-9]+[.]{2}[0-9]+"
     */
    String[] excludes() default {};
  }
}
