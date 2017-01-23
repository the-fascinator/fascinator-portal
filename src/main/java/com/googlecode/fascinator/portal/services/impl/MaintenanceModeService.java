package com.googlecode.fascinator.portal.services.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.googlecode.fascinator.common.FascinatorHome;

@Component(value = "maintenanceModeService")
public class MaintenanceModeService {

	public boolean isMaintanceMode() {
		return FascinatorHome.getPathFile("maintenance.mode").exists();
	}

	public void toggleMaintenanceMode(boolean state) throws IOException {
		File stateFile = FascinatorHome.getPathFile("maintenance.mode");
		if (state) {
			if (!stateFile.exists()) {
				FileUtils.writeStringToFile(stateFile,
						"Maintenace mode state file. Delete this file to turn off maintenance mode");
			}
		} else {
			if (stateFile.exists()) {
				FileUtils.forceDelete(stateFile);
			}
		}

	}
}
