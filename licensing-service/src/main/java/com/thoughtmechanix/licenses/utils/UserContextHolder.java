package com.thoughtmechanix.licenses.utils;


import org.springframework.util.Assert;

public class UserContextHolder {
    private static final ThreadLocal<UserContext> threadLocalUserContext = new ThreadLocal<UserContext>();

    public static final UserContext getContext(){
        UserContext context = threadLocalUserContext.get();

        if (context == null) {
            context = createEmptyContext();
            threadLocalUserContext.set(context);

        }
        return threadLocalUserContext.get();
    }

    public static final void setContext(UserContext context) {
        Assert.notNull(context, "Only non-null UserContext instances are permitted");
        threadLocalUserContext.set(context);
    }

    public static final UserContext createEmptyContext(){
        return new UserContext();
    }
}
