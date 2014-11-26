package security;

import models.security.User;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.core.models.Subject;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class MyDeadboltHandler extends AbstractDeadboltHandler {

    @Override
    public F.Promise<Result> beforeAuthCheck(final Http.Context context) {
        if (PlayAuthenticate.isLoggedIn(context.session())) {
            // user is logged in
            return F.Promise.pure(null);
        } else {
            PlayAuthenticate.storeOriginalUrl(context);
            return F.Promise.promise(new F.Function0<Result>()
            {
                @Override
                public Result apply() throws Throwable
                {
                    return redirect(PlayAuthenticate.getResolver().login());
                }
            });
        }
    }

    @Override
    public Subject getSubject(final Http.Context context) {
        final AuthUserIdentity u = PlayAuthenticate.getUser(context);
        // Caching might be a good idea here
        return (Subject)User.findByAuthUserIdentity(u);
    }

    @Override
    public DynamicResourceHandler getDynamicResourceHandler(
            final Http.Context context) {
        return null;
    }

    @Override
    public F.Promise<Result> onAuthFailure(final Http.Context context,
                                           final String content) {
        // if the user has a cookie with a valid user and the local user has
        // been deactivated/deleted in between, it is possible that this gets
        // shown. You might want to consider to sign the user out in this case.
        return F.Promise.promise(new F.Function0<Result>()
        {
            @Override
            public Result apply() throws Throwable
            {
                return forbidden("This resource is not available to you based on your current roles.");
            }
        });
    }
}