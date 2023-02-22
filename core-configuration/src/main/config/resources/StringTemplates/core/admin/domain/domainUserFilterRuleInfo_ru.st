$domainUserFilterRuleInfo_common()$
<div class="domainUserFilterRuleHelp">
Это правило определяется записью выражения
<b><a class="prefixedNotationHelp" href="#">Обозначение</a></b> с префиксом в скобках.<br/>
<p>
$if(domainGoogle)$
Он позволяет применять комбинацию правил к учетным записям пользователей Google.<br/>
Каждый пользователь представлен структурой JSON в биржах, как подробно
<b><a href="https://developers.google.com/admin-sdk/directory/v1/reference/users" target="_blank">здесь</a></b> .
$endif$
</p>
Правила следующие:
<div class="precision" style="margin-top:3px"><b>[атрибут ресурса]</b> представляет собой полный путь к структуре JSON.</div>
<div class="precision"><b>[атрибут ресурса]</b> чувствителен к регистру, а <b>[строковое значение]</b> — нет.</div>
<ul>
<li><b>[атрибут ресурса] = <i>[значение в строке символов]</i></b>
нацелены на учетные записи, значение атрибута которых равно указанному значению
</li>
<li><b>[значение в строке символов]</b> может содержать одно или несколько вхождений специального символа
<b>%</b> . Позволяет указать ни один символ или любую последовательность символов
</li>
</ul>
<p>
<b>Основное правило может быть заполнено напрямую</b> , например:
<ul>
$if(domainGoogle)$
<li><span class="code">orgUnitPath = /SIEGE</span> для таргетинга на учетные записи пользователей, организационное подразделение которых точно <span class="code">/SIEGE</span> .</li>
<li><span class="code">orgUnitPath = /SIEGE%</span> для целевых учетных записей пользователей, организационное подразделение которых начинается с <span class="code">/SIEGE</span> .</li>
<li><span class="code">name.familyName = Cha%</span> для целевых учетных записей пользователей, имя которых начинается с <span class="code">Cha</span> .</li>
$endif$
</ul>
</p>
<p>
Чтобы <b>совместить между собой несколько основных правил</b> , необходимо использовать операторы
доступный:
<ul>
<li><b>&amp;</b> : пересечение целевых счетчиков каждого из операндов</li>
<li><b>|</b> : объединение целевых счетчиков каждого из операндов</li>
<li><b>!</b> : все учетные записи платформы без целевых в операнде. Он не
может иметь для этого оператора только один операнд
</li>
</ul>
</p>
<p>
Когда атрибут относится к <b>данным в виде списка</b> , необходимо использовать
<span class="code"><b>[</b></span> и <span class="code"><b>]</b></span> для представления
и укажите между этими двумя символами правило или комбинацию правил.<br/>
Например :
<ul>
$if(domainGoogle)$
<li><span class="code">адреса[locality=GRENOBLE]</span> для целевых учетных записей пользователей
чья местность одного из адресов <span class="code">ГРЕНОБЛЬ</span> .</li>
<li><span class="code">электронные письма [|(address=%.com)(address=%.org)]</span> для целевых учетных записей
пользователи, у которых один из адресов электронной почты заканчивается на <span class="code">.com</span> или
<span class="code">.орг</span> .</li>
$endif$
</ul>
</p>
<p>
Некоторые примеры комбинаций правил:
<ul>
$if(domainGoogle)$
<li><span class="code">&amp;(|(orgUnitPath=/ГОЛОВНОЙ ОФИС%)(orgUnitPath=/AGENCIES%))(!(customSchemas.flags.technical=True))</span>
: нацелен на учетные записи, организационное подразделение которых начинается с <span class="code">/SIEGE.</span>
или <span class="code">/АГЕНТСТВА</span> и которые не являются техническими учетными записями
<div class="precision"><span class="code">customSchemas.flags.technical</span> — это атрибут
не навязано Google, а администраторами учетной записи Google G-Suite
<a href="https://developers.google.com/admin-sdk/directory/v1/reference/users#customSchemas" target="_blank">(см. документ)</a></div>
</li>
<li><span class="code">&amp;(адреса[|(locality=GRENOBLE)(postalCode=38000)])(isAdmin=false)</span>
: нацелены на учетные записи, для которых один из почтовых адресов содержит <span class="code">GRENOBLE</span> для города
или <span class="code">38000</span> для почтового индекса и которые не являются учетными записями администратора
</li>
$endif$
<li>
и т. д.
</li>
</ul>
</p>
</div>
