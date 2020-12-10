package com.zres.project.localnet.portal.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 拦截所有的请求
 */
public class RefererFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RefererFilter.class);

    private String address;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("RefererFilter init -- ");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        LOG.info("RefererFilter doFilter -- ");
        HttpServletRequest request = ((HttpServletRequest) servletRequest);

        String origin = request.getHeader("Origin");
        InetAddress ia = InetAddress.getLocalHost();
        String localIp = ia.getHostAddress();

        if (origin == null || origin.startsWith("http://" + localIp) || origin.startsWith("https://" + localIp)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            servletResponse.setContentType("application/json");
            OutputStream os = servletResponse.getOutputStream();
            os.write("不受信任的访问".getBytes("UTF-8"));
        }
    }

    @Override
    public void destroy() {
        LOG.info("RefererFilter destroy -- ");
    }

}
