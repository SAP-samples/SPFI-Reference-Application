package com.sap.lm.sl.spfi.refapp.mocks;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.lm.sl.spfi.refapp.controllers.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping(path = MockControllerConstants.IASTEST_PATH)
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;


    @org.springframework.beans.factory.annotation.Autowired
    public TestController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        this.request = request;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getTenants() throws AppException {
        logger.debug("IAS Test");

        ResponseEntity<List<String>> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        return responseEntity;
    }


}
