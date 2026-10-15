# The Koco ↔ Kojo script translator in router/

`POST /translate` translates a Kojo script between English (Kojo) and Turkish (Koco, the Turkish
edition of Kojo) on the router. It does not touch the compiler: it translates lexically (token by
token) and takes milliseconds per request.

## Endpoint

```
POST /translate?direction=tr2en|en2tr|auto     body: script (UTF-8, at most 64 KB)
→ 200 application/json
  { "direction": "tr2en", "code": "<translated script>",
    "report": { "translated": 12,
                "leftovers": [ { "name": "başlık", "count": 2 } ],
                "ambiguous": [ { "name": "…", "chosen": "…", "others": ["…"], "line": 4 } ] },
    "remainingKeywords": [] }
→ 400 text/plain   invalid direction
→ 413              body larger than 64 KB
```

- If `direction` is missing or `auto`, it is detected from the script: a Turkish keyword (`dez`,
  `den`, `tanım`, `eğer`, …) means `tr2en`, otherwise `en2tr`.
- `leftovers`: what the translator calls "still Turkish" (names with Turkish letters or names the
  dictionary knows: the user's own names, gaps in the dictionary). An ASCII user name is left
  untranslated but not listed. Filled for `tr2en` only (English names are valid in Koco too, so
  leaving them is cosmetic). No line numbers: the translator keeps only counts.
- `ambiguous`: names with several possible translations, translated with the most frequent one;
  `line` starts at 1.
- `remainingKeywords`: the output still contains a keyword of the source language (translation incomplete).
- Not cached (`Cache-Control: no-cache`); CORS is the same as `/compile`. Translation runs on its own
  two-thread daemon pool, so it cannot starve the compiler's dispatcher.

## Copied files (never edited by hand)

The translator is developed and tested in the Koco desktop project (bulent2k2/kojo: `TranslatorTest`,
`TranslatorCompileTest`, `TranslatorLibraryTest`). These files under `router/` are BYTE-FOR-BYTE
copies from it; the commit in `TRANSLATOR_VERSION` decides the version:

| file |
|---|
| `src/main/scala/net/kogics/kojo/lite/i18n/tr/{translator,translationdictionary,dict}.scala` |
| `src/main/resources/i18n/tr/{translation-dictionary,translation-rules}.tsv` |
| `lib/scalariform.jar` (scalariform patched to know the Turkish keywords) |

A copy fixed here would mean two translators, so fix it in the desktop project and bump the SHA.

- Verify: `router/translator-sync.sh --kojo <kojo clone> --check` (exit 1 on any difference).
- Upgrade: change the SHA in `TRANSLATOR_VERSION`, copy with `router/translator-sync.sh --kojo <kojo clone>`,
  commit both together.

`DictionaryGenerator`, `TranslatorMain` and `TranslationValidator` stay in the desktop project (they read the
desktop sources or use the compiler) and are not copied here.

## Trap: scala-xml

The lexer in `scalariform.jar` needs `scala.xml.parsing.TokenTests` at run time. `scalatest` brings it onto
the Test classpath, so the unit tests pass, but without `scala-xml` on the production classpath the first
request fails with `NoClassDefFoundError`. It is an explicit dependency in `build.sbt`; the unit tests do NOT
catch its removal.

## Testing

- `sbt router/test` → `TranslateTest`: direction detection, invalid direction, JSON shape, Turkish characters.
- By hand, end to end: start the router with `SCALAFIDDLE_PORT=18880 SCALAFIDDLE_INTERFACE=127.0.0.1`, then
  `curl -X POST --data-binary @script.kojo 'localhost:18880/translate?direction=auto'`.
