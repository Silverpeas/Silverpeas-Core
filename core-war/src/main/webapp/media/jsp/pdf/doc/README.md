# Updating PDF.JS plugin

Updating PDF.JS means a bit of work.

1. First thing is
   to [download the new version](https://mozilla.github.io/pdf.js/getting_started/#download).
2. Renaming some source names. All `.mjs` have to be renamed into `.min.js` in order to be provided
   rightly by the application server. Setting `.min` part into file name avoid the minification of
   the file.
3. Removing all `.map` files.
4. Copying `js` from `build` folder to the Silverpeas's `core` one.
5. Adapting `viewer.jsp` content with the new `viewer.html` one:
    * first, it consists into copying exactly the body HTML element;
    * then the `<script>` content must be adapted.
6. The previous action induces some manual modifications to perform on `viewer.min.js` file. 

As requested by the plugin authors, `Please re-skin it or build upon it`, the viewer has already
been re-skinned. Perhaps some additional CSS instructions should be adapted into `sp-viewer.css`
file. For specific skinning, `sp-viewer-addon.css` file can be filled (empty by default).

Let's follow what's been adapted for updates.

## From 2.2.228 to 4.0.379

This represents a considerable gap.

### L10N management

This new version is now using plugin of [Fluent Project](https://projectfluent.org).
It consists into getting the translation text content (into [Fluent syntax](https://projectfluent.org/fluent/guide/index.html)) from `locale.json` locale mapping.

For Silverpeas's implementation, some translations must be modified, and some must be added (
from `viewerBundle.properties`). In this aim, `window.SP_PDF_VIEWER_L10N_TEXT_MODIFIER(text)`
function is implemented into `viewer.jsp`. It is responsible to complete the given translation text.
The function is wired into `viewer.min.js`:

![l10n.png](l10n.png)

The result of the method returning `text` value MUST be given
to `window.SP_PDF_VIEWER_L10N_TEXT_MODIFIER(text)` method to get the final l10n text source.

### Viewer starting

The viewer starting is perform by `webViewerLoad` method of `viewer.min.js` source. Two phases can be inside identified:

1. dispatching a custom event `webviewerloaded` just after reading the viewer configuration (mainly
   identifying all the DOM containers)
2. running the configuration (means starting the display of the viewer)

To add Silverpeas's functionalities, it is needed to change some stuffs of the read configuration
(phase 1), but also it is needed to add/change some viewer behavior after it has started (after
phase 2).

![webViewerLoad.png](webViewerLoad.png)

Custom event detail is completed with needed instances. After the viewer has started, a Silverpeas's
promise is resolved to indicates that behavior to implement after the viewer start can be added.

### viewer.jsp

This is the Silverpeas page responsible for starting and configuring the PDF viewer according to the
user's context. Please consult GIT history on the file to identify all the modifications performed
into `<script>` module.

