#!/bin/sh
CMD="rm *.o"
CMD="gcc-4  -o libjpgplot.jnilib -L/usr/X11/lib/ -L$PGPLOT_DIR -lX11 -lpng -lcpgplot -lm -lgfortran -bundle -I/Developer/SDKs/MacOSX10.4u.sdk/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Headers/  *.c $PGPLOT_DIR/libpgplot.a"
echo $CMD
$CMD

#CMD="cp libbarrycentre.so ../../../lib/"
#echo $CMD
#$CMD

