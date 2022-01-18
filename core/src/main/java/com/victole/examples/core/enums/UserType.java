package com.victole.examples.core.enums;

import java.util.Arrays;

public enum UserType {

    EXTERNAL("external", new String[]{"external-users"}),
    EMPLOYEES("employees", new String[]{ "employees-users" }),
    ALL("all", new String[]{"external-users", "employees-users"}),
    NONE("none", new String[]{});

    private final String type;

    private final String[] groupNames;

    UserType(String type, String[] groupNames) {
        this.type = type;
        this.groupNames = groupNames;
    }

    public String getType() {
        return type;
    }

    public String[] getGroupNames() {
        if (groupNames != null) {
            return Arrays.copyOf(groupNames, groupNames.length);
        }
        return new String[0];
    }

    public static UserType valueFromUserType(String type) {
        if (type != null) {
            for (UserType userType : UserType.values()) {
                if (type.equals(userType.getType())) {
                    return userType;
                }
            }
        }
        return EMPLOYEES;
    }
}
