package com.forms;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created on 9/16/2016.
 */
public class PersonObject extends RealmObject {

    @PrimaryKey
    private long id;

    private String title;
    private long formId;

    private boolean draft;

    private RealmList<FieldObjects> extras;

    public PersonObject() {
        id = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RealmList<FieldObjects> getExtras() {
        return extras;
    }

    public void setExtras(RealmList<FieldObjects> extras) {
        this.extras = extras;
    }

    public long getFormId() {
        return formId;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public void setFormId(long formId) {
        this.formId = formId;
    }
}
