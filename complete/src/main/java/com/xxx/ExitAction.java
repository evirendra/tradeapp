package com.xxx;

public class ExitAction {

	private String callOption;
	private String callOptionQty;
	private String putOption;
	private String putOptionQty;
	private boolean exitActionEnabled;
	
	


	public boolean isExitActionEnabled() {
		return exitActionEnabled;
	}

	public void setExitActionEnabled(boolean exitActionEnabled) {
		this.exitActionEnabled = exitActionEnabled;
	}

	public String getCallOption() {
		return callOption;
	}

	public void setCallOption(String callOption) {
		this.callOption = callOption;
	}

	public String getCallOptionQty() {
		return callOptionQty;
	}
	

	public void setCallOptionQty(String callOptionQty) {
		this.callOptionQty = callOptionQty;
	}

	public String getPutOption() {
		return putOption;
	}

	public void setPutOption(String putOption) {
		this.putOption = putOption;
	}

	public String getPutOptionQty() {
		return putOptionQty;
	}

	public void setPutOptionQty(String putOptionQty) {
		this.putOptionQty = putOptionQty;
	}

	
}
