package com.xxx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.qrtz.cache.Cache;

@RestController

public class LoginController {

	// private static final String API_SECRET="5d541q3woc5fxjf4e3fszpatofiqv95f";

	// private static final String API_KEY="hes00cdr4giilrsk";
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@RequestMapping("/api/hello")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public String hello() {
		return "hello";
	}

	@RequestMapping("/api/login")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public ResponseEntity<String> login(@RequestParam(value = "name", defaultValue = "World") String name) {

		String loginURL = "https://kite.trade/connect/login?v= {v}&api_key={api_key}";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		Map<String, String> params = new HashMap<>();
		params.put("v", "3");
		params.put("api_key", KeyCache.getAPIKey());
		HttpMethod httpMethod = HttpMethod.POST;

		ResponseEntity<String> response = fetchResponse(loginURL, headers, params, httpMethod);

		return response;
	}

	@RequestMapping("/api/home")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public ResponseEntity<String> home(@RequestParam(value = "request_token") String requestToken) throws JSONException {

		String hashableText = KeyCache.getAPIKey() + requestToken + KeyCache.getAPISecretKey();
		String sha256hex = DigestUtils.sha256Hex(hashableText);
		String SessionTokenURL = "https://api.kite.trade/session/token";

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("api_key", KeyCache.getAPIKey());
		map.add("request_token", requestToken);
		map.add("checksum", sha256hex);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(SessionTokenURL, request, String.class);

		String jsonSessionInfo = response.getBody();

		JSONObject jsonObject = new JSONObject(jsonSessionInfo);
		JSONObject dataObject = (JSONObject) jsonObject.get("data");
		String accessToken = (String) dataObject.get("access_token");
		KeyCache.addAccessTokenKey(accessToken);
		logger.info("KeyCache :" + KeyCache.convertToString() );
		// ResponseEntity<String> responseforLTP = fetchLTP(accessToken);
		return response;

	}

	@RequestMapping("/api/bankNiftyData")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public BankNiftyData bankNiftyData() throws JSONException, InterruptedException {

		BankNiftyData bankNiftyData = new BankNiftyData();

//		ResponseEntity<String> response = fetchLTP(bankNiftyData.getInstrumentName());
//		fillBankNiftyLtpPrice(bankNiftyData, response);
		Cache.subscribe(bankNiftyData.getInstrumentName());
		Thread.sleep(2000);
		bankNiftyData.setLtpPrice(Cache.get(bankNiftyData.getInstrumentName()));

		String populateOptionData = bankNiftyData.populateOptionData();
//		String ltpURL = "https://api.kite.trade/quote/ltp?" + populateOptionData;
//		ResponseEntity<String> optionResponse = fetchLTPFromURL(ltpURL);
		Thread.sleep(2000);
//		fillLastPriceOptionData(bankNiftyData, optionResponse);
		fillLastPriceOptionData(bankNiftyData);
		validateAndPerformSellORExit(bankNiftyData);
		// logger.info("Access Token From Session: " +
		// session.getAttribute(ACCESS_TOKEN));
		return bankNiftyData;

	}

	private void validateAndPerformSellORExit(BankNiftyData bankNiftyData) {
		ExitAction exitAction = ExitCache.getExitAction();
		if (exitAction.isExitActionEnabled()) {
			int action = ExitCache.determineAction(bankNiftyData);

			if (action == 1) {
				logger.info("Exit Condition triggerd at :" + bankNiftyData.getLtpPrice());

				String callInstrumentSymbol = BankNiftyOptionData.getInstrumentName(exitAction.getCallOption(), true);
				String putInstrumentSymbol = BankNiftyOptionData.getInstrumentName(exitAction.getPutOption(), false);
				placeBuyOrder(callInstrumentSymbol, exitAction.getCallOptionQty());
				placeBuyOrder(putInstrumentSymbol, exitAction.getPutOptionQty());
				exitAction.setExitActionEnabled(false);
				logger.info("Exited");
			}
			if (action == 2) {
				logger.info("Sell Condition triggerd at :" + bankNiftyData.getLtpPrice());

				String callInstrumentSymbol = BankNiftyOptionData.getInstrumentName(exitAction.getCallOption(), true);
				String putInstrumentSymbol = BankNiftyOptionData.getInstrumentName(exitAction.getPutOption(), false);
				if (!ExitCache.isTestMode()) {
					placeSellOrder(callInstrumentSymbol, exitAction.getCallOptionQty());
					placeSellOrder(putInstrumentSymbol, exitAction.getPutOptionQty());
					exitAction.setSellIfTotalReachesFlag(false);
					logger.info("Sold");
				} else {
					logger.info("Running in Test Mode, Sell order will not be Placed");
				}
			}
		}
	}

