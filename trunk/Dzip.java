/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dzip;

import java.util.ArrayList;

/**
 *
 * @author thoang
 */
public class Dzip {

    /**
     * dzip up to N events of the data
     * @param dataname
     * @param N 
     */
    static void dzip(String dataname, int N){
        Data d=new Data();
        System.err.println("Reading the data ...");
        Clustering c=d.readData(dataname,N);
        c.hierarchical_clustering();
        c.labels=d.readLabels(dataname);
        c.print();        
    }
    
    /**
     * 
     * @param dataname
     * @param K should set to 300
     * @param alpha should set to 0.01
     * @param N 
     */
    static void dcomp(String dataname,int K, double alpha,int N){
        Data d=new Data();
        System.err.println("Reading the data ...");
        Decomposition c=d.readDataDecomposition(dataname,N);
        c.K=K;
        c.alpha=alpha;
        c.labels=d.readLabels(dataname);
        ArrayList<ArrayList<Integer>> r=c.decompose();
        for(int i=0;i<r.size();i++){
                System.out.print("[ ");
                for(int j=0;j<r.get(i).size();j++){
                     if(!c.labels.isEmpty()){
                         System.out.print(c.labels.get(r.get(i).get(j)));
                     } else{
                         System.out.print(r.get(i).get(j));
                     }
                     if(j!=r.get(i).size()-1)
                        System.out.print(" ");                                                 
                }
                System.out.println(" ]");                
        }
        
        for(int i=0;i<r.size();i++){
                for(int j=0;j<r.get(i).size();j++){
                      System.out.print(r.get(i).get(j));
                     if(j!=r.get(i).size()-1)
                        System.out.print(" ");                                                 
                }
                System.out.println();                
        }
    }
    
    
    static void compsize(String dataname, int N){
        Data d=new Data();
        System.err.println("Reading the data ...");
        Clustering c=d.readDataClusters(dataname,N);
        c.compress_size();
        System.out.println(c.size);        
    }
    
    static void randomization(String dataname, int N){
        Randomization r=new Randomization(dataname,N);
        //r.sampling(1000);
        r.samplingClusters(1000);
    }
    
     static void prediction(String dataname, int N){
        Markov r=new Markov(dataname,N);
        //r.learn_global();
        r.learn_lobal();
        r.prediction();       
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
       long startTime = System.currentTimeMillis();
       dzip(args[0], Integer.parseInt(args[1]));
       //randomization(args[0], Integer.parseInt(args[1]));
       //prediction(args[0], Integer.parseInt(args[1]));
       //compsize(args[0],Integer.parseInt(args[1]));
       //Test.LZ78();
       //Test.Dependency(); 
       //Test.Decomposition();
       //Test.Dzip();
       //dcomp(args[0],Integer.parseInt(args[1]),Double.parseDouble(args[2]),Integer.parseInt(args[3]));
       long endTime   = System.currentTimeMillis();
       long totalTime = endTime - startTime;
       System.out.println("Running time: "+totalTime/1000+" seconds");
    }
}
