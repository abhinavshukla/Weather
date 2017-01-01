package konnect.in.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import controllers.WebserviceController;
import interfaces.WebControllerInterface;
import model.CityModel;
import model.Weather;
import utils.ConnectionDetector;
import utils.JSONWeatherParser;
import utils.LinksAndKeys;

public class MainActivity extends AppCompatActivity implements WebControllerInterface, OnMapReadyCallback {

    private int READ_STORAGE_PERMISSION_CODE = 23;
    private GoogleMap mMap;
    GPSTracker gps;
    TextView tx,textViewmsg;
    AutoCompleteTextView editTextCity;
    SupportMapFragment mapFragment;
    double lat,lon;
    RelativeLayout relativeLayout1,relativeLayout2;
    LinearLayout linearLayout;
    ImageView searchButton,logoImage;
    ConnectionDetector connectionDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();


        editTextCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(editTextCity.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    if(connectionDetector.isConnectedToInternet())
                    searchResult(editTextCity.getText().toString(),"q");
                    else
                        Toast.makeText(getApplicationContext(),"Oops you don't have internet connection",Toast.LENGTH_LONG).show();

                    return true;
                }
                return false;
            }
        });

        editTextCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //studentAdapter.
            }

            @Override
            public void afterTextChanged(Editable s) {
               if(editTextCity.getText().toString().length()>=4)
               {
                   if(connectionDetector.isConnectedToInternet())
                  getCity(editTextCity.getText().toString());
               }

            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.getView().setVisibility(View.GONE);
                linearLayout.setVisibility(View.GONE);
                logoImage.setVisibility(View.GONE);
                relativeLayout1.setVisibility(View.VISIBLE);
                textViewmsg.setVisibility(View.VISIBLE);
                textViewmsg.setText("Please enter City name for getting the weather report.");
            }
        });


    }

    private void init() {

        tx=(TextView)findViewById(R.id.tx);
        logoImage=(ImageView)findViewById(R.id.logo);
        connectionDetector =new ConnectionDetector(this);
        relativeLayout1=(RelativeLayout)findViewById(R.id.layout);
        linearLayout=(LinearLayout)findViewById(R.id.layout1);
        editTextCity=(AutoCompleteTextView)findViewById(R.id.cityEdit);
        textViewmsg=(TextView)findViewById(R.id.msg);
        searchButton=(ImageView)findViewById(R.id.search_button2);
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);


        if ( Build.VERSION.SDK_INT >=23)
            if(!isReadLocationAllowed())
                requestLocationPermission();
        if(isReadLocationAllowed())
        {
            if(connectionDetector.isConnectedToInternet())
            getLocation();
            else
                Toast.makeText(this,"Oops you don't have internet connection",Toast.LENGTH_LONG).show();
        }
        //mapFragment.getMapAsync(this);
    }

    private void getCity(String text)
    {
        HashMap<String, String> paramsList = new HashMap<String, String>();
        paramsList.put("id", text);



        WebserviceController ws = new WebserviceController(
                MainActivity.this, MainActivity.this);
        String hitURL = LinksAndKeys.URL;
        ws.sendSilentRequest(hitURL, paramsList, 2);
    }

    public  void getLocation()
{
    gps = new GPSTracker(MainActivity.this);

    // check if GPS enabled
    if(gps.canGetLocation()){
        relativeLayout1.setVisibility(View.GONE);
        textViewmsg.setVisibility(View.GONE);
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();

        if(latitude!=0 || longitude!=0)
        {
            HashMap<String, String> paramsList = new HashMap<String, String>();
            paramsList.put(LinksAndKeys.API, LinksAndKeys.API_ID);
            paramsList.put(LinksAndKeys.LAT, String.valueOf(latitude));
            paramsList.put(LinksAndKeys.LON, String.valueOf(longitude));
            paramsList.put(LinksAndKeys.UNIT, LinksAndKeys.TEMP_UNIT);


            WebserviceController ws = new WebserviceController(
                    MainActivity.this, MainActivity.this);
            String hitURL = LinksAndKeys.BASE_URL;
            ws.sendGETRequest(hitURL, paramsList, 1);
        }

    }else{
        mapFragment.getView().setVisibility(View.GONE);
        linearLayout.setVisibility(View.GONE);
        logoImage.setVisibility(View.GONE);
        relativeLayout1.setVisibility(View.VISIBLE);
        textViewmsg.setVisibility(View.VISIBLE);
        textViewmsg.setText("Your current location cannot be found. Please enter City name for getting the weather report.");
        //Toast.makeText(getApplicationContext(), "Your current location cannot be found.", Toast.LENGTH_LONG).show();

    }
}

    public void searchResult(String search,String parameter)
    {
        HashMap<String, String> paramsList = new HashMap<String, String>();
        paramsList.put(LinksAndKeys.API, LinksAndKeys.API_ID);
        paramsList.put(parameter,search);
        paramsList.put(LinksAndKeys.UNIT, LinksAndKeys.TEMP_UNIT);


        WebserviceController ws = new WebserviceController(
                MainActivity.this, MainActivity.this);
        String hitURL = LinksAndKeys.BASE_URL;
        ws.sendGETRequest(hitURL, paramsList, 1);
    }

    private boolean isReadLocationAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    private void requestLocationPermission(){

       /* if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }*/

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},READ_STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == READ_STORAGE_PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast
                if(connectionDetector.isConnectedToInternet())
                getLocation();
                else
                    Toast.makeText(this,"Oops you don't have internet connection",Toast.LENGTH_LONG).show();
            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void getResponse(int responseCode, String responseString, String requestType, int requestCode) {
        if(requestCode==2)
        {
            final ArrayList<CityModel> cityModelsList=new ArrayList<>();
            final ArrayList<String> arrayListCity = new ArrayList<>();
            try {
                JSONArray jsonArray=new JSONArray(responseString);
                for(int i=0;i<jsonArray.length();i++)
                {
                    JSONObject responseObject = jsonArray.getJSONObject(i);
                    CityModel cityModel=new CityModel();
                    cityModel.setCity(responseObject.getString("name"));
                    cityModel.setId(responseObject.getString("id"));
                     arrayListCity.add(responseObject.getString("name"));
                    cityModelsList.add(cityModel);
                }
                Collections.sort(arrayListCity);
                ArrayAdapter<String> adapterLocations = new ArrayAdapter<String>
                        (this, android.R.layout.select_dialog_item, arrayListCity);
                editTextCity.setThreshold(1);
                editTextCity.setAdapter(adapterLocations);
                editTextCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                        InputMethodManager inputManager = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);

                        inputManager.hideSoftInputFromWindow(editTextCity.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                        String city=arrayListCity.get(pos).toString();
                        String cityID="";
                        for (int i = 0; i < cityModelsList.size(); i += 1) {
                            if (cityModelsList.get(i).getCity().indexOf(city) != -1) {
                                cityID = cityModelsList.get(i).getId();
                            }
                        }
                        if(!cityID.equals(""))
                        {
                            if(connectionDetector.isConnectedToInternet())
                            searchResult(cityID,"id");
                            else
                                Toast.makeText(getApplicationContext(),"Oops you don't have internet connection",Toast.LENGTH_LONG).show();
                        }

                        Log.e("CityID",cityID);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode==1)
        {
            Log.e("esponseCode","esponseCode"+responseCode);
            if(responseCode==200)
            {
                Log.e("esponseCode","esponseCode"+responseCode);
                mapFragment.getView().setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                relativeLayout1.setVisibility(View.GONE);
                logoImage.setVisibility(View.VISIBLE);
                textViewmsg.setVisibility(View.GONE);
                Weather weather = new Weather();
                Log.e("cod","ok");
                try {
                    weather = JSONWeatherParser.getWeather(responseString);
                    Log.e("cod",weather.location.getCod());
                    if(weather.location.getCod().equals("200"))
                    {
                        String weatherData="<div><span style='color: #0000ff;'>"+weather.location.getCity()+"</span> <span style='color: #333333;'>right now "+weather.temperature.getTemp()+"℃ "+weather.currentCondition.getDescr()+"</span></div>";
                        lat=weather.location.getLatitude();
                        lon=weather.location.getLongitude();
                        mapFragment.getMapAsync(this);
                        Log.e("data",weatherData);
                        tx.setText(Html.fromHtml(weatherData));
                        Picasso.with(this).load(LinksAndKeys.IMG_URL+weather.currentCondition.getIcon()+".png").into(logoImage);
                    }
                    else
                    {
                        mapFragment.getView().setVisibility(View.GONE);
                        linearLayout.setVisibility(View.GONE);
                        relativeLayout1.setVisibility(View.VISIBLE);
                        logoImage.setVisibility(View.GONE);
                        textViewmsg.setVisibility(View.VISIBLE);
                        textViewmsg.setText("Please enter City name for getting the weather report.");
                    }


                } catch (JSONException e) {
                    Log.e("cod",""+e.getMessage());
                    e.printStackTrace();
                }
            }
            else
            {
                mapFragment.getView().setVisibility(View.GONE);
                linearLayout.setVisibility(View.GONE);
                relativeLayout1.setVisibility(View.VISIBLE);
                logoImage.setVisibility(View.GONE);
                textViewmsg.setVisibility(View.VISIBLE);
                textViewmsg.setText("Please enter City name for getting the weather report.");
            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);

        // Add a marker in Sydney and move the camera
        LatLng latLng = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Map"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,8));
    }


}
