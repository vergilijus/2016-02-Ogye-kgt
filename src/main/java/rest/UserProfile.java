package rest;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author esin88
 */
public class UserProfile {
    private static final AtomicLong ID_GENETATOR = new AtomicLong(0);
    @NotNull
    private String login;
    @NotNull
    private String password;
    @NotNull
    private String email;
    private long id;

    public UserProfile() {
        login = "";
        password = "";
        email = "";
        id = ID_GENETATOR.getAndIncrement();
    }

    public UserProfile(@NotNull String login, @NotNull String password, @NotNull String email) {
        this.login = login;
        this.password = password;
        this.email = email;
        this.id = ID_GENETATOR.getAndIncrement();
    }

    @NotNull
    public String getLogin() {
        return login;
    }

    public void setLogin(@NotNull String login) {
        this.login = login;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    @NotNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public class Resp {
        private long id;

        public Resp(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    public Resp getResponse() {
        return new Resp(id);
    }

}
