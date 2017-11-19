package com.forms;

import io.realm.RealmCollection;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created on 10/6/2016.
 */

public class FormObject extends RealmObject {

    @PrimaryKey
    private long id;



    private String name;
    private RealmList<FieldObjects> fields;

    public FormObject() {
        id = System.currentTimeMillis();
    }

    public FormObject(long id, String name, RealmList<FieldObjects> fields) {
        this.id = id;
        this.name = name;
        this.fields = fields;
    }






    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<FieldObjects> getFields() {
        return fields;
    }

    public void setFields(RealmList<FieldObjects> fields) {
        this.fields = fields;
    }
}
