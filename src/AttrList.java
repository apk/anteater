// -*- Mode: Java; c-basic-offset: 4; tab-width: 8; -*-
// Copyright (C) 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package org.uberluser.ant;

import java.util.Vector;
import java.util.HashMap;

class AttrList {
    private Vector<String> names = new Vector<String> ();
    private Vector<String> values = new Vector<String> ();

    AttrList (String s) {
	int p = 0;
	while (true) {
	    while (s.length () > p && Node.isspc (s.charAt (p))) {
		p ++;
	    }
	    if (s.length () == p) return;

	    int q = p;
	    while (s.length () > p && Node.isidc (s.charAt (p))) {
		p ++;
	    }
	    if (q == p) throw new IllegalArgumentException ("bad name for attrlist");
	    String n = s.substring (q, p);
	    while (s.length () > p && Node.isspc (s.charAt (p))) {
		p ++;
	    }
	    if (s.length () == p || s.charAt (p) != '=') {
		throw new IllegalArgumentException ("no '=' for attrlist");
	    }
	    p ++;

	    while (s.length () > p && Node.isspc (s.charAt (p))) {
		p ++;
	    }
	    
	    if (s.length () == p || s.charAt (p) != '"') {
		System.out.println ("<" + p + ":" + s + ">");
		throw new IllegalArgumentException ("no initial '\"' for attrlist");
	    }
	    p ++;
	    StringBuffer sb = new StringBuffer ();
	    while (s.length () > p && s.charAt (p) != '"') {
		sb.append (s.charAt (p));
		p ++;
	    }
	    if (s.length () == p || s.charAt (p) != '"') {
		throw new IllegalArgumentException ("no final '\"' for attrlist");
	    }
	    p ++;
	    names.addElement (n.intern ());
	    values.addElement (sb.toString ());
	}
    }

    public boolean empty () {
	return names.size () == 0;
    }

    public String firstName () {
	if (names.size () > 0) return names.elementAt (0);
	return null;
    }

    public String pull (String n) {
	n = n.intern ();
	for (int i = 0; i < names.size (); i ++) {
	    if (names.elementAt (i) == n) {
		n = values.elementAt (i);
		values.removeElementAt (i);
		names.removeElementAt (i);
		return n;
	    }
	}
	return null;
    }

    public boolean isPresent (String n) {
	n = n.intern ();
	for (int i = 0; i < names.size (); i ++) {
	    if (names.elementAt (i) == n) {
		return true;
	    }
	}
	return false;
    }
}
