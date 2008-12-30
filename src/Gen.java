// -*- Mode: Java; c-basic-offset: 4; tab-width: 8; -*-
// Copyright (C) 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package org.uberluser.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.*;
import java.util.Vector;
import java.util.HashMap;
import java.io.PrintStream;

public class Gen extends Task {
    private String say, txt;
    private File dst = new File ("def.xml"), src;

    public void setSay (String say) { this.say = say; }
    public void setDest (File dst) { this.dst = dst; }
    public void setSrc (File src) { this.src = src; }

    public void addText (String txt) { this.txt = txt;
	System.out.println ("Setting txt: <<" + txt + ">>");
    }

    private HashMap<String, Target> targets;

    // The method executing the task
    public void execute() throws BuildException {
	if (say != null) {
	    System.out.println (say);
	}
        System.out.println (" -> " + dst);
	Node ls = new Node ("*top*", "", 1, null);
	try {
	    if (src != null) {
		System.out.println (" <- " + src);
		Vector<String> res = new Vector<String> ();
		StringBuffer sb = new StringBuffer ();
		Reader f = new FileReader (src);
		int n = 0;
		while (true) {
		    int c = f.read ();
		    if (c == -1) break;
		    if (c == '\n') {
			String r = sb.toString ();
			res.addElement (r);
			sb = new StringBuffer ();
			n = 0;
		    } else {
			if (c == '\t') {
			    do {
				sb.append (' ');
				n ++;
			    } while ((n % 8) != 0);
			} else {
			    sb.append ((char)c);
			    n ++;
			}
		    }
		}
		String r = sb.toString ();
		if (r.length () > 0) {
		    res.addElement (r);
		}
// 	    for (String s: res) {
// 		System.out.println (">" + s + "<");
// 	    }
		ls.parseBody (res);
// 	    ls.dump ();
	    }
	    if (txt != null) {
		Vector<String> res = new Vector<String> ();
		for (String a: txt.split ("\n")) {
		    res.addElement (a);
		}
		ls.parseBody (res);
	    }
	    targets = new HashMap<String, Target> ();
	    for (Node l: ls.getNodes ()) {
		interp (l);
	    }
	    PrintStream ps = new PrintStream (new FileOutputStream (dst));
	    ps.println ("<!-- generated - do not edit - use " + src + " -->");
	    ps.println ("<project>");
	    for (Target t: targets.values ()) {
		t.dump (ps, "  ");
	    }
	    ps.println ("</project>");
	    ps.close ();
	} catch (IOException e) {
	    throw new BuildException (e);
	}
    }

    private Target getTarget (String n) {
	Target t = targets.get (n);
	if (t == null) {
	    t = new Target (n);
	    targets.put (n, t);
	}
	return t;
    }

    private static int cnt = 0;

    private void interp (Node l) {
	if (l.tag == "javac") {
	    AttrList al = new AttrList (l.getParam ());
	    String base = al.pull ("dir");
	    if (base == null) base = "";
	    else base += "/";
	    String src = al.pull ("src");
	    if (src == null) src = "src";
	    src = base + src;
	    String cls = al.pull ("classes");
	    if (cls == null) cls = "classes";
	    cls = base + cls;
	    String jar = al.pull ("jar");
	    if (jar == null) jar = "jar";
	    jar = base + jar;
	    if (!al.empty ()) {
		throw new IllegalArgumentException ("extra args in javac");
	    }
	    Target lcomp = getTarget ("compile-" + ++ cnt);
	    Target lclean = getTarget ("clean-" + ++ cnt);
	    getTarget ("compile").addDep (lcomp);
	    getTarget ("clean").addDep (lclean);
	    // Java is stupid
	    final String srcdir = src;
	    final String clsdir = cls;
	    final String jardir = jar;
	    lcomp.addNode (new Node ("delete") {{
		    addParam ("dir", clsdir);
	    }});
	    lcomp.addNode (new Node ("mkdir") {{
		    addParam ("dir", clsdir);
	    }});
	    lcomp.addNode (new Node ("javac") {{
		    addParam ("srcdir", srcdir);
		    addParam ("destdir", clsdir);
		    addNode (new Node ("classpath") {{
			addNode (new Node ("fileset") {{
			    addParam ("dir", "${basedir}");
			    addNode (new Node ("include") {{
				addParam ("name", "lib/*.jar");
			    }});
			}});
		    }});
	    }});
	    lcomp.addNode (new Node ("mkdir") {{
		    addParam ("dir", jardir);
	    }});
	    lcomp.addNode (new Node ("jar") {{
		addParam ("destfile", jardir + "/cls.jar"/*XXX*/);
		addNode (new Node ("fileset") {{
		    addParam ("dir", clsdir);
		}});
	    }});
//   <target name="compile" depends="resolve" description="--> compile the project">
//     <mkdir dir="${classes.dir}" />
//     <javac srcdir="${src.dir}" destdir="${classes.dir}" debug="true" source="1.6" target="1.6">
//       <classpath>
//         <fileset dir="${basedir}">
//           <include name="${lib.dir}/*.jar"/>
//           <include name="${libspecial.dir}/*.jar" />
//         </fileset>
//       </classpath>
//     </javac>
//   </target>

	    return;
	}
	if (l.tag == "target") {
	    AttrList al = new AttrList (l.getParam ());
	    String n = al.pull ("name");
	    if (n == null) throw new IllegalArgumentException ("no name in target");
	    Target t = getTarget (n);
	    while ((n = al.pull ("dep")) != null) {
		t.addDep (getTarget (n));
	    }
	    while ((n = al.pull ("from")) != null) {
		getTarget (n).addDep (t);
	    }
	    if (al.isPresent ("depends")) {
		throw new IllegalArgumentException ("sorry, no dependencies in target allowed. Use multiple deps");
	    }
	    // positive name check? Can leave that to ant...
	    while ((n = al.firstName ()) != null) {
		String v = al.pull (n);
		t.addAttr (n, v);
	    }
	    for (Node x: l.getNodes ()) {
		t.addNode (x);
	    }
	    return;
	}
	throw new IllegalArgumentException ("Bad tag " + l.tag);
    }
}
