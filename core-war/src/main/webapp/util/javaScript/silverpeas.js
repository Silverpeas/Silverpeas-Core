/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* some web navigators (like IE < 9) doesn't support completely the javascript standard (ECMA) */

if (!Array.prototype.indexOf) {
  Object.defineProperty(Array.prototype, 'indexOf', {
    enumerable : false, value : function(elt /*, from*/) {
      var len = this.length >>> 0;

      var from = Number(arguments[1]) || 0;
      from = (from < 0) ? Math.ceil(from) : Math.floor(from);
      if (from < 0) {
        from += len;
      }

      for (; from < len; from++) {
        if (from in this && this[from] === elt) {
          return from;
        }
      }
      return -1;
    }
  });
}

if (!Array.prototype.addElement) {
  Object.defineProperty(Array.prototype, 'indexOfElement', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      var discriminator = arguments.length > 1 ? arguments[1] : undefined;
      var discLeft = discriminator, discRight = discriminator;
      var isPos = typeof discriminator === 'number';
      var isDisc = typeof discriminator === 'string';
      if (isDisc) {
        var discParts = discriminator.split('=', 2);
        if (discParts.length > 1) {
          discLeft = discParts[0];
          discRight = discParts[1];
        }
      }
      for (var i = 0; i < this.length; i++) {
        var element = this[i];
        if ((element === elt) || (isPos && discriminator === i) ||
            (isDisc && element[discLeft] === elt[discRight])) {
          return i;
        }
      }
      return -1;
    }
  });
  Object.defineProperty(Array.prototype, 'getElement', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      var index = this.indexOfElement.apply(this, arguments);
      if (index >= 0) {
        return this[index];
      }
      return undefined;
    }
  });
  Object.defineProperty(Array.prototype, 'addElement', {
    enumerable : false, value : function(elt) {
      this.push(elt);
    }
  });
  Object.defineProperty(Array.prototype, 'updateElement', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      var index = this.indexOfElement.apply(this, arguments);
      if (index >= 0) {
        this[index] = elt;
        return true;
      }
      return false;
    }
  });
  Object.defineProperty(Array.prototype, 'removeElement', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      var index = this.indexOfElement.apply(this, arguments);
      if (index >= 0) {
        this.splice(index, 1);
        return true;
      }
      return false;
    }
  });
  Object.defineProperty(Array.prototype, 'extractElementAttribute', {
    enumerable : false, value : function(attributeName, mapper) {
      var isMapper = typeof mapper === 'function';
      var attributeValues = [];
      for (var i = 0; i < this.length; i++) {
        var element = this[i];
        if (element) {
          var attributeValue = element[attributeName];
          if (typeof attributeValue !== 'undefined') {
            if (isMapper) {
              attributeValue = mapper.call(this, attributeValue);
            }
            attributeValues.push(attributeValue);
          }
        }
      }
      return attributeValues;
    }
  });
}

if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(str) {
    return this.indexOf(str) === 0;
  };
}

if (!String.prototype.endsWith) {
  String.prototype.endsWith = function(str) {
    var endIndex = this.indexOf(str) + str.length;
    return endIndex === this.length;
  };
}

if (!String.prototype.replaceAll) {
  String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
  };
}

if (!String.prototype.isDefined) {
  String.prototype.isDefined = function() {
    var withoutWhitespaces = this.replace(/[ \r\n\t]/g, '');
    return withoutWhitespaces.length > 0 && withoutWhitespaces !== 'null';
  };
}

if (!String.prototype.isNotDefined) {
  String.prototype.isNotDefined = function() {
    return !this.isDefined();
  };
}

