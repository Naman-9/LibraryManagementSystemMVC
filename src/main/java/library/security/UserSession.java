
package library.security;

import library.exception.AuthenticationException;

public class UserSession {
    private final String userId;
    private final UserRole role;

    public UserSession(String userId, UserRole role) {

        if (userId == null || userId.isBlank()) {
            throw new AuthenticationException("Invalid user ID.");
        }

        if (role == null) {
            throw new AuthenticationException("User role cannot be null.");
        }

        this.userId = userId;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }

}
