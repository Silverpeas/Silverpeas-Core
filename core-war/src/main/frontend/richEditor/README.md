# Tiptap vendor bundle

Tiptap 3.32.2 (MIT), ProseMirror Markdown 1.13.4 (MIT).
Only open source packages; no cloud service, Pro extension, runtime CDN or React/Vue.
The application integration is maintained separately in `../../webapp/util/javaScript/richEditor/silverpeas-rich-editor.js`.

Rebuild with Node and npm:

```sh
npm ci --ignore-scripts
npm run build
```

The lockfile pins the exact npm tarball integrity for all dependencies.
The generated bundle is committed so ordinary Maven builds remain offline-capable.
All package copyright and license notices are preserved in
`../../webapp/util/javaScript/richEditor/vendor/tiptap-THIRD-PARTY-NOTICES.txt`.
Source: https://github.com/ueberdosis/tiptap and https://github.com/ProseMirror/prosemirror-markdown.

Run `sha256sum ../../webapp/util/javaScript/richEditor/vendor/tiptap-3.32.2.min.js` to identify the generated bundle.
