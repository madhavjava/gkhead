package org.mifos.accounting.struts.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.mifos.application.accounting.business.GlBalancesBO;
import org.mifos.application.servicefacade.AccountingServiceFacade;
import org.mifos.application.servicefacade.AccountingServiceFacadeWebTier;
import org.mifos.dto.domain.ViewOpeningBalanceDto;
import org.mifos.dto.domain.ViewStageTransactionsDto;
import org.mifos.framework.struts.action.BaseAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewOpeningBalancesAction extends BaseAction{
	private static final Logger logger = LoggerFactory
			.getLogger(ViewOpeningBalancesAction.class);

	private AccountingServiceFacade accountingServiceFacade = new AccountingServiceFacadeWebTier();

	public ActionForward load(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		List<ViewOpeningBalanceDto> glBalancesBos = accountingServiceFacade.getOpeningBalanceTransaction();
		
		storingSession(request, "viewOpeningBalances",glBalancesBos);
		return mapping.findForward("load_success");
	}
	
	public void storingSession(HttpServletRequest httpServletRequest, String s,
			Object o) {
		httpServletRequest.getSession().setAttribute(s, o);
	}
	
	public String getOfficeHierarchy(String office) {

		String returnValue = "1";

		if (office.equals("Head Office")) {
			returnValue = "1";
		}
		if (office.equals("Regional Office")) {
			returnValue = "2";
		}
		if (office.equals("Divisional Office")) {
			returnValue = "3";
		}
		if (office.equals("Area Office")) {
			returnValue = "4";
		}
		if (office.equals("Branch Office")) {
			returnValue = "5";
		}
		if (office.equals("Center")) {
			returnValue = "6";
		}
		if (office.equals("Group")) {
			returnValue = "7";
		}

		return returnValue;
	}

}
