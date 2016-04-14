package services;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.ws.client.sei.ResponseBuilder;
import models.UserLoginRequest;
import models.UserProfile;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.service.ServiceRegistry;
import org.jetbrains.annotations.Nullable;
import services.dao.UserProfileDAO;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class AccountServiceImpl implements AccountService {
    // todo: заменить UserProfile на id.
    private Map<String, UserProfile> sessions = new ConcurrentHashMap<>();
    private Map<String, Long> sessionsId = new ConcurrentHashMap<>();

    private SessionFactory sessionFactory;

    public AccountServiceImpl(Configuration configuration) {
        sessionFactory = createSessionFactory(configuration);
    }

    @FunctionalInterface
    public interface HibernateUnit<T> {
        T operate(Session session);
    }

    public <T> T doInSession(@NotNull HibernateUnit<T> work) throws DatabaseException {
        final Session session = sessionFactory.openSession();
        try {
            session.getTransaction().begin();
            final T result = work.operate(session);
            session.getTransaction().commit();
            return result;
        } catch (RuntimeException e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE
                    || session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK) {
                session.getTransaction().rollback();
            }
            throw new DatabaseException("Fail to perform database transaction", e);
        } finally {
            session.close();
        }
    }


    @Override
    public String getLocalStatus() {
        final String status;
        try (Session session = sessionFactory.openSession()) {
            final Transaction transaction = session.beginTransaction();
            status = transaction.getStatus().toString();
            transaction.commit();
        }
        return status;
    }

    // FIXME: тестовый метод, не выкладывать в прод.
    @Override
    @Nullable
    public Collection<UserProfile> getAllUsers() throws DatabaseException {
        return doInSession((session) -> {
            final UserProfileDAO dao = new UserProfileDAO(session);
            return dao.readAll();
        });
    }

    @Override
    public long addUser(UserProfile userProfile) throws DatabaseException {
        return doInSession((session) -> {
            final UserProfileDAO dao = new UserProfileDAO(session);
            return dao.save(userProfile);
        });
    }


    @Override
    @Nullable
    public UserProfile getUser(long userId) throws DatabaseException {
        return doInSession((session) -> {
            final UserProfileDAO dao = new UserProfileDAO(session);
            return dao.read(userId);
        });
    }

    @Override
    @Nullable
    public UserProfile getUserByLogin(String login) throws DatabaseException {
        return doInSession((session) -> {
            final UserProfileDAO dao = new UserProfileDAO(session);
            return dao.readByName(login);
        });
    }

    @Override
    public UserProfile getUserBySession(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public boolean removeUser(long userId) {
        try {
            return doInSession((session) -> {
                final UserProfileDAO dao = new UserProfileDAO(session);
                dao.delete(userId);
                return true;
            });
        } catch (DatabaseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeUser(String sessionId, long userId) {
        final UserProfile userProfile = sessions.get(sessionId);
        if (userProfile == null) return false;
        if (userProfile.getId() != userId) return false;
        return removeUser(userId);
    }

    @Override
    public boolean updateUser(final String sessionId, final long userId, UserProfile newProfile) {
        final UserProfile userProfile = getUserBySession(sessionId);
        if (userProfile == null) return false;
        if (userProfile.getId() != userId) return false;

        if (!UserProfile.isLoginValid(newProfile.getLogin())) return false;
        if (!UserProfile.isPasswordValid(newProfile.getPassword())) return false;
        if (!UserProfile.isEmailValid(newProfile.getEmail())) return false;

        userProfile.setLogin(newProfile.getLogin());
        userProfile.setPassword(newProfile.getPassword());
        userProfile.setEmail(newProfile.getEmail());
        try (Session session = sessionFactory.openSession()) {
            final Transaction transaction = session.beginTransaction();
            final UserProfileDAO dao = new UserProfileDAO(session);
            dao.update(userProfile);
            transaction.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void addSession(String sessionId, UserProfile userProfile) {
        sessions.put(sessionId, userProfile);
    }

    private void closeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public boolean isAuthorised(String sessioinId) {
        return sessions.containsKey(sessioinId);
    }

    public long checkAuth(String sessionId) {
        return sessionsId.get(sessionId);
    }


    @Override
    @Nullable
    public UserProfile doLogin(String sessionId, UserLoginRequest user) throws DatabaseException {
        final String login = user.getLogin();
        final UserProfile userProfile = getUserByLogin(login);
        if (userProfile == null) return null;
        addSession(sessionId, userProfile);
        //noinspection UnnecessaryLocalVariable
        final boolean isPasswordEqual = userProfile.getPassword().equals(user.getPassword());
        if (isPasswordEqual) {
            return userProfile;
        } else {
            return null;
        }
    }

    @Override
    public boolean doLogout(String sessionId) {
        if (getUserBySession(sessionId) == null) return false;
        closeSession(sessionId);
        return true;
    }

    private static SessionFactory createSessionFactory(Configuration configuration) {
        final StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        final ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    public static class DatabaseException extends Exception {
        public DatabaseException(String message) {
            super(message);
        }

        public DatabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidUserException extends Exception {
        public InvalidUserException(String message) {
            super(message);
        }
    }

    @Override
    public boolean drop() {
        try (Session session = sessionFactory.openSession()) {
            final Transaction transaction = session.beginTransaction();
            session.createSQLQuery("DROP TABLE UserProfile");
            transaction.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
