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

@RestController

public class LoginController {

	// private static final String API_SECRET="5d541q3woc5fxjf4e3fszpatofiqv95f";

	// private static final String API_KEY="hes00cdr4giilrsk";
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	private static final String API_KEY = "7ff4ape1tn1i1ni5";

	private static final String API_SECRET = "rjgxz2ey6xc1qf3rw44lr51tjwe86czy";

	private String accessToken = null;

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
		params.put("api_key", API_KEY);
		HttpMethod httpMethod = HttpMethod.POST;

		ResponseEntity<String> response = fetchResponse(loginURL, headers, params, httpMethod);

		return response;
	}

	@RequestMapping("/api/home")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public ResponseEntity<String> home(@RequestParam(value = "request_token") String requestToken)
			throws JSONException {

		String hashableText = API_KEY + requestToken + API_SECRET;
		String sha256hex = DigestUtils.sha256Hex(hashableText);
		String SessionTokenURL = "https://api.kite.trade/session/token";

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("api_key", API_KEY);
		map.add("request_token", requestToken);
		map.add("checksum", sha256hex);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(SessionTokenURL, request, String.class);

		String jsonSessionInfo = response.getBody();

		JSONObject jsonObject = new JSONObject(jsonSessionInfo);
		JSONObject dataObject = (JSONObject) jsonObject.get("data");
		accessToken = (String) dataObject.get("access_token");

		// ResponseEntity<String> responseforLTP = fetchLTP(accessToken);
		return response;

	}

	@RequestMapping("/api/bankNiftyData")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public BankNiftyData bankNiftyData() throws JSONException, InterruptedException {

		BankNiftyData bankNiftyData = new BankNiftyData();

		ResponseEntity<String> response = fetchLTP(bankNiftyData.getInstrumentName());
		
		JSONObject spotPriceJsonObject = new JSONObject(response.getBody());
		JSONObject spotPriceDataObject = (JSONObject) spotPriceJsonObject.get("data");
		Number lastPrice = getLastPrice(bankNiftyData, spotPriceDataObject);
		bankNiftyData.setLtpPrice(lastPrice);

		Thread.sleep(1000);
		String ltpURL = "https://api.kite.trade/quote/ltp?" + bankNiftyData.populateOptionData();
		ResponseEntity<String> optionResponse = fetchLTPFromURL(ltpURL);
		
		fillLastPriceOptionData(bankNiftyData, optionResponse);
		boolean shouldExit = ExitCache.shouldExit(bankNiftyData);
		
		if (shouldExit) {
			System.out.println("Exit Condition:" + bankNiftyData.getLtpPrice());
			logger.info("Exit Condition:" + bankNiftyData.getLtpPrice());
			ExitAction exitAction = ExitCache.getExitAction();
			if(exitAction.isExitActionEnabled()) {
				placeBuyOrder(exitAction.getCallOption(), exitAction.getCallOptionQty());
				placeBuyOrder(exitAction.getPutOption(), exitAction.getPutOptionQty());
				exitAction.setExitActionEnabled(false);
				System.out.println("Exited");
			}
		}
		
		return bankNiftyData;

	}
	
	@RequestMapping("/api/updateExitCache")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public boolean  updateExitCache(@RequestParam(value = "bankNiftyUpperThreshHold") Long bankNiftyUpperThreshHold,
			@RequestParam(value = "bankNiftyLowerThreshHold") Long bankNiftyLowerThreshHold) throws JSONException{
		
		System.out.println(bankNiftyUpperThreshHold + "- " + bankNiftyLowerThreshHold);
		ExitCache.addExitData(ExitCache.BANK_NIFTY_UPPER_THRESHOLD, bankNiftyUpperThreshHold);
		ExitCache.addExitData(ExitCache.BANK_NIFTY_LOWER_THRESHOLD, bankNiftyLowerThreshHold);		
		return true;
	}
	
	

	private void fillLastPriceOptionData(BankNiftyData bankNiftyData, ResponseEntity<String> optionResponse) throws JSONException {
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

	private Number getLastPrice(BankNiftyData bankNiftyData, JSONObject dataObject) throws JSONException {
		JSONObject instrumentDetails = (JSONObject) dataObject.get(bankNiftyData.getInstrumentName());
		Number lastPrice  = (Number) instrumentDetails.get("last_price");
		return lastPrice;
	}
	
	private Number getLastPrice(BankNiftyOptionData bankNiftyOptionData, JSONObject dataObject) throws JSONException {
		JSONObject instrumentDetails = (JSONObject) dataObject.get(bankNiftyOptionData.getInstrumentName());
		Number lastPrice  = (Number) instrumentDetails.get("last_price");
		return lastPrice;
	}


	private ResponseEntity<String> fetchLTP(String instrument) {
		String ltpURL = "https://api.kite.trade/quote/ltp?i={instr}";
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Kite-Version", "3");
		headers.add("Authorization", "token " + API_KEY + ":" + accessToken);
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
		headers.add("Authorization", "token " + API_KEY + ":" + accessToken);
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
		
		
		ExitAction exitAction = ExitCache.getExitAction();
		exitAction.setCallOption(callOptionSymbol);
		exitAction.setCallOptionQty(callOptionQty);
		exitAction.setPutOption(putOptionSymbol);
		exitAction.setPutOptionQty(putOptionQty);
		exitAction.setExitActionEnabled(true);
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
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		String orderType = "BUY";
		params.add("quantity", qty);
		String[] split = symbol.split(":");
		params.add("exchange", split[0]);
		params.add("tradingsymbol", split[1]);
		params.add("transaction_type", orderType);
		
		return placeOrder(params);
	}

	private String placeOrder(MultiValueMap<String, String> params) {
		String tradeURL = "https://api.kite.trade/orders/regular";
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Kite-Version", "3");
		headers.add("Authorization", "token " + API_KEY + ":" + accessToken);
		
	
		params.add("order_type", "MARKET");
		params.add("product", "MIS");
		params.add("validity", "DAY");
		
		HttpEntity<MultiValueMap<String, String>> requestEntity= 
                new HttpEntity<MultiValueMap<String, String>>(params, headers);
		
		

		String response = fetchResponse(tradeURL, requestEntity) ;
		return response;
	}
	
	private String fetchResponse(String url, HttpEntity<MultiValueMap<String, String>> requestEntity ) {
		RestTemplate restTemplate = new RestTemplate();
		String response = restTemplate.postForObject(url,  requestEntity, String.class);
		return response;
	}


}
