package com.sap.lm.sl.spfi.refapp.controllers;


import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.sap.lm.sl.spfi.refapp.services.SpfiOperatorService;

@RestController
@RequestMapping(path = ControllersConstants.NOTIFY_PATH)
public class NotificationController {
    @Inject
    private SpfiOperatorService spfiOperatorService;
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Async
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> handleNotification(
         @RequestHeader(value = ControllersConstants.HEADER_FULFILLMENT_NOTIFICATION_ID, required = true) String xSapSaasfulfillmentNotifcationId,
         @RequestHeader(value = ControllersConstants.HEADER_FULFILLMENT_CALLBACK_URL, required = true) String xSapSaasfulfillmentCallbackUrl)
    {
        logger.debug("Processing POST /notify request with notification ID: {}, Callback Url: {}", xSapSaasfulfillmentNotifcationId, xSapSaasfulfillmentCallbackUrl);

        try {
            spfiOperatorService.processFulfillment(xSapSaasfulfillmentNotifcationId, xSapSaasfulfillmentCallbackUrl);
        } catch (Exception e) {
            logger.error("Couldn't process notification", e);
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
}
