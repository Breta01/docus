package com.bretahajek.docus.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Tag {
    // TODO: Document count
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    @NonNull
    public String name;

    @Ignore
    public boolean state;

    @Ignore
    public boolean changed;

    public Tag(final String name) {
        this.name = name;
        this.state = false;
        this.changed = false;
    }

    public Tag(final String name, final boolean state) {
        this.name = name;
        this.state = state;
        this.changed = false;
    }

    public void switchState() {
        state = !state;
        changed = true;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public boolean isState() {
        return state;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
