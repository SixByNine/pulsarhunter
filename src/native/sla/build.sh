#!/bin/sh
CMD="rm *.o"
echo $CMD
$CMD
CMD="$F77 -fPIC -c *.f"
echo $CMD
$CMD

CMD="$CC -fPIC -c *.c -I$JDK_HOME/include/ -I$JDK_HOME/include/linux"
echo $CMD
$CMD

CMD="$CC -o libbarrycentre.so -lm $LDFLAGS $PH_PIPELINE_LIBGFORTRAN -shared *.o"
echo $CMD
$CMD

CMD="cp libbarrycentre.so ../../lib/"
echo $CMD
$CMD

