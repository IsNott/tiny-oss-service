package com.nott.movie.entity;

import java.io.Serializable;

public class SysMovieFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String parentId;

    private String fileId;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}