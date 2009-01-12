#!/bin/sh
CMD="rm *.o"
echo $CMD
$CMD
CMD="/usr/local/bin/g77 -c *.f"
echo $CMD
$CMD
CMD="gcc  -o libbarrycentre.jnilib -lm -lg2c -bundle -I/Developer/SDKs/MacOSX10.4u.sdk/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Headers/  *.c *.o"
echo $CMD
$CMD

#CMD="cp libbarrycentre.so ../../../lib/"
#echo $CMD
#$CMD

