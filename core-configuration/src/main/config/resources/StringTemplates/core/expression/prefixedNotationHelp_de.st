<div>
  <p>A prefixed notation expression with parentheses permits to use operators associated with a
    variable number of operands.</p>
  <p>Parentheses <u>must</u> wrap an operand.<br/>
    An operator with its operands <u>can be</u> wrapped by parentheses.</p>
  <p>Use '\' character to escape '(' or ')' character when both of them do not define an
    operand.</p>
</div>
<hr style="border-color: #ECECEC; color: #ECECEC"/>
<div style="font-style: italic">
  <p>Example : the aim of the expression here is to evaluate a simple mathematical operation.</p>
  The operators could be the following ones:
  <ul>
    <li><b>+</b> : addition operator</li>
    <li><b>-</b> : subtraction operator</li>
  </ul>
  The following expressions could be evaluated:
  <ul>
    <li><b>+(1)(2)(5)</b> returns 8, the sum of 1, 2 and 5</li>
    <li><b>-(+(3)(4))(+(-2)(4))</b> returns 5, the subtraction between the sum of 3 and 4, and the
      sum of -2 and 4
    </li>
    <li><b>-(+(3)(4))(-(+(5)(6))(4))</b>...</li>
    <li>etc.</li>
  </ul>
</div>