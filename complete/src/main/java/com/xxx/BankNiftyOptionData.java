package com.xxx;

public class BankNiftyOptionData {
	private static final String exchange = "NFO";
	private static final String expiry = "05JUL18";
	private static final String symbol = "BANKNIFTY";
	private long referenceValue;
	private Number ltpPrice;
	private boolean isCallOption = false;

	public Number getLtpPrice() {
		return ltpPrice;
	}

	public void setLtpPrice(Number ltpPrice) {
		this.ltpPrice = ltpPrice;
	}

	public long getReferenceValue() {
		return referenceValue;
	}

	public void setReferenceValue(long referenceValue) {
		this.referenceValue = referenceValue;
	}

	public String getInstrumentName() {
		String instrumentName = exchange + ":" + symbol + expiry + referenceValue;
		if (isCallOption) {
			instrumentName = instrumentName + "CE";
		} else {
			instrumentName = instrumentName + "PE";
		}
		return instrumentName;
	}
	
	public static String getInstrumentName(String referenceValueData, boolean isCallOptionData) {
		String instrumentName = exchange + ":" + symbol + expiry + referenceValueData;
		if (isCallOptionData) {
			instrumentName = instrumentName + "CE";
		} else {
			instrumentName = instrumentName + "PE";
		}
		return instrumentName;
	}

	public boolean isCallOption() {
		return isCallOption;
	}

	public void setCallOption(boolean isCallOption) {
		this.isCallOption = isCallOption;
	}

}
