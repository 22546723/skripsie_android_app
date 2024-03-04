package com.example.planthelper;

public class AddDeviceModel {
    private String device_name;
    private boolean can_edit;

    public AddDeviceModel(String device_name) {
        this.device_name = device_name;
        this.can_edit = false;
    }

    public String getDevice_name() {
        return this.device_name;
    }

    public boolean getCanEdit() {
        return  this.can_edit;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public void setCan_edit(boolean can_edit) {
        this.can_edit = can_edit;
    }

}
