#!/bin/bash
# Copies (or checks) the Koco <-> Kojo translator copy in router/ from bulent2k2/kojo.
#
#   router/translator-sync.sh --kojo ../kojo          # copy (kojo must contain the commit in TRANSLATOR_VERSION)
#   router/translator-sync.sh --kojo ../kojo --check  # compare only; exit 1 on any difference (CI)
#
# The list of copied files lives in one place, below. Why byte-for-byte copies: the translator is
# developed and tested on the desktop (TranslatorTest, TranslatorCompileTest, TranslatorLibraryTest);
# a copy that is fixed by hand here would mean two translators.
# NOTE: shell variable names must be ASCII.
set -eu
HERE="$(cd "$(dirname "$0")" && pwd)"
KOJO=""; CHECK=0
while [ $# -gt 0 ]; do
  case "$1" in
    --kojo) KOJO="$2"; shift 2 ;;
    --check) CHECK=1; shift ;;
    *) echo "usage: $0 --kojo <kojo clone> [--check]" >&2; exit 2 ;;
  esac
done
[ -d "$KOJO/.git" ] || { echo "ERROR: --kojo must be a git clone" >&2; exit 2; }

SHA="$(grep -v '^#' "$HERE/TRANSLATOR_VERSION" | grep -m1 -E '^[0-9a-f]{40}$' || true)"
[ -n "$SHA" ] || { echo "ERROR: no 40-digit SHA in TRANSLATOR_VERSION" >&2; exit 2; }

# path in kojo : path in router
LIST="
src/main/scala/net/kogics/kojo/lite/i18n/tr/translator.scala:src/main/scala/net/kogics/kojo/lite/i18n/tr/translator.scala
src/main/scala/net/kogics/kojo/lite/i18n/tr/translationdictionary.scala:src/main/scala/net/kogics/kojo/lite/i18n/tr/translationdictionary.scala
src/main/scala/net/kogics/kojo/lite/i18n/tr/dict.scala:src/main/scala/net/kogics/kojo/lite/i18n/tr/dict.scala
src/main/resources/i18n/tr/translation-dictionary.tsv:src/main/resources/i18n/tr/translation-dictionary.tsv
src/main/resources/i18n/tr/translation-rules.tsv:src/main/resources/i18n/tr/translation-rules.tsv
lib/scalariform.jar:lib/scalariform.jar
"

differs=0
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT
for pair in $LIST; do
  src="${pair%%:*}"; dst="$HERE/${pair##*:}"
  # The content of the pinned commit, NOT the working tree: same result whatever branch the clone is on
  if ! git -C "$KOJO" show "$SHA:$src" > "$TMP" 2>/dev/null; then
    echo "ERROR: $src not found in $SHA (does the clone contain that commit? git fetch)" >&2; exit 2
  fi
  if [ "$CHECK" = 1 ]; then
    if ! cmp -s "$TMP" "$dst"; then echo "DIFFERENT: ${pair##*:}"; differs=1; fi
  else
    mkdir -p "$(dirname "$dst")"; cp "$TMP" "$dst"; echo "copied: ${pair##*:}"
  fi
done
if [ "$CHECK" = 1 ]; then
  if [ "$differs" = 0 ]; then echo "router/ translator copy is byte-identical to kojo@${SHA:0:12}."; else
    echo "ERROR: the copy has drifted from kojo@${SHA:0:12}. Do not fix it by hand; fix it in kojo and bump the SHA." >&2; exit 1; fi
fi