if (!String.prototype.removeAccent) {
  (function() {
    var Latinise={};Latinise.latin_map={"Á":"A","Ă":"A","Ắ":"A","Ặ":"A","Ằ":"A","Ẳ":"A","Ẵ":"A","Ǎ":"A","Â":"A","Ấ":"A","Ậ":"A","Ầ":"A","Ẩ":"A","Ẫ":"A","Ä":"A","Ǟ":"A","Ȧ":"A","Ǡ":"A","Ạ":"A","Ȁ":"A","À":"A","Ả":"A","Ȃ":"A","Ā":"A","Ą":"A","Å":"A","Ǻ":"A","Ḁ":"A","Ⱥ":"A","Ã":"A","Ꜳ":"AA","Æ":"AE","Ǽ":"AE","Ǣ":"AE","Ꜵ":"AO","Ꜷ":"AU","Ꜹ":"AV","Ꜻ":"AV","Ꜽ":"AY","Ḃ":"B","Ḅ":"B","Ɓ":"B","Ḇ":"B","Ƀ":"B","Ƃ":"B","Ć":"C","Č":"C","Ç":"C","Ḉ":"C","Ĉ":"C","Ċ":"C","Ƈ":"C","Ȼ":"C","Ď":"D","Ḑ":"D","Ḓ":"D","Ḋ":"D","Ḍ":"D","Ɗ":"D","Ḏ":"D","ǲ":"D","ǅ":"D","Đ":"D","Ƌ":"D","Ǳ":"DZ","Ǆ":"DZ","É":"E","Ĕ":"E","Ě":"E","Ȩ":"E","Ḝ":"E","Ê":"E","Ế":"E","Ệ":"E","Ề":"E","Ể":"E","Ễ":"E","Ḙ":"E","Ë":"E","Ė":"E","Ẹ":"E","Ȅ":"E","È":"E","Ẻ":"E","Ȇ":"E","Ē":"E","Ḗ":"E","Ḕ":"E","Ę":"E","Ɇ":"E","Ẽ":"E","Ḛ":"E","Ꝫ":"ET","Ḟ":"F","Ƒ":"F","Ǵ":"G","Ğ":"G","Ǧ":"G","Ģ":"G","Ĝ":"G","Ġ":"G","Ɠ":"G","Ḡ":"G","Ǥ":"G","Ḫ":"H","Ȟ":"H","Ḩ":"H","Ĥ":"H","Ⱨ":"H","Ḧ":"H","Ḣ":"H","Ḥ":"H","Ħ":"H","Í":"I","Ĭ":"I","Ǐ":"I","Î":"I","Ï":"I","Ḯ":"I","İ":"I","Ị":"I","Ȉ":"I","Ì":"I","Ỉ":"I","Ȋ":"I","Ī":"I","Į":"I","Ɨ":"I","Ĩ":"I","Ḭ":"I","Ꝺ":"D","Ꝼ":"F","Ᵹ":"G","Ꞃ":"R","Ꞅ":"S","Ꞇ":"T","Ꝭ":"IS","Ĵ":"J","Ɉ":"J","Ḱ":"K","Ǩ":"K","Ķ":"K","Ⱪ":"K","Ꝃ":"K","Ḳ":"K","Ƙ":"K","Ḵ":"K","Ꝁ":"K","Ꝅ":"K","Ĺ":"L","Ƚ":"L","Ľ":"L","Ļ":"L","Ḽ":"L","Ḷ":"L","Ḹ":"L","Ⱡ":"L","Ꝉ":"L","Ḻ":"L","Ŀ":"L","Ɫ":"L","ǈ":"L","Ł":"L","Ǉ":"LJ","Ḿ":"M","Ṁ":"M","Ṃ":"M","Ɱ":"M","Ń":"N","Ň":"N","Ņ":"N","Ṋ":"N","Ṅ":"N","Ṇ":"N","Ǹ":"N","Ɲ":"N","Ṉ":"N","Ƞ":"N","ǋ":"N","Ñ":"N","Ǌ":"NJ","Ó":"O","Ŏ":"O","Ǒ":"O","Ô":"O","Ố":"O","Ộ":"O","Ồ":"O","Ổ":"O","Ỗ":"O","Ö":"O","Ȫ":"O","Ȯ":"O","Ȱ":"O","Ọ":"O","Ő":"O","Ȍ":"O","Ò":"O","Ỏ":"O","Ơ":"O","Ớ":"O","Ợ":"O","Ờ":"O","Ở":"O","Ỡ":"O","Ȏ":"O","Ꝋ":"O","Ꝍ":"O","Ō":"O","Ṓ":"O","Ṑ":"O","Ɵ":"O","Ǫ":"O","Ǭ":"O","Ø":"O","Ǿ":"O","Õ":"O","Ṍ":"O","Ṏ":"O","Ȭ":"O","Ƣ":"OI","Ꝏ":"OO","Ɛ":"E","Ɔ":"O","Ȣ":"OU","Ṕ":"P","Ṗ":"P","Ꝓ":"P","Ƥ":"P","Ꝕ":"P","Ᵽ":"P","Ꝑ":"P","Ꝙ":"Q","Ꝗ":"Q","Ŕ":"R","Ř":"R","Ŗ":"R","Ṙ":"R","Ṛ":"R","Ṝ":"R","Ȑ":"R","Ȓ":"R","Ṟ":"R","Ɍ":"R","Ɽ":"R","Ꜿ":"C","Ǝ":"E","Ś":"S","Ṥ":"S","Š":"S","Ṧ":"S","Ş":"S","Ŝ":"S","Ș":"S","Ṡ":"S","Ṣ":"S","Ṩ":"S","Ť":"T","Ţ":"T","Ṱ":"T","Ț":"T","Ⱦ":"T","Ṫ":"T","Ṭ":"T","Ƭ":"T","Ṯ":"T","Ʈ":"T","Ŧ":"T","Ɐ":"A","Ꞁ":"L","Ɯ":"M","Ʌ":"V","Ꜩ":"TZ","Ú":"U","Ŭ":"U","Ǔ":"U","Û":"U","Ṷ":"U","Ü":"U","Ǘ":"U","Ǚ":"U","Ǜ":"U","Ǖ":"U","Ṳ":"U","Ụ":"U","Ű":"U","Ȕ":"U","Ù":"U","Ủ":"U","Ư":"U","Ứ":"U","Ự":"U","Ừ":"U","Ử":"U","Ữ":"U","Ȗ":"U","Ū":"U","Ṻ":"U","Ų":"U","Ů":"U","Ũ":"U","Ṹ":"U","Ṵ":"U","Ꝟ":"V","Ṿ":"V","Ʋ":"V","Ṽ":"V","Ꝡ":"VY","Ẃ":"W","Ŵ":"W","Ẅ":"W","Ẇ":"W","Ẉ":"W","Ẁ":"W","Ⱳ":"W","Ẍ":"X","Ẋ":"X","Ý":"Y","Ŷ":"Y","Ÿ":"Y","Ẏ":"Y","Ỵ":"Y","Ỳ":"Y","Ƴ":"Y","Ỷ":"Y","Ỿ":"Y","Ȳ":"Y","Ɏ":"Y","Ỹ":"Y","Ź":"Z","Ž":"Z","Ẑ":"Z","Ⱬ":"Z","Ż":"Z","Ẓ":"Z","Ȥ":"Z","Ẕ":"Z","Ƶ":"Z","Ĳ":"IJ","Œ":"OE","ᴀ":"A","ᴁ":"AE","ʙ":"B","ᴃ":"B","ᴄ":"C","ᴅ":"D","ᴇ":"E","ꜰ":"F","ɢ":"G","ʛ":"G","ʜ":"H","ɪ":"I","ʁ":"R","ᴊ":"J","ᴋ":"K","ʟ":"L","ᴌ":"L","ᴍ":"M","ɴ":"N","ᴏ":"O","ɶ":"OE","ᴐ":"O","ᴕ":"OU","ᴘ":"P","ʀ":"R","ᴎ":"N","ᴙ":"R","ꜱ":"S","ᴛ":"T","ⱻ":"E","ᴚ":"R","ᴜ":"U","ᴠ":"V","ᴡ":"W","ʏ":"Y","ᴢ":"Z","á":"a","ă":"a","ắ":"a","ặ":"a","ằ":"a","ẳ":"a","ẵ":"a","ǎ":"a","â":"a","ấ":"a","ậ":"a","ầ":"a","ẩ":"a","ẫ":"a","ä":"a","ǟ":"a","ȧ":"a","ǡ":"a","ạ":"a","ȁ":"a","à":"a","ả":"a","ȃ":"a","ā":"a","ą":"a","ᶏ":"a","ẚ":"a","å":"a","ǻ":"a","ḁ":"a","ⱥ":"a","ã":"a","ꜳ":"aa","æ":"ae","ǽ":"ae","ǣ":"ae","ꜵ":"ao","ꜷ":"au","ꜹ":"av","ꜻ":"av","ꜽ":"ay","ḃ":"b","ḅ":"b","ɓ":"b","ḇ":"b","ᵬ":"b","ᶀ":"b","ƀ":"b","ƃ":"b","ɵ":"o","ć":"c","č":"c","ç":"c","ḉ":"c","ĉ":"c","ɕ":"c","ċ":"c","ƈ":"c","ȼ":"c","ď":"d","ḑ":"d","ḓ":"d","ȡ":"d","ḋ":"d","ḍ":"d","ɗ":"d","ᶑ":"d","ḏ":"d","ᵭ":"d","ᶁ":"d","đ":"d","ɖ":"d","ƌ":"d","ı":"i","ȷ":"j","ɟ":"j","ʄ":"j","ǳ":"dz","ǆ":"dz","é":"e","ĕ":"e","ě":"e","ȩ":"e","ḝ":"e","ê":"e","ế":"e","ệ":"e","ề":"e","ể":"e","ễ":"e","ḙ":"e","ë":"e","ė":"e","ẹ":"e","ȅ":"e","è":"e","ẻ":"e","ȇ":"e","ē":"e","ḗ":"e","ḕ":"e","ⱸ":"e","ę":"e","ᶒ":"e","ɇ":"e","ẽ":"e","ḛ":"e","ꝫ":"et","ḟ":"f","ƒ":"f","ᵮ":"f","ᶂ":"f","ǵ":"g","ğ":"g","ǧ":"g","ģ":"g","ĝ":"g","ġ":"g","ɠ":"g","ḡ":"g","ᶃ":"g","ǥ":"g","ḫ":"h","ȟ":"h","ḩ":"h","ĥ":"h","ⱨ":"h","ḧ":"h","ḣ":"h","ḥ":"h","ɦ":"h","ẖ":"h","ħ":"h","ƕ":"hv","í":"i","ĭ":"i","ǐ":"i","î":"i","ï":"i","ḯ":"i","ị":"i","ȉ":"i","ì":"i","ỉ":"i","ȋ":"i","ī":"i","į":"i","ᶖ":"i","ɨ":"i","ĩ":"i","ḭ":"i","ꝺ":"d","ꝼ":"f","ᵹ":"g","ꞃ":"r","ꞅ":"s","ꞇ":"t","ꝭ":"is","ǰ":"j","ĵ":"j","ʝ":"j","ɉ":"j","ḱ":"k","ǩ":"k","ķ":"k","ⱪ":"k","ꝃ":"k","ḳ":"k","ƙ":"k","ḵ":"k","ᶄ":"k","ꝁ":"k","ꝅ":"k","ĺ":"l","ƚ":"l","ɬ":"l","ľ":"l","ļ":"l","ḽ":"l","ȴ":"l","ḷ":"l","ḹ":"l","ⱡ":"l","ꝉ":"l","ḻ":"l","ŀ":"l","ɫ":"l","ᶅ":"l","ɭ":"l","ł":"l","ǉ":"lj","ſ":"s","ẜ":"s","ẛ":"s","ẝ":"s","ḿ":"m","ṁ":"m","ṃ":"m","ɱ":"m","ᵯ":"m","ᶆ":"m","ń":"n","ň":"n","ņ":"n","ṋ":"n","ȵ":"n","ṅ":"n","ṇ":"n","ǹ":"n","ɲ":"n","ṉ":"n","ƞ":"n","ᵰ":"n","ᶇ":"n","ɳ":"n","ñ":"n","ǌ":"nj","ó":"o","ŏ":"o","ǒ":"o","ô":"o","ố":"o","ộ":"o","ồ":"o","ổ":"o","ỗ":"o","ö":"o","ȫ":"o","ȯ":"o","ȱ":"o","ọ":"o","ő":"o","ȍ":"o","ò":"o","ỏ":"o","ơ":"o","ớ":"o","ợ":"o","ờ":"o","ở":"o","ỡ":"o","ȏ":"o","ꝋ":"o","ꝍ":"o","ⱺ":"o","ō":"o","ṓ":"o","ṑ":"o","ǫ":"o","ǭ":"o","ø":"o","ǿ":"o","õ":"o","ṍ":"o","ṏ":"o","ȭ":"o","ƣ":"oi","ꝏ":"oo","ɛ":"e","ᶓ":"e","ɔ":"o","ᶗ":"o","ȣ":"ou","ṕ":"p","ṗ":"p","ꝓ":"p","ƥ":"p","ᵱ":"p","ᶈ":"p","ꝕ":"p","ᵽ":"p","ꝑ":"p","ꝙ":"q","ʠ":"q","ɋ":"q","ꝗ":"q","ŕ":"r","ř":"r","ŗ":"r","ṙ":"r","ṛ":"r","ṝ":"r","ȑ":"r","ɾ":"r","ᵳ":"r","ȓ":"r","ṟ":"r","ɼ":"r","ᵲ":"r","ᶉ":"r","ɍ":"r","ɽ":"r","ↄ":"c","ꜿ":"c","ɘ":"e","ɿ":"r","ś":"s","ṥ":"s","š":"s","ṧ":"s","ş":"s","ŝ":"s","ș":"s","ṡ":"s","ṣ":"s","ṩ":"s","ʂ":"s","ᵴ":"s","ᶊ":"s","ȿ":"s","ɡ":"g","ᴑ":"o","ᴓ":"o","ᴝ":"u","ť":"t","ţ":"t","ṱ":"t","ț":"t","ȶ":"t","ẗ":"t","ⱦ":"t","ṫ":"t","ṭ":"t","ƭ":"t","ṯ":"t","ᵵ":"t","ƫ":"t","ʈ":"t","ŧ":"t","ᵺ":"th","ɐ":"a","ᴂ":"ae","ǝ":"e","ᵷ":"g","ɥ":"h","ʮ":"h","ʯ":"h","ᴉ":"i","ʞ":"k","ꞁ":"l","ɯ":"m","ɰ":"m","ᴔ":"oe","ɹ":"r","ɻ":"r","ɺ":"r","ⱹ":"r","ʇ":"t","ʌ":"v","ʍ":"w","ʎ":"y","ꜩ":"tz","ú":"u","ŭ":"u","ǔ":"u","û":"u","ṷ":"u","ü":"u","ǘ":"u","ǚ":"u","ǜ":"u","ǖ":"u","ṳ":"u","ụ":"u","ű":"u","ȕ":"u","ù":"u","ủ":"u","ư":"u","ứ":"u","ự":"u","ừ":"u","ử":"u","ữ":"u","ȗ":"u","ū":"u","ṻ":"u","ų":"u","ᶙ":"u","ů":"u","ũ":"u","ṹ":"u","ṵ":"u","ᵫ":"ue","ꝸ":"um","ⱴ":"v","ꝟ":"v","ṿ":"v","ʋ":"v","ᶌ":"v","ⱱ":"v","ṽ":"v","ꝡ":"vy","ẃ":"w","ŵ":"w","ẅ":"w","ẇ":"w","ẉ":"w","ẁ":"w","ⱳ":"w","ẘ":"w","ẍ":"x","ẋ":"x","ᶍ":"x","ý":"y","ŷ":"y","ÿ":"y","ẏ":"y","ỵ":"y","ỳ":"y","ƴ":"y","ỷ":"y","ỿ":"y","ȳ":"y","ẙ":"y","ɏ":"y","ỹ":"y","ź":"z","ž":"z","ẑ":"z","ʑ":"z","ⱬ":"z","ż":"z","ẓ":"z","ȥ":"z","ẕ":"z","ᵶ":"z","ᶎ":"z","ʐ":"z","ƶ":"z","ɀ":"z","ﬀ":"ff","ﬃ":"ffi","ﬄ":"ffl","ﬁ":"fi","ﬂ":"fl","ĳ":"ij","œ":"oe","ﬆ":"st","ₐ":"a","ₑ":"e","ᵢ":"i","ⱼ":"j","ₒ":"o","ᵣ":"r","ᵤ":"u","ᵥ":"v","ₓ":"x"};
    String.prototype.removeAccent = function() {
      return this.replace(/[^A-Za-z0-9\[\] ]/g, function(a) {
        return Latinise.latin_map[a] || a
      })
    };
  })()
}

