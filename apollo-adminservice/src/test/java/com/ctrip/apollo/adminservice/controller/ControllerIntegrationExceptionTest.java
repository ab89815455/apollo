package com.ctrip.apollo.adminservice.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpStatusCodeException;

import com.ctrip.apollo.biz.entity.App;
import com.ctrip.apollo.biz.service.AdminService;
import com.ctrip.apollo.biz.service.AppService;
import com.ctrip.apollo.core.dto.AppDTO;
import com.google.gson.Gson;

public class ControllerIntegrationExceptionTest extends AbstractControllerTest {

  @Autowired
  AppController appController;

  @Mock
  AdminService adminService;

  @Autowired
  AppService appService;

  Gson gson = new Gson();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(appController, "adminService", adminService);
  }

  private String getBaseAppUrl() {
    return "http://localhost:" + port + "/apps/";
  }

  @Test
  @Sql(scripts = "/controller/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  public void testCreateFailed() {
    AppDTO dto = generateSampleDTOData();

    when(adminService.createNewApp(any(App.class))).thenThrow(new RuntimeException("save failed"));

    try {
      restTemplate.postForEntity(getBaseAppUrl(), dto, AppDTO.class);
    } catch (HttpStatusCodeException e) {
      @SuppressWarnings("unchecked")
      Map<String, String> attr = gson.fromJson(e.getResponseBodyAsString(), Map.class);
      Assert.assertEquals("save failed", attr.get("message"));
    }
    App savedApp = appService.findOne(dto.getAppId());
    Assert.assertNull(savedApp);
  }

  private AppDTO generateSampleDTOData() {
    AppDTO dto = new AppDTO();
    dto.setAppId("someAppId");
    dto.setName("someName");
    dto.setOwnerName("someOwner");
    dto.setOwnerEmail("someOwner@ctrip.com");
    return dto;
  }
}