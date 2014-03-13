package org.mifos.accounting.struts.actionform;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.framework.struts.actionforms.BaseActionForm;

public class ViewOpeningBalanceActionForm  extends BaseActionForm{
	
	public ActionForward load(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		
		
		
		return mapping.findForward(ActionForwards.load_success.toString());
	}

}
