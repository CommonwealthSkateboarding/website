package security;

import be.objectify.deadbolt.java.cache.HandlerCache;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

/**
 * Created by cdelargy on 1/20/16.
 */
public class MyDeadboltHook extends Module {
    @Override
    public Seq<Binding<?>> bindings(final Environment environment,
                                    final Configuration configuration)
    {
        return seq(bind(HandlerCache.class).toInstance(new MyHandlerCache(new MyDeadboltHandler())));
    }
}
