package io.github.adainish.clandorus.obj.clan;

import io.github.adainish.clandorus.enumeration.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Role {
    private Roles role;
    private List<UUID> memberList = new ArrayList<>();
    public Role() {}

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public List <UUID> getMemberList() {
        return memberList;
    }

    public void setMemberList(List <UUID> memberList) {
        this.memberList = memberList;
    }
}
