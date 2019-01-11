$domainUserFilterRuleInfo_common()$
<div class="domainUserFilterRuleHelp">
  This rule is defined by writing a <b><a class="prefixedNotationHelp" href="#">Prefixed
    Notation</a></b> expression with parentheses.<br/>
  <p>
    $if(domainGoogle)$
      It permits to combine the ground rules on Google user accounts.<br/>
      During the exchanges between Silverpeas and external repository, users are represented
      by a JSON structure as described
      <b><a href="https://developers.google.com/admin-sdk/directory/v1/reference/users" target="_blank">here</a></b>.
    $endif$
    </p>
    The rules are the followings:
    <div class="precision" style="margin-top:3px"><b>[resource attribute]</b> represents the full path in JSON structure</div>
    <div class="precision"><b>[resource attribute]</b> is case sensitive, <b>[string value]</b> is not</div>
    <ul>
      <li><b>[resource attribute] = <i>[string value]</i></b>
        targets all accounts which the attribute value is equal to the specified value
      </li>
      <li><b>[string value]</b> can contain one or more occurrences of the special character
       <b>%</b>. It allows to indicate no character or any sequence of characters
      </li>
    </ul>
    <p>
    The rule can be directly a <b>ground rule</b>, for example:
    <ul>
      $if(domainGoogle)$
      <li><span class="code">orgUnitPath = /SIEGE</span> targets all user accounts which organizational unit is exactly equal to <span class="code">/SIEGE</span>.</li>
      <li><span class="code">orgUnitPath = /SIEGE%</span> targets all user accounts which organizational unit begins with <span class="code">/SIEGE</span>.</li>
      <li><span class="code">name.familyName = Cha%</span> targets all user accounts which the family name begins with <span class="code">Cha</span>.</li>
      $endif$
    </ul>
  </p>
  <p>
    Use the expression operators in order to <b>combine several ground rules</b>:
    <ul>
      <li><b>&</b>: intersection between targeted accounts of each of operands</li>
      <li><b>|</b>: union between targeted accounts of each of operands</li>
      <li><b>!</b>: all accounts of the platform without those targeted by the operand. For this
        operator it is not possible to have several operands
      </li>
    </ul>
  </p>
  <p>
    When attribute is about <b>data from a list</b>, then use
    <span class="code"><b>[</b></span> and <span class="code"><b>]</b></span> in order to represent it
    and fill between this two characters the ground rule or the combination of ground rules.<br/>
    For example :
    <ul>
      $if(domainGoogle)$
      <li><span class="code">addresses[locality=GRENOBLE]</span> targets all user accounts with
       the locality of an address is <span class="code">GRENOBLE</span>.</li>
      <li><span class="code">emails[|(address=%.com)(address%=.org)]</span> targets all user accounts
       with an e-mail that ends with <span class="code">.com</span> or
       <span class="code">.org</span>.</li>
      $endif$
    </ul>
  </p>
  <p>
    Some examples:
    <ul>
    $if(domainGoogle)$
      <li><span class="code">&(|(orgUnitPath=/SIEGE%)(orgUnitPath=/AGENCES%))(!(customSchemas.flags.technical=True))</span>
        : targets accounts whose organizational unit begins with <span class="code">/SIEGE</span>
         or <span class="code">/AGENCES</span> and which are not technical accounts
        <div class="precision"><span class="code">customSchemas.flags.technical</span> is not a Google's attribute
        but an attribute created by administrators of Google G-Suite's account
        <a href="https://developers.google.com/admin-sdk/directory/v1/reference/users#customSchemas" target="_blank">(cf. doc)</a></div>
      </li>
      <li><span class="code">&(addresses[|(locality=GRENOBLE)(postalCode=38000)])(isAdmin=false)</span>
        : targets accounts with a postal address that contains <span class="code">GRENOBLE</span> for the locality
         or <span class="code">38000</span> for the postal code and that are not administrator accounts
      </li>
    $endif$
      <li>
        etc.
      </li>
    </ul>
  </p>
</div>