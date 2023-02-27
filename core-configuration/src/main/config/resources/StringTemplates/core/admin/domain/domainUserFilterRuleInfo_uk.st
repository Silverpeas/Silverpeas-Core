$domainUserFilterRuleInfo_common()$
<div class="domainUserFilterRuleHelp">
Це правило визначається записом виразу
<b><a class="prefixedNotationHelp" href="#">Позначення з префіксом</a></b> у дужках.<br/>
<p>
$if(domainGoogle)$
Це дозволяє застосовувати комбінацію правил до облікових записів користувачів Google.<br/>
Кожен користувач представлений структурою JSON на біржах, як детально
<b><a href="https://developers.google.com/admin-sdk/directory/v1/reference/users" target="_blank">тут</a></b> .
$endif$
</p>
Правила такі:
<div class="precision" style="margin-top:3px"><b>[атрибут ресурсу]</b> представляє повний шлях до структури JSON</div>
<div class="precision"><b>[атрибут ресурсу]</b> чутливий до регістру, а <b>[рядкове значення]</b> – ні</div>
<ul>
<li><b>[атрибут ресурсу] = <i>[значення в рядку символів]</i></b>
націлює облікові записи, значення атрибута яких дорівнює вказаному значенню
</li>
<li><b>[значення в символьному рядку]</b> може містити один або кілька входжень спеціального символу
<b>%</b> . Це дозволяє вказувати жодного символу або будь-яку послідовність символів
</li>
</ul>
<p>
<b>Основне правило</b> можна заповнити безпосередньо, наприклад:
<ul>
$if(domainGoogle)$
<li><span class="code">orgUnitPath = /SIEGE</span> для націлювання на облікові записи користувачів, організаційним підрозділом яких є саме <span class="code">/SIEGE</span> .</li>
<li><span class="code">orgUnitPath = /SIEGE%</span> для націлювання на облікові записи користувачів, організаційний підрозділ яких починається з <span class="code">/SIEGE</span> .</li>
<li><span class="code">name.familyName = Cha%</span> для націлювання на облікові записи користувачів, ім’я яких починається на <span class="code">Cha</span> .</li>
$endif$
</ul>
</p>
<p>
Щоб <b>поєднати кілька основних правил</b> між собою, необхідно використовувати оператори
в наявності:
<ul>
<li><b>&amp;</b> : перетин між цільовими підрахунками кожного з операндів</li>
<li><b>|</b> : об&#39;єднання між цільовими підрахунками кожного з операндів</li>
<li><b>!</b> : усі облікові записи платформи без націлених в операнді. Він... ні
може мати для цього оператора лише один операнд
</li>
</ul>
</p>
<p>
Якщо атрибут відноситься до <b>даних у формі списку</b> , необхідно використовувати
<span class="code"><b>[</b></span> і <span class="code"><b>]</b></span> , щоб представляти його
і вкажіть між цими двома символами правило або комбінацію правил.<br/>
Наприклад :
<ul>
$if(domainGoogle)$
<li><span class="code">адреси [locality=GRENOBLE]</span> для цільових облікових записів користувачів
місцем розташування однієї з адрес якого є <span class="code">ГРЕНОБЛЬ</span> .</li>
<li><span class="code">електронні листи[|(address=%.com)(address=%.org)]</span> для цільових облікових записів
користувачів, у яких одна з електронних адрес закінчується на <span class="code">.com</span> або
<span class="code">.org</span> .</li>
$endif$
</ul>
</p>
<p>
Деякі приклади комбінацій правил:
<ul>
$if(domainGoogle)$
<li><span class="code">&amp;(|(orgUnitPath=/ГОЛОВНИЙ ОФІС%)(orgUnitPath=/AGENCIES%))(!(customSchemas.flags.technical=True))</span>
: призначено для облікових записів, організаційний підрозділ яких починається з <span class="code">/SIEGE</span>
або <span class="code">/AGENCIES</span> і які не є технічними обліковими записами
<div class="precision"><span class="code">customSchemas.flags.technical</span> є атрибутом
нав’язується не Google, а адміністраторами облікового запису Google G-Suite
<a href="https://developers.google.com/admin-sdk/directory/v1/reference/users#customSchemas" target="_blank">(див. документ)</a></div>
</li>
<li><span class="code">&amp;(адреси[|(місце=GRENOBLE)(postalCode=38000)])(isAdmin=false)</span>
: націлені облікові записи, одна з поштових адрес яких містить <span class="code">GRENOBLE</span> для міста
або <span class="code">38000</span> для поштового індексу та які не є обліковими записами адміністратора
</li>
$endif$
<li>
тощо
</li>
</ul>
</p>
</div>