if (!String.prototype.nbChars) {
  String.prototype.nbChars = function() {
    return this.split(/\n/).length + this.length;
  };
}

if (!String.prototype.unescapeHTML) {
  String.prototype.unescapeHTML = function() {
    var div = document.createElement("div");
    div.innerHTML = this;
    return div.innerText || div.textContent || '';
  };
}

if (!String.prototype.convertNewLineAsHtml) {
  String.prototype.convertNewLineAsHtml = function() {
    return this.replace(/\n/g, '<br/>');
  };
}

if (!String.prototype.noHTML) {
  String.prototype.noHTML = function() {
    return this
        .replace(/&/g, '&amp;')
        .replace(/>/g, '&gt;')
        .replace(/</g, '&lt;');
  };
}

if (!Number.prototype.roundDown) {
  Number.prototype.roundDown = function(digit) {
    if (digit || digit === 0) {
      var digitCoef = Math.pow(10, digit);
      var result = Math.floor(this * digitCoef);
      return result / digitCoef;
    }
    return this;
  };
}
if (!Number.prototype.roundHalfDown) {
  Number.prototype.roundHalfDown = function(digit) {
    if (digit || digit === 0) {
      var digitCoef = Math.pow(10, digit);
      var result = Math.floor(this * digitCoef);
      var half = Math.floor((this * (digitCoef * 10))) % 10;
      if (5 < half && half <= 9) {
        result++;
      }

      return result / digitCoef;
    }
    return this;
  };
}
if (!Number.prototype.roundHalfUp) {
  Number.prototype.roundHalfUp = function(digit) {
    if (digit || digit === 0) {
      var digitCoef = Math.pow(10, digit);
      var result = Math.floor(this * digitCoef);
      var half = Math.floor((this * (digitCoef * 10))) % 10;
      if (5 <= half && half <= 9) {
        result++;
      }

      return result / digitCoef;
    }
    return this;
  };
}
if (!Number.prototype.roundUp) {
  Number.prototype.roundUp = function(digit) {
    if (digit || digit === 0) {
      var digitCoef = Math.pow(10, digit);
      var result = Math.ceil(this * digitCoef);
      return result / digitCoef;
    }
    return this;
  };
}

if (!window.StringUtil) {
  window.StringUtil = new function() {
    var _self = this;
    this.isDefined = function(aString) {
      return typeof aString === 'string' && aString != null && aString.isDefined();
    };
    this.isNotDefined = function(aString) {
      return !_self.isDefined(aString);
    };
    this.removeAccent = function(aString) {
      return (typeof aString === 'string') ? aString.removeAccent() : aString;
    };
    this.nbChars = function(aString) {
      return (typeof aString === 'string') ? aString.nbChars() : 0;
    };
  };
}

if (!window.SilverpeasError) {
  window.SilverpeasError = new function() {
    var _self = this;
    var _errors = [];
    this.reset = function() {
      _errors = [];
    };
    this.add = function(message) {
      if (StringUtil.isDefined(message)) {
        _errors.push(message);
      }
      return _self;
    };
    this.existsAtLeastOne = function() {
      return _errors.length > 0;
    };
    this.show = function() {
      if (_self.existsAtLeastOne()) {
        var errorContainer = jQuery('<div>');
        for (var i = 0; i < _errors.length; i++) {
          jQuery('<div>').append(_errors[i]).appendTo(errorContainer);
        }
        jQuery.popup.error(errorContainer.html());
        _self.reset();
        return true;
      }
      return false;
    };
  };
}

function SP_openWindow(page, name, width, height, options) {
  var top = (screen.height - height) / 2;
  var left = (screen.width - width) / 2;
  if (screen.height - 20 <= height) {
    top = 0;
  }
  if (screen.width - 10 <= width) {
    left = 0;
  }
  var features = "top=" + top + ",left=" + left + ",width=" + width + ",height=" + height + "," +
      options;
  if (typeof page === 'object') {
    var pageOptions = extendsObject({
      "params" : ''
    }, page);
    if (typeof pageOptions.params === 'string') {
      return window.open(pageOptions.url + pageOptions.params, name, features);
    }
    var selector = "form[target=" + name + "]";
    var form = document.querySelector(selector);
    if (!form) {
      form = document.createElement('form');
      var formContainer = document.createElement('div');
      formContainer.style.display = 'none';
      formContainer.appendChild(form);
      document.body.appendChild(formContainer);
    }
    var actionUrl = pageOptions.url;
    var pivotIndex = actionUrl.indexOf("?");
    if (pivotIndex > 0) {
      var splitParams = actionUrl.substring(pivotIndex + 1).split("&");
      actionUrl = actionUrl.substring(0, pivotIndex);
      splitParams.forEach(function(param) {
        var splitParam = param.split("=");
        if (splitParam.length === 2) {
          var key = splitParam[0];
          var value = splitParam[1];
          pageOptions.params[key] = value;
        }
      });
    }
    form.setAttribute('action', actionUrl);
    form.setAttribute('method', 'post');
    form.setAttribute('target', name);
    form.innerHTML = '';
    applyTokenSecurity(form.parentNode);
    for (var paramKey in pageOptions.params) {
      var paramValue = pageOptions.params[paramKey];
      var paramInput = document.createElement("input");
      paramInput.setAttribute("type", "hidden");
      paramInput.setAttribute("name", paramKey);
      paramInput.value = paramValue;
      form.appendChild(paramInput);
    }
    var spWindow = window.open('', name, features);
    form.submit();
    return spWindow;
  }
  return window.open(page, name, features);
}

