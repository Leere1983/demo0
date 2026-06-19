package com.example.demo0.interceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
@Component
@Slf4j
public class RequestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute("requestId", requestId);
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        log.info("Request [{}] started: {} {} from {}",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr());
        return true;
    }
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String requestId = (String) request.getAttribute("requestId");
        log.debug("Request [{}] completed: Response status {}",
                requestId,
                response.getStatus());
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestId = (String) request.getAttribute("requestId");
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        if (ex != null) {
            log.error("Request [{}] failed after {}ms: {}",
                    requestId,
                    duration,
                    ex.getMessage());
        } else {
            log.info("Request [{}] finished in {}ms",
                    requestId,
                    duration);
        }
    }
}