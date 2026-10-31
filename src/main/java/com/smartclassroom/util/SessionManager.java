package com.smartclassroom.util;

import com.smartclassroom.model.User;

/**
 * Thread-safe singleton that holds the currently authenticated {@link User} for
 * the duration of an application session.
 *
 * <p>Retrieve the singleton via {@link #getInstance()}. All JavaFX controllers
 * should read/write the current user through this class rather than passing it
 * between scenes manually.
 */
public class SessionManager {

    /** The single instance of this class. */
    private static SessionManager instance;

    /** The currently logged-in user; null when no one is authenticated. */
    private User currentUser;

    /**
     * Private constructor – use {@link #getInstance()} to obtain the singleton.
     */
    private SessionManager() {
    }

    /**
     * Returns the singleton {@code SessionManager} instance.
     * Uses lazy initialization with synchronized access for thread safety.
     *
     * @return the application-wide {@code SessionManager}
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Returns the currently logged-in user.
     *
     * @return the current {@link User}, or {@code null} if no user is authenticated
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the currently logged-in user after a successful authentication.
     *
     * @param user the authenticated {@link User} to store in the session
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Clears the current session by setting the stored user to {@code null}.
     * Should be called on logout.
     */
    public void clearSession() {
        this.currentUser = null;
    }

    /**
     * Indicates whether a user is currently authenticated.
     *
     * @return {@code true} if a user is stored in the session; {@code false} otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
