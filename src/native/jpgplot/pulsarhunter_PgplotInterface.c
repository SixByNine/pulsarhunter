#include "pulsarhunter_PgplotInterface.h"
#include "quikgray.h"
#include "cpgplot.h"

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgopen
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgopen
(JNIEnv *env, jclass cl, jstring device){
	
	int one=1,zero=0;
	jint strlen =  (*env)->GetStringUTFLength(env,device);

	char *deviceStr = (char*) (*env)->GetStringUTFChars(env,device,NULL);

	

//	pgbeg__(&zero,deviceStr,&one,&one,strlen);
	cpgopen(deviceStr);

	(*env)->ReleaseStringUTFChars(env,device,deviceStr);
}


/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgclose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgclose
(JNIEnv *env, jclass cl){
	cpgclos();
}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgsvp
 * Signature: (FFFF)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgsvp
(JNIEnv *env, jclass cl, jfloat xleft, jfloat xright, jfloat ybot, jfloat ytop){

	cpgsvp(xleft,xright,ybot,ytop);
}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgswin
 * Signature: (FFFF)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgswin
(JNIEnv *env, jclass cl,  jfloat xleft, jfloat xright, jfloat ybot, jfloat ytop){

	cpgswin(xleft,xright,ybot,ytop);

}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgqwin
 * Signature: ([F)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgqwin
(JNIEnv *env, jclass cl, jfloatArray retVal){
	jfloat* retValArr = (*env)->GetFloatArrayElements(env,retVal,0);

	cpgqwin(retValArr,retValArr+1,retValArr+2,retValArr+3);

	(*env)->ReleaseFloatArrayElements(env,retVal,retValArr,0);

}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgmove
 * Signature: (FF)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgmove
(JNIEnv *env, jclass cl, jfloat x, jfloat y){

	cpgmove(x,y);

}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgdraw
 * Signature: (FF)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgdraw
(JNIEnv *env, jclass cl, jfloat x, jfloat y){
	cpgdraw(x,y);

}

JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgsch
(JNIEnv *env, jclass cl, jfloat x){
        cpgsch(x);

}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pglab
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pglab
(JNIEnv *env, jclass cl, jstring x, jstring y, jstring top){

	char *xStr = (char*)(*env)->GetStringUTFChars(env,x,NULL);
	char *yStr = (char*)(*env)->GetStringUTFChars(env,y,NULL);
	char *topStr = (char*)(*env)->GetStringUTFChars(env,top,NULL);
        jint strlen1 =  (*env)->GetStringUTFLength(env,x);
        jint strlen2 =  (*env)->GetStringUTFLength(env,y);
        jint strlen3 =  (*env)->GetStringUTFLength(env,top);


	cpglab(xStr,yStr,topStr);

	(*env)->ReleaseStringUTFChars(env,x,xStr);
	(*env)->ReleaseStringUTFChars(env,y,yStr);
	(*env)->ReleaseStringUTFChars(env,top,topStr);



}

JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgtext
(JNIEnv *env, jclass cl, jstring text, jfloat x, jfloat y){

        char *textStr = (char*)(*env)->GetStringUTFChars(env,text,NULL);
        jint strlen =  (*env)->GetStringUTFLength(env,text);


        cpgtext(x,y,textStr);

        (*env)->ReleaseStringUTFChars(env,text,textStr);



}


/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pggray
 * Signature: ([FIIIIIIFFF)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pggray
(JNIEnv *env, jclass cl, jfloatArray arr, jint idim, jint jdim, jint i1, jint i2, jint j1, jint j2, jfloat fg, jfloat bg, jfloatArray tr){

	jfloat* trArr = (*env)->GetFloatArrayElements(env,tr,0);
	jfloat* arrArr = (*env)->GetFloatArrayElements(env,arr,0);

	cpggray(arrArr,idim,jdim,i1,i2,j1,j2,fg,bg,trArr);

	(*env)->ReleaseFloatArrayElements(env,tr,trArr,0);
	(*env)->ReleaseFloatArrayElements(env,arr,arrArr,0);



}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgbox
 * Signature: (Ljava/lang/String;FFLjava/lang/String;FI)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgbox
(JNIEnv *env, jclass cl, jstring xopt, jfloat xtic, jint nxsub , jstring yopt, jfloat ytic, jint nysub){
	char *xStr = (char*)(*env)->GetStringUTFChars(env,xopt,NULL);
	char *yStr = (char*)(*env)->GetStringUTFChars(env,yopt,NULL);

        jint strlen1 =  (*env)->GetStringUTFLength(env,xopt);
       jint strlen2 =  (*env)->GetStringUTFLength(env,yopt);


	cpgbox(xStr,xtic,nxsub,yStr,ytic,nysub);

	(*env)->ReleaseStringUTFChars(env,xopt,xStr);
	(*env)->ReleaseStringUTFChars(env,yopt,yStr);


}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    quikgray
 * Signature: ([FIII)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_quikgray
(JNIEnv *env, jclass cl, jfloatArray dat, jint nx, jint ny, jint nxx){
	jfloat* datArr = (*env)->GetFloatArrayElements(env,dat,0);

	quikgray(datArr,nx,ny,nxx);

	(*env)->ReleaseFloatArrayElements(env,dat,datArr,0);
}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgshls
 * Signature: (IFFF)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgshls
  (JNIEnv *env, jclass cl, jint ci, jfloat h , jfloat s, jfloat l){
	cpgshls(ci,h,s,l);
}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgsci
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgsci
  (JNIEnv *env, jclass cl, jint ci){
	cpgsci(ci);
}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgcirc
 * Signature: (FFF)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgcirc
  (JNIEnv *env, jclass cl, jfloat x, jfloat y , jfloat r){
	cpgcirc(x,y,r);
}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgsfs
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgsfs
  (JNIEnv *env, jclass cl, jint v){
	cpgsfs(v);
}



/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgslw
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgslw
  (JNIEnv *env, jclass cl , jint w){
	cpgslw(w);
}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgsls
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgsls
  (JNIEnv *env, jclass cl, jint s){
	cpgsls(s);
}


/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgtxt
 * Signature: (FFFFLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgtxt
  (JNIEnv *env, jclass cl, jfloat x, jfloat y, jfloat angle, jfloat fjust, jstring text){

	char *textStr = (char*)(*env)->GetStringUTFChars(env,text,NULL);
        jint strlen =  (*env)->GetStringUTFLength(env,text);


        cpgptxt(x,y,angle,fjust,textStr);

        (*env)->ReleaseStringUTFChars(env,text,textStr);

}

/*
 * Class:     pulsarhunter_PgplotInterface
 * Method:    pgscf
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_pulsarhunter_PgplotInterface_pgscf
  (JNIEnv *env, jclass cl , jint val){
	cpgscf(val);
}



void MKtestMe(){

	printf("test!\n");
}
