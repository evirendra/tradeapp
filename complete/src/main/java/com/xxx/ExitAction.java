package com.xxx;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

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

	public void populateExitAction(String callOptionSymbol, String callOptionQty, String putOptionSymbol,
			String putOptionQty, boolean exitActionEnabled) {
		this.setCallOption(callOptionSymbol);
		this.setCallOptionQty(callOptionQty);
		this.setPutOption(putOptionSymbol);
		this.setPutOptionQty(putOptionQty);
		this.setExitActionEnabled(exitActionEnabled);
	}
	
	@Override
	public String toString() {
		String str =  StringUtils.arrayToCommaDelimitedString(new String[] {callOption, callOptionQty, putOption, putOptionQty, ""+ exitActionEnabled});
		return str;
	}

}
