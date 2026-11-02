package users;

import lookup.LookupItem;

public class Admin extends User {

    private int roleLevel;

    public Admin(int userId, String fullName, String email, String phone, String password, int roleLevel) {
        super(userId, fullName, email, phone, password);
        this.roleLevel = roleLevel;
    }

    public void verifySystemLookup(LookupItem item) {
        item.activate();
        System.out.println("[ADMIN " + fullName + "] verified & activated lookup item: " + item.getName());
    }

    public int getRoleLevel() { return roleLevel; }
    public void setRoleLevel(int roleLevel) { this.roleLevel = roleLevel; }
}
