package security;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;

/**
 * Created by cdelargy on 1/20/16.
 */
public class MyHandlerCache implements HandlerCache
{
    private final DeadboltHandler handler;

    public MyHandlerCache(final DeadboltHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public DeadboltHandler apply(String s)
    {
        return handler;
    }

    @Override
    public DeadboltHandler get()
    {
        return handler;
    }
}
