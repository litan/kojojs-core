#!/bin/sh
# Makes a copy of a staged compilerServer whose scala-compiler and scala-reflect are the patched ones
# (for example the Turkish keywords of kojo's scala-tr), to run under its own label:
#
#   sbt compilerServer/stage
#   tools/stage-patched-compiler.sh compiler-server/target/universal/stage compiler-tr tr
#   SCALAFIDDLE_COMPILER_LABEL=2.13-tr compiler-tr/bin/scalafiddle-core
#
# and the router gets SCALAFIDDLE_SCALAVERSIONS='["2.13","2.13-tr"]'.
#
# The third argument says where the patched jars come from: a name from tools/patched-compilers.txt
# (downloaded from the release url there and checked against the sha256 there), or a directory that has
# scala-compiler.jar and scala-reflect.jar (for example the lib/ of `sbt dist/mkPack` of a Scala checkout
# with the patch applied).
# Both jars are replaced together: the patch is in both, and one without the other fails at run time with
# a NoSuchMethodError. The Scala version of the jars must be the one of the staged compiler.
set -eu

[ $# -eq 3 ] || { echo "usage: $0 <staged-compiler-dir> <output-dir> <name | jar-dir>" >&2; exit 2; }
STAGE=$1
OUT=$2
SOURCE=$3
[ -d "$STAGE/lib" ] || { echo "error: $STAGE/lib not found (run sbt compilerServer/stage)" >&2; exit 1; }
[ ! -e "$OUT" ] || { echo "error: $OUT exists" >&2; exit 1; }

WORK=
cleanup() { [ -z "$WORK" ] || rm -rf "$WORK"; }
trap cleanup EXIT

sha256() { if command -v sha256sum >/dev/null 2>&1; then sha256sum "$1" | cut -d' ' -f1; else shasum -a 256 "$1" | cut -d' ' -f1; fi; }

if [ -d "$SOURCE" ]; then
  LIB=$SOURCE
else
  TABLE=$(dirname "$0")/patched-compilers.txt
  row=$(grep -v '^#' "$TABLE" | awk -v n="$SOURCE" '$1 == n { print; exit }')
  [ -n "$row" ] || { echo "error: $SOURCE is neither a directory nor a name in $TABLE" >&2; exit 1; }
  url=$(echo "$row" | awk '{print $2}')
  WORK=$(mktemp -d)
  LIB=$WORK
  for base in scala-compiler scala-reflect; do
    want=$(echo "$row" | awk -v b="$base" '{ print (b == "scala-compiler") ? $3 : $4 }')
    curl -fsSL "$url/$base.jar" -o "$WORK/$base.jar"
    got=$(sha256 "$WORK/$base.jar")
    [ "$got" = "$want" ] || { echo "error: $url/$base.jar has sha256 $got, expected $want" >&2; exit 1; }
  done
fi

for base in scala-compiler scala-reflect; do
  [ -f "$LIB/$base.jar" ] || { echo "error: $LIB/$base.jar not found" >&2; exit 1; }
  set -- "$STAGE/lib/"org.scala-lang.$base-*.jar
  { [ $# -eq 1 ] && [ -f "$1" ]; } || { echo "error: expected one org.scala-lang.$base-*.jar in $STAGE/lib, found: $*" >&2; exit 1; }
done

# The same Scala version, or the swap succeeds and the compiler breaks later.
want=$(basename "$(ls "$STAGE/lib/"org.scala-lang.scala-compiler-*.jar)" .jar); want=${want##*-}
got=$(unzip -p "$LIB/scala-compiler.jar" compiler.properties | sed -n 's/^version.number=//p')
case "$got" in
  "$want"*) ;;
  *) echo "error: the patched compiler is Scala $got, the staged one $want" >&2; exit 1;;
esac

cp -R "$STAGE" "$OUT"
for base in scala-compiler scala-reflect; do
  dest=$(ls "$OUT/lib/"org.scala-lang.$base-*.jar)
  cp "$LIB/$base.jar" "$dest"
  echo "$(basename "$dest") <- $LIB/$base.jar"
done
