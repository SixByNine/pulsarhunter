/*
 * Pair.java
 *
 * Created on 10 October 2007, 23:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter;

/**
 *
 * @author Mike Keith
 */
public class Pair <A,B> {
    
    private A a;
    private B b;
    
    /** Creates a new instance of Pair */
    public Pair(A a, B b) {
        this.a=a;
        this.b=b;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public String toString() {
        return a.toString()+":"+b.toString();
    }

    public boolean equals(Object obj) {
        if(obj instanceof Pair){
            Pair o = (Pair)obj;
            return this.a.equals(o.a) && this.b.equals(o.b);
        } else return false;
       
    }
    
    
    
}
