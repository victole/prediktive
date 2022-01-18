package com.victole.examples.core.dto;

import java.util.List;

public class UserListingResults {

    private List<String> users;

    public UserListingResults(List<String> users) {
        this.users = users;
    }

    public UserListingResults() {
        super();
    }

    public List<String> getUsers() {
        return users;
    }
}
