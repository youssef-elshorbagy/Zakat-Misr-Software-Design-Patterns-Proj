package presentation;

import users.Registration;
import users.User;
import users.UserService;

import java.util.function.Consumer;

/** MVC Controller - login and self-registration (the "Create" of the user CRUD). */
public class AuthController {

    private final UserService userService;
    private final AuthView view;
    private final Consumer<User> onLoggedIn;

    public AuthController(UserService userService, AuthView view, Consumer<User> onLoggedIn) {
        this.userService = userService;
        this.view = view;
        this.onLoggedIn = onLoggedIn;
        view.onLogin(e -> login(view.getEmail(), view.getPassword()));
        view.getRegistrationForm().onSubmit(e -> register());
    }

    public void login(String email, String password) {
        User u = userService.authenticate(email, password);
        if (u == null) {
            view.showError("Wrong e-mail or password");
            return;
        }
        view.reset();
        onLoggedIn.accept(u);
    }

    private void register() {
        try {
            Registration r = view.getRegistrationForm().read();
            userService.createAccount(r);
            view.getRegistrationForm().clear();
            view.showStatusMessage("Account created - you can log in now");
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }
}