	@RequestMapping("/api/refreshBankNiftyData")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public BankNiftyData refreshBankNiftyData(@RequestParam(value = "bankNiftyLtpPrice") Double bankNiftyLtpPrice)
			throws JSONException {

		BankNiftyData bankNiftyData = new BankNiftyData();

		bankNiftyData.setLtpPrice(bankNiftyLtpPrice);

		String populateOptionData = bankNiftyData.populateOptionData();
//		String ltpURL = "https://api.kite.trade/quote/ltp?" + populateOptionData + "&i=" + bankNiftyData.getInstrumentName();

//		ResponseEntity<String> optionResponse = fetchLTPFromURL(ltpURL);
//		fillBankNiftyLtpPrice(bankNiftyData, optionResponse);
		bankNiftyData.setLtpPrice(Cache.get(bankNiftyData.getInstrumentName()));
//		fillLastPriceOptionData(bankNiftyData, optionResponse);
		fillLastPriceOptionData(bankNiftyData);
		validateAndPerformSellORExit(bankNiftyData);

		return bankNiftyData;

	}

	@RequestMapping("/api/exitNow")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public boolean exitNow() throws JSONException {

		ExitAction exitAction = ExitCache.getExitAction();

		logger.info("Exit Immediately triggerd");

		if (exitAction.isExitActionEnabled()) {

			String callInstrumentSymbol = BankNiftyOptionData.getInstrumentName(exitAction.getCallOption(), true);
			String putInstrumentSymbol = BankNiftyOptionData.getInstrumentName(exitAction.getPutOption(), false);
			placeBuyOrder(callInstrumentSymbol, exitAction.getCallOptionQty());
			placeBuyOrder(putInstrumentSymbol, exitAction.getPutOptionQty());
			exitAction.setExitActionEnabled(false);
			logger.info("Exited");
		} else {
			logger.info("Exit Action is Disabled");
		}

		return true;

	}

	private void fillBankNiftyLtpPrice(BankNiftyData bankNiftyData, ResponseEntity<String> optionResponse)
			throws JSONException {
		JSONObject jsonObject = new JSONObject(optionResponse.getBody());
		JSONObject dataObject = (JSONObject) jsonObject.get("data");
		Number lastPrice = getLastPrice(bankNiftyData, dataObject);
		bankNiftyData.setLtpPrice(lastPrice);
	}

	@RequestMapping("/api/updateExitCache")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public boolean updateExitCache(@RequestParam(value = "bankNiftyUpperThreshHold") String bankNiftyUpperThreshHold,
			@RequestParam(value = "bankNiftyLowerThreshHold") String bankNiftyLowerThreshHold,
			@RequestParam(value = "callOptionSymbol") String callOptionSymbol,
			@RequestParam(value = "callOptionQty") String callOptionQty,
			@RequestParam(value = "putOptionSymbol") String putOptionSymbol,
			@RequestParam(value = "putOptionQty") String putOptionQty,
			@RequestParam(value = "optionTotalUpper") String optionTotalUpper,
			@RequestParam(value = "optionTotalLower") String optionTotalLower,
			@RequestParam(value = "exitAtCallOption") String exitAtCallOption,
			@RequestParam(value = "exitAtPutOption") String exitAtPutOption,
			@RequestParam(value = "exitActionEnabled") String exitActionEnabled,
			@RequestParam(value = "testMode") String testMode,
			@RequestParam(value = "sellIfTotalReachesFlag") String sellIfTotalReachesFlag,
			@RequestParam(value = "sellIfTotalReachesAmount") String sellIfTotalReachesAmount) throws JSONException {

		ExitCache.addExitData(ExitCache.BANK_NIFTY_UPPER_THRESHOLD, bankNiftyUpperThreshHold);
		ExitCache.addExitData(ExitCache.BANK_NIFTY_LOWER_THRESHOLD, bankNiftyLowerThreshHold);
		ExitCache.addExitData(ExitCache.OPTION_TOTAL_UPPER, optionTotalUpper);
		ExitCache.addExitData(ExitCache.OPTION_TOTAL_LOWER, optionTotalLower);
		ExitCache.addExitData(ExitCache.EXIT_AT_CALL_OPTION, exitAtCallOption);
		ExitCache.addExitData(ExitCache.EXIT_AT_PUT_OPTION, exitAtPutOption);
		ExitCache.addExitData(ExitCache.TEST_MODE, testMode);
		ExitCache.getExitAction().populateExitAction(callOptionSymbol, callOptionQty, putOptionSymbol, putOptionQty,
				Boolean.parseBoolean(exitActionEnabled), Boolean.parseBoolean(sellIfTotalReachesFlag),
				sellIfTotalReachesAmount);

		logger.info("Exit Action Defined :" + ExitCache.convertToString());

		return true;
	}

	private void fillLastPriceOptionData(BankNiftyData bankNiftyData, ResponseEntity<String> optionResponse)
			throws JSONException {
		JSONObject jsonObject = new JSONObject(optionResponse.getBody());
		JSONObject dataObject = (JSONObject) jsonObject.get("data");
		List<BankNiftyOptionData> callOptionData = bankNiftyData.getCallOptionData();
		for (BankNiftyOptionData bankNiftyOptionData : callOptionData) {
			Number lastPrice = getLastPrice(bankNiftyOptionData, dataObject);
			bankNiftyOptionData.setLtpPrice(lastPrice);
		}

		List<BankNiftyOptionData> putOptionData = bankNiftyData.getPutOptionData();
		for (BankNiftyOptionData bankNiftyOptionData : putOptionData) {
			Number lastPrice = getLastPrice(bankNiftyOptionData, dataObject);
			bankNiftyOptionData.setLtpPrice(lastPrice);
		}

	}
	
