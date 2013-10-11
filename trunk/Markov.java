/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dzip;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author thoang
 */
public class Markov {
    ArrayList<Integer> seq;
    ArrayList<Integer> cid; 
    ArrayList<HashMap<Integer,Double>> markov;
    ArrayList<Integer> prediction;
    double split; // split into training and testing
    
    void learn_global(){
        markov=new ArrayList();
        for(int i=0;i<seq.size()*split;i++){
            if(seq.get(i)>=markov.size()){
                for(int j=markov.size();j<=seq.get(i);j++)
                    markov.add(new HashMap());
            }
            if(!markov.get(seq.get(i)).containsKey(seq.get(i+1)))
                markov.get(seq.get(i)).put(seq.get(i+1), 1.0);
            else
                markov.get(seq.get(i)).put(seq.get(i+1),markov.get(seq.get(i)).get(seq.get(i+1))+1.0);
        }
        
        prediction =new ArrayList();
        for(int i=0;i<markov.size();i++){
            double max=0;
            int max_transition=i;
            for(Integer key:markov.get(i).keySet()){
                if(markov.get(i).get(key)>max){
                    max=markov.get(i).get(key);
                    max_transition=key;
                }
            }
            prediction.add(max_transition);
        }
        for(int i=0;i<prediction.size();i++){
            System.out.println(i + " --> " +prediction.get(i));
        }
    }
    
    void learn_lobal(){
        markov=new ArrayList();
        for(int i=0;i<seq.size()*split;i++){
            if(seq.get(i)>=markov.size()){
                for(int j=markov.size();j<=seq.get(i);j++)
                    markov.add(new HashMap());
            }
            if(!markov.get(seq.get(i)).containsKey(seq.get(i+1)))
                markov.get(seq.get(i)).put(seq.get(i+1), 1.0);
            else
                markov.get(seq.get(i)).put(seq.get(i+1),markov.get(seq.get(i)).get(seq.get(i+1))+1.0);
        }
        
        prediction =new ArrayList();
        for(int i=0;i<markov.size();i++){
            double max=0;
            int max_transition=i;
            for(Integer key:markov.get(i).keySet()){
                if(markov.get(i).get(key)>max&&cid.get(i)==cid.get(key)){
                    max=markov.get(i).get(key);
                    max_transition=key;
                }
            }
            prediction.add(max_transition);
        }
        
        for(int i=0;i<prediction.size();i++){
            System.out.println(i + " --> " +prediction.get(i));
        }
    }
    
    void prediction(){
        int correct=0;
        int N=5;
        for(int i=(int)Math.round(seq.size()*split)-N;i<seq.size()-1;i++){
            if(prediction.get(seq.get(i))==seq.get(i+1)||prediction.get(seq.get(i))==seq.get(i+2)||prediction.get(seq.get(i))==seq.get(i+3)||prediction.get(seq.get(i))==seq.get(i+4)||prediction.get(seq.get(i))==seq.get(i+5)){
                correct++;
            }
        }
        System.out.println(correct/(seq.size()-seq.size()*split));
    }
    
    Markov(String dataname, int N){
        Clustering c;
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
        c.hierarchical_clustering();
         c.labels=d.readLabels(dataname);
        c.print();
        for(int i=0;i<c.clusters.size();i++){
            if(c.clusters.get(i).alphabet.size()>1){
                for(int j=0;j<c.clusters.get(i).alphabet.size();j++)
                    cid.set(c.clusters.get(i).alphabet.get(j), i);
            }
        }
        split=0.8;           
    }
}
