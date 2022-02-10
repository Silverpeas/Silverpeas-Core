/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.expression;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.function.BiFunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author Yohann Chastagnier
 */
public class PrefixedNotationExpressionEngineAlphabeticalTest {

  private static BiFunction<String, String, String> concatBehavior =
      (initial, value) -> initial == null ? value : (initial + value);

  private static PrefixedNotationExpressionEngine.OperatorFunction<String> concatSign =
      new PrefixedNotationExpressionEngine.OperatorFunction<>("+", concatBehavior);

  private static PrefixedNotationExpressionEngine.OperatorFunction<String> concatMethod =
      new PrefixedNotationExpressionEngine.OperatorFunction<>("concat", concatBehavior);

  @SuppressWarnings("unchecked")
  private final static PrefixedNotationExpressionEngine<String> engine =
      PrefixedNotationExpressionEngine.from(s -> s, concatSign, concatMethod);

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void simpleExpression() {
    assertThat(engine.evaluate(""), nullValue());
    assertThat(engine.evaluate("1"), is("1"));
    assertThat(engine.evaluate("(3)"), is("3"));
    assertThat(engine.evaluate(" 1"), is("1"));
    assertThat(engine.evaluate("1 "), is("1"));
    assertThat(engine.evaluate(" 1 "), is("1"));
    assertThat(engine.evaluate(" (1)"), is("1"));
    assertThat(engine.evaluate("(1) "), is("1"));
    assertThat(engine.evaluate(" (1) "), is("1"));
    assertThat(engine.evaluate(" ( 1)"), is("1"));
    assertThat(engine.evaluate("(1 ) "), is("1"));
    assertThat(engine.evaluate(" ( 1 ) "), is("1"));
    assertThat(engine.evaluate(" \\( 1 \\) "), is("( 1 )"));
    assertThat(engine.evaluate(" \\( 1 \\)\\"), is("( 1 )\\"));
    assertThat(engine.evaluate(" A \\( 1 \\)\\"), is("A ( 1 )\\"));
    assertThat(engine.evaluate(" A \\28 1 \\29"), is("A \\28 1 \\29"));
    assertThat(engine.evaluate(" A \\28 1 \\29\\"), is("A \\28 1 \\29\\"));
    assertThat(engine.evaluate(" A \\T 1 \\T"), is("A \\T 1 \\T"));
    assertThat(engine.evaluate(" (( &é#\"'\\(çà_-èé+ù%*µ$£¤!§/:;.,?ÖÛ\\) ))"),
        is("&é#\"'(çà_-èé+ù%*µ$£¤!§/:;.,?ÖÛ)"));
  }

  @Test
  public void detectOperatorShouldNeverThrowError() {
    assertThat(engine.detectOperator(null), is(false));
    assertThat(engine.detectOperator(""), is(false));

    assertThat(engine.detectOperator("+"), is(false));
    assertThat(engine.detectOperator("++"), is(true));
    assertThat(engine.detectOperator("-"), is(false));
    assertThat(engine.detectOperator(" +"), is(false));
    assertThat(engine.detectOperator(" + "), is(true));
    assertThat(engine.detectOperator("+ "), is(true));
    assertThat(engine.detectOperator("a+"), is(false));

    assertThat(engine.detectOperator("+ A"), is(true));
    assertThat(engine.detectOperator("++ A"), is(true));
    assertThat(engine.detectOperator("- A"), is(false));
    assertThat(engine.detectOperator(" + A"), is(true));
    assertThat(engine.detectOperator(" + A "), is(true));
    assertThat(engine.detectOperator("+ A "), is(true));
    assertThat(engine.detectOperator("a+ A"), is(false));
  }

  @Test(expected = NullPointerException.class)
  public void nullExpressionShouldThrowError() {
    engine.evaluate(null);
  }

