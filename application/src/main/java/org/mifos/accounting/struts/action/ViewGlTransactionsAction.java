/*
 * Copyright (c) 2005-2011 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.accounting.struts.action;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.mifos.accounting.struts.actionform.JournalVoucherActionForm;
import org.mifos.accounting.struts.actionform.ViewGlTransactionsActionForm;
import org.mifos.application.servicefacade.AccountingServiceFacade;
import org.mifos.application.servicefacade.AccountingServiceFacadeWebTier;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.dto.domain.DynamicOfficeDto;
import org.mifos.dto.domain.GLCodeDto;
import org.mifos.dto.domain.OfficeDetailsDto;
import org.mifos.dto.domain.OfficeGlobalDto;
import org.mifos.dto.domain.OfficeHierarchy;
import org.mifos.dto.domain.OfficesList;
import org.mifos.dto.domain.ViewGlTransactionPaginaitonVariablesDto;
import org.mifos.dto.domain.ViewTransactionsDto;
import org.mifos.dto.screen.OfficeFormDto;
import org.mifos.framework.struts.action.BaseAction;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.security.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewGlTransactionsAction extends BaseAction {
	private static final Logger logger = LoggerFactory
			.getLogger(ViewGlTransactionsAction.class);
	private AccountingServiceFacade accountingServiceFacade = new AccountingServiceFacadeWebTier();

	public ActionForward load(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		List<GLCodeDto> accountingDtos = null;
		ViewGlTransactionsActionForm actionForm = (ViewGlTransactionsActionForm) form;
		java.util.Date trxnDate = DateUtils.getCurrentDateWithoutTimeStamp();
		actionForm.setToTrxnDate(trxnDate);
		accountingDtos = accountingServiceFacade.accountHeadAll(actionForm.getMainAccount());
		storingSession(request, "AccountHeadGlCodes", accountingDtos);
		//madhav
		UserContext context = getUserContext(request);
		actionForm.setOfficeLevelId(String.valueOf(context.getOfficeLevelId()));
		 Short officeLevel = context.getOfficeLevelId();
	        OfficeFormDto officeFormDto = this.officeServiceFacade.retrieveOfficeFormInformation(officeLevel);
	        List<OfficeDetailsDto> listOfOfficeHierarchyObjectddd=officeFormDto.getOfficeLevels();
	        List<OfficeHierarchy> listofofficess=new ArrayList<OfficeHierarchy>();
	        OfficeHierarchy officeHierarchy;
	   
	        for(OfficeDetailsDto officeDetailsDto:listOfOfficeHierarchyObjectddd){
	            officeHierarchy=new OfficeHierarchy(officeDetailsDto.getLevelId().toString(),officeDetailsDto.getLevelName());
	            listofofficess.add(officeHierarchy);   
	        }
	        storingSession(request, "listOfOffices", listofofficess);
		return mapping.findForward(ActionForwards.load_success.toString());
	}
	
	
	public ActionForward loadOffices(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {

		ViewGlTransactionsActionForm actionForm = (ViewGlTransactionsActionForm) form;


		UserContext userContext = getUserContext(request);


		//List<OfficeGlobalDto> officeDetailsDtos = null;
		List<OfficeGlobalDto> dynamicOfficeDetailsDtos = null;
		List<OfficesList> offices = new ArrayList<OfficesList>();
		// list of offices for a single parent office
		List<DynamicOfficeDto> listOfOffices = null;



		listOfOffices = accountingServiceFacade.getOfficeDetails(String.valueOf(userContext.getBranchId()),String.valueOf(userContext.getOfficeLevelId()));
		OfficesList officesList = null;
		for(DynamicOfficeDto officeDto :listOfOffices){

		if (actionForm.getOfficeHierarchy().equals("")) {
			offices = null;
		// to recognise center and group
		} else if (actionForm.getOfficeHierarchy().equals("6") || actionForm.getOfficeHierarchy().equals("7") ) {

			if(actionForm.getOfficeHierarchy().equals(String.valueOf(officeDto.getOfficeLevelId()))){
				officesList = new OfficesList(officeDto.getCustomerId(), officeDto.getDisplayName(), officeDto.getCustomerLevelId(), officeDto.getGlobalCustomerNumber());
				offices.add(officesList);
			}

		} else {

				if(actionForm.getOfficeHierarchy().equals(String.valueOf(officeDto.getOfficeLevelId()))){

					officesList = new OfficesList(officeDto.getOfficeId(), officeDto.getDisplayName(), officeDto.getOfficeLevelId(), officeDto.getGlobalOfficeNumber());
					offices.add(officesList);
				}

			}

		}

//		storingSession(request, "OfficesOnHierarchy", officeDetailsDtos);
		storingSession(request, "DynamicOfficesOnHierarchy", offices);

		return mapping.findForward(ActionForwards.load_success.toString());
	}

	public ActionForward submit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		ViewGlTransactionsActionForm actionForm = (ViewGlTransactionsActionForm) form;
		int noOfRecordsPerPage = 10; // Number of records show on per page
		int noOfPagesIndex = 10; // Number of pages index shown
		/*
		 * this program displays the pagination concept as view page displaying
		 * limited number of page links(number of page links value carrying with
		 * noOfPagesIndex)
		 */

		int totalNoOfRowsForPagination = nullIntconv(request
				.getParameter("totalNoOfRowsForPagination"));
		int iTotalPages = nullIntconv(request.getParameter("iTotalPages"));
		int iPageNo = nullIntconv(request.getParameter("iPageNo"));
		int cPageNo = nullIntconv(request.getParameter("cPageNo"));

		int startRecordCurrentPage = 0;
		int endRecordCurrentPage = 0;

		if (iPageNo == 0) {
			iPageNo = 0;
		} else {
			iPageNo = Math.abs((iPageNo - 1) * noOfRecordsPerPage);
		}

		/*List<ViewTransactionsDto> viewTransactionsDtos = accountingServiceFacade
				.getAccountingTransactions(
						DateUtils.getDate(actionForm.getToTrxnDate()),DateUtils.getDate(actionForm.getFromTrxnDate()), iPageNo,
						noOfRecordsPerPage);
		storingSession(request, "ViewTransactionsDtos", viewTransactionsDtos);*/
		
		//madhav
		List<ViewTransactionsDto> viewTransactionsDtos=null;
		if(actionForm.getMainAccount()!=null)
		{
		viewTransactionsDtos = accountingServiceFacade
				.getAccountingTransactionsByAccountHead(
						DateUtils.getDate(actionForm.getToTrxnDate()),DateUtils.getDate(actionForm.getFromTrxnDate()), iPageNo,
						noOfRecordsPerPage,actionForm.getMainAccount());
		}
		else
		{
			viewTransactionsDtos = accountingServiceFacade
					.getAccountingTransactions(
							DateUtils.getDate(actionForm.getToTrxnDate()),DateUtils.getDate(actionForm.getFromTrxnDate()), iPageNo,
							noOfRecordsPerPage);
			
		}
		storingSession(request, "ViewTransactionsDtos", viewTransactionsDtos);
		// // this will count total number of rows

		
		
		totalNoOfRowsForPagination = accountingServiceFacade
				.getNumberOfTransactions(DateUtils.getDate(actionForm
						.getToTrxnDate()),DateUtils.getDate(actionForm.getFromTrxnDate()));

		// // calculate next record start record and end record
		if (totalNoOfRowsForPagination < (iPageNo + noOfRecordsPerPage)) {
			endRecordCurrentPage = totalNoOfRowsForPagination;
		} else {
			endRecordCurrentPage = (iPageNo + noOfRecordsPerPage);
		}

		startRecordCurrentPage = (iPageNo + 1);
		iTotalPages = ((int) (Math.ceil((double) totalNoOfRowsForPagination
				/ noOfRecordsPerPage)));

		// // index of pages

		int cPage = 0;
		cPage = ((int) (Math.ceil((double) endRecordCurrentPage
				/ (noOfPagesIndex * noOfRecordsPerPage))));
		int prePageNo = (cPage * noOfPagesIndex)
				- ((noOfPagesIndex - 1) + noOfPagesIndex); // we can say it as
															// pre cPage
		int i = (cPage * noOfPagesIndex) + 1;
		ViewGlTransactionPaginaitonVariablesDto dto = new ViewGlTransactionPaginaitonVariablesDto();
		dto.setcPageNo(cPageNo);
		dto.setI(i);
		dto.setcPage(cPage);
		dto.setPrePageNo(prePageNo);
		dto.setNoOfPagesIndex(noOfPagesIndex);
		dto.setiPageNo(iPageNo);
		dto.setNoOfRecordsPerPage(noOfRecordsPerPage);
		dto.setiTotalPages(iTotalPages);
		dto.setStartRecordCurrentPage(startRecordCurrentPage);
		dto.setEndRecordCurrentPage(endRecordCurrentPage);
		dto.setTotalNoOfRowsForPagination(totalNoOfRowsForPagination);

		storingSession(request, "ViewGlTransactionPaginaitonVariablesDto", dto);

		return mapping.findForward("submit_success");
	}

	public ActionForward cancel(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.cancel_success.toString());
	}

	public void storingSession(HttpServletRequest httpServletRequest, String s,
			Object o) {
		httpServletRequest.getSession().setAttribute(s, o);
	}

	public int nullIntconv(String str) {
		int conv = 0;
		if (str == null) {
			str = "0";
		} else if ((str.trim()).equals("null")) {
			str = "0";
		} else if (str.equals("")) {
			str = "0";
		}
		try {
			conv = Integer.parseInt(str);
		} catch (Exception e) {

		}
		return conv;
	}
	public ActionForward loadAccountHeads(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		ViewGlTransactionsActionForm actionForm = (ViewGlTransactionsActionForm) form;
		
		
		return null;
	}
}