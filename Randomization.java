/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dzip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author thoang
 */
public class Randomization {
    Clustering c;
    ArrayList<Integer> seq;
    ArrayList<Integer> cid;
    ArrayList<HashMap<Integer,Double>> markov;
    ArrayList<Double> output;
    int NC; //the number of clusters
    Random r;
    
    void learn(){
        markov=new ArrayList();
        for(int i=0;i<seq.size()-1;i++){
            if(seq.get(i)>=markov.size()){
                for(int j=markov.size();j<=seq.get(i);j++)
                    markov.add(new HashMap());
            }
            if(!markov.get(seq.get(i)).containsKey(seq.get(i+1)))
                markov.get(seq.get(i)).put(seq.get(i+1), 1.0);
            else
                markov.get(seq.get(i)).put(seq.get(i+1),markov.get(seq.get(i)).get(seq.get(i+1))+1.0);
        }       
    }
    
    int move(int from){
        int to=from;
        double sum=0;
        ArrayList<Double> p=new ArrayList();
        for(Integer key:markov.get(from).keySet()){
           sum+=markov.get(from).get(key);
        }
        double n= r.nextInt((int)sum);
        sum=0;
        for(Integer key:markov.get(from).keySet()){
           sum+=markov.get(from).get(key);
           if(sum>n){
               to=key;
               break;
           }
        }
        return to;
    }
    
    void nextSample(){
        for(int i=0;i<c.clusters.size();i++)
            c.clusters.get(i).seq.clear();
        int current=5;
        for(int i=0;i<seq.size();i++){
            current=move(current);
            Event e=new Event(current,i);  
            c.clusters.get(cid.get(current)).seq.add(e);
        }        
        output.add(c.compress_size());
    }
    
    void randomSampleClusters(){
        HashMap<Integer,Integer> hm=new HashMap();
        for(int i=0;i<cid.size();i++){
                if(cid.get(i)==-1)
                    continue;
                int key=r.nextInt(NC);
                if(!hm.containsKey(key)){
                    hm.put(key, hm.size());
                }
                cid.set(i, hm.get(key));
        }     
        c.clusters.clear();
        for(int i=0;i<hm.size();i++){
            Cluster cc=new Cluster();
            cc.seq=new ArrayList();
            cc.alphabet=new ArrayList();
            c.clusters.add(cc);
        }
        for(int i=0;i<seq.size();i++){
            Event e=new Event(seq.get(i),i);  
            c.clusters.get(cid.get(seq.get(i))).seq.add(e);            
        }  
        for(int i=0;i<cid.size();i++){
            if(cid.get(i)==-1)
                continue;
            c.clusters.get(cid.get(i)).alphabet.add(i);            
        }
        output.add(c.compress_size());       
    }
     
    void samplingClusters(int K){
        c.hierarchical_clustering();
        double x=c.compress_size();
        NC=c.clusters.size();
        for(int i=0;i<c.clusters.size();i++)
            for(int j=0;j<c.clusters.get(i).alphabet.size();j++){
                cid.set(c.clusters.get(i).alphabet.get(j),i);
            }
        for(int i=0;i<K;i++)
            randomSampleClusters();
        System.out.println("p-value: "+p_value(1.98));
        System.out.println("p-value: "+p_value(1.99));
        chunkling();
    }
    
    void sampling(int K){
        c.hierarchical_clustering();
        double x=c.compress_size();
        NC=c.clusters.size();
        learn();
        for(int i=0;i<c.clusters.size();i++){
            for(int j=0;j<c.clusters.get(i).alphabet.size();j++)
                cid.set(c.clusters.get(i).alphabet.get(j), i);
        }        
        for(int i=0;i<K;i++)
            nextSample();
        System.out.println("p-value: "+p_value(x));
        chunkling();
    }
    
    void chunkling(){
        double mu=0,sigma=0; //parameters of the normal distribution
        for(int i=0;i<output.size();i++){
            mu+=output.get(i);
        }
        mu=mu/output.size();
        for(int i=0;i<output.size();i++){
            sigma+=(output.get(i)-mu)*(output.get(i)-mu);
        }
        sigma=sigma/output.size();
        sigma=Math.sqrt(sigma);
        double bin_size=sigma/5;
        int steps=(int)Math.round(2/bin_size);
        ArrayList<Integer> chunks=new ArrayList();
        for(int i=0;i<steps;i++){
            chunks.add(0);
        }
        for(int i=0;i<output.size();i++){
            chunks.set((int)Math.round(output.get(i) /bin_size), chunks.get((int)Math.round(output.get(i) /bin_size))+1);
        } 
         for(int i=0;i<steps;i++){
            System.out.println(i*bin_size+" "+chunks.get(i));
        }
         
    }
    
     /**
     * the cdf function of the Standard Normal distribution 
     * @param xx
     * @return 
     */
    double standard_normal_cdf(double xx){
	double x=xx;
	if (xx<0)
		x=-x;		
	double b0=0.2316419, b1=0.319381530, b2=-0.356563782, b3=1.781477937, b4=-1.821255978, b5=1.330274429;
	double t=1/(1+b0*x);
	double pi=4.0*Math.atan(1.0);
	double pdf= 1/Math.sqrt(2*pi)*Math.exp(-0.5*x*x); //standard normal distribution's pdf
	if (xx>0)	
		return 1-pdf*(b1*t+b2*t*t+b3*t*t*t+b4*t*t*t*t+b5*t*t*t*t*t);
	else
		return pdf*(b1*t+b2*t*t+b3*t*t*t+b4*t*t*t*t+b5*t*t*t*t*t);
    }
    
    double p_value(double x){
        double p=0;
        double mu=0,sigma2=0; //parameters of the normal distribution
        for(int i=0;i<output.size();i++){
            mu+=output.get(i);
        }
        mu=mu/output.size();
        for(int i=0;i<output.size();i++){
            sigma2+=(output.get(i)-mu)*(output.get(i)-mu);
        }
        sigma2=sigma2/output.size();
        p=Math.abs(x-mu);
        p=p/Math.sqrt(sigma2);
        return 1-standard_normal_cdf(p);
    }
    
    
    Randomization(String dataname, int N){
        Data d=new Data();
        c=d.readData(dataname, N); 
        seq=new ArrayList();
        int max=0;
        for(int i=0;i<c.Nevents;i++)
            seq.add(0);
        for(int i=0;i<c.clusters.size();i++){
            for(int j=0;j<c.clusters.get(i).seq.size();j++)
                seq.set(c.clusters.get(i).seq.get(j).timestamp,c.clusters.get(i).seq.get(j).id);
            for(int j=0;j<c.clusters.get(i).alphabet.size();j++)
                if(max<c.clusters.get(i).alphabet.get(j))
                    max=c.clusters.get(i).alphabet.get(j);
        }
        cid =new ArrayList();
        for(int i=0;i<=max;i++)
            cid.add(-1);
        r=new Random(System.currentTimeMillis());
        output= new ArrayList();
    }    
}
