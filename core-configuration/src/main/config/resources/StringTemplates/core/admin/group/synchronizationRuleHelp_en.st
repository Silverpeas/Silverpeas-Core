$synchronizationRuleHelp_common()$
<div class="synchronizationRuleHelp">
  This rule is defined by writing a <b><a class="prefixedNotationHelp" href="#">Prefixed
  Notation</a></b> expression with parentheses.<br/>
  It permits to combine the following ground rules:
  <ul>
    <li><b>DS_AccessLevel = <i>[*, A, D, K, U ou G]</i></b>
      targets accounts which have the specified access level:<br/>
      <b>*</b> = all accounts, <b>A</b> = all administrator accounts,<br/>
      <b>D</b> = all domain manager accounts, <b>K</b> = all taxonomy manager account,<br/>
      <b>U</b> = all user accounts, <b>G</b> = all <i>guest</i> account
    </li>
    <li><b>DS_Domains = <i>[domain identifiers separated by comma]</i></b>
      targets all accounts registered into the specified domains<br/>
      (using it only for groups created into the <i>shared domain</i>)
    </li>
    <li><b>DC_<i>[property]</i> = <i>[value]</i></b>
      targets the accounts which have the value of specified property equal to the specified value.<br/>
      <b>[value]</b> can contain one or more occurrences of the special character
      <b>%</b>. It allows to indicate no character or any sequence of characters
    </li>
    <li><b>DR_Groups = <i>[group identifiers separated by comma]</i></b>
      targets the accounts which are directly referenced by the specified groups (accounts
      referenced by sub groups are ignored)
    </li>
    <li><b>DR_GroupsWithSubGroups = <i>[group identifiers separated by comma]</i></b>
      targets the accounts which are referenced by the specified groups or referenced by sub groups
      of specified groups
    </li>
  </ul>
  The rule can be directly a ground one, for example:<br/>
  <span class="example">DS_AccessLevel = A</span> targets all administrator accounts.
  <br/><br/>
  Use the expression operators in order to combine several ground rules:
  <ul>
    <li><b>&</b>: intersection between targeted accounts of each of operands</li>
    <li><b>|</b>: union between targeted accounts of each of operands</li>
    <li><b>!</b>: all accounts of the platform without those targeted by the operand. For this
      operator it is not possible to have several operands
    </li>
  </ul>
  Some examples:
  <ul>
    <li><span class="example">&(|(DS_AccessLevel=A)(DS_AccessLevel=U))(DC_company=Silverpeas)</span>:
      targets the administrator and user accounts which have the value of 'company' property
      matching 'Silverpeas'
    </li>
    <li>
      <span class="example">!(&(|(DS_AccessLevel=A)(DS_AccessLevel=U))(DC_company=Silverpeas))</span>:
      the negation of the previous rule. So, it targets all platform accounts without those targeted
      by the previous rule
    </li>
    <li>
      <span class="example">&(|(DS_AccessLevel=A)(DS_AccessLevel=U))(!(DC_company=Silverpeas))</span>:
      targets the administrator and user accounts which have the value of 'company' property not
      matching 'Silverpeas'
    </li>
    <li>
      etc.
    </li>
  </ul>
</div>