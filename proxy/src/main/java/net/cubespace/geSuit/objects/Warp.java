package net.cubespace.geSuit.objects;

import com.google.common.base.Strings;

public class Warp {
    private Location loc;
    private String name;
    private boolean hidden;
    private boolean global;
    private String description;

    public Warp(String name, Location loc, boolean hidden, boolean global) {
        this.name = name;
        this.loc = loc;
        this.hidden = hidden;
        this.global = global;
        this.description = "";
    }

    public Warp(String name, Location loc, boolean hidden, boolean global, String description) {
        this.name = name;
        this.loc = loc;
        this.hidden = hidden;
        this.global = global;
        this.description = description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public Location getLocation() {
        return loc;
    }

    public String getDescriptionOrName() {
        if (Strings.isNullOrEmpty(description))
            return name;
        else
            return description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }
}
