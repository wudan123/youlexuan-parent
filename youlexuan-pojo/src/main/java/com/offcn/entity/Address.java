package com.offcn.entity;

import com.offcn.pojo.TbAddress;
import com.offcn.pojo.TbUser;

import java.io.Serializable;

public class Address implements Serializable {
    private TbAddress address;

    private TbUser user;

    public TbAddress getAddress() {
        return address;
    }

    public void setAddress(TbAddress address) {
        this.address = address;
    }

    public TbUser getUser() {
        return user;
    }

    public void setUser(TbUser user) {
        this.user = user;
    }
}
