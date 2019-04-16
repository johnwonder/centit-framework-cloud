package com.centit.framework.filters;

import com.centit.framework.common.WebOptUtils;
import com.centit.framework.security.model.CentitUserDetails;
import com.centit.framework.utils.RestRequestContext;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Component
public class TrackingFilter extends ZuulFilter {
    private static final int      FILTER_ORDER = 10;
    private static final boolean  SHOULD_FILTER=true;
    private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);


    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return null;
        }

        String correlationId = request.getHeader(RestRequestContext.CORRELATION_ID);
        if (StringUtils.isBlank(correlationId)) {
            correlationId = ctx.getZuulRequestHeaders().get(RestRequestContext.CORRELATION_ID);
        }

        if (StringUtils.isBlank(correlationId)) {
            correlationId = UUID.randomUUID().toString();
            ctx.addZuulRequestHeader(RestRequestContext.CORRELATION_ID, correlationId);
        }

        ctx.addZuulRequestHeader(RestRequestContext.CORRELATION_ID, correlationId);
        String sessionId = request.getHeader(RestRequestContext.SESSION_ID_TOKEN);
        if(StringUtils.isBlank(sessionId)){
            sessionId = request.getSession().getId();
        }
        ctx.addZuulRequestHeader(RestRequestContext.SESSION_ID_TOKEN, sessionId);
        ctx.addZuulRequestHeader(RestRequestContext.AUTHORIZATION_TOKEN, request.getHeader(RestRequestContext.AUTHORIZATION_TOKEN));
        //  如何确保 zuul 过滤器在 spring session 的过滤器之后
        CentitUserDetails ud = WebOptUtils.getLoginUser(request);
        if(ud != null){
            ctx.addZuulRequestHeader(RestRequestContext.USER_CODE_ID, ud.getUserCode());
            ctx.addZuulRequestHeader(RestRequestContext.CURRENT_UNIT_CODE, ud.getCurrentUnitCode());
        }
        return null;
    }
}