function SP_openUserPanel(page, name, options) {
  return SP_openWindow(page, name, 800, 600, options);
}

/**
 * Resizing and positioning method.
 * If no resize is done, then no positioning is done.
 */
if (!window.currentPopupResize) {
  window.currentPopupResize = function() {
    var log = function(message) {
      //console.log("POPUP RESIZE - " + message);
    };
    var resize = function(context) {
      var $document = jQuery(document.body);
      $document.removeClass("popup-compute-finally");
      $document.addClass("popup-compute-settings");
      var widthOffset = window.outerWidth - $document.width();
      var heightOffset = window.outerHeight - window.innerHeight;
      $document.removeClass("popup-compute-settings");
      $document.addClass("popup-compute-finally");
      var limitH = 0;
      var scrollBarExistence = getWindowScrollBarExistence();
      if (scrollBarExistence.h) {
        // Scroll left exists, so scroll bar is displayed
        limitH = Math.max(document.body.scrollHeight, document.body.offsetHeight,
            document.documentElement.clientHeight, document.documentElement.scrollHeight,
            document.documentElement.offsetHeight);
      }
      var wWidthBefore = window.outerWidth;
      var wHeightBefore = window.outerHeight;
      var wWidth = Math.min((screen.width - 250), (widthOffset + 10 + $document.width() + limitH));
      var wHeight = Math.min((screen.height - 100), (heightOffset + 10 + $document.height()));

      // Setting if necessary new sizes and new position
      context.attempt += 1;
      if (context.attempt <= 1 &&
          (wWidthBefore !== wWidth || wHeightBefore !== wHeight || wHeight <= 200)) {
        log("modify attempt " + context.attempt);
        if (wHeight > 200) {
          log("resizeTo width = " + wWidth + ', height = ' + wHeight);
          context.effectiveResize += 1;
          window.resizeTo(wWidth, wHeight);
          var top = (screen.height - window.outerHeight) / 2;
          var left = (screen.width - window.outerWidth) / 2;
          if (!context.moveDone) {
            log("moveTo left = " + left + ', height = ' + top);
            context.moveDone = true;
            window.moveTo(left, top);
          }
        }
        window.setTimeout(function() {
          resize(context);
        }, 100);
      } else {
        if (context.effectiveResize > 1) {
          log("resize done");
        } else {
          log('wWidthBefore = ' + wWidthBefore + ", wWidth = " + wWidth + ', wHeightBefore = ' +
              wHeightBefore + ', wHeight = ' + wHeight);
          log("no resize performed");
        }
      }
    };
    whenSilverpeasReady(function() {
      window.setTimeout(function() {
        resize({attempt : 0, effectiveResize : 0});
      }, 0);
    });
  };
}

/**
 * Indicates the existence of Horizontal and Vertical scroll bar of Window.
 * @return {{h: boolean, v: boolean}}
 */
function getWindowScrollBarExistence() {
  var document = window.document, c = document.compatMode;
  var r = c && /CSS/.test(c) ? document.documentElement : document.body;
  if (typeof window.innerWidth == 'number') {
    // incredibly the next two lines serves equally to the scope
    // I prefer the first because it resembles more the feature
    // being detected by its functionality than by assumptions
    return {h : (window.innerHeight > r.clientHeight), v : (window.innerWidth > r.clientWidth)};
    //return {h : (window.innerWidth > r.clientWidth), v : (window.innerHeight > r.clientHeight)};
  } else {
    return {h : (r.scrollWidth > r.clientWidth), v : (r.scrollHeight > r.clientHeight)};
  }
}

/**
 * Gets the thickness size of Window ScrollBar.
 * @return {{h: number, v: number}}
 */
function getWindowScrollBarThicknessSize() {
  var document = window.document, body = document.body, r = {h : 0, v : 0}, t;
  if (body) {
    t = document.createElement('div');
    t.style.cssText =
        'position:absolute;overflow:scroll;top:-100px;left:-100px;width:100px;height:100px;';
    body.insertBefore(t, body.firstChild);
    r.h = t.offsetHeight - t.clientHeight;
    r.v = t.offsetWidth - t.clientWidth;
    body.removeChild(t);
  }
  return r;
}

if (!window.SilverpeasPluginBundle) {
  SilverpeasPluginBundle = function(bundle) {
    var translations = bundle ? bundle : {};
    this.get = function() {
      var key = arguments[0];
      var translation = translations[key];

      var paramIndex = 0;
      for (var i = 1; i < arguments.length; i++) {
        var params = arguments[i];
        if (params && typeof params === 'object' && params.length) {
          params.forEach(function(param) {
            translation =
                translation.replace(new RegExp('[{]' + (paramIndex++) + '[}]', 'g'), param);
          });
        } else if (params && typeof params !== 'object') {
          translation =
              translation.replace(new RegExp('[{]' + (paramIndex++) + '[}]', 'g'), params);
        }
      }
      return translation.replace(/[{][0-9]+[}]/g, '');
    };
  };
}

if (!window.SilverpeasPluginSettings) {
  SilverpeasPluginSettings = function(theSettings) {
    var settings = theSettings ? theSettings : {};
    this.get = function() {
      var key = arguments[0];
      return settings[key];
    };
  };
}

if (typeof extendsObject === 'undefined') {
  /**
   * Merge the contents of two or more objects together into the first object.
   * By default it performs a deep copy (recursion). To perform light copy (no recursion), please
   * give false as first argument. Giving true as first argument as no side effect and perform a
   * deep copy.
   * @returns {*}
   */
  function extendsObject() {
    var params = [];
    Array.prototype.push.apply(params, arguments);
    var firstArgumentType = params[0];
    if (typeof firstArgumentType === 'object') {
      params.splice(0, 0, true);
    } else if (typeof firstArgumentType === 'boolean' && !params[0]) {
      params.shift();
    }
    return jQuery.extend.apply(this, params);
  }
}

if (typeof SilverpeasClass === 'undefined') {
  window.SilverpeasClass = function() {
    this.initialize && this.initialize.apply(this, arguments);
  };
  SilverpeasClass.extend = function(childPrototype) {
    var parent = this;
    var child = function() {
      return parent.apply(this, arguments);
    };
    child.extend = parent.extend;
    var Surrogate = function() {};
    Surrogate.prototype = parent.prototype;
    child.prototype = new Surrogate();
    for (var prop in childPrototype) {
      var childProtoTypeValue = childPrototype[prop];
      var parentProtoTypeValue = parent.prototype[prop];
      if (typeof childProtoTypeValue !== 'function' || !parentProtoTypeValue) {
        child.prototype[prop] = childProtoTypeValue;
        continue;
      }
      child.prototype[prop] = (function(parentMethod, childMethod) {
        var _super = function() {
          return parentMethod.apply(this, arguments);
        };
        return function() {
          var __super = this._super, returnedValue;
          this._super = _super;
          returnedValue = childMethod.apply(this, arguments);
          this._super = __super;
          return returnedValue;
        };
      })(parentProtoTypeValue, childProtoTypeValue);
    }
    return child;
  };
}
if (!window.SilverpeasCache) {
  (function() {

    function __clearCache(storage, name) {
      storage.removeItem(name);
    }

    function __getCache(storage, name) {
      var cache = storage.getItem(name);
      if (!cache) {
        cache = {};
        __setCache(storage, name, cache);
      } else {
        cache = JSON.parse(cache);
      }
      return cache;
    }

    function __setCache(storage, name, cache) {
      storage.setItem(name, JSON.stringify(cache));
    }

    window.SilverpeasCache = SilverpeasClass.extend({
      initialize : function(cacheName) {
        this.cacheName = cacheName;
      },
      getCacheStorage : function() {
        return localStorage;
      },
      clear : function() {
        __clearCache(this.getCacheStorage(), this.cacheName);
      },
      put : function(key, value) {
        var cache = __getCache(this.getCacheStorage(), this.cacheName);
        cache[key] = value;
        __setCache(this.getCacheStorage(), this.cacheName, cache);
      },
      get : function(key) {
        var cache = __getCache(this.getCacheStorage(), this.cacheName);
        return cache[key];
      },
      remove : function(key) {
        var cache = __getCache(this.getCacheStorage(), this.cacheName);
        delete cache[key];
        __setCache(this.getCacheStorage(), this.cacheName, cache);
      }
    });

    window.SilverpeasSessionCache = SilverpeasCache.extend({
      getCacheStorage : function() {
        return sessionStorage;
      }
    });
  })();
}

