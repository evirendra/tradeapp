package com.xxx;

import java.util.HashMap;

public class ExitCache {

	public static final String BANK_NIFTY_UPPER_THRESHOLD = "bankNiftyUpperThreshHold";
	public static final String BANK_NIFTY_LOWER_THRESHOLD = "bankNiftyLowerThreshHold";
	public static final HashMap<String, Long> data = new HashMap<>();

	private static  ExitAction exitAction = new ExitAction();

	public static void addExitData(String key, Long value) {
		data.put(key, value);
	}

	public static boolean shouldExit(BankNiftyData bankNiftyData) {
		boolean exit = false;
		Number ltpPrice = bankNiftyData.getLtpPrice();

		Long upperLevel = data.get(BANK_NIFTY_UPPER_THRESHOLD);
		Long lowerLevel = data.get(BANK_NIFTY_LOWER_THRESHOLD);
		if (ltpPrice != null) {

			if (upperLevel != null && ltpPrice.doubleValue() >= upperLevel) {
				exit = true;
			}
			if (lowerLevel != null && ltpPrice.doubleValue() <= lowerLevel) {
				exit = true;
			}
		}
		return exit;
	}

	public static ExitAction getExitAction() {
		return exitAction;
	}



}
