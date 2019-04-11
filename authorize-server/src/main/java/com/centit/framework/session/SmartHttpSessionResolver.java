package com.centit.framework.session;

import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Http request/response 映射 session 策略
 * 同时支持cookie和header的session映射策略
 */
public class SmartHttpSessionResolver implements HttpSessionIdResolver {

    private CookieHttpSessionIdResolver browser;//= new CookieHttpSessionStrategy();
    private HeaderHttpSessionIdResolver api; // = new HeaderHttpSessionStrategy();

    public void setCookieFirst(boolean cookieFirst) {
        this.cookieFirst = cookieFirst;
    }

    private boolean cookieFirst;

    public SmartHttpSessionResolver(boolean cookieFirst, String cookiePath) {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookiePath(cookiePath);
        browser = new CookieHttpSessionIdResolver();
        api = new HeaderHttpSessionIdResolver("x-auth-token");
        this.cookieFirst = cookieFirst;
    }

    /**
     * Resolve the session ids associated with the provided {@link HttpServletRequest}.
     * For example, the session id might come from a cookie or a request header.
     *
     * @param request the current request
     * @return the session ids
     */
    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {
        if(cookieFirst){
            List<String> sessionIds = browser.resolveSessionIds(request);
            if(sessionIds!=null && sessionIds.size()>0)
                return sessionIds;
            return api.resolveSessionIds(request);
        }else {
            List<String> sessionIds = api.resolveSessionIds(request);
            if(sessionIds!=null && sessionIds.size()>0)
                return sessionIds;
            return browser.resolveSessionIds(request);
        }
    }

    /**
     * Send the given session id to the client. This method is invoked when a new session
     * is created and should inform a client what the new session id is. For example, it
     * might create a new cookie with the session id in it or set an HTTP response header
     * with the value of the new session id.
     *
     * @param request   the current request
     * @param response  the current response
     * @param sessionId the session id
     */
    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
        api.setSessionId(request, response, sessionId);
        browser.setSessionId(request, response, sessionId);
    }

    /**
     * Instruct the client to end the current session. This method is invoked when a
     * session is invalidated and should inform a client that the session id is no longer
     * valid. For example, it might remove a cookie with the session id in it or set an
     * HTTP response header with an empty value indicating to the client to no longer
     * submit that session id.
     *
     * @param request  the current request
     * @param response the current response
     */
    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {
        api.expireSession(request, response);
        browser.expireSession(request, response);
    }
}