if (!window.SilverpeasAjaxConfig) {
  SilverpeasRequestConfig = SilverpeasClass.extend({
    initialize : function(url) {
      this.url = url;
      this.method = 'GET';
      this.parameters = {};
    },
    withParams : function(params) {
      this.parameters = (params) ? params : {};
      return this;
    },
    withParam : function(name, value) {
      this.parameters[name] = value;
      return this;
    },
    addParam : function(name, value) {
      var currentValue = this.parameters[name];
      if (!currentValue) {
        this.withParam(name, value);
      } else {
        if (typeof currentValue === 'object') {
          currentValue.push(value);
        } else {
          this.parameters[name] = [currentValue, value];
        }
      }
      return this;
    },
    byPostMethod : function() {
      this.method = 'POST';
      return this;
    },
    getUrl : function() {
      return (this.method !== 'POST') ? sp.formatUrl(this.url, this.parameters) : this.url;
    },
    getMethod : function() {
      return this.method;
    },
    getParams : function() {
      return this.parameters;
    }
  });
  SilverpeasFormConfig = SilverpeasRequestConfig.extend({
    initialize : function(url) {
      this.target = '';
      var pivotIndex = url.indexOf("?");
      if (pivotIndex > 0) {
        var splitParams = url.substring(pivotIndex + 1).split("&");
        var urlWithoutParam = url.substring(0, pivotIndex);
        this._super(urlWithoutParam);
        splitParams.forEach(function(param) {
          var splitParam = param.split("=");
          if (splitParam.length === 2) {
            var key = splitParam[0];
            var value = splitParam[1];
            this.withParam(key, value);
          }
        }.bind(this));
      } else {
        this._super(url);
      }
    },
    getUrl : function() {
      return this.url;
    },
    toTarget : function(target) {
      this.target = (target) ? target : '';
      return this;
    },
    getTarget : function() {
      return this.target;
    },
    submit : function() {
      return silverpeasFormSubmit(this);
    }
  });
  SilverpeasAjaxConfig = SilverpeasRequestConfig.extend({
    initialize : function(url) {
      this._super(url);
      this.headers = {};
    },
    withHeaders : function(headerParams) {
      this.headers = (headerParams) ? headerParams : {};
      return this;
    },
    withHeader : function(name, value) {
      this.headers[name] = value;
      return this;
    },
    getHeaders : function() {
      return this.headers;
    },
    byPostMethod : function() {
      this.method = 'POST';
      return this;
    },
    byPutMethod : function() {
      this.method = 'PUT';
      return this;
    },
    byDeleteMethod : function() {
      this.method = 'DELETE';
      return this;
    },
    send : function(content) {
      if (this.method.startsWith('P')) {
        if (typeof content === 'object') {
          this.withHeader('Accept', 'application/json, text/plain, */*');
          this.withHeader('Content-Type', 'application/json; charset=UTF-8');
          this.withParams(JSON.stringify(content));
        } else if (content) {
          this.withHeader('Accept', 'application/json, text/plain, */*');
          this.withHeader('Content-Type', 'application/json; charset=UTF-8');
          this.withParams("" + content);
        }
      } else if (typeof content === 'object') {
        this.withParams(content)
      } else {
        sp.log.warning('SilverpeasAjaxConfig - content has to be send, but AJAX REQUEST is not well configured to perform this');
      }
      return silverpeasAjax(this);
    },
    sendAndPromiseJsonResponse : function(content) {
      return this.send(content).then(function(request) {
        return request.responseAsJson();
      });
    }
  });
}

if (typeof window.silverpeasAjax === 'undefined') {
  if (Object.getOwnPropertyNames) {
    XMLHttpRequest.prototype.responseAsJson = function() {
      return typeof this.response === 'string' ? JSON.parse(this.response) : this.response;
    }
  }
  function silverpeasAjax(options) {
    if (typeof options === 'string') {
      options = {url : options};
    }
    var params;
    if (typeof options.getUrl !== 'function') {
      params = extendsObject({"method" : "GET", url : '', headers : {}}, options);
    } else {
      var ajaxConfig = options;
      params = {
        url : ajaxConfig.getUrl(),
        method : ajaxConfig.getMethod(),
        headers : ajaxConfig.getHeaders()
      };
      if (ajaxConfig.getMethod().startsWith('P')) {
        params.data = ajaxConfig.getParams();
        if (!ajaxConfig.getHeaders()['Content-Type']) {
          if (typeof params.data === 'object') {
            var formData = new FormData();
            for (var key in params.data) {
              formData.append(key, params.data[key]);
            }
            params.data = formData;
          } else {
            params.data = "" + params.data;
            params.headers['Content-Type'] = 'text/plain; charset=UTF-8';
          }
        }
      }
    }
    if (params.method === 'GET') {
      params.headers['If-Modified-Since'] = 0;
    }
    return new Promise(function(resolve, reject) {

      if (Object.getOwnPropertyNames) {
        var xhr = new XMLHttpRequest();
        xhr.onload = function() {
          notySetupRequestComplete.call(this, xhr);
          if (xhr.status < 400) {
            resolve(xhr);
          } else {
            reject(xhr);
            console.log("HTTP request error: " + xhr.status);
          }
        };

        xhr.onerror = function() {
          reject(Error("Network error..."));
        };

        if (typeof params.onprogress === 'function') {
          xhr.upload.addEventListener("progress", params.onprogress, false);
        }

        xhr.open(params.method, params.url);
        var headerKeys = Object.getOwnPropertyNames(params.headers);
        for (var i = 0; i < headerKeys.length; i++) {
          var headerKey = headerKeys[i];
          xhr.setRequestHeader(headerKey, params.headers[headerKey]);
        }
        xhr.send(params.data);

      } else {

        // little trick for old browsers
        var options = {
          url : params.url,
          type : params.method,
          cache : false,
          success : function(data, status, jqXHR) {
            resolve({
              readyState : jqXHR.readyState,
              responseText : jqXHR.responseText,
              status : jqXHR.status,
              statusText : jqXHR.statusText,
              responseAsJson : function() {
                return typeof jqXHR.responseText === 'string' ? JSON.parse(jqXHR.responseText) : jqXHR.responseText;
              }
            });
          },
          error : function(jqXHR, textStatus, errorThrown) {
            reject(Error("Network error: " + errorThrown));
          }
        };

        // Adding settings
        if (params.data) {
          options.data = jQuery.toJSON(params.data);
          options.contentType = "application/json";
        }

        // Ajax request
        jQuery.ajax(options);
      }
    });
  }

  function silverpeasFormSubmit(silverpeasFormConfig) {
    if (!(silverpeasFormConfig instanceof SilverpeasFormConfig)) {
      sp.log.error(
          "silverpeasFormSubmit function need an instance of SilverpeasFormConfig as first parameter.");
      return;
    }
    if (!silverpeasFormConfig.getTarget()) {
      if (window.top.jQuery && window.top.jQuery.progressMessage) {
        window.top.jQuery.progressMessage();
      } else if (window.jQuery && window.jQuery.progressMessage) {
        window.jQuery.progressMessage();
      }
    }
    var selector = "form[target=silverpeasFormSubmit]";
    var form = document.querySelector(selector);
    if (!form) {
      form = document.createElement('form');
      var formContainer = document.createElement('div');
      formContainer.style.display = 'none';
      formContainer.appendChild(form);
      document.body.appendChild(formContainer);
    }
    form.setAttribute('action', silverpeasFormConfig.getUrl());
    form.setAttribute('method', silverpeasFormConfig.getMethod());
    form.setAttribute('target', silverpeasFormConfig.getTarget());
    form.innerHTML = '';
    applyTokenSecurity(form.parentNode);
    for (var paramKey in silverpeasFormConfig.getParams()) {
      var paramValue = silverpeasFormConfig.getParams()[paramKey];
      var paramInput = document.createElement("input");
      paramInput.setAttribute("type", "hidden");
      paramInput.setAttribute("name", paramKey);
      paramInput.value = paramValue;
      form.appendChild(paramInput);
    }
    form.submit();
  }
}

