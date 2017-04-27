import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

@SuppressWarnings("deprecation")
public class ChopClient {
    private final String authorization = "1234567890abcdef1234567890abcdeq";
    public static void main(String[] args) throws Exception {
        ChopClient http = new ChopClient();
        http.sendPost();
    }

    // HTTP POST request
    private void sendPost() throws Exception {
        String url = "http://dev.citconpay.com/chop/chop";
        @SuppressWarnings("resource")
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        // add header
        post.setHeader("Authorization", authorization);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("payment_method", "alipay"));
        urlParameters.add(new BasicNameValuePair("amount", "2"));
        urlParameters.add(new BasicNameValuePair("allow_duplicates", "yes"));
        urlParameters.add(new BasicNameValuePair("currency", "USD"));
        urlParameters.add(new BasicNameValuePair("reference", "jkh25jh1348fd89sg"));
        urlParameters.add(new BasicNameValuePair("ipn_url", "http://merchant.com/ipn"));
        urlParameters.add(new BasicNameValuePair("callback_url_success", "http://merchant.com/success"));
        urlParameters.add(new BasicNameValuePair("callback_url_fail", "http://merchant.com/fail"));
        urlParameters.add(new BasicNameValuePair("mobile_result_url", "http://merchant.com/mobile_confirm?reference=jkh25jh1348fd89sg"));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = null;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        // redirect to url string in result to continue processing
        System.out.println(result.toString());
    }
}

