package presentation;

import lookup.DonationCategory;
import lookup.LookupItem;
import lookup.LookupService;
import lookup.Skill;
import users.Admin;
import users.Registration;
import users.User;
import users.UserService;

/**
 * MVC Controller for the user control panel - FULL CRUD per user.
 *  Create : createAccount()        (admin creates users; users self-register on the login screen)
 *  Read   : viewProfile(), refreshUsers()
 *  Update : updateUserProfile(), changeUserPassword()
 *  Delete : deleteAccount(), deleteUser()
 *  + CRUD on lookup tables (admin): manageLookupTable()
 */
public class UserControlPanelController {

    private User userModel;
    private UserControlPanelView view;
    private LookupService lookupService;
    private UserService userService;
    private Runnable onAccountDeleted = () -> { };

    public UserControlPanelController(User userModel, UserControlPanelView view, LookupService lookupService,
                                      UserService userService) {
        this.userModel = userModel;
        this.view = view;
        this.lookupService = lookupService;
        this.userService = userService;
        bind();
    }

    public void setOnAccountDeleted(Runnable r) { this.onAccountDeleted = r; }

    private void bind() {
        view.onUpdateProfile(e -> updateUserProfile(view.getNameInput(), view.getPhoneInput()));
        view.onChangePassword(e -> { changeUserPassword(view.getNewPassword()); view.clearPassword(); });
        view.onDeleteAccount(e -> deleteAccount());
        view.onDeleteUser(e -> deleteUser(view.getSelectedUser()));
        view.getCreateForm().onSubmit(e -> {
            try { createAccount(view.getCreateForm().read()); }
            catch (IllegalArgumentException ex) { view.showError(ex.getMessage()); }
        });
        view.onLookupRead(e -> manageLookupTable(null, "read"));
        view.onLookupCreate(e -> manageLookupTable(buildLookupFromForm(true), "create"));
        view.onLookupUpdate(e -> manageLookupTable(buildLookupFromForm(false), "update"));
        view.onLookupDelete(e -> manageLookupTable(view.getSelectedLookup(), "delete"));
        view.onLookupActivate(e -> manageLookupTable(view.getSelectedLookup(), "activate"));
        view.onLookupDeactivate(e -> manageLookupTable(view.getSelectedLookup(), "deactivate"));
        view.renderProfile(userModel);
        if (isAdmin()) {
            refreshUsers();
            manageLookupTable(null, "read");
        }
    }

    private boolean isAdmin() { return userModel instanceof Admin; }

    // ---------- Create ----------
    public void createAccount(Registration r) {
        if (!isAdmin()) { view.showError("Access denied: only an admin can create users here"); return; }
        try {
            User u = userService.createAccount(r);
            view.showStatusMessage("User created: " + u.getFullName() + " (" + u.getRole() + ")");
            view.getCreateForm().clear();
            refreshUsers();
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }

    // ---------- Read ----------
    public void viewProfile() {
        view.renderProfile(userModel);
    }

    public void refreshUsers() {
        if (isAdmin()) view.displayUsers(userService.getAll());
    }

    // ---------- Update ----------
    public void updateUserProfile(String name, String phone) {
        try {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be empty");
            UserService.validatePhone(phone);
            userModel.updateProfile(name, phone);
            userService.update(userModel);
            view.showStatusMessage("Profile updated successfully");
            view.renderProfile(userModel);
            refreshUsers();
        } catch (IllegalArgumentException ex) {
            view.showError("Profile NOT updated: " + ex.getMessage());
        }
    }

    public void changeUserPassword(String newPass) {
        try {
            userModel.changePassword(newPass);
            view.showStatusMessage("Password changed successfully");
        } catch (IllegalArgumentException ex) {
            view.showError("Password NOT changed: " + ex.getMessage());
        }
    }

    // ---------- Delete ----------
    public void deleteAccount() {
        if (userModel instanceof Admin && userService.countAdmins() <= 1) {
            view.showError("You are the last admin - create another admin first");
            return;
        }
        if (!view.confirm("Delete your account permanently?")) return;
        userService.delete(userModel);
        view.showStatusMessage("Account of " + userModel.getFullName() + " deleted");
        onAccountDeleted.run();
    }

    public void deleteUser(User target) {
        if (!isAdmin()) { view.showError("Access denied"); return; }
        if (target == null) { view.showError("Select a user first"); return; }
        if (target == userModel) { view.showError("Use 'Delete my account' for your own account"); return; }
        if (target instanceof Admin && userService.countAdmins() <= 1) {
            view.showError("Cannot delete the last admin");
            return;
        }
        if (!view.confirm("Delete user " + target.getFullName() + "?")) return;
        userService.delete(target);
        view.showStatusMessage("User deleted: " + target.getFullName());
        refreshUsers();
    }

    // ---------- Lookup tables (admin) ----------
    private LookupItem buildLookupFromForm(boolean isNew) {
        int id;
        if (isNew) {
            id = lookupService.nextId();
        } else {
            LookupItem sel = view.getSelectedLookup();
            if (sel == null) return null;
            id = sel.getId();
        }
        LookupItem item = view.isCategorySelected()
                ? new DonationCategory(id, view.getLookupCode(), view.getLookupName(), view.isTaxDeductible())
                : new Skill(id, view.getLookupCode(), view.getLookupName(), view.getLookupExtra());
        LookupItem current = isNew ? null : view.getSelectedLookup();
        if (current != null && !current.isActive()) item.deactivate();   // keep the active flag on update
        return item;
    }

    public void manageLookupTable(LookupItem item, String action) {
        if (!isAdmin()) {
            view.showError("Access denied: lookup tables are managed by admins");
            return;
        }
        try {
            String a = action.toLowerCase();
            if (!a.equals("read") && item == null) throw new IllegalArgumentException("Select an item first");
            if (item != null && !a.equals("read") && !a.equals("delete") && !a.equals("activate") && !a.equals("deactivate")
                    && (item.getCode().isBlank() || item.getName().isBlank()))
                throw new IllegalArgumentException("Code and name are required");
            switch (a) {
                case "create":
                    lookupService.create(item);
                    view.showStatusMessage("Lookup item created: " + item.getName());
                    break;
                case "read":
                    view.displayLookup(lookupService.readAll());
                    return;
                case "update":
                    lookupService.update(item);
                    view.showStatusMessage("Lookup item updated: " + item.getName());
                    break;
                case "delete":
                    if (!view.confirm("Delete '" + item.getName() + "'?")) return;
                    lookupService.delete(item.getId());
                    view.showStatusMessage("Lookup item deleted: " + item.getName());
                    break;
                case "activate":
                    ((Admin) userModel).verifySystemLookup(item);   // admin verifies & activates
                    lookupService.update(item);
                    view.showStatusMessage("Activated: " + item.getName());
                    break;
                case "deactivate":
                    item.deactivate();
                    lookupService.update(item);
                    view.showStatusMessage("Deactivated: " + item.getName());
                    break;
                default:
                    view.showError("Unknown action: " + action);
                    return;
            }
            view.displayLookup(lookupService.readAll());
        } catch (IllegalArgumentException ex) {
            view.showError("Lookup action failed: " + ex.getMessage());
        }
    }
}
