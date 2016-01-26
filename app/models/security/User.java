package models.security;

import java.util.*;

import javax.persistence.*;
import com.avaje.ebean.*;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Subject;
import com.feth.play.module.pa.user.*;
import models.skatepark.Membership;
import models.skatepark.OnlinePassSale;
import play.data.format.Formats;
import play.data.validation.Constraints;

/**
 * Created by cdelargy on 11/17/14.
 */

@Entity
@Table(name = "users")
public class User extends Model implements Subject {
    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Email
    // to enforce uniqueness, must implement merge workflow
    // @Column(unique = true)
    public String email;

    public String name;
    
    //public String fullName;
    //public String firstName;
    //public String lastName;
    //public String nickName;

    public String photoUrl;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date lastLogin;

    public boolean active;

    public boolean emailValidated;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "membership_id")
    public Membership membership;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "purchased_by_id")
    public List<OnlinePassSale> passSales;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name="guardian")
    public List<Membership> guardianOf;

    @ManyToMany(cascade = CascadeType.ALL)
    public List<SecurityRole> roles;

    @OneToMany(cascade = CascadeType.ALL)
    public List<LinkedAccount> linkedAccounts;

    @Override
    public List<SecurityRole> getRoles() {
        return roles;
    }

    public static final Finder<Long, User> find = new Finder<Long, User>(
            Long.class, User.class);

    public static boolean existsByAuthUserIdentity(
            final AuthUserIdentity identity) {
        final ExpressionList<User> exp = getAuthUserFind(identity);
        return exp.findRowCount() > 0;
    }

    private static ExpressionList<User> getAuthUserFind(
            final AuthUserIdentity identity) {
        return find.where().eq("active", true)
                .eq("linkedAccounts.providerUserId", identity.getId())
                .eq("linkedAccounts.providerKey", identity.getProvider());
    }

    public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
        if (identity == null) {
            return null;
        }
        return getAuthUserFind(identity).findUnique();
    }

    public void merge(final User otherUser) {
        for (final LinkedAccount acc : otherUser.linkedAccounts) {
            this.linkedAccounts.add(LinkedAccount.create(acc));
        }
        // do all other merging stuff here - like resources, etc.

        // deactivate the merged user that got added to this one
        otherUser.active = false;
        Ebean.save(Arrays.asList(new User[] { otherUser, this }));
    }

    public static User create(final AuthUser authUser) {
        final User user = new User();
        user.active = true;
        user.lastLogin = new Date();
        user.linkedAccounts = Collections.singletonList(LinkedAccount
                .create(authUser));

        if (authUser instanceof EmailIdentity) {
            final EmailIdentity identity = (EmailIdentity) authUser;
            user.email = identity.getEmail();
            user.emailValidated = false;
        }

        if (authUser instanceof PicturedIdentity) {
            final PicturedIdentity identity = (PicturedIdentity) authUser;
            user.photoUrl = identity.getPicture();
            user.emailValidated = false;
        }

        if (authUser instanceof NameIdentity) {
            final NameIdentity identity = (NameIdentity) authUser;
            final String name = identity.getName();
            if (name != null) {
                user.name = name;
            }
        }

        user.save();
        //user.saveManyToManyAssociations("roles"); - removed from Model in 2.4?
        return user;
    }

    public static void setLastLoginDate(final AuthUser knownUser) {
        final User u = User.findByAuthUserIdentity(knownUser);
        u.lastLogin = new Date();
        u.save();
    }

    public static void merge(final AuthUser oldUser, final AuthUser newUser) {
        User.findByAuthUserIdentity(oldUser).merge(
                User.findByAuthUserIdentity(newUser));
    }

    public Set<String> getProviders() {
        final Set<String> providerKeys = new HashSet<String>(
                linkedAccounts.size());
        for (final LinkedAccount acc : linkedAccounts) {
            providerKeys.add(acc.providerKey);
        }
        return providerKeys;
    }

    public static void addLinkedAccount(final AuthUser oldUser,
                                        final AuthUser newUser) {
        final User u = User.findByAuthUserIdentity(oldUser);
        u.linkedAccounts.add(LinkedAccount.create(newUser));
        u.save();
    }

    public static User findByEmail(final String email) {
        return getEmailUserFind(email).findUnique();
    }

    private static ExpressionList<User> getEmailUserFind(final String email) {
        return find.where().eq("active", true).eq("email", email);
    }

    public LinkedAccount getAccountByProvider(final String providerKey) {
        return LinkedAccount.findByProviderKey(this, providerKey);
    }

    @Override
    public List<? extends Permission> getPermissions() {
        return null;
    }

    @Override
    public String getIdentifier() {
        return Long.toString(id);
    }
}
