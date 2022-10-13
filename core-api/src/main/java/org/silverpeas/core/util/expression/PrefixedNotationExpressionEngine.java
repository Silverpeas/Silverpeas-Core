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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This engine reads a prefixed notation expression in order to evaluate it.<br>
 * Each part of the expression is composed of an operator with one or several operands.<br>
 * Each operand must be wrapped into parentheses.<br>
 * An operand can be:
 * <ul>
 * <li><b>a value to evaluate</b> (by the converter given as first parameter of
 * {@link #from(Function, OperatorFunction[])}) method. The value to convert is detected
 * when it has no parenthesis</li>
 * <li><b>an operator with</b> one or several operands</li>
 * </ul>
 * Each operator must be defined by the caller and given to variable parameter of
 * {@link #from(Function, OperatorFunction[])} method.<br>
 * So, by calling {@link #from(Function, OperatorFunction[])} method, the caller defines the
 * behaviour of the operators of the expression to evaluate.<br>
 * An operator without operands means that the operator is part of the value to evaluate (by the
 * converter).
 * <p>
 * For example:<br>
 * <pre>
 *   PrefixedNotationExpressionEngine&lt;Integer&gt; engine = from(
 *     (aString) -&gt; aString == null ? 0 : Integer.parseInt(aString), // the converter
 *     new OperatorFunction&lt;Integer&gt;("+", (a,b) -&gt; (a == null ? 0 : a) + b), // ADD operator
 *     new OperatorFunction&lt;Integer&gt;("-", (a,b) -&gt; (a == null ? 0 : a) - b) // SUBTRACT operator
 *   )
 *
 *   engine.evaluate("+(+(3)(4))(+(-(5))(2))"); // gives 4
 *
 *   // Decomposed treatment:
 *   // +(7)(+(-(5))(2))
 *   // +(7)(+(-5)(2))
 *   // +(7)(-3)
 *   // 4
 *
 *   engine.evaluate("+(+(3)(4))(+(-5)(2))"); // gives also 4, here the minus character from '-5'
 *                                            // is taken into account as part of the value and not
 *                                            // as an operator
 * </pre>
 * <p>
 * Some errors can be thrown as {@link IllegalArgumentException} with message containing an error
 * key. It is free to the caller to use these keys.<br>
 * The errors:
 * <ul>
 * <li><b>expression.operation.malformed</b> 1 (1)</li>
 * <li><b>expression.operation.operator.none</b> (1)(2)</li>
 * <li><b>expression.operation.operand.parentheses.missing.open</b> +1)(2)</li>
 * <li><b>expression.operation.operand.parentheses.missing.close</b> +(1)(2"</li>
 * </ul>
 * @param <R> the type the data the evaluation must result.
 * @author Yohann Chastagnier
 */
public class PrefixedNotationExpressionEngine<R> {

  private final Function<String, R> converter;
  private final Map<String, OperatorFunction<R>> operatorFunctions = new HashMap<>();
  private final Pattern operandPattern;

  /**
   * Hidden constructor.
   */
  @SuppressWarnings("unchecked")
  private PrefixedNotationExpressionEngine(Function<String, R> converter,
      OperatorFunction<R>... operationFunctions) {
    this.converter = converter;
    StringBuilder operators = new StringBuilder();
    for (OperatorFunction<R> operationFunction : operationFunctions) {
      this.operatorFunctions.put(operationFunction.getOperator(), operationFunction);
      if (operators.length() > 0) {
        operators.append("|");
      }
      String operator = operationFunction.getOperator();
      for (int i = 0; i < operator.length(); i++) {
        operators.append("[").append(operator.charAt(i)).append("]");
      }
    }
    operandPattern = Pattern.compile("(?i)^\\s*(" + operators.toString() + ")?\\s*(.+)\\s*$");
  }

  /**
   * Initializes the instance.
   * @param <R> the type of the expression result.
   * @param converter a function that converts a String representation of a data to
   * the concrete type of that data.
   * @param operationFunctions instances of {@link OperatorFunction} where each one represents
   * an operator and its behavior.
   * @return the initialized instance.
   */
  @SuppressWarnings("unchecked")
  public static <R> PrefixedNotationExpressionEngine<R> from(Function<String, R> converter,
      OperatorFunction<R>... operationFunctions) {
    return new PrefixedNotationExpressionEngine<>(converter, operationFunctions);
  }

  /**
   * Evaluates the given expression.
   * @param expression the expression to evaluate.
   * @return the typed result.
   */
  public R evaluate(String expression) {
    return parse(expression);
  }

  /**
   * Parses recursively the given expression.
   * @param expression the expression to parse recursively.
   * @return the current typed result.
   */
  private R parse(final String expression) {
    R computed = converter.apply(null);
    Matcher matcher = operandPattern.matcher(expression);
    if (matcher.find()) {
      OperatorFunction<R> operator = operatorFunctions.get(matcher.group(1));
      String operands = matcher.group(2);
      List<String> operandsAsList = readOperands(operands);
      if (operandsAsList.isEmpty()) {
        // If no operand, then it is a simple value, even if an operator sign has been detected.
        operator = null;
      }
      if (operator != null) {
        for (String element : operandsAsList) {
          computed = operator.getFunction().apply(computed, parse(element));
        }
      } else {
        if (operandsAsList.size() > 1) {
          throw new IllegalArgumentException("expression.operation.operator.none");
        } else if (operandsAsList.size() == 1) {
          computed = parse(operandsAsList.get(0));
        } else {
          computed = converter.apply(escapeExpression(expression).trim());
        }
      }
    }
    return computed;
  }

  /**
   * Indicates if the given expression is contains an operator, and so, a potential expression to
   * evaluate.
   * @param expression the expression to verify.
   * @return true if the expression contains an operator, false otherwise.
   */
  public boolean detectOperator(final String expression) {
    Matcher matcher = operandPattern.matcher(expression != null ? expression : "");
    if (matcher.find()) {
      OperatorFunction<R> operator = operatorFunctions.get(matcher.group(1));
      if (operator != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * Reads the operands.
   * @param operation an operation.
   * @return a list of operands.
   */
  private List<String> readOperands(String operation) {
    List<String> operands = new ArrayList<>();
    StringBuilder operand = new StringBuilder();
    int nbOpening = 0;
    int i = 0;
    while (i < operation.length()) {
      char currentChar = operation.charAt(i);
      if (subexpression(operation, operand, nbOpening, i, currentChar)) {
        i += 2;
        continue;
      }
      switch (currentChar) {
        case '(':
          nbOpening++;
          processStartExpression(operand, nbOpening, currentChar);
          break;
        case ')':
          processEndExpression(operands, operand, nbOpening, currentChar);
          nbOpening--;
          break;
        default:
          processOperand(operands, operand, nbOpening, currentChar);
          break;
      }
      i++;
    }

    if (nbOpening > 0) {
      throw new IllegalArgumentException("expression.operation.operand.parentheses.missing.close");
    }

    return operands;
  }

  private boolean subexpression(String operation, final StringBuilder operand, final int nbOpening,
      final int index, final char currentChar) {
    if (currentChar == '\\' && (index + 1) < operation.length()) {
      char nextChar = operation.charAt(index + 1);
      if ('(' == nextChar || ')' == nextChar) {
        if (nbOpening > 0) {
          operand.append('\\');
        }
        operand.append(nextChar);
        return true;
      }
    }
    return false;
  }

  private void processOperand(final List<String> operands, final StringBuilder operand,
      final int nbOpening, final char currentChar) {
    if (currentChar == ' ' && nbOpening == 0 && operand.length() == 0) {
      return;
    }
    if (nbOpening == 0 && !operands.isEmpty()) {
      throw new IllegalArgumentException(
          "expression.operation.operand.parentheses.missing.open");
    }
    operand.append(currentChar);
  }

  private void processEndExpression(final List<String> operands, final StringBuilder operand,
      final int nbOpening, final char currentChar) {
    if (nbOpening == 0) {
      throw new IllegalArgumentException(
          "expression.operation.operand.parentheses.missing.open");
    } else if (nbOpening == 1) {
      operands.add(operand.toString());
      operand.setLength(0);
    } else {
      operand.append(currentChar);
    }
  }

  private void processStartExpression(final StringBuilder operand, final int nbOpening,
      final char currentChar) {
    if (nbOpening == 1) {
      if (operand.length() > 0) {
        throw new IllegalArgumentException("expression.operation.malformed");
      }
    } else {
      operand.append(currentChar);
    }
  }

  /**
   * Escape the given expression.
   * @param expression the expression to escape.
   * @return the escaped expression.
   */
  private String escapeExpression(String expression) {
    StringBuilder escapedExpression = new StringBuilder();
    for (int i = 0; i < expression.length(); i++) {
      char currentChar = expression.charAt(i);
      if (currentChar == '\\' && (i + 1) < expression.length()) {
        char nextChar = expression.charAt(i + 1);
        if ('(' == nextChar || ')' == nextChar) {
          continue;
        }
      }
      escapedExpression.append(currentChar);
    }

    return escapedExpression.toString();
  }

  /**
   * Defines an operator behavior.
   * @param <T> the concrete type of the argument the function accepts.
   */
  public static class OperatorFunction<T> {
    private final String operator;
    private final BiFunction<T, T, T> function;

    public OperatorFunction(final String operator, final BiFunction<T, T, T> function) {
      this.operator = operator;
      this.function = function;
    }

    public String getOperator() {
      return operator;
    }

    public BiFunction<T, T, T> getFunction() {
      return function;
    }
  }
}
