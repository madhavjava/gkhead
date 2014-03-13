package org.mifos.dto.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class ViewOpeningBalanceDto implements Serializable {

	private static final long serialVersionUID = 1L;

	public int glBalancesId;
	public String officeHierarchy;
	public String glCodeValue;
	public BigDecimal openingBalance;
	public BigDecimal transactionDebitSum;
	public BigDecimal transactionCreditSum;
	public BigDecimal closingBalance;

	public int getGlBalancesId() {
		return glBalancesId;
	}

	public void setGlBalancesId(int glBalancesId) {
		this.glBalancesId = glBalancesId;
	}

	public String getOfficeHierarchy() {
		return officeHierarchy;
	}

	public void setOficeHierarchy(String officeHierarchy) {
		this.officeHierarchy = officeHierarchy;
	}

	/*
	 * public String getOfficeId() { return officeId; }
	 * 
	 * 
	 * 
	 * public void setOfficeId(String officeId) { this.officeId = officeId; }
	 */

	public String getGlCodeValue() {
		return glCodeValue;
	}

	public void setGlCodeValue(String glCodeValue) {
		this.glCodeValue = glCodeValue;
	}

	public BigDecimal getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(BigDecimal openingBalance) {
		this.openingBalance = openingBalance;
	}

	public BigDecimal getTransactionDebitSum() {
		return transactionDebitSum;
	}

	public void setTransactionDebitSum(BigDecimal transactionDebitSum) {
		this.transactionDebitSum = transactionDebitSum;
	}

	public BigDecimal getTransactionCreditSum() {
		return transactionCreditSum;
	}

	public void setTransactionCreditSum(BigDecimal transactionCreditSum) {
		this.transactionCreditSum = transactionCreditSum;
	}

	public BigDecimal getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(BigDecimal closingBalance) {
		this.closingBalance = closingBalance;
	}

	public ViewOpeningBalanceDto() {
		super();
	}

	public ViewOpeningBalanceDto(int glBalancesId, String officeHierarchy,
			String glCodeValue, BigDecimal openingBalance,
			BigDecimal transactionDebitSum, BigDecimal transactionCreditSum,
			BigDecimal closingBalance) {
		super();
		this.glBalancesId = glBalancesId;
		this.officeHierarchy = officeHierarchy;
		this.glCodeValue = glCodeValue;
		this.openingBalance = openingBalance;
		this.transactionDebitSum = transactionDebitSum;
		this.transactionCreditSum = transactionCreditSum;
		this.closingBalance = closingBalance;
	}

}
