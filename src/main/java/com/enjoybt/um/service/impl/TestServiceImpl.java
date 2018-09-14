package com.enjoybt.um.service.impl;

import com.enjoybt.common.dao.CommonDAO;
import com.enjoybt.um.service.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    private static final Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);

    @Autowired
    CommonDAO dao;

    @Override
    public String test() {
        String result = null;
        try {
            result = (String)dao.selectObject("um.test");
        } catch (Exception e) {
            logger.info("ERROR", e);
        }
        return result;
    }
}
