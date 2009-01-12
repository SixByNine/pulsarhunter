/*
Copyright (C) 2005-2007 Michael Keith, University Of Manchester

email: mkeith@pulsarastronomy.net
www  : www.pulsarastronomy.net/wiki/Software/PulsarHunter

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

*/
/*
 * KSTest.java
 *
 * Created on 13 October 2005, 13:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package pulsarhunter.jreaper.peckscorer;

import java.util.Arrays;

/**
 *
 * @author mkeith
 */
public class KSTest {
    
    
    
    
    
    StatisticalResult ks2d1s(double x1[], double y1[],KS2DFunction func){
        double d1=0.0;
        int n1 = x1.length;
        double prob;
        for (int j=0;j<n1;j++) {
            QuadCount f = quadct(x1[j],y1[j],x1,y1);
            QuadCount g = func.call(x1[j],y1[j]);
            //System.out.println(f.a+"  "+g.a);
            d1=Math.max(d1,Math.abs(f.a-g.a));
            d1=Math.max(d1,Math.abs(f.b-g.b));
            d1=Math.max(d1,Math.abs(f.c-g.c));
            d1=Math.max(d1,Math.abs(f.d-g.d));
            
        }
        StatisticalResult s = this.pearsn(x1,y1);
        double sqen=Math.sqrt((double)n1);
        double rr=Math.sqrt(1.0-s.r*s.r);
        d1 = d1 / 2;
        prob=probks(d1*sqen/(1.0+rr*(0.25-0.75/sqen)));
        s.prob = prob;
        s.d = d1;
        return s;
    }
    
    
    
    public StatisticalResult ks2d2s(double x1[], double y1[],  double x2[], double y2[]) {
        int n1 = x1.length;
        int n2 = x2.length;
        double d,prob;
        int j;
        double d1,d2,dum,dumm,fa,fb,fc,fd,ga,gb,gc,gd,r1,r2,rr,sqen;
        d1=0.0;
        for (j=0;j<n1;j++) {
            QuadCount f = quadct(x1[j],y1[j],x1,y1);
            
            QuadCount g = quadct(x1[j],y1[j],x2,y2);
            
            d1=Math.max(d1,Math.abs(f.a-g.a));
            d1=Math.max(d1,Math.abs(f.b-g.b));
            d1=Math.max(d1,Math.abs(f.c-g.c));
            d1=Math.max(d1,Math.abs(f.d-g.d));
        }
        d2=0.0;
        for (j=0;j<n2;j++) {
            QuadCount f = quadct(x2[j],y2[j],x1,y1);
            QuadCount g = quadct(x2[j],y2[j],x2,y2);
            d2=Math.max(d2,Math.abs(f.a-g.a));
            d2=Math.max(d2,Math.abs(f.b-g.b));
            d2=Math.max(d2,Math.abs(f.c-g.c));
            d2=Math.max(d2,Math.abs(f.d-g.d));
        }
        d=0.5*(d1+d2);
        sqen=Math.sqrt(n1*n2/(double)(n1+n2));
        StatisticalResult s1 = pearsn(x1,y1);
        StatisticalResult s2 = pearsn(x2,y2);
        rr=Math.sqrt(1.0-0.5*(s1.r*s1.r+s2.r*s2.r));
        
        prob=probks(d*sqen/(1.0+rr*(0.25-0.75/sqen)));
        StatisticalResult s = new StatisticalResult();
        s.d = d;
        s.prob = prob;
        return s;
    }
    
    
    /*public StatisticalResult ksone(double data[],  Function f ){
        int j;
        int n = data.length;
        double d;
        double prob;
        double dt,en,ff,fn,fo=0.0;
        Arrays.sort(data);
        en = n;
        d=0.0;
     
        for (j=0;j<n;j++) {
            fn=j/en;
            ff = f.compute(data[j]);
            dt=Math.max(Math.abs(fo-ff),Math.abs(fn-ff));
            if (dt > d) d=dt;
            fo=fn;
        }
        en=Math.sqrt(en);
        prob=probks((en+0.12+0.11/en)*(d));
        StatisticalResult s = new StatisticalResult();
        s.prob = prob;
        s.d = d;
        return s;
    }
     */
    
    
    
    
    public StatisticalResult kstwo(double data1[],  double data2[] ){
        
        int j1=1,j2=1;
        double d1,d2,dt,en1,en2,en,fn1=0.0,fn2=0.0;
        int n1 = data1.length;
        int n2 = data2.length;
        Arrays.sort(data1);
        Arrays.sort(data2);
        en1=n1;
        en2=n2;
        double d=0.0;
        double prob;
        while (j1 < n1 && j2 < n2) {
            if ((d1=data1[j1]) <= (d2=data2[j2])) fn1=j1++/en1;
            if (d2 <= d1) fn2=j2++/en2;
            if ((dt=Math.abs(fn2-fn1)) > d) d=dt;
        }
        en=Math.sqrt(en1*en2/(en1+en2));
        prob=probks((en+0.12+0.11/en)*(d));
        
        StatisticalResult s = new StatisticalResult();
        s.prob = prob;
        s.d = d;
        return s;
        
    }
    
    
    
    
    
    
    public class StatisticalResult{
        public double r; // correlation coefficient
        public double prob; // prob
        public double z; // fishers z
        public double d;
    }
    