if (!window.SilverpeasContributionIdentifier) {
  SilverpeasContributionIdentifier = SilverpeasClass.extend({
    initialize : function(instanceId, type, localId) {
      this.instanceId = instanceId;
      this.type = type;
      this.localId = localId;
    },
    getComponentInstanceId : function() {
      return this.instanceId;
    },
    getType : function() {
      return this.type;
    },
    getLocalId : function() {
      return this.localId;
    },
    asString : function() {
      return this.instanceId + ':' + this.type + ':' + this.localId;
    },
    asBase64 : function() {
      return sp.base64.encode(this.instanceId + ':' + this.type + ':' + this.localId);
    },
    sameAs : function(other) {
      return (other instanceof SilverpeasContributionIdentifier)
          && this.componentInstanceId === other.getComponentInstanceId()
          && this.type === other.getType()
          && this.localId === other.getLocalId()
    }
  });
}

if(typeof window.whenSilverpeasReady === 'undefined') {
  var whenSilverpeasReadyPromise = false;
  function whenSilverpeasReady(callback) {
    if (!whenSilverpeasReadyPromise) {
      whenSilverpeasReadyPromise = Promise.resolve();
    }
    if (window.bindPolyfillDone) {
      jQuery(document).ready(function() {
        whenSilverpeasReadyPromise.then(function() {
          callback.call(this)
        }.bind(this));
      }.bind(this));
    } else {
      if (document.readyState !== 'interactive' &&
          document.readyState !== 'loaded' &&
          document.readyState !== 'complete') {
        document.addEventListener('DOMContentLoaded', function() {
          whenSilverpeasReadyPromise.then(function() {
            callback.call(this)
          }.bind(this));
        }.bind(this));
      } else {
        whenSilverpeasReadyPromise.then(function() {
          callback.call(this)
        }.bind(this));
      }
    }
  }

  /**
   * Applies a "ready" behaviour on the given instance.
   * After that it is possible to write :
   *    instance.ready(function() {
   *      ...
   *    });
   * Functions given to the ready method will be executed after the instance notifies its ready.
   * @param instance
   * @returns {Promise}
   */
  function applyReadyBehaviorOn(instance) {
    var promise = new Promise(function(resolve, reject) {
      this.notifyReady = resolve;
      this.notifyError = reject;
    }.bind(instance));
    instance.ready = function(callback) {
      promise.then(function() {
        callback.call(this);
      }.bind(instance));
    };
    return promise;
  }

  /**
   * Applies an event dispatching behaviour on the given instance.
   * After that, the instance exposes following methods :
   * - addEventListener(eventName, listener, listenerId) where listenerId permits to identify a
   * callback by an id instead of by its function instance.
   * - removeEventListener(eventName, listenerOrListenerId) where listenerOrListenerId permits to
   * aim the listener by its function instance or by an id given to addEventListener() method.
   * - dispatchEvent(eventName) : permits to the instance to dispatch an event when it is necessary
   * @param instance
   * @param options
   */
  function applyEventDispatchingBehaviorOn(instance, options) {
    var $document = window.document;
    var __id = $document['__sp_event_uuid'];
    if (typeof __id === 'undefined') {
      __id = 0;
    } else {
      __id = __id + 1;
    }
    $document['__sp_event_uuid'] = __id;
    var __normalizeEventName = function(eventName) {
      return "__sp_event_" + __id + "_" + eventName;
    };

    var __listeners = {};
    var __options = extendsObject({onAdd : false, onRemove : false}, options);

    instance.dispatchEvent = function(eventName, data) {
      var normalizedEventName = __normalizeEventName(eventName);
      $document.body.dispatchEvent(new CustomEvent(normalizedEventName, {
        detail : {
          from : this,
          data : data
        },
        bubbles : true,
        cancelable : true
      }));
    };
    instance.addEventListener = function(eventName, listener, listenerId) {
      if (listenerId) {
        instance.removeEventListener(eventName, listenerId);
        __listeners[listenerId] = listener;
      } else {
        instance.removeEventListener(eventName, listener);
      }

      var normalizedEventName = __normalizeEventName(eventName);
      $document.addEventListener(normalizedEventName, listener);
      if (typeof __options.onAdd === 'function') {
        __options.onAdd.call(instance, eventName, listener);
      }
    };
    instance.removeEventListener = function(eventName, listenerOrListenerId) {
      var oldListener;
      var listenerType = typeof listenerOrListenerId;
      if (listenerType === 'function') {
        oldListener = listenerOrListenerId;
      } else if (listenerType === 'string') {
        oldListener = __listeners[listenerOrListenerId];
        delete __listeners[listenerOrListenerId];
      }
      if (oldListener) {
        var normalizedEventName = __normalizeEventName(eventName);
        $document.removeEventListener(normalizedEventName, oldListener);
        if (typeof __options.onRemove === 'function') {
          __options.onRemove.call(instance, eventName, oldListener);
        }
      }
    };
  }
}

