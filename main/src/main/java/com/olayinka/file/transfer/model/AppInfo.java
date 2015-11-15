package com.olayinka.file.transfer.model;

/**
 * Created by Olayinka on 11/2/2015.
 */
public class AppInfo {
    private long dbVersion;
    private String name;
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(long dbVersion) {
        this.dbVersion = dbVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppInfo appInfo = (AppInfo) o;

        if (dbVersion != appInfo.dbVersion) return false;
        if (name != null ? !name.equals(appInfo.name) : appInfo.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (dbVersion ^ (dbVersion >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
