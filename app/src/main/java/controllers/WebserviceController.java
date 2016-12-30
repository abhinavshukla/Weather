package controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

import dmax.dialog.SpotsDialog;
import interfaces.WebControllerInterface;

/**
 * Created by ABHINAV SHUKLA on 29/12/2016.
 */

public class WebserviceController {

    Context context;
    AlertDialog pDialog;
    WebControllerInterface myInterface;

    public WebserviceController(Context context, Object obj) {
        this.context = context;
        this.myInterface = (WebControllerInterface) obj;
    }


    public void sendGETRequest(String url, HashMap<String, String> paramsList, final int requestCode) {

        pDialog = new SpotsDialog(context);
        pDialog.setMessage("Please Wait ...");
        pDialog.setCancelable(false);
        pDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        Iterator it = paramsList.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            params.put(pair.getKey().toString(), pair.getValue());
        }

        client.setTimeout(60000);
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                // TODO Auto-generated method stub
                Toast.makeText(context,"Something went wrong! Please try later..",Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }

            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                // TODO Auto-generated meth
                pDialog.dismiss();
                try {
                    String response = new String(arg2, "UTF-8");
                    Log.e("response",response+"abhinav");
                    myInterface.getResponse(arg0, response, "GET", requestCode);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });
    }

    public void sendPOSTRequest(String url, HashMap<String, String> paramsList, final int requestCode) {
        pDialog = new SpotsDialog(context);
        pDialog.setMessage("...");
        pDialog.setCancelable(false);

        pDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();

        if (paramsList != null) {
            Iterator it = paramsList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                try {
                    params.put("" + pair.getKey(), "" + pair.getValue());
                    // System.out.println("Params ---- " + pair.getKey() + " - " + pair.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        client.setTimeout(60000);
        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                // TODO Auto-generated method stub
                pDialog.dismiss();
                myInterface.getResponse(arg0, "Sorry We are Facing some Problem. Please try later", "POST", requestCode);
            }

            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                // TODO Auto-generated meth
                pDialog.dismiss();
                try {
                    String response = new String(arg2, "UTF-8");
                    myInterface.getResponse(arg0, response, "POST", requestCode);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }

        });
    }

    public void sendSilentRequest(String url, HashMap<String, String> paramsList, final int requestCode) {

        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();

        if (paramsList != null) {
            Iterator it = paramsList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                try {
                    params.put("" + pair.getKey(), "" + pair.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        client.setTimeout(60000);
        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                // TODO Auto-generated method stub
                myInterface.getResponse(arg0, "Sorry We are Facing some Problem. Please try later", "POST", requestCode);
            }

            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                // TODO Auto-generated meth
                try {
                    String response = new String(arg2, "UTF-8");
                    myInterface.getResponse(arg0, response, "POST", requestCode);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        });
    }
}