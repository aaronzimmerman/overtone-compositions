package com.zimmermusic;


import java.util.*;

/**
 * This tracks a value given envelope from the current time
 * Every MS is given a value
 */
public class TemporalVariable {

    private final String name;

    public TemporalVariable(String name){
        this.name = name;
    }


    private Queue<Double> upcomingValues = new LinkedList<Double>();

    public void addEnvelope(Collection<Double> fromNow){

        try {
            Queue<Double> newQ = new LinkedList<Double>();

            Iterator<Double> newIt = fromNow.iterator();

            synchronized (upcomingValues) {

                if (!upcomingValues.isEmpty()) {
                    Iterator<Double> it = upcomingValues.iterator();

                    while (it.hasNext() && newIt.hasNext()) {
                        double one = it.next();
                        double two = newIt.next();
                        //System.out.println("was " + one + " adding " + two);
                        newQ.add(one + two);
                    }

                }
                while (newIt.hasNext()) {
                    newQ.add(newIt.next());
                }

                upcomingValues = newQ;
            }
        } catch (Exception e){
            System.out.println("ERROR:  " + e.getMessage());
            e.printStackTrace();
        }


    }

    public double valueForNow(){

        synchronized(upcomingValues) {
            if (upcomingValues.isEmpty()) {
                return 0.0;
            }

            return upcomingValues.poll();
        }
    }



}
