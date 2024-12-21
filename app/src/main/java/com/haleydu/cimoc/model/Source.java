package com.haleydu.cimoc.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class Source {

    @Id
    private Long id;    //序号
    @NotNull
    private String title; //源名称
    @Unique
    private int type; //源类型
    @NotNull
    private boolean enable; //是否开启

    @Generated(hash = 615387317)
    public Source() {
    }

    @Generated(hash = 1339691905)
    public Source(Long id, @NotNull String title, int type, boolean enable) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.enable = enable;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Source && ((Source) o).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : id.hashCode();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
