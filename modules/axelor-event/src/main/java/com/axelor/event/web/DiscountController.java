package com.axelor.event.web;

import java.math.BigDecimal;

import com.axelor.event.db.Discount;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class DiscountController {

	public void discountAmount(ActionRequest request, ActionResponse response) {

		Discount discount = request.getContext().asType(Discount.class);
		System.out.println(discount.getDiscountPercent());

		BigDecimal eventFees = (BigDecimal) request.getContext().getParent().get("eventFees");
		BigDecimal discountAmount = BigDecimal.ZERO;

		if (eventFees != null && discount.getDiscountPercent() != null) {
			discountAmount = eventFees.multiply(discount.getDiscountPercent()).divide(new BigDecimal(100));
		}
		
		response.setValue("discountAmount", discountAmount);
	}

}
