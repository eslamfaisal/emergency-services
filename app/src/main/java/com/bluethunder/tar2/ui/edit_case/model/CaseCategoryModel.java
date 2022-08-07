package com.bluethunder.tar2.ui.edit_case.model;


import androidx.annotation.Keep;

import java.io.Serializable;


@Keep
public final class CaseCategoryModel implements Serializable {

    private String id;

    private String nameAr;

    private String nameEn;

    private String reference;

    private Integer priority = 0;

    public CaseCategoryModel() {
    }

    public CaseCategoryModel(String id, String nameAr, String nameEn, String reference, Integer priority) {
        this.id = id;
        this.nameAr = nameAr;
        this.nameEn = nameEn;
        this.reference = reference;
        this.priority = priority;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "CaseCategoryModel{" +
                "id='" + id + '\'' +
                ", nameAr='" + nameAr + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", reference='" + reference + '\'' +
                ", priority=" + priority +
                '}';
    }
}
