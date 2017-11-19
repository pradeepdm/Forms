package com.forms;

import java.util.UUID;

import io.realm.RealmObject;

/**
 * Created on 9/16/2016.
 */
public class FieldObjects extends RealmObject {

    private String id;

    private int type;
    private String title;
    private String value;

    private String known_type;
    private boolean required;

    public FieldObjects(int type, String title, String value) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.title = title;
        this.value = value;
        this.required = false;
    }


    public FieldObjects(String id, int type, String title, String value, String known_type) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.value = value;
        this.known_type = known_type;
        this.required = false;
    }

    public FieldObjects(String id, int type, String title, String value, String known_type, boolean required) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.value = value;
        this.known_type = known_type;
        this.required = required;
    }

    public FieldObjects() {
    }


    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKnown_type() {
        return known_type;
    }

    public void setKnown_type(String known_type) {
        this.known_type = known_type;
    }
}
