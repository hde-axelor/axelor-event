package com.axelor.event.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.axelor.common.FileUtils;
import com.axelor.data.Listener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.Model;
import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;

public class EvenRegistrationServiceImpl implements EventRegistrationService {

	@Override
	public boolean checkRegDate(EventRegistration eventRegistration, Event event) {

		LocalDate date = eventRegistration.getRegistrationDate();

		if (event != null && event.getRegistartionOpen() != null && event.getRegistrationClose() != null
				&& date != null) {

			if (date.compareTo(event.getRegistartionOpen()) >= 0 && date.compareTo(event.getRegistrationClose()) <= 0) {
				return true;
			}

		}
		return false;
	}

	@Override
	public BigDecimal registrationAmount(EventRegistration eventRegistration, Event event) {

		BigDecimal amount = BigDecimal.ZERO;

		long noOfDaysBetween = ChronoUnit.DAYS.between(eventRegistration.getRegistrationDate(),
				event.getRegistrationClose());

		List<Discount> discounts = event.getDiscounts();
		for (Discount dis : discounts) {
			if (dis.getBeforeDays() == noOfDaysBetween)
				amount = event.getEventFees().subtract(dis.getDiscountAmount());
		}
		if (amount == BigDecimal.ZERO)
			amount = event.getEventFees();
		return amount;

	}

  @Override
  public void importEventRegistrationData(MetaFile file, Event event) {
	  File tmpDir = null;
	    File configXML = null;
	    File csvFile = null;

	    try {
	      tmpDir = Files.createTempDir();
	      configXML = new File(tmpDir, "input-config.xml");
	      InputStream bindInputStream =
	          this.getClass().getResourceAsStream("/import-configs/input-config.xml");

	      if (bindInputStream == null) {
	        throw new Error("No file found.");
	      }

	      FileOutputStream outputstream = new FileOutputStream(configXML);
	      IOUtils.copy(bindInputStream, outputstream);

	      csvFile = new File(tmpDir, "input.csv");
	      Files.copy(MetaFiles.getPath(file).toFile(), csvFile);

	      CSVImporter csvImporter =
	          new CSVImporter(tmpDir + "/input-config.xml", tmpDir.getAbsolutePath());

	      Map<String, Object> context = new HashMap<>();
	      context.put("_event", event);
	      csvImporter.setContext(context);

	      csvImporter.addListener(
	          new Listener() {

	            @Override
	            public void imported(Integer total, Integer success) {
	              System.err.println("Total: " + total + " Success: " + success);
	            }

	            @Override
	            public void imported(Model bean) {}

	            @Override
	            public void handle(Model bean, Exception e) {}
	          });
	      csvImporter.run();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    configXML.delete();
	    csvFile.delete();
	    try {
      FileUtils.deleteDirectory(tmpDir);
    } catch (IOException e) {
    e.printStackTrace();
    }
	  }

    
  }

