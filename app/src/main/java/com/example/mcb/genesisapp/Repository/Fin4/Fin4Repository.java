package com.example.mcb.genesisapp.Repository.Fin4;


import android.widget.Toast;


import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mcb.genesisapp.Repository.SQLite.BasicSQLiteRepo;
import com.example.mcb.genesisapp.State.StateCallback;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;



import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import Features.properties.IProperty;
import Features.properties.basic.DecimalsProperty;
import Features.properties.basic.GeneralSupplyProperty;
import Features.properties.basic.GenesisSupplyProperty;
import Features.properties.basic.NameProperty;
import Features.properties.basic.SymbolProperty;
import Token.basic.BasicToken;


public class Fin4Repository extends BasicSQLiteRepo implements IFin4Repo {



    //protected String   serverUrl ="http://10.0.2.2:3000"; // talks with a local instance of fin4, needs the android emulator (not mobile phone) please refere to: https://github.com/FuturICT2/fin4-core
    protected String serverUrl = "http://www.finfour.net"; //talks with the live instance of fin4 server (wwww.finfour.net), needs to run on an android phone connected via USB

    RequestQueue queue;

    /*
        Just for demonstration purposes Todo: generalize User
     */
    protected String name="dummyUser";
    protected String email="dummyUser@ethz.ch";
    protected String pwd="resUymmud";



    protected boolean loginFinished = false;




    boolean tokenUpToDate=false;
    List<BasicToken> allTokens = new ArrayList<>();

    protected StateCallback stateActivity;

    public Fin4Repository(StateCallback stateCallback) {
        super(stateCallback);
        this.stateActivity = stateCallback;
        postRegister(); // just for demonstration purposes, try to register every time, after his is finished, postLogin is called

    }


    protected RequestQueue getQueue(){
        if(this.queue==null){
            DefaultHttpClient httpclient = new DefaultHttpClient();

            CookieStore cookieStore = new BasicCookieStore();
            httpclient.setCookieStore( cookieStore );

            HttpStack httpStack = new HttpClientStack( httpclient );
            this.queue = Volley.newRequestQueue( getContext(), httpStack  );
        }
        return this.queue;
    }


    /*
        IFin4Repo methods
     */

    @Override
    public void postRegister() {


        HashMap<Object, Object> params = new HashMap<Object, Object>();
        params.put("name",name);
        params.put("email",email);
        params.put("password",pwd);
        params.put("agreeToTerms",true);
        params.put("isFastSignup",false);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, serverUrl+"/wapi/register", new JSONObject(params), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(),"Register success: " + response.toString(), Toast.LENGTH_SHORT).show();
                        postLogin(); // just for demonstration purposes, it works!
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getContext(),"Register failure: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        postLogin();
                    }
                });






        getQueue().add(jsonObjectRequest);



    }


    public void setCSRF(){

        JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest
                (Request.Method.GET, serverUrl+"/wapi/csrf", null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // mTextView.setText("Response: " + response.toString());
                        Toast.makeText(getContext(),"CSRF success: " + response.toString(), Toast.LENGTH_SHORT).show();
                        HashMap<String, String> params = new HashMap<String, String>();
                        try{
                            params.put("X-Csrf-Token",response.getString("token"));
                        }catch (Exception ex){

                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getContext(),"CSRF failure: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


        getQueue().add(jsonObjectRequest2);


    }

    @Override
    public void postLogin() {
        HashMap<Object, Object> params = new HashMap<Object, Object>();
        params.put("name",name);
        params.put("email",email);
        params.put("password",pwd);



        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, serverUrl+"/wapi/login", new JSONObject(params), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(),"Login success: " + response.toString(), Toast.LENGTH_SHORT).show();
                        loginFinished =true;


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getContext(),"Login failure: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }){
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                //return super.parseNetworkResponse(response);
               // Log.i("response",response.headers.toString());
                Map<String, String> responseHeaders = response.headers;
                String rawCookies = responseHeaders.get("Set-Cookie");
                //Log.i("cookies",rawCookies);
                return super.parseNetworkResponse(response);
            }
        };

        getQueue().add(jsonObjectRequest);
    }

    @Override
    public void postLogout() {

    }

    @Override
    public void getSession() {
        HashMap<Object, Object> params = new HashMap<Object, Object>();
//        params.put("name",name);
//        params.put("email",email);
//        params.put("password",pwd);



        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, serverUrl+"/wapi/session", null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(),"Session success: " + response.toString(), Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getContext(),"Session failure: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });



        getQueue().add(jsonObjectRequest);
    }

    @Override
    public void deleteSession() {

    }

    @Override
    public void postForgotPassRequestNew() {

    }

    @Override
    public void postForgotPassRequestReset() {

    }

    @Override
    public void postUserPassword() {

    }

    @Override
    public void postUserEmail() {

    }

    @Override
    public void getUserEmailConfirm() {

    }

    @Override
    public void getBalances() {

    }

    @Override
    public void getPersonID() {

    }



    /*
        TOKEN FUNCTION
     */

    @Override
    public void getTokens() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, serverUrl+"/wapi/tokens", null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(),"get Token success: " + response.toString(), Toast.LENGTH_LONG).show();
                        JSONArray array = new JSONArray();
                        try {
                            array = response.getJSONArray("Entries");
                        }catch(Exception ex){

                        }
                        int size = array.length();
                        for(int i=0; i< size;i++){

                            String id = "";
                            String name = "";
                            String tokenName = "";
                            String Symbol = "";
                            try {
                                JSONObject object = array.getJSONObject(i);
                                id = object.getString("ID");
                                name = object.getString("CreatorName");
                                tokenName = object.getString("Name");
                                Symbol = object.getString("Symbol");
                            }catch (Exception ex){

                            }

                            List<IProperty> propertyList = new ArrayList();

                            NameProperty propertyName = new NameProperty(tokenName);
                            SymbolProperty symbolProperty = new SymbolProperty(Symbol);
                            DecimalsProperty decimalsProperty = new DecimalsProperty(3);
                            GeneralSupplyProperty supplyProperty = new GeneralSupplyProperty(1000);
                            GenesisSupplyProperty genesisSupplyProperty = new GenesisSupplyProperty(10);

                            propertyList.add(propertyName);
                            propertyList.add(symbolProperty);
                            //dummy data -> else null pointer exception: TODO: fix nullpointer exception
                            propertyList.add(decimalsProperty);
                            propertyList.add(supplyProperty);
                            propertyList.add(genesisSupplyProperty);

                            allTokens.add(new BasicToken(new ArrayList<>(),propertyList, new ArrayList<>(),Fin4Repository.this));

                        }
                        // add all tokens to list
                        tokenUpToDate = true;
                        // notify StateActiviy that tokenList chagned, hence triggers an update of views
                        stateActivity.dataBaseModified();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getContext(),"getToken failure: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



        getQueue().add(jsonObjectRequest);
    }

    @Override
    public void getTokensTokendID() {

    }

    @Override
    public void createToken() {

    }

    @Override
    public void toggle_token_like() {

    }

    @Override
    public void createClaim() {

    }

    @Override
    public void approveClaim() {

    }


    @Override
    public Collection<BasicToken> getAllTokens() {
        if(!tokenUpToDate && loginFinished){
            this.getTokens();
        }
        return this.allTokens;



    }
}
