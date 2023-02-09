$domainUserFilterRuleInfo_common()$
<div class="domainUserFilterRuleHelp">
Esta regla se define escribiendo una expresión por
<b><a class="prefixedNotationHelp" href="#">Notación Prefijada</a></b> con paréntesis.<br/>
<p>
$if(domainGoogle)$
Le permite aplicar una combinación de reglas a las cuentas de usuario de Google.<br/>
Cada usuario está representado por una estructura JSON en los intercambios como se detalla
<b><a href="https://developers.google.com/admin-sdk/directory/v1/reference/users" target="_blank">aquí</a></b>
$endif$
</p>
Las reglas son las siguientes:
<div class="precision" style="margin-top:3px"><b>[atributo de recurso]</b> representa la ruta completa de la estructura JSON</div>
<div class="precision"><b>[atributo de recurso]</b> distingue entre mayúsculas y minúsculas y <b>[valor de cadena]</b> no lo es</div>
<ul>
<li><b>[atributo de recurso] = <i>[valor en cadena de caracteres]</i></b>
se dirige a cuentas cuyo valor de atributo es igual al valor especificado
</li>
<li><b>[valor en cadena de caracteres]</b> puede contener una o más apariciones del carácter especial
<b>%</b> . Permite indicar ningún carácter o cualquier secuencia de caracteres
</li>
</ul>
<p>
Una <b>regla básica se puede completar directamente</b> , por ejemplo:
<ul>
$if(domainGoogle)$
<li><span class="code">orgUnitPath = /SIEGE</span> para dirigirse a cuentas de usuario cuya unidad organizativa sea exactamente <span class="code">/SIEGE</span> .</li>
<li><span class="code">orgUnitPath = /SIEGE%</span> para orientar las cuentas de usuario cuya unidad organizativa comienza con <span class="code">/SIEGE</span> .</li>
<li><span class="code">name.familyName = Cha%</span> para orientar las cuentas de usuario cuyo nombre comienza con <span class="code">Cha</span> .</li>
$endif$
</ul>
</p>
<p>
Para <b>combinar varias reglas básicas</b> entre ellas, es necesario utilizar los operadores
disponible:
<ul>
<li><b>&amp;</b> : intersección entre los conteos objetivo de cada uno de los operandos</li>
<li><b>|</b> : unión entre los conteos objetivo de cada uno de los operandos</li>
<li><b>!</b> : todas las cuentas de la plataforma sin las objetivo en el operando. El no
puede tener, para este operador, sólo un operando
</li>
</ul>
</p>
<p>
Cuando el atributo se relaciona con <b>datos en forma de lista</b> , debe usar
<span class="code"><b>[</b></span> y <span class="code"><b>]</b></span> para representarlo
e indicar entre estos dos caracteres la regla o combinación de reglas.<br/>
Por ejemplo :
<ul>
$if(domainGoogle)$
<li><span class="code">direcciones [localidad = GRENOBLE]</span> para las cuentas de usuario de destino
cuya localidad de uno de los domicilios es <span class="code">GRENOBLE</span> .</li>
<li><span class="code">correos electrónicos [|(address=%.com)(address=%.org)]</span> a las cuentas de destino
usuarios que tienen uno de sus correos electrónicos terminados en <span class="code">.com</span> o
<span class="code">.org</span> .</li>
$endif$
</ul>
</p>
<p>
Algunos ejemplos de combinaciones de reglas:
<ul>
$if(domainGoogle)$
<li><span class="code">&amp;(|(orgUnitPath=/OFICINA CENTRAL%)(orgUnitPath=/AGENCIAS%))(!(customSchemas.flags.technical=True))</span>
: cuentas de destino cuya unidad organizativa comienza con <span class="code">/SIEGE</span>
o <span class="code">/AGENCIAS</span> y que no sean cuentas técnicas
<div class="precision"><span class="code">customSchemas.flags.technical</span> es un atributo
no impuesta por Google sino por los administradores de la cuenta de Google G-Suite
<a href="https://developers.google.com/admin-sdk/directory/v1/reference/users#customSchemas" target="_blank">(ver documento)</a></div>
</li>
<li><span class="code">&amp;(direcciones[|(localidad=GRENOBLE)(código postal=38000)])(isAdmin=false)</span>
: cuentas de destino para las cuales una de las direcciones postales contiene <span class="code">GRENOBLE</span> para la ciudad
o <span class="code">38000</span> para el código postal y que no sean cuentas de administrador
</li>
$endif$
<li>
etc.
</li>
</ul>
</p>
</div>
