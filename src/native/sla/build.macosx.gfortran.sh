#!/bin/sh
CMD="rm *.o"
echo $CMD
$CMD
CMD="gfortran -c *.f"
echo $CMD
$CMD
CMD="gcc-4  -o libbarrycentre.jnilib -lm -lgfortran -bundle -I/Developer/SDKs/MacOSX10.4u.sdk/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Headers/  *.c *.o"
echo $CMD
$CMD

#CMD="cp libbarrycentre.so ../../../lib/"
#echo $CMD
#$CMD

