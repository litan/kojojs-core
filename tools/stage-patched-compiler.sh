#!/bin/sh
# Makes a copy of a staged compilerServer whose scala-compiler and scala-reflect are the patched ones
# (for example the Turkish keywords of kojo's scala-tr), to run under its own label:
#
#   sbt compilerServer/stage
#   tools/stage-patched-compiler.sh compiler-server/target/universal/stage compiler-tr
#   SCALAFIDDLE_COMPILER_LABEL=2.13-tr compiler-tr/bin/scalafiddle-core
#
# and the router gets SCALAFIDDLE_SCALAVERSIONS='["2.13","2.13-tr"]'.
#
# Where the patched jars come from (scala-compiler.jar and scala-reflect.jar, from `sbt dist/mkPack` of
# a Scala checkout with the patch applied):
#   SCALA_PATCHED_LIB=<dir>                a directory that has them, or
#   SCALA_PATCHED_URL=<url> and SCALA_PATCHED_SHA256=<hash>
#                                          a .tar.gz of such a directory (the files at its top, or in lib/),
#                                          downloaded and checked against the hash.
# Both jars are replaced together: the patch is in both, and one without the other fails at run time with
# a NoSuchMethodError. The Scala version of the jars must be the one of the staged compiler.
set -eu

[ $# -eq 2 ] || { echo "usage: $0 <staged-compiler-dir> <output-dir>" >&2; exit 2; }
STAGE=$1
OUT=$2
[ -d "$STAGE/lib" ] || { echo "error: $STAGE/lib not found (run sbt compilerServer/stage)" >&2; exit 1; }
[ ! -e "$OUT" ] || { echo "error: $OUT exists" >&2; exit 1; }

WORK=
cleanup() { [ -z "$WORK" ] || rm -rf "$WORK"; }
trap cleanup EXIT

LIB=${SCALA_PATCHED_LIB:-}
if [ -z "$LIB" ]; then
  [ -n "${SCALA_PATCHED_URL:-}" ] && [ -n "${SCALA_PATCHED_SHA256:-}" ] ||
    { echo "error: set SCALA_PATCHED_LIB, or SCALA_PATCHED_URL and SCALA_PATCHED_SHA256" >&2; exit 1; }
  WORK=$(mktemp -d)
  curl -fsSL "$SCALA_PATCHED_URL" -o "$WORK/patched.tar.gz"
  if command -v sha256sum >/dev/null 2>&1; then got=$(sha256sum "$WORK/patched.tar.gz" | cut -d' ' -f1)
  else got=$(shasum -a 256 "$WORK/patched.tar.gz" | cut -d' ' -f1); fi
  [ "$got" = "$SCALA_PATCHED_SHA256" ] || { echo "error: sha256 is $got, expected $SCALA_PATCHED_SHA256" >&2; exit 1; }
  mkdir "$WORK/x" && tar -xzf "$WORK/patched.tar.gz" -C "$WORK/x"
  LIB=$WORK/x
  [ -f "$LIB/scala-compiler.jar" ] || LIB=$WORK/x/lib
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
