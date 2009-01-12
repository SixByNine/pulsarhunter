#!/bin/sh
CMD="$CC -fPIC -c *.c -I$JDK_HOME/include/ -I$JDK_HOME/include/linux"
echo $CMD
$CMD

CMD="$CC -Wall -o libjpgplot.so $LDFLAGS $CPPFLAGS $PH_PIPELINE_LIBGFORTRAN -L/usr/X11R6/lib -lX11 -lpng -lm -shared *.o $PGPLOT_DIR/libcpgplot.a $PGPLOT_DIR/libpgplot.a"
echo $CMD
$CMD



CMD="cp libjpgplot.so ../../lib/"
echo $CMD
$CMD

