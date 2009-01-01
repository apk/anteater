// -*- Mode: Java; c-basic-offset: 4; tab-width: 8; indent-tab-mode: nil -*-
// Copyright (C) 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>

package org.uberluser.ant;

public class Basifier {
    private String base;
    public Basifier (String b) {
	base = b != null ? b + "/" : "";
    }
    public String basify (String s) {
	return base + s;
    }
    public String basify (String s, String def) {
	return basify (s != null ? s : def);
    }
}
