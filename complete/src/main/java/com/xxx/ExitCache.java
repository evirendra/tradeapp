package com.xxx;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class ExitCache {

	public static final String BANK_NIFTY_UPPER_THRESHOLD = "bankNiftyUpperThreshHold";
	public static final String BANK_NIFTY_LOWER_THRESHOLD = "bankNiftyLowerThreshHold";
	public static final String OPTION_TOTAL_UPPER = "optionTotalUpper";
	public static final String OPTION_TOTAL_LOWER = "optionTotalLower";
	public static final String EXIT_AT_CALL_OPTION = "exitAtCallOption";
	public static final String EXIT_AT_PUT_OPTION = "exitAtPutOption";
	public static final String TEST_MODE = "testMode";

	public static final HashMap<String, String> data = new HashMap<>();

	private static ExitAction exitAction = new ExitAction();
	private static final Logger logger = LoggerFactory.getLogger(ExitCache.class);

	public static void addExitData(String key, String value) {
		data.put(key, value);
	}

	public static boolean shouldExit(BankNiftyData bankNiftyData) {
		boolean exit = false;
		Number ltpPrice = bankNiftyData.getLtpPrice();
		BankNiftyPosition  bankNiftyPosition = new BankNiftyPosition();

		String upperLevel = data.get(BANK_NIFTY_UPPER_THRESHOLD);
		String lowerLevel = data.get(BANK_NIFTY_LOWER_THRESHOLD);
		if (ltpPrice != null) {

			if (!StringUtils.isEmpty(upperLevel) && ltpPrice.doubleValue() >= Long.parseLong(upperLevel)) {
				logger.info("Upper Threshhold Reached- Must Exit :" + ltpPrice);
				return true;
			}
			if (!StringUtils.isEmpty(lowerLevel) && ltpPrice.doubleValue() <= Long.parseLong(lowerLevel)) {
				logger.info("Lower Threshhold Reached- Must Exit :" + ltpPrice);
				return true;
			}

			String exitCallOption = getExitAction().getCallOption();
			bankNiftyPosition.setCallOption(exitCallOption);
			Double callOptionLtpPrice = null;
			if (!StringUtils.isEmpty(exitCallOption)) {
				callOptionLtpPrice = getOptionLTPPrice(Long.parseLong(exitCallOption),
						bankNiftyData.getCallOptionData());
				bankNiftyPosition.setCallOptionLtp(callOptionLtpPrice);

				if (!StringUtils.isEmpty(data.get(EXIT_AT_CALL_OPTION))
						&& callOptionLtpPrice >= Double.parseDouble(data.get(EXIT_AT_CALL_OPTION))) {
					logger.info("Call Option Upper  threshold Reached- Must Exit :");
					return true;
				}
			}
			String exitPutOption = getExitAction().getPutOption();
			bankNiftyPosition.setPutOption(exitPutOption);
			Double putOptionLtpPrice = null;
			if (!StringUtils.isEmpty(exitPutOption)) {
				putOptionLtpPrice = getOptionLTPPrice(Long.parseLong(exitPutOption), bankNiftyData.getPutOptionData());
				bankNiftyPosition.setPutOptionLtp(putOptionLtpPrice);
				if (!StringUtils.isEmpty(data.get(EXIT_AT_PUT_OPTION))
						&& putOptionLtpPrice >= Double.parseDouble(data.get(EXIT_AT_PUT_OPTION))) {
					logger.info("Put Option Upper  threshold Reached- Must Exit :");
					return true;
				}
			}

			if (callOptionLtpPrice != null && putOptionLtpPrice != null) {
				Double optionTotal = Double.sum(callOptionLtpPrice, putOptionLtpPrice);
				bankNiftyPosition.setTotalOptionLtp(optionTotal);
				if (!StringUtils.isEmpty(data.get(OPTION_TOTAL_UPPER))
						&& optionTotal >= Double.parseDouble(data.get(OPTION_TOTAL_UPPER))) {
					logger.info(" Option total  Upper  threshold Reached- Must Exit :");
					return true;
				}

				if (!StringUtils.isEmpty(data.get(OPTION_TOTAL_LOWER))
						&& optionTotal <= Double.parseDouble(data.get(OPTION_TOTAL_LOWER))) {
					logger.info(" Option total  Lower  threshold Reached- Must Exit :");
					return true;
				}
			}
		}
		bankNiftyData.setBankNiftyPosition(bankNiftyPosition);
		return exit;
	}

	private static Double getOptionLTPPrice(long option, List<BankNiftyOptionData> callOptionData) {
		for (BankNiftyOptionData bankNiftyOptionData : callOptionData) {
			if (bankNiftyOptionData.getReferenceValue() == option) {
				double optionLtpPrice = bankNiftyOptionData.getLtpPrice().doubleValue();
				return optionLtpPrice;
			}
		}
		return null;
	}

	private static boolean checkOptionUpperLevel(long option, String optionUpperThreshHold,
			List<BankNiftyOptionData> callOptionData) {
		boolean shouldExit = false;
		for (BankNiftyOptionData bankNiftyOptionData : callOptionData) {
			if (bankNiftyOptionData.getReferenceValue() == option) {
				double optionLtpPrice = bankNiftyOptionData.getLtpPrice().doubleValue();

				if (!StringUtils.isEmpty(optionUpperThreshHold)
						&& optionLtpPrice >= Double.parseDouble(optionUpperThreshHold)) {
					shouldExit = true;
				}
			}
		}
		return shouldExit;
	}

	public static ExitAction getExitAction() {
		return exitAction;
	}

	public static boolean isTestMode() {
		if (data.containsKey(TEST_MODE)) {
			return Boolean.parseBoolean(data.get(TEST_MODE));
		}
		return false;
	}

	public static String convertToString() {
		String str = data.toString() + "\n";
		str = str + getExitAction().toString();
		return str;
	}

}
