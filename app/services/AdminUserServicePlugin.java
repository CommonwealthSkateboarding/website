package services;

        import controllers.Slack;
        import models.security.User;
        import play.Application;

        import com.feth.play.module.pa.user.AuthUser;
        import com.feth.play.module.pa.user.AuthUserIdentity;
        import com.feth.play.module.pa.service.UserServicePlugin;

/**
 * Created by cdelargy on 11/17/14.
 */
public class AdminUserServicePlugin extends UserServicePlugin {

    public AdminUserServicePlugin(final Application app) {
        super(app);
    }

    @Override
    public Object save(final AuthUser authUser) {
        final boolean isLinked = User.existsByAuthUserIdentity(authUser);
        if (!isLinked) {
            return User.create(authUser).id;
        } else {
            // we have this user already, so return null
            return null;
        }
    }

    @Override
    public Object getLocalIdentity(final AuthUserIdentity identity) {
        // For production: Caching might be a good idea here...
        // ...and dont forget to sync the cache when users get deactivated/deleted
        final User u = User.findByAuthUserIdentity(identity);
        if(u != null) {
            return u.id;
        } else {
            return null;
        }
    }

    @Override
    public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
        if (!oldUser.equals(newUser)) {
            User.merge(oldUser, newUser);
        }
        return oldUser;
    }

    @Override
    public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
        User.addLinkedAccount(oldUser, newUser);
        return newUser;
    }

    @Override
    public AuthUser update(final AuthUser knownUser) {
        // User logged in again, bump last login date
        User.setLastLoginDate(knownUser);
        Slack.emitLogin(User.findByAuthUserIdentity(knownUser));
        return knownUser;
    }
}
