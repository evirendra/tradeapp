package com.xxx;

public class BankNiftyPosition {
	private String callOption;
	private Double callOptionLtp;
	private String putOption;
	private Double putOptionLtp;
	private Double totalOptionLtp;

	public String getCallOption() {
		return callOption;
	}

	public void setCallOption(String callOption) {
		this.callOption = callOption;
	}

	public String getPutOption() {
		return putOption;
	}

	public void setPutOption(String putOption) {
		this.putOption = putOption;
	}

	public Double getCallOptionLtp() {
		return callOptionLtp;
	}

	public void setCallOptionLtp(Double callOptionLtp) {
		this.callOptionLtp = callOptionLtp;
	}

	public Double getPutOptionLtp() {
		return putOptionLtp;
	}

	public void setPutOptionLtp(Double putOptionLtp) {
		this.putOptionLtp = putOptionLtp;
	}

	public Double getTotalOptionLtp() {
		return totalOptionLtp;
	}

	public void setTotalOptionLtp(Double totalOptionLtp) {
		this.totalOptionLtp = totalOptionLtp;
	}

}