if (typeof window.sp === 'undefined') {
  var debug = true;
  window.sp = {
    log : {
      infoActivated : true,
      warningActivated : true,
      errorActivated : true,
      debugActivated : false,
      formatMessage : function() {
        var message = "";
        for (var i = 0; i < arguments.length; i++) {
          var item = arguments[i];
          if (typeof item !== 'string') {
            item = JSON.stringify(item);
          }
          if (i > 0) {
            message += " ";
          }
          message += item;
        }
        return message;
      },
      info : function() {
        if (sp.log.infoActivated) {
          console &&
          console.info('SP - INFO - ' + sp.log.formatMessage.apply(sp.log, arguments));
        }
      },
      warning : function() {
        if (sp.log.warningActivated) {
          console &&
          console.warn('SP - WARNING - ' + sp.log.formatMessage.apply(sp.log, arguments));
        }
      },
      error : function() {
        if (sp.log.errorActivated) {
          console &&
          console.error('SP - ERROR - ' + sp.log.formatMessage.apply(sp.log, arguments));
        }
      },
      debug : function() {
        if (sp.log.debugActivated) {
          console &&
          console.log('SP - DEBUG - ' + sp.log.formatMessage.apply(sp.log, arguments));
        }
      }
    },
    base64 : {
      encode : function(str) {
        return window.btoa(str);
      },
      decode : function(str) {
        return window.atob(str);
      }
    },
    promise : {
      deferred : function() {
        var deferred = {};
        deferred.promise = new Promise(function(resolve, reject){
          deferred.resolve = resolve;
          deferred.reject = reject;
        });
        return deferred;
      },
      isOne : function(object) {
        return object && typeof object.then === 'function';
      },
      whenAllResolved : function(promises) {
        return Promise.all(promises);
      },
      resolveDirectlyWith : function(data) {
        return Promise.resolve(data);
      },
      rejectDirectlyWith : function(data) {
        return Promise.reject(data);
      }
    },
    moment : {
      /**
       * Creates a new moment by taking of offset if any.
       * @param date
       * @param format
       */
      make : function(date, format) {
        if (typeof date === 'string' && !format) {
          return date.length === 10 ? moment(date, 'YYYY-MM-DD') : moment.parseZone(date);
        }
        return moment.apply(undefined, arguments);
      },
      /**
       * Formats the given date as ISO Java string.
       * @param date the date as ISO string
       */
      toISOJavaString : function(date) {
        if (typeof date === 'string' && date.length === 10) {
          return date;
        }
        return sp.moment.make(date).toISOString().replace(':00.000Z', 'Z');
      },
      /**
       * Gets the offset ('-01:00' for example) of the given zone id.
       * @param zoneId the zone id ('Europe/Berlin' for example)
       */
      getOffsetFromZoneId : function(zoneId) {
        return moment().tz(zoneId).format('Z');
      },
      /**
       * Sets the given date at the given timezone.
       * @param date a data like the one given to the moment constructor
       * @param zoneId the zone id ('Europe/Berlin' for example)
       */
      atZoneIdSameInstant : function(date, zoneId) {
        return sp.moment.make(date).tz(zoneId);
      },
      /**
       * Sets the given date at the given timezone without changing the time.
       * @param date a data like the one given to the moment constructor
       * @param zoneId the zone id ('Europe/Berlin' for example)
       */
      atZoneIdSimilarLocal : function(date, zoneId) {
        return sp.moment.make(date).utcOffset(sp.moment.getOffsetFromZoneId(zoneId), true);
      },
      /**
       * Adjusts the the time minutes in order to get a rounded time.
       * @param date a data like the one given to the moment constructor.
       * @param hasToCurrentTime true to set current time, false otherwise
       * @private
       */
      adjustTimeMinutes : function(date, hasToCurrentTime) {
        var myMoment = sp.moment.make(date);
        if (hasToCurrentTime) {
          var $timeToSet = moment();
          myMoment.hour($timeToSet.hour());
          myMoment.minute($timeToSet.minute());
        }
        var minutes = myMoment.minutes();
        var minutesToAdjust = minutes ? minutes % 10 : 0;
        var offset = minutesToAdjust < 5 ? 0 : 10;
        return myMoment.add((offset - minutesToAdjust), 'm');
      },
      /**
       * Gets the nth day of month from the given moment in order to display it as a date.
       * @param date a data like the one given to the moment constructor.
       * @private
       */
      nthDayOfMonth : function(date) {
        var dayInMonth = sp.moment.make(date).date();
        return Math.ceil(dayInMonth / 7);
      },
      /**
       * Formats the given moment in order to display it as a date.
       * @param date a data like the one given to the moment constructor.
       * @private
       */
      displayAsDayDate : function(date) {
        return sp.moment.make(date).format('LLLL').replaceAll(' [0-9]+:[0-9]+','');
      },
      /**
       * Formats the given moment in order to display it as a date.
       * @param date a data like the one given to the moment constructor.
       * @private
       */
      displayAsDate : function(date) {
        return sp.moment.make(date).format('L');
      },
      /**
       * Formats the given moment in order to display it as a time.
       * @param time a data like the one given to the moment constructor.
       * @private
       */
      displayAsTime : function(time) {
        return moment.parseZone(time).format('HH:mm');
      },
      /**
       * Formats the given moment in order to display it as a date time.
       * @param date a data like the one given to the moment constructor.
       * @private
       */
      displayAsDateTime : function(date) {
        return sp.moment.displayAsDate(date) + sp.moment.make(date).format('LT');
      },
      /**
       * Replaces from the given text date or date time which are specified into an ISO format.
       * Two kinds of replacement are performed :
       * - "${[ISO string date],date}" is replaced by a readable date
       * - "${[ISO string date],datetime}" is replaced by a readable date and time
       * @param text
       * @returns {*}
       */
      formatText : function(text) {
        var formattedText = text;
        var dateOrDateTimeRegExp = /\$\{([^,]+),date(time|)}/g;
        var match = dateOrDateTimeRegExp.exec(text);
        while (match) {
          var toReplace = match[0];
          var temporal = match[1];
          var isTime = match[2];
          if (isTime) {
            formattedText = formattedText.replace(toReplace, sp.moment.displayAsDateTime(temporal));
          } else {
            formattedText = formattedText.replace(toReplace, sp.moment.displayAsDate(temporal));
          }
          match = dateOrDateTimeRegExp.exec(text);
        }
        return formattedText;
      }
    },
    formRequest : function(url) {
      return new SilverpeasFormConfig(url);
    },
    ajaxRequest : function(url) {
      return new SilverpeasAjaxConfig(url);
    },
    formatUrl : function(url, params) {
      var paramPart = url.indexOf('?') > 0 ? '&' : '?';
      if (params) {
        for (var key in params) {
          var paramList = params[key];
          var typeOfParamList = typeof paramList;
          if (!paramList && typeOfParamList !== 'boolean') {
            continue;
          }
          if (typeOfParamList !== 'object') {
            paramList = [paramList];
          }
          if (paramPart.length > 1) {
            paramPart += '&';
          }
          paramPart += key + "=" + paramList.join("&" + key + "=");
        }
      }
      return url + (paramPart.length === 1 ? '' : paramPart);
    },
    load : function(targetOrArrayOfTargets, ajaxConfig, isGettingFullHtmlContent) {
      return silverpeasAjax(ajaxConfig).then(function(request) {
        return sp.updateTargetWithHtmlContent(targetOrArrayOfTargets, request.responseText, isGettingFullHtmlContent);
      }, function(request) {
        sp.log.error(request.status + " " + request.statusText);
      });
    },
    updateTargetWithHtmlContent : function(targetOrArrayOfTargets, html, isGettingFullHtmlContent) {
      var targetIsArrayOfCssSelector = typeof targetOrArrayOfTargets === 'object' && Array.isArray(targetOrArrayOfTargets);
      var targets = !targetIsArrayOfCssSelector ? [targetOrArrayOfTargets] : targetOrArrayOfTargets;
      targets.forEach(function(target) {
        var targetIsCssSelector = typeof target === 'string';
        if (!isGettingFullHtmlContent || !targetIsCssSelector) {
          jQuery(target).html(html);
        } else {
          var $container = jQuery('<div>');
          $container.html(html);
          var $content = jQuery(target, $container);
          jQuery(target).replaceWith($content);
        }
      });
      return sp.promise.resolveDirectlyWith(html);
    },
    navigation : {
      mute : function() {
        Mousetrap.pause();
      },
      unmute: function() {
        document.body.focus();
        Mousetrap.unpause();
      },
      previousNextOn : function(target, onPreviousOrNext) {
        Mousetrap.bind('left', function() {
          onPreviousOrNext(true);
        });
        Mousetrap.bind('right', function() {
          onPreviousOrNext(false);
        });
      }
    },
    element : {
      isInView: function (element, fullyInView, view) {
        var viewTop = !view ? jQuery(window).scrollTop() : jQuery(view).offset().top;
        var viewBottom = viewTop + jQuery(view).height();
        var elementTop = jQuery(element).offset().top;
        var elementBottom = elementTop + jQuery(element).height();

        if (fullyInView === true) {
          return ((viewTop < elementTop) && (viewBottom > elementBottom));
        } else {
          return ((elementTop <= viewBottom) && (elementBottom >= viewTop));
        }
      }
    },
    selection : {
      newCheckboxMonitor : function(cssSelector) {
        return new function() {
          var __shift = false;
          var __selectedAtStart = [];
          var __selected = [];
          var __unselected = [];
          var __init = function() {
            __selectedAtStart = [];
            __selected = [];
            __unselected = [];
            var checkboxes = document.querySelectorAll(cssSelector);
            [].slice.call(checkboxes, 0).forEach(function(checkbox) {
              if (checkbox.checked) {
                __selectedAtStart.addElement(checkbox.value);
              }
              checkbox.addEventListener('change', __handler);
            });
          };

          Mousetrap.bindGlobal('shift', function(e) {
            if (!__shift) {
              __shift = true;
            }
          });
          Mousetrap.bindGlobal('shift', function(e) {
            if (__shift) {
              __shift = false;
            }
          }, 'keyup');

          var __handler = function(e) {
            var checkboxReference = e.target;
            var checkboxesToHandle = [];
            if (__shift) {
              var checkboxes = [].slice.call(document.querySelectorAll(cssSelector), 0);
              for (var i = 0; i < checkboxes.length; i++) {
                var checkbox = checkboxes[i];
                if (checkbox === checkboxReference) {
                  break;
                }
                if (checkboxReference.checked === checkbox.checked) {
                  checkboxesToHandle = [];
                } else {
                  checkboxesToHandle.push(checkbox);
                }
              }
            }
            checkboxesToHandle.push(checkboxReference);
            checkboxesToHandle.forEach(function(checkbox) {
              checkbox.checked = checkboxReference.checked;
              if (checkbox.checked) {
                if (__selectedAtStart.indexOf(checkbox.value) < 0) {
                  __selected.addElement(checkbox.value);
                }
                __unselected.removeElement(checkbox.value);
              } else {
                if (__selectedAtStart.indexOf(checkbox.value) >= 0) {
                  __unselected.addElement(checkbox.value);
                }
                __selected.removeElement(checkbox.value);
              }
            });
          };
          this.pageChanged = function() {
            __init();
          };
          this.prepareAjaxRequest = function(ajaxRequest, options) {
            var params = extendsObject({
              clear : true,
              paramSelectedIds : 'selectedIds',
              paramUnselectedIds : 'unselectedIds'
            }, options);
            __selected.forEach(function(value) {
              ajaxRequest.addParam(params.paramSelectedIds, value);
            });
            __unselected.forEach(function(value) {
              ajaxRequest.addParam(params.paramUnselectedIds, value);
            });
            if (params.clear) {
              __init();
            }
          };
          __init();
        };
      }
    },
    arrayPane : {
      ajaxControls : function(containerCssSelector, options) {
        var __refreshFromRequestResponse = function(request) {
          return sp.updateTargetWithHtmlContent(containerCssSelector, request.responseText, true)
              .then(function() {
                window.top.spProgressMessage.hide();
              });
        };
        var params = {
          before : false,
          success : __refreshFromRequestResponse
        };
        if (typeof options === 'function') {
          params.success = options;
        } else if (typeof options === 'object') {
          params = extendsObject(params, options);
        }
        var $container = jQuery(containerCssSelector);
        var __ajaxRequest = function(url) {
          var ajaxConfig = sp.ajaxConfig(url);
          ajaxConfig.withParam("ajaxRequest", true);
          if (typeof params.before === 'function') {
            params.before(ajaxConfig);
          }
          return silverpeasAjax(ajaxConfig).then(function(request) {
            if (typeof params.success === 'function') {
              var result = params.success(request);
              if (sp.promise.isOne(result)) {
                return result;
              }
            }
            return sp.promise.resolveDirectlyWith();
          });
        };
        var __clickHandler = function(index, linkElement) {
          var url = linkElement.href;
          if (url && '#' !== url && !url.startsWith('javascript')) {
            linkElement.href = 'javascript:void(0)';
            linkElement.addEventListener('click', function() {
              __ajaxRequest(url);
            }, false);
          }
        };
        jQuery('thead a', $container).each(__clickHandler);
        jQuery('tfoot a', $container).each(__clickHandler);
        jQuery('.list-pane-nav a', $container).each(__clickHandler);
        jQuery('.pageJumper input', $container).each(function(index, jumperInput) {
          jumperInput.ajax = __ajaxRequest;
        });
        return {
          refreshFromRequestResponse : __refreshFromRequestResponse
        }
      }
    },
    volatileIdentifier : {
      newOn : function(componentInstanceId) {
        var url = webContext + '/services/volatile/' + componentInstanceId + '/new';
        return sp.ajaxRequest(url).send().then(function(request) {
          return request.responseText;
        });
      }
    },
    editor : {
      wysiwyg : {
        configFor : function(componentInstanceId, resourceType, resourceId, options) {
          var params = extendsObject({
            configName : undefined,
            height : undefined,
            width : undefined,
            language : undefined,
            toolbar : undefined,
            toolbarStartExpanded : undefined,
            fileBrowserDisplayed : undefined,
            stylesheet : undefined
          }, options);
          var url = webContext + '/services/wysiwyg/editor/' + componentInstanceId + '/' + resourceType + '/' + resourceId;
          return sp.ajaxRequest(url).withParams(params).sendAndPromiseJsonResponse();
        },
        promiseFirstEditorInstance : function() {
          var deferred = sp.promise.deferred();
          whenSilverpeasReady(function() {
            CKEDITOR.on('instanceReady', function() {
              var editor;
              for(var editorName in CKEDITOR.instances) {
                editor = CKEDITOR.instances[editorName];
                break;
              }
              deferred.resolve(editor);
            });
          });
          return deferred.promise;
        },
        promiseEditorInstanceById : function(id) {
          var deferred = sp.promise.deferred();
          whenSilverpeasReady(function() {
            CKEDITOR.on('instanceReady', function() {
              deferred.resolve(CKEDITOR.instances[id]);
            });
          });
          return deferred.promise;
        },
        fullScreenOnMaximize : function(editorIdOrName) {
          sp.editor.wysiwyg.promiseEditorInstanceById(editorIdOrName).then(function(editor) {
            editor.on('maximize', function() {
              var _fullscreen = spLayout.getBody().getContent().toggleFullscreen();
              spFscreen.onfullscreenchange(function() {
                if (_fullscreen && spFscreen.fullscreenElement() === null) {
                  editor.execCommand('maximize');
                }
              });
            });
          });
        },
        backupManager : function(options) {
          var instance = new function() {
            var params = extendsObject({
              componentInstanceId : undefined,
              resourceType : undefined,
              resourceId : undefined,
              unvalidatedContentCallback : undefined
            }, options);
            var _editor;
            var timer = 0;
            var dataOnLastClear;
            var cacheKey = 'sp.editor.wysiwyg.writingCacheHandler_' +
                currentUserId + '#' + params.componentInstanceId + '#' + params.resourceType +
                '#' + params.resourceId;
            cacheKey = cacheKey.replace(/[#](null|undefined)/g, '#');
            var cache = new SilverpeasCache(cacheKey);

            var __stash = function() {
              if (typeof dataOnLastClear === 'string' && dataOnLastClear === _editor.getData()) {
                dataOnLastClear = undefined;
                return;
              }
              cache.put("data", _editor.getData());
            }.bind(this);
            var __unStash = function() {
              if (this.existsUnvalidatedContent()) {
                if (typeof params.unvalidatedContentCallback === 'function') {
                  params.unvalidatedContentCallback();
                } else {
                  _editor.setData(this.getUnvalidatedContent());
                }
                this.clear(true);
              }
            }.bind(this);

            this.getUnvalidatedContent = function() {
              return cache.get("data");
            };
            this.existsUnvalidatedContent = function() {
              return typeof this.getUnvalidatedContent() === 'string';
            };
            this.clear = function(notRegisterLastData) {
              if (!notRegisterLastData) {
                dataOnLastClear = this.getUnvalidatedContent();
              } else {
                dataOnLastClear = undefined;
              }
              cache.clear();
            };

            sp.editor.wysiwyg.promiseFirstEditorInstance().then(function(editor) {
              _editor = editor;
              if (this.existsUnvalidatedContent()) {
                var confirmationUrl = webContext +
                    '/wysiwyg/jsp/confirmUnvalidatedContentExistence.jsp';
                var ajaxConfig = sp.ajaxConfig(confirmationUrl);
                displaySingleConfirmationPopupFrom(ajaxConfig.getUrl(), {
                  callback : __unStash,
                  alternativeCallback : function() {this.clear()}.bind(this)
                }).then(function() {
                  document.querySelector('#unvalidated-wysiwyg-content-container').innerHTML =
                      this.getUnvalidatedContent();
                }.bind(this));
              }

              _editor.on('change', function() {
                if (timer) {
                  clearTimeout(timer);
                }
                timer = setTimeout(__stash, 1000);
              });
            }.bind(this));
          };
          sp.editor.wysiwyg.lastBackupManager.instance = instance;
          return instance;
        },
        lastBackupManager : {
          instance : undefined,
          clear : function() {
            if (sp.editor.wysiwyg.lastBackupManager.instance) {
              sp.editor.wysiwyg.lastBackupManager.instance.clear();
            }
          }
        }
      }
    },
    search : {
      on : function(queryDescription) {
        if (typeof queryDescription === 'string') {
          queryDescription = {query : queryDescription};
        }
        var params = extendsObject({
          query : undefined,
          taxonomyPosition : undefined,
          spaceId : undefined,
          appId : undefined,
          startDate : undefined,
          endDate : undefined,
          form : undefined
        }, queryDescription);
        var url = webContext + '/services/search';
        return sp.ajaxRequest(url).withParams(params).sendAndPromiseJsonResponse();
      }
    },
    contribution : {
      id : {
        from : function() {
          if (arguments.length > 2) {
            var instanceId = arguments[0];
            var type = arguments[1];
            var localId = arguments[2];
            return new SilverpeasContributionIdentifier(instanceId, type, localId);
          } else {
            var contributionId = arguments[0];
            if (contributionId instanceof SilverpeasContributionIdentifier) {
              return contributionId;
            } else {
              var decodedContributionId = sp.contribution.id.fromString(contributionId);
              if (!decodedContributionId) {
                decodedContributionId = sp.contribution.id.fromBase64(contributionId);
              }
              return decodedContributionId;
            }
          }
        },
        fromString : function(contributionId) {
          var contributionIdRegExp = /^([^:]+):([^:]+):(.+)$/g;
          var match = contributionIdRegExp.exec(contributionId);
          if (match) {
            var instanceId = match[1];
            var type = match[2];
            var localId = match[3];
            return new SilverpeasContributionIdentifier(instanceId, type, localId);
          }
        },
        fromBase64: function(contributionId) {
          return sp.contribution.id.from(sp.base64.decode(contributionId));
        }
      }
    }
  };
  sp.listPane = sp.arrayPane;

  /**
   * @deprecated use instead sp.formRequest
   */
  sp.formConfig = sp.formRequest;
  /**
   * @deprecated use instead sp.ajaxRequest
   */
  sp.ajaxConfig = sp.ajaxRequest;
}