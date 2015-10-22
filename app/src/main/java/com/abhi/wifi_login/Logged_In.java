package com.abhi.wifi_login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Logged_In extends Activity {
    /**
     * Called when the activity is first created.
     */
    TextView tV,tV2;
    Button btn;
    String details, id, pwd,result1,pass1;
    InputStream inputStream = null;
    String result = "";
    boolean started = false;
    Handler handler;
    Runnable runnable;
    int log_out=1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logged_in);

        Intent i = getIntent();
        // getting attached intent data
        details = i.getStringExtra("data");
        //Log.i("MyActivity1",details);

        try {
            JSONObject jObject = new JSONObject(details);
            id = jObject.getString("id");
            pwd = jObject.getString("pwd");
        } catch (JSONException e1) {
            e1.printStackTrace();
            // Toast.makeText(getBaseContext(), "No item Found",
            // Toast.LENGTH_LONG).show();
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        btn = (Button) findViewById(R.id.button2);
        tV = (TextView) findViewById(R.id.textView);
        tV2 = (TextView) findViewById(R.id.textView2);
        tV.setText("Logged In as ");
        tV2.setText(id);
        int SPLASH_TIME_OUT = 60000;

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Connection_Detector cd = new Connection_Detector(getApplicationContext());
                if (cd.isConnectingToInternet())
                // true or false
                {
                    new HttpAsyncTask().execute("https://10.0.1.254:4100/wgcgi.cgi");
                } else {
                    showAlertDialog(Logged_In.this,
                            "No Internet Connection",
                            "No internet connection.", false);
                }
            }
        });


        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                new HttpAsyncTaskLogin().execute("https://10.0.1.254:4100/wgcgi.cgi");
                if(started) {
                    start();
                }
            }
        };

        start();
        //Log.i("result1234", "after_start");
    }

    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    public void start() {
        //Log.i("result1234", "in_start");
        started = true;
        Connection_Detector cd1 = new Connection_Detector(getApplicationContext());
        if (cd1.isConnectingToInternet() && log_out==1)
        // true or false
        {
            Log.i("log_out=1", String.valueOf(log_out));
            handler.postDelayed(runnable, 60000);
        }
        else if(!cd1.isConnectingToInternet()){
            showAlertDialog(Logged_In.this,
                    "No Internet Connection",
                    "No internet connection.", false);
        }
        else
        {
            Log.i("log_out=0", String.valueOf(log_out));
        }

    }

    public void showAlertDialog(Context context, String title, String message,
                                Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        // alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String res = "";
        while ((line = bufferedReader.readLine()) != null)
            res += line;
        inputStream.close();
        return res;
    }

    public String request(String url, List request)
            throws ClientProtocolException, IOException, IllegalStateException,
            JSONException {

        DefaultHttpClient client = (DefaultHttpClient) WebClientDevWrapper.getNewHttpClient();

        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(request));
        HttpResponse response = client.execute(post);
        inputStream = response.getEntity().getContent();

        // 10. convert inputstream to string
        if (inputStream != null) {
            result = convertInputStreamToString(inputStream);
            //Log.i("result123",result);
        }
        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            try {
                nameValuePairs.add(new BasicNameValuePair("Logout", "Logout"));
                nameValuePairs.add(new BasicNameValuePair("action", "fw_logon"));
                nameValuePairs.add(new BasicNameValuePair("fw_logon_type", "logout"));
            } catch (Exception e) {
                Log.d("makepass", e.getLocalizedMessage());
            }

            try {
                result1=request(urls[0], nameValuePairs);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result1;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Info Sent!", Toast.LENGTH_LONG).show();
//            Log.i("result1238", result);
            Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_LONG).show();
            log_out=0;
            Intent i = new Intent(Logged_In.this, Main_Activity.class);
            startActivity(i);
            finish();
        }
    }

    public String POST(String url) {
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            //HttpClient httpclient = new DefaultHttpClient();
            DefaultHttpClient client = (DefaultHttpClient) WebClientDevWrapper.getNewHttpClient();

            // 2. make POST request to the given URL
            //HttpPost httpPost = new HttpPost(url);
            HttpPost post = new HttpPost(url);

            String json = "";

            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("fw_username", id));
            nameValuePairs.add(new BasicNameValuePair("fw_password", pwd));
            nameValuePairs.add(new BasicNameValuePair("fw_domain", "gpra.in"));
            nameValuePairs.add(new BasicNameValuePair("submit", "Login"));
            nameValuePairs.add(new BasicNameValuePair("action", "fw_logon"));
            nameValuePairs.add(new BasicNameValuePair("fw_logon_type", "logon"));
            nameValuePairs.add(new BasicNameValuePair("redirect", ""));
            nameValuePairs.add(new BasicNameValuePair("lang", "en-US"));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = client.execute(post);

            // 9. receive response as inputStream
            inputStream = response.getEntity().getContent();

            // 10. convert inputstream to string
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
                //Log.i("result123",result);
            } else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private class HttpAsyncTaskLogin extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            return POST(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            //Log.i("result1234", result);
            String str2 = "successfully authenticated";
            if(result.contains(str2)) {
                try {
                    Toast.makeText(getBaseContext(), "Logged In", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Log.d("makepass", e.getLocalizedMessage());
                }
            }
            else
            {
                Toast.makeText(getBaseContext(), "Not Authenticated, Please Check your Credentials!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
