# Updating GrapesJS plugin

1. First thing is
   to [download the new version](https://github.com/GrapesJS/grapesjs/releases).

   The downloaded archive contains dist resources (`js` and `css`) and the `i18n` files
   into `src/i18n` folder.

2. Replace `grapes.min.js` and linked map file with those of archive.
3. Same stuffs for `css` file.
4. Verifying also versions of [ckeditor](https://github.com/GrapesJS/ckeditor/releases)
   and [preset-newsletter](https://github.com/GrapesJS/preset-newsletter/releases) plugins.
5. Reporting new `i18n` translations from archive to silverpeas by merging it. Be careful to keep
   the `richTextEditor` translations which are specifically added by ourselves.

   Be careful, keep the `gI18n` constant declaration (instead of `export default`) if the used
   sources are the `min.js` ones (in other words, if it is pure JavaScript sources and not the
   modular JavaScript).

6. Unfortunately, if nothing more is done, the following error occurred:

   > Refused to load the
   stylesheet 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css'
   because it violates the following Content Security Policy directive

   There is mainly two solutions. The first one would be to set Silverpeas's security about CSP
   style sources.

   The second one (the one chosen for now) is to download the `font-awesome.min.css` to host it
   directly into Silverpeas and replace the right URL to access it into `grapes.min.js` and
   linked `map` file.
7. Be aware about potential breaking changes since the last Silverpeas's version and process them if
   any. (Could be a bit of work...).
8. Verify all setup performed into `silvereaps-ddwe.js` file.
9. Verify all additional Silverpeas's behaviors and components.

# Customization

This section is dedicated to actions to perform into a specific context.

About CSS, the directives can be overridden by
filling `silverpeas-ddwe-addon.css` (empty by default).

About component set, additional ones can be implemented into `silverpeas-ddwe-addon.js`. Please
consult the content of the original one.

