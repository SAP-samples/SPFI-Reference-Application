package com.sap.lm.sl.spfi.refapp.mocks;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.sap.lm.sl.spfi.operations.client.model.ServerError;
import com.sap.lm.sl.spfi.refapp.controllers.ControllersConstants;
import com.sap.lm.sl.spfi.refapp.controllers.NotificationController;

@Component
public class TestHandlerInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Inject
    private IntegrationTests integrationTests;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object object, Exception arg3) throws Exception {
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object object, ModelAndView model) throws Exception {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String notificationId = request.getHeader("x-sap-saasfulfillment-notification-id");
        String callbackUrl = request.getHeader("x-sap-saasfulfillment-callback-url");

        if ((integrationTests.isTestTenantByName(notificationId, callbackUrl) || integrationTests.isNotificationFailTestTenant(notificationId, callbackUrl)) && integrationTests.isActivation(notificationId, callbackUrl))  {
            logger.error("Notification failure test");
            ServerError apiError = new ServerError("Notification failure test", "020");
            response.getWriter().write(new Gson().toJson(apiError));
            response.setStatus(400);
            response.setContentType("application/json");
            return false;
        }

        return true;
    }
}
