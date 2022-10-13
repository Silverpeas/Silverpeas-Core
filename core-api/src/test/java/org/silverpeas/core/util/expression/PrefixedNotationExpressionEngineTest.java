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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.expression;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.silverpeas.core.util.StringUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author Yohann Chastagnier
 */
public class PrefixedNotationExpressionEngineTest {

  private static PrefixedNotationExpressionEngine.OperatorFunction<Integer> add =
      new PrefixedNotationExpressionEngine.OperatorFunction<>("+",
          (initial, value) -> initial == null ? value : (initial + value));

  private static PrefixedNotationExpressionEngine.OperatorFunction<Integer> subtract =
      new PrefixedNotationExpressionEngine.OperatorFunction<>("-",
          (initial, value) -> initial == null ? value : (initial - value));

  private static PrefixedNotationExpressionEngine.OperatorFunction<Integer> multiply =
      new PrefixedNotationExpressionEngine.OperatorFunction<>("multiply",
          (initial, value) -> initial == null ? value : (initial * value));

  @SuppressWarnings("unchecked")
  private final static PrefixedNotationExpressionEngine<Integer> engine =
      PrefixedNotationExpressionEngine.from(exprPart -> {
        if (StringUtil.isNotDefined(exprPart)) {
          return null;
        }
        return Integer.parseInt(StringUtil.defaultStringIfNotDefined(exprPart, "0"));
      }, add, subtract, multiply);

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void simpleExpression() {
    assertThat(engine.evaluate(""), nullValue());
    assertThat(engine.evaluate("1"), is(1));
    assertThat(engine.evaluate("(3)"), is(3));
    assertThat(engine.evaluate(" 1"), is(1));
    assertThat(engine.evaluate("1 "), is(1));
    assertThat(engine.evaluate(" 1 "), is(1));
    assertThat(engine.evaluate(" (1)"), is(1));
    assertThat(engine.evaluate("(1) "), is(1));
    assertThat(engine.evaluate(" (1) "), is(1));
    assertThat(engine.evaluate(" ( 1)"), is(1));
    assertThat(engine.evaluate("(1 ) "), is(1));
    assertThat(engine.evaluate(" ( 1 ) "), is(1));
  }

  @Test(expected = NullPointerException.class)
  public void nullExpressionShouldThrowError() {
    engine.evaluate(null);
  }

  @Test(expected = NumberFormatException.class)
  public void nonIntegerCharacterShouldThrowErrorInContextOfThisTestClass() {
    engine.evaluate("1 1");
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
  public void parenthesisIntoValueShouldThrowAnError() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("expression.operation.malformed");
    engine.evaluate("1 (+ (1) (2))  ");
  }

  @Test(expected = NumberFormatException.class)
  public void addTwoValuesWithoutParenthesisShouldThrowError() {
    engine.evaluate("+ 1 2");
  }

  @Test(expected = NumberFormatException.class)
  public void addTwoValuesByMissingInternalParenthesisShouldThrowError() {
    engine.evaluate("(+ 1 2)");
  }

  @Test
  public void usingOperatorForSimpleValueShouldBeTakenASPartOfTheValueAndNotAsAnOperator() {
    assertThat(engine.evaluate("-7"), is(-7));
  }

  @Test
  public void addTwoValuesShouldWork() {
    assertThat(engine.evaluate("+(1)(2)"), is(3));
    assertThat(engine.evaluate("(+(1)(2))"), is(3));
    assertThat(engine.evaluate("  ((  +  ((  1  ) )  (  2  )   ) )  "), is(3));
  }

  @Test
  public void subtractTwoValuesShouldWork() {
    assertThat(engine.evaluate("-(6)(2)"), is(4));
    assertThat(engine.evaluate("(-(7)(2))"), is(5));
    assertThat(engine.evaluate("(+(-7)(2))"), is(-5));
  }


  @Test
  public void multiplyTwoValuesShouldWork() {
    assertThat(engine.evaluate("multiply(6)(2)"), is(12));
  }

  @Test
  public void addSubtractMultiplyValuesWithSubOperationShouldWork() {
    assertThat(engine.evaluate("+(+(3)(4))(-(2)(5))"), is(4));
    assertThat(engine.evaluate("+(+(3)(4))(+(-(5))(2))"), is(14));
    assertThat(engine.evaluate("+(+(3)(4))(+(-5)(2))"), is(4));
    assertThat(engine.evaluate("multiply(+(+(3)(4))(+(-(5))(2)))(10)"), is(140));
    assertThat(engine.evaluate("multiply(+(+(3)(4))(+(-5)(2)))(10)"), is(40));
    assertThat(engine.evaluate("-(+(+(3)(4))(multiply(-5)(2)))(10)"), is(-13));
    assertThat(engine.evaluate("+(+(+(3)(4))(multiply(-5)(2)))(10)"), is(7));
    assertThat(engine.evaluate("+(+(+(3)(4))(multiply(5)(2)))(10)"), is(27));
    assertThat(engine.evaluate("-(+(3)(4))(+(-2)(4))"), is(5));
  }
}