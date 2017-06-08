package citcondemo;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

@Controller
public class Citcon {

	private static Logger logger = Logger.getLogger(Citcon.class);

	@RequestMapping("/receipt_success")
	public String receipt(
			@RequestParam(value = "payment_method", required = false, defaultValue = "") String payment_method,
			Model model) {
		String pay_method = "";
		if (payment_method.equals("alipay")) {
			pay_method = "with Alipay";
		} else if (payment_method.equals("wechatpay")) {
			pay_method = "with WeChat";
		} else if (payment_method.equals("cc")) {
			pay_method = "with Credit Card";
		}
		model.addAttribute("payment_method", pay_method);
		return "receipt_success";
	}

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public void index(HttpServletResponse httpServletResponse) {
		try {
			httpServletResponse.sendRedirect("/checkout.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping("/pay")
	public void pay(@RequestParam String payment_method, @RequestParam String transaction_id,
			HttpServletResponse httpServletResponse) {

		String url = "http://dev.citconpay.com/chop/chop";

		try {
			String baseurl = "http://54.159.174.139:8080/";
			String token = "1234567890abcdef1234567890abcdeq";
			int amount = 1;
			String currency = "USD";
			String callback_url_success = baseurl + "receipt_success";
			String ipn_url = baseurl + "ipn";
			String mobile_result_url = baseurl + "receipt_success?reference=" + transaction_id;
			String callback_url_fail = "";
			String allow_duplicates = "yes";

			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(url);

			post.setHeader("Authorization: Bearer ", token);

			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("payment_method", payment_method));
			urlParameters.add(new BasicNameValuePair("currency", currency));
			urlParameters.add(new BasicNameValuePair("amount", String.valueOf(amount)));
			urlParameters.add(new BasicNameValuePair("reference", transaction_id));
			urlParameters.add(new BasicNameValuePair("ipn_url", ipn_url));
			urlParameters.add(new BasicNameValuePair("mobile_result_url", mobile_result_url));
			urlParameters.add(new BasicNameValuePair("callback_url_success", callback_url_success));
			urlParameters.add(new BasicNameValuePair("callback_url_fail", callback_url_fail));
			urlParameters.add(new BasicNameValuePair("allow_duplicates", allow_duplicates));

			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpResponse execution = client.execute(post);
			BufferedReader reader = new BufferedReader(new InputStreamReader(execution.getEntity().getContent()));
			
			StringBuilder result = new StringBuilder();
			String line = "";

			while ((line = reader.readLine()) != null) {
				result.append(line);
			}

			String response = result.toString();

			if (response.indexOf("success") >= 0) {
				int urlIndex = response.indexOf("http");
				String redirect = response.substring(urlIndex, response.length() - 2);

				redirect = redirect.replace("/", "");
				httpServletResponse.sendRedirect(redirect);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update_order(String reference, String notify_status, String time, String id, String notify_id) {
		logger.error("in ipn, update_order with " + reference + ", " + notify_status + ", " + time + ", " + id + ", "
				+ notify_id);
	}

	public void start_fulfillment(String reference) {
		logger.error("in ipn, start_fulfillment with " + reference);
	}

	public String sign_ipn(Map<String, String> reply, String token) {

		StringBuilder flat_reply = new StringBuilder();
		SortedSet<String> sortedKeys = new TreeSet<String>(reply.keySet());

		for (String key : sortedKeys) {
			String val = reply.get(key);
			if (val == null) {
				val = "";
			}
			flat_reply.append(key + "=" + val + "&");
		}

		flat_reply.append("token=" + token);
		String flat_reply_MD5 = flat_reply.toString();

		logger.debug("string=" + flat_reply_MD5);
		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		md.update(flat_reply_MD5.getBytes());
		byte[] digest = md.digest();
		return DatatypeConverter.printHexBinary(digest).toLowerCase();
	}

	@RequestMapping("/ipn")
	public void ipn(@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "notify_status", required = false) String notify_status,
			@RequestParam(value = "currency", required = false) String currency,
			@RequestParam(value = "time", required = false) String time,
			@RequestParam(value = "reference", required = false) String reference,
			@RequestParam(value = "notify_id", required = false) String notify_id,
			@RequestParam(value = "partner_id", required = false) String partner_id,
			@RequestParam(value = "sign", required = false) String sign,
			@RequestParam(value = "fields", required = false) String fields,
			@RequestParam(value = "extra", required = false) Map<String, String> extra,
			HttpServletResponse servResponse) {

		String token = "1234567890abcdef1234567890abcdeq";

		logger.error(">>>>> in chopdemo ipn receiver >>>>>");

		Map<String, String> listOfParams = new HashMap<String, String>();
		listOfParams.put("id", id);
		listOfParams.put("amount", amount);
		listOfParams.put("notify_status", notify_status);
		listOfParams.put("currency", currency);
		listOfParams.put("time", time);
		listOfParams.put("reference", reference);
		listOfParams.put("notify_id", notify_id);
		listOfParams.put("partner_id", partner_id);
		listOfParams.put("sign", sign);

		Set<String> sortedKeys = new HashSet<String>(listOfParams.keySet());

		for (String keys : sortedKeys) {
			logger.error(keys + "=" + listOfParams.get(keys));
		}

		logger.error(">>>>> end of chopdemo ipn");

		Map<String, String> data = new HashMap<String, String>();

		data.put("fields", fields);

		if (fields != null && !fields.isEmpty()) {
			StringTokenizer tok = new StringTokenizer(fields, ",");
			while (tok.hasMoreTokens()) {
				String nextTok = tok.nextToken().toString();
				data.put(nextTok, listOfParams.get(nextTok));
			}
		}

		String mysign = sign_ipn(data, token);

		StringBuilder flat_message = new StringBuilder();
		SortedSet<String> dataSortedKeys = new TreeSet<String>(data.keySet());

		for (String key : dataSortedKeys) {
			flat_message.append(key + "=" + data.get(key) + "&");
		}

		logger.error(">>>> flat_message in ipn receiver >>>" + flat_message.toString());
		logger.error(">>>> sign=" + sign);
		logger.error("mysign=" + mysign);

		if (sign != null && sign.equals(mysign)) {
			update_order(reference, notify_status, time, id, notify_id);
			start_fulfillment(reference);
		}

		try {
			servResponse.getWriter().println("ok");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
