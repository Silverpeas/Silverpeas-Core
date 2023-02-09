$synchronizationRuleHelp_common()$
<div class="synchronizationRuleHelp">
Esta regla se define escribiendo una expresión por
<b><a class="prefixedNotationHelp" href="#">Notación Prefijada</a></b> con paréntesis.<br/>
Permite establecer una combinación entre las siguientes reglas básicas:
<ul>
<li><b>DS_AccessLevel = <i>[*, A, D, K, U o G]</i></b>
cuentas de destino con el nivel de acceso especificado:<br/>
<b>*</b> = todas las cuentas, <b>A</b> = cuentas de administrador,<br/>
<b>D</b> = cuentas de administrador de dominio, <b>K</b> = cuentas de administrador de plan
Clasificación,<br/>
<b>U</b> = cuentas de usuario, <b>G</b> = cuentas de <i>invitados</i>
</li>
<li><b>DS_Domains = <i>[identificadores de dominio separados por comas]</i></b>
apunta a cuentas registradas en dominios específicos<br/>
(solo funciona para grupos creados en el <i>dominio mixto</i> )
</li>
<li><b>DC_ <i>[propiedad]</i> = <i>[valor]</i></b>
cuentas de destino cuya propiedad especificada es igual al valor especificado.<br/>
<b>[valor]</b> puede contener una o más apariciones del carácter especial
<b>%</b> . Permite indicar ningún carácter o cualquier secuencia de caracteres
</li>
<li><b>DR_Groups = <i>[ID de grupo separados por comas]</i></b>
destina cuentas directamente vinculadas a uno de los grupos indicados (cuentas de
los subgrupos son ignorados)
</li>
<li><b>DR_GroupsWithSubGroups = <i>[identificadores de grupo separados por comas]</i></b>
cuentas de destino vinculadas a uno de los grupos indicados o a uno de los subgrupos de los grupos
indicado
</li>
</ul>
Una regla básica se puede introducir directamente, por ejemplo:<br/>
<span class="example">DS_AccessLevel = A</span> para apuntar a todas las cuentas de administrador.
<br/><br/>
Para combinar varias reglas básicas entre ellas, es necesario utilizar los operadores
disponible:
<ul>
<li><b>&amp;</b> : intersección entre los conteos objetivo de cada uno de los operandos</li>
<li><b>|</b> : unión entre los conteos objetivo de cada uno de los operandos</li>
<li><b>!</b> : todas las cuentas de la plataforma sin las objetivo en el operando. El no
puede tener, para este operador, sólo un operando
</li>
</ul>
Algunos ejemplos :
<ul>
<li><span class="example">&amp;(|(DS_AccessLevel=A)(DS_AccessLevel=U))(DC_company=Silverpeas)</span>
: se dirige a cuentas de administrador y cuentas de usuario cuyo valor asociado con el
propiedad &#39;compañía&#39; es igual a &#39;Silverpeas&#39;
</li>
<li>
<span class="example">!(&amp;(|(DS_AccessLevel=A)(DS_AccessLevel=U))(DC_company=Silverpeas))</span>
: la negación de la regla anterior. Por lo tanto, esto apunta a todas las cuentas en la plataforma.
que no están sujetos a la regla anterior
</li>
<li>
<span class="example">&amp;(|(DS_AccessLevel=A)(DS_AccessLevel=U))(!(DC_company=Silverpeas))</span>
: apunta a cuentas de administrador y cuentas de usuario cuyo valor asociado con el
La propiedad &#39;compañía&#39; no es igual a &#39;Silverpeas&#39;
</li>
<li>
etc.
</li>
</ul>
</div>
