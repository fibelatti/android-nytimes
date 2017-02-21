
package com.fibelatti.nytimes.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Byline {

    @SerializedName("person")
    @Expose
    private List<Person> person = null;
    @SerializedName("original")
    @Expose
    private String original;
    @SerializedName("contributor")
    @Expose
    private String contributor;

    public List<Person> getPerson() {
        return person;
    }

    public void setPerson(List<Person> person) {
        this.person = person;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

}
