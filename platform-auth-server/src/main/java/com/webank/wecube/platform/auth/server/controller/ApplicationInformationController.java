package com.webank.wecube.platform.auth.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.service.ApplicationInformationService;

@RestController
@RequestMapping(ApplicationConstants.ApiInfo.PREFIX_DEFAULT)
public class ApplicationInformationController {
	private static final Logger log = LoggerFactory.getLogger(ApplicationInformationController.class);

	@Autowired
	private ApplicationInformationService applicationInformationService;

	@GetMapping("/health-check")
	public ResponseEntity<CommonResponseDto> healthCheck() {
		try {
			applicationInformationService.dbHealthCheck();
			log.info("Health Checking finished");
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(CommonResponseDto.okay());
		} catch (Exception e) {
			log.warn("Health checking failed", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
					.body(CommonResponseDto.error(e.getMessage()));
		}
	}
}
