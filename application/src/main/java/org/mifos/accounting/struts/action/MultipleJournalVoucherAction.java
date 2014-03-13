package org.mifos.accounting.struts.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.mifos.accounting.struts.actionform.MultipleGeneralLedgerActionForm;
import org.mifos.accounting.struts.actionform.MultipleJournalVoucherActionForm;
import org.mifos.application.accounting.business.GlDetailBO;
import org.mifos.application.accounting.business.GlMasterBO;
import org.mifos.application.servicefacade.AccountingServiceFacade;
import org.mifos.application.servicefacade.AccountingServiceFacadeWebTier;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.dto.domain.CoaNamesDto;
import org.mifos.dto.domain.DynamicOfficeDto;
import org.mifos.dto.domain.GLCodeDto;
import org.mifos.dto.domain.OfficeDetailsDto;
import org.mifos.dto.domain.OfficeGlobalDto;
import org.mifos.dto.domain.OfficeHierarchy;
import org.mifos.dto.domain.OfficesList;
import org.mifos.dto.domain.RolesActivityDto;
import org.mifos.dto.screen.OfficeFormDto;
import org.mifos.framework.struts.action.BaseAction;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.security.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleJournalVoucherAction extends BaseAction{
	private static final Logger logger = LoggerFactory
			.getLogger(JournalVoucherAction.class);
	private AccountingServiceFacade accountingServiceFacade = new AccountingServiceFacadeWebTier();
	List<GLCodeDto> glcodelist=null;
	public ActionForward load(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {

		MultipleJournalVoucherActionForm actionForm = (MultipleJournalVoucherActionForm) form;
		List<GLCodeDto> accountingDtos = null;
		List<RolesActivityDto> rolesactivitydto=null;
		accountingDtos = accountingServiceFacade.loadDebitAccounts();
		java.util.Date voucherDate = DateUtils.getCurrentDateWithoutTimeStamp();
		actionForm.setVoucherDate(voucherDate);
		rolesactivitydto=accountingServiceFacade.jvloadRolesActivity();
		UserContext context = getUserContext(request);
		actionForm.setOfficeLevelId(String.valueOf(context.getOfficeLevelId()));
	//	List listOfOfficeHierarchyObject = getOfficeLevels(actionForm);
		boolean journalVoucherSave=rolesactivitydto.isEmpty();
		//madhav
	      Short officeLevel = context.getOfficeLevelId();
	        OfficeFormDto officeFormDto = this.officeServiceFacade.retrieveOfficeFormInformationForfa(officeLevel);
	        List<OfficeDetailsDto> listOfOfficeHierarchyObjectddd=officeFormDto.getOfficeLevels();
	        List<OfficeHierarchy> listofofficess=new ArrayList<OfficeHierarchy>();
	        OfficeHierarchy officeHierarchy;
	        for(OfficeDetailsDto officeDetailsDto:listOfOfficeHierarchyObjectddd){
	            officeHierarchy=new OfficeHierarchy(officeDetailsDto.getLevelId().toString(),officeDetailsDto.getLevelName());
	            listofofficess.add(officeHierarchy);   
	        }
	        
		storingSession(request, "listOfOffices", listofofficess);
		storingSession(request, "jvsave", journalVoucherSave);
		storingSession(request, "DebitAccountGlCodes", accountingDtos);
		return mapping.findForward(ActionForwards.load_success.toString());
	}

	public ActionForward loadOffices(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {

		MultipleJournalVoucherActionForm actionForm = (MultipleJournalVoucherActionForm) form;


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

	public ActionForward loadCreditAccount(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		MultipleJournalVoucherActionForm actionForm = (MultipleJournalVoucherActionForm) form;
		List<GLCodeDto> accountingDtos = null;
		accountingDtos = accountingServiceFacade.loadCreditAccounts(actionForm
				.getDebitAccountHead());
		storingSession(request, "CreditAccounts", accountingDtos);
		return mapping.findForward(ActionForwards.load_success.toString());
	}

	public ActionForward preview(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		
		
		MultipleJournalVoucherActionForm actionForm = (MultipleJournalVoucherActionForm) form;
		String [] accounthead=actionForm.getCreditAccountHead();
		String [] amounts=actionForm.getAmount();
		String [] trannotes=actionForm.getVoucherNotes();
		// saveErrors(request, actionerrors);
		double total=0;
	 glcodelist=new ArrayList<GLCodeDto>();
		for(int i=0;i<amounts.length;i++)
		{
			total =total+ Double.parseDouble(amounts[i]);
			GLCodeDto gLcodeDto= new GLCodeDto();
			gLcodeDto.setAccountHead(accounthead[i]);
			gLcodeDto.setAmounts(amounts[i]);
			gLcodeDto.setTrannotes(trannotes[i]);
			glcodelist.add(gLcodeDto);
		}
		//actionForm.setTotal((String.format("%.2f", total)));
		storingSession(request, "accounHeadValues", glcodelist);
		storingSession(request, "multiplejournalvoucheractionform", actionForm);
		monthClosingServiceFacade.validateTransactionDate(DateUtils.getDate(actionForm.getVoucherDate()));
		return mapping.findForward(ActionForwards.preview_success.toString());
	}

	public ActionForward previous(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.previous_success.toString());
	}

	public ActionForward cancel(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.cancel_success.toString());

	}

	public ActionForward submit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {
		MultipleJournalVoucherActionForm actionForm = (MultipleJournalVoucherActionForm) form;
		int stage=1;
		insertionSaveAndStage(actionForm,request,stage);
		return mapping.findForward("submit_success");
	}

	public ActionForward saveStageSubmit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			@SuppressWarnings("unused") HttpServletResponse response)
			throws Exception {

		MultipleJournalVoucherActionForm actionForm = (MultipleJournalVoucherActionForm) form;

		int stage=0;
		//
		insertionSaveAndStage(actionForm,request,stage);
		return mapping.findForward("submit_success");
	}


	public void insertionSaveAndStage(MultipleJournalVoucherActionForm actionForm,HttpServletRequest request,int stage)
    {
		List<String> amountActionList = getAmountAction(actionForm);
		List<GlDetailBO> glDetailBOList = getGlDetailBOList(actionForm,
				amountActionList);
		//
		GlMasterBO glMasterBO = new GlMasterBO();
		glMasterBO.setTransactionDate(DateUtils.getDate(actionForm.getVoucherDate()));
		glMasterBO.setTransactionType(actionForm.getTrxnType());
		glMasterBO.setFromOfficeLevel(new Integer(actionForm
				.getOfficeHierarchy()));
		glMasterBO.setFromOfficeId(actionForm.getOffice());
		glMasterBO
				.setToOfficeLevel(new Integer(actionForm.getOfficeHierarchy()));
		glMasterBO.setToOfficeId(actionForm.getOffice());
		glMasterBO.setMainAccount(actionForm.getDebitAccountHead());
		String amounts[]=actionForm.getAmount();
		 BigDecimal total=new BigDecimal(0);
		for (int i = 0; i <amounts.length; i++) {
			total=total.add(new BigDecimal(amounts[i]));
		glMasterBO.setTransactionAmount(total);
		glMasterBO.setAmountAction(amountActionList.get(0));
		glMasterBO.setTransactionNarration("succes");
		glMasterBO.setGlDetailBOList(glDetailBOList);
		glMasterBO.setStatus("");
		glMasterBO.setStage(stage);
		glMasterBO.setTransactionBy(0);
		glMasterBO.setCreatedBy(getUserContext(request).getId());
		glMasterBO.setCreatedDate(DateUtils.getCurrentDateWithoutTimeStamp());
		if(stage==0)
		{
			accountingServiceFacade.savingStageAccountingTransactions(glMasterBO);
		}else{
		accountingServiceFacade.savingAccountingTransactions(glMasterBO);
		}
		}
    }


/*	List<GlDetailBO> getGlDetailBOList(MultipleJournalVoucherActionForm actionForm,
			List<String> amountActionList) {
		List<GlDetailBO> glDetailBOList = new ArrayList<GlDetailBO>();
		glDetailBOList.add(new GlDetailBO(actionForm.getCreditAccountHead(),
				new BigDecimal(actionForm.getAmount()),
				amountActionList.get(1), null, null, null, null,actionForm.getVoucherNotes()));
		return glDetailBOList;

	}*/
	
	List<GlDetailBO> getGlDetailBOList(MultipleJournalVoucherActionForm actionForm,
			List<String> amountActionList) {
		List<GlDetailBO> glDetailBOList = new ArrayList<GlDetailBO>();
		String [] amounts =actionForm.getAmount();
		String [] trannotes=actionForm.getVoucherNotes();
		String [] accountHeads=actionForm.getCreditAccountHead();
	  
		List <GLCodeDto> gLCodeDtolist=new ArrayList<GLCodeDto>();
	for (int i = 0; i <amounts.length; i++) {
		
		GLCodeDto gLCodeDto=new GLCodeDto();
		gLCodeDto.setAmounts(amounts[i]);
		gLCodeDto.setAccountHead(accountHeads[i]);
		gLCodeDto.setTrannotes(trannotes[i]);
		/*if(amounts.length<=accountHeads.length)
		{
			gLCodeDto.setAccountHead(accountHeads[i]);
		}*/
		gLCodeDtolist.add(gLCodeDto);
		}
		for(GLCodeDto glCodeDto:gLCodeDtolist)
		{
			if(glCodeDto.getAccountHead() !=null)
			{
			List<CoaNamesDto> subaccounthead=accountingServiceFacade.loadCoaNamesWithGlcodeValues(glCodeDto.getAccountHead());
		for(CoaNamesDto subaccount:subaccounthead)
		{
			double amount=Double.parseDouble(glCodeDto.getAmounts());
			String Accounthead=glCodeDto.getAccountHead();
			if(amount>0)
			{

				glDetailBOList.add(new GlDetailBO(subaccount.getGlcodeValue(),new BigDecimal(glCodeDto.getAmounts()),amountActionList.get(1),null,null,null,null,glCodeDto.getTrannotes()));
			}
		}
			}
		}

		return glDetailBOList;
	}

	public List<String> getAmountAction(MultipleJournalVoucherActionForm actionForm) {
		List<String> amountActionList = new ArrayList<String>();

		amountActionList.add("debit");// for MainAccount amountAction
		amountActionList.add("credit");// for SubAccount amountAction

		return amountActionList;
	}

	public void storingSession(HttpServletRequest httpServletRequest, String s,
			Object o) {
		httpServletRequest.getSession().setAttribute(s, o);
	}

public List getOfficeLevels(MultipleJournalVoucherActionForm actionForm){

		List listOfOffices = new ArrayList();
		OfficeHierarchy officeHierarchy = null;

		 switch (Integer.parseInt(actionForm.getOfficeLevelId())){
		  case 1:

			    officeHierarchy = new OfficeHierarchy("1","Head Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("2","Regional Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("3","Divisional Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("4","Area Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("5","Branch Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("6","Center");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("7","Group");
			    listOfOffices.add(officeHierarchy);


		 break;
		  case 2:

			    officeHierarchy = new OfficeHierarchy("2","Regional Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("3","Divisional Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("4","Area Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("5","Branch Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("6","Center");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("7","Group");
			    listOfOffices.add(officeHierarchy);


		  break;
		  case 3:

			    officeHierarchy = new OfficeHierarchy("3","Divisional Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("4","Area Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("5","Branch Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("6","Center");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("7","Group");
			    listOfOffices.add(officeHierarchy);

		  break;
		  case 4:


			    officeHierarchy = new OfficeHierarchy("4","Area Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("5","Branch Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("6","Center");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("7","Group");
			    listOfOffices.add(officeHierarchy);


		  break;

		  case 5:
			    officeHierarchy = new OfficeHierarchy("5","Branch Office");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("6","Center");
			    listOfOffices.add(officeHierarchy);
			    officeHierarchy = new OfficeHierarchy("7","Group");
			    listOfOffices.add(officeHierarchy);


	      break;


		  }

		return listOfOffices;
	}
}
