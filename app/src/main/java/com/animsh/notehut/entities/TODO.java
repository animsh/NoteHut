package com.animsh.notehut.entities;

import java.io.Serializable;

public class TODO implements Serializable {

    private boolean isChecked;
    private String taskName;

    public TODO(boolean isChecked, String taskName) {
        this.isChecked = isChecked;
        this.taskName = taskName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