  @Test
  public void noOperatorWithSeveralPartsShouldThrowAnError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.operator.none");
    engine.evaluate("(1)(1)");
  }

  @Test
  public void missingOpeningParenthesisShouldThrowAnError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.operand.parentheses.missing.open");
    engine.evaluate("1)");
  }

  @Test
  public void escapedCharactersOutOfOperandsWithDetectedOperandsShouldThrowAnError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.malformed");
    engine.evaluate(" \\(( 1 \\))\\");
  }

  @Test
  public void charactersOutOfOperandsWithDetectedOperandsShouldThrowAnError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.malformed");
    engine.evaluate(" A ( 1 )");
  }

  @Test
  public void missingClosingParenthesisShouldThrowAnError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.operand.parentheses.missing.close");
    engine.evaluate("(1");
  }

  @Test
  public void missingClosingParenthesisIntoComplexExpressionShouldThrowAnError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.operand.parentheses.missing.close");
    engine.evaluate("(  ((  +  ((  1  ) )  (  2  )   ) )  ");
  }

  @Test
  public void valueOutOfOperandsShouldThrowAnError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.malformed");
    assertThat(engine.evaluate("1 (+ (1) (2))  "), is("1 (+ (1) (2))"));
  }

  @Test
  public void missingClosingParenthesisIntoValueShouldThrowError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.operand.parentheses.missing.close");
    engine.evaluate("( c(def )");
  }

  @Test
  public void escapedOpeningParenthesisShouldBeTakenIntoAccountAsValue() {
    assertThat(engine.evaluate("( c\\(def )"), is("c(def"));
  }

  @Test
  public void missingOpeningParenthesisIntoValueShouldThrowError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.operand.parentheses.missing.open");
    engine.evaluate("( cde)f )");
  }

  @Test
  public void escapeEscapeCharacterShouldKeepEscapeCharacter() {
    assertThat(engine.evaluate("( cde\\\\f )"), is("cde\\\\f"));
  }

  @Test
  public void escapedClosingParenthesisShouldBeTakenIntoAccountAsValue() {
    assertThat(engine.evaluate("( cde\\)f )"), is("cde)f"));
  }

  @Test
  public void operatorWithoutOperandIsPartOfValue() {
    assertThat(engine.evaluate("+ 1 2"), is("+ 1 2"));
    assertThat(engine.evaluate("(+ 1 2)"), is("+ 1 2"));
  }

  @Test
  public void spaceBetweenLettersShouldBetakenIntoAccount() {
    assertThat(engine.evaluate("1 1"), is("1 1"));
  }

  @Test
  public void escapedCharacterShouldBeTakenIntoAccount() {
    assertThat(engine.evaluate("+ ( ab ) ( cd )"), is("abcd"));
    assertThat(engine.evaluate("+ ( a + b ) ( cd )"), is("a + bcd"));
    assertThat(engine.evaluate("concat ( ab ) ( cd )"), is("abcd"));
    assertThat(engine.evaluate("concat ( a b ) ( cd )"), is("a bcd"));
    assertThat(engine.evaluate("concat( + ( a )( b ) ) ( cd )"), is("abcd"));
    assertThat(engine.evaluate("concat( + ( a )( b ) ) ( c+\\(de\\)f )"), is("abc+(de)f"));
  }

  @Test
  public void combineStringWithNonEscapedParenthesisIntoOperandShouldThrowError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.malformed");
    engine.evaluate("concat( + ( a )( b ) ) ( c(de)f )");
  }

  @Test
  public void combineStringPartShouldWork() {
    assertThat(engine.evaluate("+ ( ab ) ( cd )"), is("abcd"));
    assertThat(engine.evaluate("+ ( a b ) ( cd )"), is("a bcd"));
    assertThat(engine.evaluate("concat ( ab ) ( cd )"), is("abcd"));
    assertThat(engine.evaluate("concat( a b ) ( cd )"), is("a bcd"));
    assertThat(engine.evaluate("concat( + a + b ) ( cd )"), is("+ a + bcd"));
  }
}