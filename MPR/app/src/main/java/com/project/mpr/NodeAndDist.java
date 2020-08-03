package com.project.mpr;

public class NodeAndDist implements Comparable<NodeAndDist>{
    public String node;
    public Double dist;

    public NodeAndDist(){}
    public NodeAndDist(String node, Double dist){
        this.node=node;
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
