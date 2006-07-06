/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
/*
 * Created on Jun 13, 2003
 *
 */
package edu.uci.ics.jung.visualization.decorators;

import edu.uci.ics.graph.Edge;

/**
 *
 * An EdgeStringer provides a string Label for any edge.
 *
 * @author danyelf
 *
 */
public interface EdgeStringer<E extends Edge> {

    public String getLabel(E e);
}