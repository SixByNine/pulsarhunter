#include "pulsarhunter_BarryCenter.h"

JNIEXPORT void JNICALL Java_pulsarhunter_BarryCenter_psrephj
  (JNIEnv* env, jclass cls , jdouble EPOCH, jint ITELNO, jdouble RA20,
jdouble DEC20, jdouble PBEPOCH, jdouble PB,
jdouble PBDOT, jdouble EPBIN, jdouble PBIN, jdouble ASINI, jdouble WBIN,
jdouble ECC, jdoubleArray result){

       double TOBS, POBS, XMA, BTDB;
       jdouble *cArr;


	psrephb_(&EPOCH, &ITELNO, &RA20, &DEC20, &PBEPOCH, &PB, &PBDOT,
&EPBIN, &PBIN, &ASINI, &WBIN, &ECC, &TOBS, &POBS, &XMA, &BTDB);

        cArr = (*env)->GetDoubleArrayElements(env,result,0);

        cArr[0] = TOBS;
        cArr[1] = POBS;
        cArr[2] = XMA;
        cArr[3] = BTDB;

        (*env)->ReleaseDoubleArrayElements(env,result,cArr,0);


}


