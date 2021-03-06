package models;

import org.junit.Test;

import static models.UserProfile.isEmailValid;
import static models.UserProfile.isLoginValid;
import static models.UserProfile.isPasswordValid;
import static org.junit.Assert.*;

public class UserProfileTest {

    @Test
    public void testIsLoginValid() throws Exception {
        final String[] validLogins = {"alex", "bill", "ann"};
        final String[] invalidLogins = {null, "", "a", "bc"};
        for (String login : validLogins)
            assertTrue("Valid login validation fault", isLoginValid(login));
        for (String login : invalidLogins)
            assertFalse("Invalid login validation fault", isLoginValid(login));
    }

    @Test
    public void testIsPasswordValid() throws Exception {
        final String[] validPasswords = {"1234", "12345", "qwerty", "qwerty1234"};
        final String[] invalidPasswords = {null, "", "1", "12", "123"};
        for (String password : validPasswords)
            assertTrue("Valid password validation fault", isPasswordValid(password));
        for (String password : invalidPasswords)
            assertFalse("Invalid password validation fault", isPasswordValid(password));
    }

    @Test
    public void testIsEmailValid() throws Exception {
        final String[] validEmails = {"alex@mail.ru", "qwer.ty@gmail.com", "qw-rty@yandex.ru"};
        final String[] invalidEmails = {null, "", "@", "alexmail.ru", ".test@gmail.com", "test@ya.ru.", "test@", "@test.ru", "te..st@ya.ru"};
        for (String email : validEmails)
            assertTrue("Valid email validation fault", isEmailValid(email));
        for (String email : invalidEmails)
            assertFalse("Invalid email validation fault", isEmailValid(email));
    }

    @Test
    public void equals() throws Exception {
        final UserProfile up1 = new UserProfile("login", "qwerty");
        final UserProfile up2 = new UserProfile("login", "qwerty");
        final UserProfile up3 = new UserProfile("login", "qwerty");
        final UserProfile up4 = new UserProfile("login", "123456");
        final UserProfile up5 = new UserProfile("other", "qwerty");
        assertEquals(up1, up2);
        assertNotEquals(up3, up4);
        assertNotEquals(up4, up5);
    }
}