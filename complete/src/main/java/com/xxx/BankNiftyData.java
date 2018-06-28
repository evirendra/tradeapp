package com.xxx;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

public class BankNiftyData {
	private static final String exchange = "NSE";
	private static final String symbol = "NIFTY BANK";
	private Number ltpPrice;
	private static final int range = 5;
	private List<BankNiftyOptionData> callOptionData = new ArrayList<>();
	private List<BankNiftyOptionData> putOptionData = new ArrayList<>();

	public Number getLtpPrice() {
		return ltpPrice;
	}

	public void setLtpPrice(Number ltpPrice) {
		this.ltpPrice = ltpPrice;
	}

	public long getReferenceValue() {
		double val = ((double)getLtpPrice() + 50) * 10;
		long n = Math.round(val);
		n = n / 1000;
		n = n * 100;
		return n;
	}

	public String getInstrumentName() {
		return exchange + ":" + symbol;
	}

	public List<String> populateCallOptionData() {
		List<String> callOptionInstrumentNames = new ArrayList<>();
		for (int i = 0; i < BankNiftyData.range; i++) {
			long newReferenceValue = getReferenceValue() + ((i + 1) * 100);
			BankNiftyOptionData bankNiftyOptionData = new BankNiftyOptionData();
			bankNiftyOptionData.setReferenceValue(newReferenceValue);
			bankNiftyOptionData.setCallOption(true);
			callOptionInstrumentNames.add(bankNiftyOptionData.getInstrumentName());
			this.callOptionData.add(bankNiftyOptionData);
		}
		return callOptionInstrumentNames;
	}

	public List<String> populatePutOptionData() {
		List<String> putOptionInstrumentNames = new ArrayList<>();
		for (int i = 0; i < BankNiftyData.range; i++) {
			long newReferenceValue = getReferenceValue() - ((i + 1) * 100);
			BankNiftyOptionData bankNiftyOptionData = new BankNiftyOptionData();
			bankNiftyOptionData.setReferenceValue(newReferenceValue);
			bankNiftyOptionData.setCallOption(false);
			putOptionInstrumentNames.add(bankNiftyOptionData.getInstrumentName());
			this.putOptionData.add(bankNiftyOptionData);
		}
		return putOptionInstrumentNames;
	}

	public String populateOptionData() {
		List<String> callOptionInstrns = populateCallOptionData();
		List<String> putOptionData = populatePutOptionData();

		String callInstrns = StringUtils.collectionToDelimitedString(callOptionInstrns, "&i=");
		String putInstrns = StringUtils.collectionToDelimitedString(putOptionData, "&i=");

		String instrns = "i=" + callInstrns + "&i=" + putInstrns;
		return instrns;
	}

	public List<BankNiftyOptionData> getCallOptionData() {
		return callOptionData;
	}

	public void setCallOptionData(List<BankNiftyOptionData> callOptionData) {
		this.callOptionData = callOptionData;
	}

	public List<BankNiftyOptionData> getPutOptionData() {
		return putOptionData;
	}

	public void setPutOptionData(List<BankNiftyOptionData> putOptionData) {
		this.putOptionData = putOptionData;
	}
	
	

}
