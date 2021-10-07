(window["webpackJsonp"] = window["webpackJsonp"] || []).push([[1],{

/***/ 191:
/***/ (function(module, exports, __webpack_require__) {

!function (e, _) {
   true ? module.exports = _(__webpack_require__(3)) : undefined;
}(this, function (e) {
  "use strict";

  e = e && e.hasOwnProperty("default") ? e.default : e;

  var _ = "يناير_فبراير_مارس_أبريل_مايو_يونيو_يوليو_أغسطس_سبتمبر_أكتوبر_نوفمبر_ديسمبر".split("_"),
      t = {
    name: "ar",
    weekdays: "الأحد_الإثنين_الثلاثاء_الأربعاء_الخميس_الجمعة_السبت".split("_"),
    weekdaysShort: "أحد_إثنين_ثلاثاء_أربعاء_خميس_جمعة_سبت".split("_"),
    weekdaysMin: "ح_ن_ث_ر_خ_ج_س".split("_"),
    months: _,
    monthsShort: _,
    weekStart: 6,
    relativeTime: {
      future: "بعد %s",
      past: "منذ %s",
      s: "ثانية واحدة",
      m: "دقيقة واحدة",
      mm: "%d دقائق",
      h: "ساعة واحدة",
      hh: "%d ساعات",
      d: "يوم واحد",
      dd: "%d أيام",
      M: "شهر واحد",
      MM: "%d أشهر",
      y: "عام واحد",
      yy: "%d أعوام"
    },
    ordinal: function (e) {
      return e;
    },
    formats: {
      LT: "HH:mm",
      LTS: "HH:mm:ss",
      L: "D/‏M/‏YYYY",
      LL: "D MMMM YYYY",
      LLL: "D MMMM YYYY HH:mm",
      LLLL: "dddd D MMMM YYYY HH:mm"
    }
  };

  return e.locale(t, null, !0), t;
});

/***/ })

}]);
//# sourceMappingURL=ar.js.map