// -*- Mode: Java; c-basic-offset: 4; tab-width: 8; -*-
// Copyright (C) 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package org.uberluser.ant;

import java.util.Vector;
import java.io.PrintStream;

class Node {
    final public int lineno;
    final public String tag;
    private String param;
    private Vector<Node> subs;

    public Node (String t, String l, int n, Vector<String> il) {
	tag = t.intern ();
	lineno = n;
	param = l;
	subs = null;

	if (il != null) {
	    parseBody (il);
	}
    }

    protected Node (String t) {
	tag = t.intern ();
	lineno = -1;
	param = null;
	subs = null;
    }

    public void addParam (String n, String v) {
	addParam (n + "=\"" + v + "\"");
    }

    public void addParam (String v) {
	if (param != null) {
	    param = param + " " + v;
	} else {
	   param = v;
	}
    }

    public String getParam () {
	return param;
    }

    final private static Vector<Node> empty = new Vector<Node> ();

    public Vector<Node> getNodes () {
	// XXX Stupid....we want to return something for foreach, but
	// not make the vector modifiable...
	return subs != null ? subs : empty;
    }

    public void dump () {
	dump ("");
    }

    private void dump (String pre) {
	System.out.println (pre + "[" + tag + "|" + param + "]");
	if (subs != null) {
	    pre += "  ";
	    for (Node l: subs) {
		l.dump (pre);
	    }
	}
    }

    public void dumpXML (PrintStream ps, String pre) {
	String e = subs != null ? ">" : "/>";
	if (param != null) {
	    ps.println (pre + "<" + tag + " " + param + e);
	} else {
	    ps.println (pre + "<" + tag + e);
	}
	if (subs != null) {
	    for (Node l: subs) {
		l.dumpXML (ps, pre + "  ");
	    }
	    ps.println (pre + "</" + tag + ">");
	}
    }

    public void parseBody (Vector<String> il) {
	Vector<String> sub = null;
	String tg = null;
	String par = null;
	int pos = 0;
	for (String s: il) {
	    System.out.println ("'" + s + "'");
	    int p = 0;
	    while (s.length () > p && isspc (s.charAt (p))) {
		p ++;
	    }
	    if (s.length () == p) {
		// Empty line
		continue;
	    }
	    if (sub != null) {
		// Have open tag for which this might be
		if (p > pos) {
		    // Goes into this tag...
		    sub.addElement (s);
		    continue;
		}
		// Need to close current element and see current line
		// in that context
		addsub (new Node (tg, par, 0, sub));
		sub = null;
		tg = null;
	    }
	    // See if there is a tag
	    int q = p;
	    while (s.length () > q && isidc (s.charAt (q))) {
		q ++;
	    }
	    if (q > p && s.length () > q && isspc (s.charAt (q))) {
		String n = s.substring (p, q);
		q ++;
		// There is a tag
		if (p > pos && tg != null) {
		    // It start the body of the tag we are scanning
		    // sub is alwas null here
		    sub = new Vector<String> ();
		    sub.addElement (s);
		    continue;
		} else {
		    // It starts a new tag on our level
		    if (tg != null) {
			addsub (new Node (tg, par, 0, sub));
			sub = null;
			tg = null;
		    }
		    pos = p;
		    tg = n;
		    par = s.substring (q);
		}
	    } else {
		// No tag on this line
		if (tg == null) {
		    throw new IllegalArgumentException ("bad param position");
		}
		par = par + " " + s.substring (p);
	    }
	}
	if (tg != null) {
	    addsub (new Node (tg, par, 0, sub));
	}
    }

    private void addsub (Node l) {
	if (subs == null) {
	    subs = new Vector<Node> ();
	}
	subs.addElement (l);
    }

    public void addNode (Node l) {
	addsub (l);
    }

    public final static boolean isspc (char c) {
	return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    public final static boolean isidc (char c) {
	return
	    (c >= 'a' && c <= 'z') ||
	    (c >= 'A' && c <= 'Z') ||
	    (c >= '0' && c <= '9') ||
	    c == '.' || c == '-' || c == '_' || c == ':';
    }
}
