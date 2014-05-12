/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package edu.uci.ics.jung.algorithms.cluster;

import java.util.Collection;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections4.Factory;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;


/**
 * @author Scott White
 */
public class TestEdgeBetweennessClusterer extends TestCase {
    public static Test suite() {
        return new TestSuite(TestEdgeBetweennessClusterer.class);
    }
    Factory<Graph<Integer,Number>> graphFactory;
    Factory<Integer> vertexFactory;
    Factory<Number> edgeFactory;

    @Override
    protected void setUp() {
        graphFactory = new Factory<Graph<Integer,Number>>() {
    		public Graph<Integer,Number> create() {
    			return new SparseMultigraph<Integer,Number>();
    		}
    	};
    	vertexFactory = new Factory<Integer>() {
    		int n = 0;
    		public Integer create() { return n++; }
    	};
    	edgeFactory = new Factory<Number>() {
    		int n = 0;
    		public Number create() { return n++; }
    	};

    }

    public void testRanker() {
    	
    	Graph<Number,Number> graph = new SparseMultigraph<Number,Number>();
    	for(int i=0; i<10; i++) {
    		graph.addVertex(i+1);
    	}
    	int j=0;
    	graph.addEdge(j++,1,2);
    	graph.addEdge(j++,1,3);
    	graph.addEdge(j++,2,3);
    	graph.addEdge(j++,5,6);
    	graph.addEdge(j++,5,7);
    	graph.addEdge(j++,6,7);
    	graph.addEdge(j++,8,10);
    	graph.addEdge(j++,7,8);
    	graph.addEdge(j++,7,10);
    	graph.addEdge(j++,3,4);
    	graph.addEdge(j++,4,6);
    	graph.addEdge(j++,4,8);

        Assert.assertEquals(graph.getVertexCount(),10);
        Assert.assertEquals(graph.getEdgeCount(),12);

        EdgeBetweennessClusterer<Number, Number> clusterer = new EdgeBetweennessClusterer<Number, Number>(3);
        Collection<Set<Number>> clusters = clusterer.transform(graph);
        
        Assert.assertEquals(clusters.size(),3);
    }
}
