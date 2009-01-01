// -*- Mode: Java; c-basic-offset: 4; tab-width: 8; -*-
// Copyright (C) 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package org.uberluser.ant;

import java.util.Vector;
import java.util.HashMap;
import java.io.PrintStream;

class Target {
    public final String name;
    private HashMap<String, String> attrs = new HashMap<String, String> ();
    private Vector<Node> tasks = new Vector<Node> ();
    private Vector<Target> deps = new Vector<Target> ();

    private boolean defined = false;

    Target (String n) {
	name = n;
    }

    public void setDefined () {
	defined = true;
    }

    public void addDep (Target t) {
	for (int i = 0; i < deps.size (); i ++) {
	    if (deps.elementAt (i) == t) return;
	}
	deps.addElement (t);
    }

    public void addNode (Node l) {
	tasks.addElement (l);
    }

    public void addAttr (String n, String v) {
	if (attrs.get (n) != null) {
	    throw new IllegalArgumentException ("attr " + n + " already set " +
						"on target " + name);
	}
	attrs.put (n, v);
    }

    public void dump (PrintStream ps, String pre) {
	if (!defined) {
	    throw new IllegalArgumentException ("Target " + name + " used but not defined");
	}
	ps.println ();
	String st = pre + "<target name=\"" + name + "\"";
	if (deps != null && deps.size () > 0) {
	    StringBuffer sb = new StringBuffer ();
	    sb.append (pre);
	    sb.append ("        depends=\"");
	    sb.append (deps.elementAt (0).name);
	    for (int i = 1; i < deps.size (); i ++) {
		sb.append (", ");
		sb.append (deps.elementAt (i).name);
	    }
	    sb.append ("\"");
	    ps.println (st);
	    st = sb.toString ();
	}
	for (String k: attrs.keySet ()) {
	    ps.println (st);
	    st = pre + "        " + k + "=\"" + attrs.get (k) + "\"";
	}
	if (tasks.size () == 0) {
	    ps.println (st + "/>");
	} else {
	    ps.println (st + ">");
	    for (Node l: tasks) {
		l.dumpXML (ps, pre + "  ");
	    }
	    ps.println (pre + "</target>");
	}
    }
}