	private void fillLastPriceOptionData(BankNiftyData bankNiftyData)
			throws JSONException {
		List<BankNiftyOptionData> callOptionData = bankNiftyData.getCallOptionData();
		for (BankNiftyOptionData bankNiftyOptionData : callOptionData) {
			bankNiftyOptionData.setLtpPrice(Cache.get(bankNiftyOptionData.getInstrumentName()));
		}

		List<BankNiftyOptionData> putOptionData = bankNiftyData.getPutOptionData();
		for (BankNiftyOptionData bankNiftyOptionData : putOptionData) {
			bankNiftyOptionData.setLtpPrice(Cache.get(bankNiftyOptionData.getInstrumentName()));
		}

	}

	private Number getLastPrice(BankNiftyData bankNiftyData, JSONObject dataObject) throws JSONException {
		JSONObject instrumentDetails = (JSONObject) dataObject.get(bankNiftyData.getInstrumentName());
		Number lastPrice = (Number) instrumentDetails.get("last_price");
		return lastPrice;
	}

	private Number getLastPrice(BankNiftyOptionData bankNiftyOptionData, JSONObject dataObject) throws JSONException {
		JSONObject instrumentDetails = (JSONObject) dataObject.get(bankNiftyOptionData.getInstrumentName());
		Number lastPrice = (Number) instrumentDetails.get("last_price");
		return lastPrice;
	}

	private ResponseEntity<String> fetchLTP(String instrument) {
		String ltpURL = "https://api.kite.trade/quote/ltp?i={instr}";
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Kite-Version", "3");
		headers.add("Authorization", KeyCache.getAuthorizationStr());
		Map<String, String> params = new HashMap<>();
		params.put("instr", instrument);
		HttpMethod httpMethod = HttpMethod.GET;

		RestTemplate restTemplate = new RestTemplate();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(ltpURL, httpMethod, entity, String.class, params);
		return response;

	}

	private ResponseEntity<String> fetchLTPFromURL(String ltpURL) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Kite-Version", "3");
		headers.add("Authorization", KeyCache.getAuthorizationStr());
		HttpMethod httpMethod = HttpMethod.GET;

		RestTemplate restTemplate = new RestTemplate();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(ltpURL, httpMethod, entity, String.class);
		return response;

	}

	private ResponseEntity<String> fetchResponse(String url, HttpHeaders headers, Map<String, String> params,
			HttpMethod httpMethod) {
		RestTemplate restTemplate = new RestTemplate();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class, params);
		return response;
	}

	@RequestMapping("/api/options/sell")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public String Sell(@RequestParam(value = "callOptionSymbol") String callOptionSymbol,
			@RequestParam(value = "callOptionQty") String callOptionQty,
			@RequestParam(value = "putOptionSymbol") String putOptionSymbol,
			@RequestParam(value = "putOptionQty") String putOptionQty) throws JSONException, InterruptedException {

		String callOptionResponse = placeSellOrder(callOptionSymbol, callOptionQty);
		String putOptionResponse = placeSellOrder(putOptionSymbol, putOptionQty);

		// ExitCache.getExitAction().populateExitAction(callOptionSymbol, callOptionQty,
		// putOptionSymbol, putOptionQty,
		// true);
		return putOptionResponse;

	}

	private String placeSellOrder(String symbol, String qty) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		String orderType = "SELL";
		params.add("quantity", qty);
		String[] split = symbol.split(":");
		params.add("exchange", split[0]);
		params.add("tradingsymbol", split[1]);
		params.add("transaction_type", orderType);

		return placeOrder(params);
	}

	private String placeBuyOrder(String symbol, String qty) {
		if (!ExitCache.isTestMode()) {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			String orderType = "BUY";
			params.add("quantity", qty);
			String[] split = symbol.split(":");
			params.add("exchange", split[0]);
			params.add("tradingsymbol", split[1]);
			params.add("transaction_type", orderType);

			return placeOrder(params);
		} else {
			logger.info("Running in Test Mode, Buy order will not be Placed");
		}
		return "";
	}

	private String placeOrder(MultiValueMap<String, String> params) {
		String tradeURL = "https://api.kite.trade/orders/regular";
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Kite-Version", "3");
		headers.add("Authorization", KeyCache.getAuthorizationStr());

		params.add("order_type", "MARKET");
		params.add("product", "MIS");
		params.add("validity", "DAY");

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params,
				headers);

		String response = fetchResponse(tradeURL, requestEntity);
		return response;
	}

	private String fetchResponse(String url, HttpEntity<MultiValueMap<String, String>> requestEntity) {
		RestTemplate restTemplate = new RestTemplate();
		String response = restTemplate.postForObject(url, requestEntity, String.class);
		return response;
	}

}
