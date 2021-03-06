package com.centit.framework.filters;

import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.utils.RestRequestContext;
import com.centit.framework.utils.RestRequestContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestRequestContextFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RestRequestContextFilter.class);

    @Value("${filter.rest.context.check.user:false}")
    protected boolean checkUserCode;
    @Value("${filter.rest.context.check.correlation:false}")
    protected boolean checkCorrelation;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestUrl = ((HttpServletRequest) servletRequest).getRequestURI();
        //不过滤swagger相关的api url
        if(!StringUtils.startsWithAny(requestUrl,"/v2/api-docs","/doc.html","/swagger-resources","/webjars/")) {
            String correlationId = httpServletRequest.getHeader(WebOptUtils.CORRELATION_ID);
            String userCode = httpServletRequest.getHeader(WebOptUtils.CURRENT_USER_CODE_TAG);
            if (checkCorrelation) {
                if (StringUtils.isBlank(correlationId)) {
                    JsonResultUtils.writeErrorMessageJson("请走Zuul网关调用该服务！", (HttpServletResponse) servletResponse);
                    return;
                }
                if (checkUserCode && StringUtils.isBlank(userCode)) {
                    JsonResultUtils.writeErrorMessageJson("请先通过auth服务登录！", (HttpServletResponse) servletResponse);
                    return;
                }
            }
            RestRequestContext restRequestContext = RestRequestContextHolder.getContext();
            restRequestContext.setCorrelationId(correlationId);
            restRequestContext.setUserCode(userCode);
            restRequestContext.setSessionIdToken(httpServletRequest.getHeader(WebOptUtils.SESSION_ID_TOKEN));
            restRequestContext.setCurrUnitCode(httpServletRequest.getHeader(WebOptUtils.CURRENT_UNIT_CODE_TAG));
            restRequestContext.setCurrStationId(httpServletRequest.getHeader(WebOptUtils.CURRENT_STATION_ID_TAG));
            restRequestContext.setAuthorizationToken(httpServletRequest.getHeader(WebOptUtils.AUTHORIZATION_TOKEN));

            logger.debug("Special Routes Service Incoming Correlation id: {}", restRequestContext.getCorrelationId());
        }
        filterChain.doFilter(httpServletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        WebOptUtils.setRequestInSpringCloud(true);
    }

    @Override
    public void destroy() {

    }
}
