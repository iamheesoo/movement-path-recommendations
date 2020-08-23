package com.project.mpr;

public class NodeAndDist implements Comparable<NodeAndDist>{
    public int index;
    public double dist;

    public NodeAndDist(){}
    public NodeAndDist(int i, double dist){
        this.index=i;
        this.dist=dist;
    }

    @Override
    public int compareTo(NodeAndDist nodeAndDist) {
        if(this.dist<nodeAndDist.dist){
            return -1;
        }else if(this.dist>nodeAndDist.dist){
            return 1;
        }
        return 0;
    }
}
