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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class MyDeadboltHandler extends AbstractDeadboltHandler {

    public F.Promise<Optional<Result>> beforeAuthCheck(final Http.Context context) {
        if (PlayAuthenticate.isLoggedIn(context.session())) {
            // user is logged in
            return F.Promise.pure(Optional.empty());
        } else {
            PlayAuthenticate.storeOriginalUrl(context);
            return F.Promise.promise(new F.Function0<Optional<Result>>()
            {
                @Override
                public Optional<Result> apply() throws Throwable
                {
                    return Optional.of(redirect(PlayAuthenticate.getResolver().login()));
                }
            });
        }
    }

    @Override
    public F.Promise<Optional<Subject>> getSubject(final Http.Context context) {
        final AuthUserIdentity u = PlayAuthenticate.getUser(context);
        return F.Promise.promise(new F.Function0<Optional<Subject>>()
        {
            @Override
            public Optional<Subject> apply() throws Throwable
            {
                // Caching might be a good idea here
                return Optional.of((Subject)User.findByAuthUserIdentity(u));
            }
        });
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