/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dzip;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An implementation of the tree structured Lempel-Ziv algorithm, i.e. the LZ78 algorithm 
 * see Thomas and Cover: Elements of Information Theory Chapter 13.4.2 for more information about LZ78
 * @author thoang
 */
public class LZ78 {
    ArrayList<Event> seq; // the sequence
    ArrayList<Word> dictionary; // the dictionary in a prefix-tree form 
    HashMap<Integer,Integer> alphabet; // the alphabet, map from id to its index in the dictionary
    /**
     * create the alphabet and initialize the dictionary with characters in the alphabet
     * @return the number of distinct events in the sequence, i.e. the alphabet size 
     */
    int alphabet(){
        alphabet=new HashMap();
        dictionary = new ArrayList();
        for(int i=0;i<seq.size();i++){
            if(!alphabet.containsKey(seq.get(i).id)){
                dictionary.add(new Word(-1,seq.get(i).id,-1,-1,1));  
                alphabet.put(seq.get(i).id, dictionary.size()-1); 
                
            }
        }
        return alphabet.size();
    }
    
    /**
     * encode the sequence with the LZ78 algorithm 
     * @return the compressed size (# bits)
     */
    double encode(){
        double sz=0;
        int from=0,sum=0;;
        alphabet();
        int Nw=dictionary.size()-1;
        HashMap<Integer,Integer> gaps=new HashMap();
        while (true){
            int m=longestMatch(from);
            from+=dictionary.get(m).length;
            if(from>=seq.size()){
                Nw++;
                //sz+=elias(Nw-m); //cost of encoding the pointer with Elias Delta code
                if(!gaps.containsKey(Nw-m)){
                    gaps.put(Nw-m, 1);
                } else
                    gaps.put(Nw-m, gaps.get(Nw-m)+1);
                sum++;
                break;
            }
            dictionary.add(new Word(m,seq.get(from).id,-1,-1,dictionary.get(m).length+1));
            if(dictionary.get(m).first==-1)
                dictionary.get(m).first=dictionary.size()-1;
            else{
                int index=m,next=dictionary.get(m).first;
                while(next!=-1){
                    index=next;
                    next=dictionary.get(next).next;
                }
                dictionary.get(index).next=dictionary.size()-1;
            }
            Nw++;
            //sz+=elias(Nw-m); //cost of encoding the pointer with Elias Delta code
            if(!gaps.containsKey(Nw-m)){
                    gaps.put(Nw-m, 1);
            } else
                    gaps.put(Nw-m, gaps.get(Nw-m)+1);
            sum++;
            sz+=lowround(log2(alphabet.size()))+1;//cost of encoding the extended character
            from++;
            if(from>=seq.size())
                break;
        }
        
        for(Integer key:gaps.keySet()){
            sz+=gaps.get(key)*(lowround(log2(sum/(gaps.get(key)+0.0))+1));
        }
        return sz;
    }
    
    /**
     * find the longest dictionary word matching the subsequence starting at from
     * @param from the position starting the match
     * @return the index of the longest word in the dictionary matching the subsequence starting at from 
     */
    int longestMatch(int from){
        int nf=from;
        int index=alphabet.get(seq.get(nf).id);
        nf++;
        while(true){
            if(nf>=seq.size())
                return index;
            int next=dictionary.get(index).first;
            while(true){
                if(next==-1)
                    return index;
                if(dictionary.get(next).symbol==seq.get(nf).id){
                  nf++;
                  index=next;
                  break;
                }                
                next=dictionary.get(next).next;                  
            }
        }       
    }
    
    /**
     * logarithm with base 2
     * @param x the input
     * @return logarithm with base 2
     */
    public static double log2(double x){
        return Math.log(x)/Math.log(2);
    }
    
       
    /**
     * logarithm with base 2
     * @param x the input
     * @return logarithm with base 2
     */
    public double elias(double a){
        double x=log2(a);
            return lowround(x)+2*lowround(log2(lowround(x)+1))+1; //ellias delta
        //return Math.log(x+1)/Math.log(2);
    }
    /**
    * 
    * @param x
    * @return the lower round value of x 
    */
    int lowround(double x){
       int y=(int) Math.round(x);
       if(y>x)
           y=y-1;
       return y;
    }
    
    /**
     * merging the sequence with seq, the order of the events are preserved
     * @param sequence the sequence to be merged
     */
    void merge(ArrayList<Event> sequence){
        ArrayList<Event> m=new ArrayList();
        int i=0,j=0;
        while(true){
            if(i>=seq.size()&&j>=sequence.size())
                break;
            else if(i>=seq.size()){
                Event e=new Event(sequence.get(j).id,sequence.get(j).timestamp);
                m.add(e);
                j++;
            } else if (j>=sequence.size()){
                Event e=new Event(seq.get(i).id,seq.get(i).timestamp);
                m.add(e);
                i++;
            } else{
                if(seq.get(i).timestamp<sequence.get(j).timestamp){
                    Event e=new Event(seq.get(i).id,seq.get(i).timestamp);
                    m.add(e);
                    i++;
                } else if (seq.get(i).timestamp>sequence.get(j).timestamp){
                    Event e=new Event(sequence.get(j).id,sequence.get(j).timestamp);
                    m.add(e);
                    j++;
                }
            }                
        }
        seq.clear();
        seq=m;
    }
    
    /**
     * get the word content 
     * @param index the id of the word
     * @return the word content
     */
    ArrayList<Integer> getWord(int index){
        ArrayList<Integer> w=new ArrayList();
        int next=index;
        while(true){
            if(next>dictionary.size()||next==-1)
                return w;
            w.add(0, dictionary.get(next).symbol);
            next=dictionary.get(next).prefix;
        }        
    }
    
    /**
     * print the dictionary content
     */
    void printDictionary(){
        for(int i=0;i<dictionary.size();i++){
            System.out.print(getWord(i));
            System.out.println(" "+dictionary.get(i).prefix+" "+dictionary.get(i).symbol+" "+dictionary.get(i).first+" "+dictionary.get(i).next);
        }
        System.out.println("-----------------");
    }
}
