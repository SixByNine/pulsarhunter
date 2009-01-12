#include <math.h> 

void quikgray(float*dat,int nx,int ny,int nxx)
{
        /*
           Coarse grey-scale plot using PG plot routines
           Assumed that viewport and window defined outside routine
           nx and ny are the areas of the array display, the x dimension is
           repeated out to nxx, for multiple cycles.
         */

        int ksym[8]={0,1,20,21,2,3,17,18};
        float s=0.;
        float ss=0.;
        float smin=1.e30;
        float smax=-1.e30;
        float aa,rms,x,y;
        int i,ii,j,k;
	int one =1;

        for (i=0;i<nx*ny;i=i+1)
        {
                aa=dat[i];
                s=s+aa;
                ss=ss+aa*aa;
                if(aa > smax)smax=aa;
                if(aa < smin)smin=aa;
        }

        s=s/(float)(nx*ny);
        rms=sqrt(ss/(float)(nx*ny)-s*s);
        if(s+7*rms > smax) rms=(smax-s)/7.0;
//        printf("max: %f min %f mean: %f rms: %f\n",smax,smin,s,rms);
        for (j=0;j<ny;j=j+1)
        {
                ii=0;
                for (i=0;i<nxx;i=i+1)
                {
                        ii=ii+1;
                        if(ii==nx) ii=0;
                        k=(int)((dat[(int)(j*nx+ii)]-s)/rms);
                        if (k>7) k=7;
                        x=(float)i+1;y=(float)j+1;
                        if (k>0) pgpt_(&one,&x,&y,ksym+k);
                }
        }
}

