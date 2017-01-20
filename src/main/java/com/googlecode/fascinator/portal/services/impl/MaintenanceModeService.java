package com.googlecode.fascinator.portal.services.impl;

import org.springframework.stereotype.Component;

import com.googlecode.fascinator.common.FascinatorHome;

@Component(value = "maintenanceModeService")
public class MaintenanceModeService {

	public boolean isMaintanceMode() {
		return FascinatorHome.getPathFile("maintenance.mode").exists();
	}

	
}