    private StatisticalResult pearsn(double[] x, double[] y){
        
        
        final double  TINY = 1.0e-20 ;
        int n = x.length;
        double r; // correlation coefficient
        double prob; // prob
        double z; // fishers z
        
        
        
        int j;
        double yt,xt,t,df;
        double syy=0.0,sxy=0.0,sxx=0.0,ay=0.0,ax=0.0;
        for (j=0;j<n;j++) {
            ax += x[j];
            ay += y[j];
        }
        ax /= n;
        ay /= n;
        for (j=0;j<n;j++) {
            xt=x[j]-ax;
            yt=y[j]-ay;
            sxx += xt*xt;
            syy += yt*yt;
            sxy += xt*yt;
        }
        r=sxy/(Math.sqrt(sxx*syy)+TINY);
        z=0.5*Math.log((1.0+(r)+TINY)/(1.0-(r)+TINY));
        df=n-2;
        t=(r)*Math.sqrt(df/((1.0-(r)+TINY)*(1.0+(r)+TINY)));
        prob=betai(0.5*df,0.5,df/(df+t*t));
        StatisticalResult s = new StatisticalResult();
        s.r = r;
        s.z = z;
        s.prob = prob;
        return s;
    }
    
    
    private double erfcc(double x){
        double t,z,ans;
        z = Math.abs(x);
        t = 1.0/(1.0+0.5*z);
        ans = t*Math.exp(-z*z-1.26551223 + t*(1.00002368+t*(0.37409196+t*(0.09678418 +
                t*(-0.18628806+t*(0.27886807+t*(-1.13520398+t*(1.48851587 +
                t*(-0.88215223+t*0.17087277)))))))));
        if(x >= 0.0) return ans;
        else return 2.0-ans;
    }
    
    
    
    
    private double gammln(double xx){
        double x,y,tmp,ser;
        double[] cof = new double[]{76.18009172947146,-86.50532032941677,24.01409824083091,-1.231739572450155,0.1208650973866179e-2,-0.5395239384953e-5};
        int j;
        
        
        y=x=xx;
        tmp=x+5.5;
        tmp -= (x+0.5)*Math.log(tmp);
        ser=1.000000000190015;
        for (j=0;j <= 5;j++) ser += cof[j]/++y;
        return -tmp+Math.log(2.5066282746310005*ser/x);
        
    }
    
    
    
    private double betai(double a, double b, double x) {
        double bt;
        if (x < 0.0 || x > 1.0) throw new RuntimeException("Bad x in routine betai");
        if (x == 0.0 || x == 1.0) bt=0.0;
        else bt=Math.exp(gammln(a+b)-gammln(a)-gammln(b)+a*Math.log(x)+b*Math.log(1.0-x));
        if (x < (a+1.0)/(a+b+2.0)) return bt*betacf(a,b,x)/a;
        else return 1.0-bt*betacf(b,a,1.0-x)/b;
    }
    
    double betacf(double a, double b, double x){
        final int MAXIT = 100;
        final double EPS = 3.0e-7;
        final double FPMIN =1.0e-30;
        
        
        
        int m,m2;
        
        double aa,c,d,del,h,qab,qam,qap;
        qab=a+b;
        qap=a+1.0;
        
        qam=a-1.0;
        c=1.0;
        d=1.0-qab*x/qap;
        if (Math.abs(d) < FPMIN) d=FPMIN;
        d=1.0/d;
        h=d;
        for (m=1;m<=MAXIT;m++) {
            m2=2*m;
            aa=m*(b-m)*x/((qam+m2)*(a+m2));
            d=1.0+aa*d;
            if (Math.abs(d) < FPMIN) d=FPMIN;
            c=1.0+aa/c;
            if (Math.abs(c) < FPMIN) c=FPMIN;
            d=1.0/d;
            h *= d*c;
            aa = -(a+m)*(qab+m)*x/((a+m2)*(qap+m2));
            d=1.0+aa*d;
            if (Math.abs(d) < FPMIN) d=FPMIN;
            c=1.0+aa/c;
            if (Math.abs(c) < FPMIN) c=FPMIN;
            d=1.0/d;
            del=d*c;
            h *= del;
            if (Math.abs(del-1.0) < EPS) break;
        }
        if (m > MAXIT) throw new RuntimeException("a or b too big, or MAXIT too small in betacf");
        return h;
    }
    
    
    
    private double probks(double alam){
        final double  EPS1 = 0.001;
        final double EPS2 = 1.0e-8;
        int j;
        double a2,fac=2.0,sum=0.0,term,termbf=0.0;
        a2 = -2.0*alam*alam;
        for (j=1;j<=100;j++) {
            term=fac*Math.exp(a2*j*j);
            sum += term;
            if (Math.abs(term) <= EPS1*termbf || Math.abs(term) <= EPS2*sum) return sum;
            fac = -fac;
            termbf=Math.abs(term);
        }
        return 1.0;
        
        
        
        
    }
    
    
    
    QuadCount quadct(double x, double y, double xx[], double yy[]) {
        int k,na,nb,nc,nd;
        int nn = xx.length;
        double ff;
        na=nb=nc=nd=0;
        for (k=0;k<nn;k++) {
            if (yy[k] > y) {
                if(xx[k] > x) ++na;
                else ++nb;
            } else {
                if(xx[k] > x)++nd;
                else ++nc;
            }
        }
        ff=1.0/nn;
        
        QuadCount q = new QuadCount();
        q.a=ff*na;
        q.b=ff*nb;
        q.c=ff*nc;
        q.d=ff*nd;
        return q;
    }
    
    
    
    
}
