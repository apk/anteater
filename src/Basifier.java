// -*- Mode: Java; c-basic-offset: 4; tab-width: 8; indent-tabs-mode: nil; -*-
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
        if (s == null) {
            if (def == null) {
                return null;
            }
            return basify (def);
        }
        return basify (s);
    }
}
