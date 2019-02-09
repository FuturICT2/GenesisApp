package com.example.mcb.genesisapp.State;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.mcb.genesisapp.Layers.base.FBaseList;
import com.example.mcb.genesisapp.R;
import com.example.mcb.genesisapp.Repository.Fin4.Fin4Repository;
import com.example.mcb.genesisapp.Repository.SQLite.BasicSQLiteRepo;


import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import Repository.IRepository;


/**
 * State Pattern. StateActivity holds state of the application.
 *
 */
public class StateActivity extends AppCompatActivity implements StateCallback{





    private static final String TAG = StateActivity.class.getSimpleName();

    static final int REQUEST_IMAGE_CAPTURE = 1;


    /**
     * Distinguishes different kinds of app starts: <li>
     * <ul>
     * First start ever ({@link #FIRST_TIME})
     * </ul>
     * <uonbl>
     * First start in this version ({@link #FIRST_TIME_VERSION})
     * </ul>
     * <ul>
     * Normal app start ({@link #NORMAL})
     * </ul>
     *
     * @author williscool
     *         inspired by
     * @author schnatterer
     */
    public enum AppStart {
        FIRST_TIME, FIRST_TIME_VERSION, NORMAL;
    }

    /**
     * The app version code (not the version name!) that was used on the last
     * start of the app.
     */
    private static final String LAST_APP_VERSION = "1";

    /**
     * Caches the result of {@link #checkAppStart(Context context, SharedPreferences sharedPreferences)}. To allow idempotent method
     * calls.
     */
    private static AppStart appStart = null;


    private SharedPreferences sharedPreferences;


    // Layer states
    public final static String LAYER = "keyLayer";


    private List<StateListener> myListeners;

    //Which state is the app in
    //should take values in Layers
    private int CURRENT_STATE = BASE_LIST;




    private IRepository repo;


    //******** DrawerLayout *********//
    // private DrawerLayout mDrawerLayout;
    // private ListView mDrawerList;
    // private ActionBarDrawerToggle mDrawerToggle;

    // Back navigation
    private FragmentManager fragmentManager;
    Stack<Integer> pageHistory;


    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;




    //private JJCircleToSimpleLineController searchAdapter;

    // private CircleSearchViewNew searchView;

    private ImageView searchButton;
    private ViewSwitcher searchSwitcher;
    private AutoCompleteTextView searchAutoComplete;

    public static final int ALL_CONCRETE_OBJECTS_LOADER = -1;










    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CurrentState", CURRENT_STATE);
        //  outState.putString("someVarB", someVarB);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CURRENT_STATE = savedInstanceState.getInt("CurrentState");
        //  someVarB = savedInstanceState.getString("someVarB");
    }


    public void registerStateListener(StateListener newListener) {

        if (newListener != null && myListeners != null) {
            myListeners.add(newListener);
        }
    }

    public void unRegisterStateListener(StateListener oldListener) {
        if (oldListener != null)
            myListeners.remove(oldListener);
    }


    /**
     * Back Press Logic
     */

    private void closeViewSwitcher() {
        this.searchSwitcher.setDisplayedChild(0);
        this.searchAutoComplete.setText("");
        closeKeyBoard();
    }

    @Override
    public void onBackPressed() {
        boolean superPress = true;


        /*
        inform listener, if one handles back, do nothing
         */
        for (StateListener listener : myListeners) {
            superPress = !listener.onBackPressed() && superPress;
        }

        /*
        else go to previous layer
         */

        if (superPress)
            goToPreviousLayer();
    }

    @Override
    public void goToPreviousLayer() {
        if (pageHistory.empty() ) {
            if((CURRENT_STATE==BASE_LIST))
                super.onBackPressed();
            else{
                displayView(StateCallback.BASE_LIST,false);
            }
        } else {
            displayView(pageHistory.pop(), false);
        }
    }

    /**
     * Display Layer Logic
     *
     * @param view
     */

    void initView(int view,Bundle... extra) {
        CURRENT_STATE = view;
        pageHistory.push(view);
        displayView(view, false,extra);
    }

    public void displayView(int position, boolean addToBackstack,Bundle... extra) {


        if (addToBackstack)
            pageHistory.push(CURRENT_STATE);
        CURRENT_STATE = position;

        Fragment fragment = FBaseList.newInstance();

        fragmentManager.beginTransaction().replace(R.id.state_frame_container,
                fragment, "tag_fragment_state_container")
                .commit();






    }


    /**
     * Entry into App
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_state);


        this.repo = getRepository();

        pageHistory = new Stack<>();
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        myListeners = new ArrayList<>();


        switch (checkAppStart(this, sharedPreferences)) {
            case NORMAL:
                // Normal startup

                break;
            case FIRST_TIME_VERSION:
                // TODO show what's new
                break;
            case FIRST_TIME:
                // first time started

                break;
            default:
                break;
        }


        if (findViewById(R.id.state_frame_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                CURRENT_STATE = savedInstanceState.getInt("CurrentState", BASE_LIST);
            }



            TextView genesisView = (TextView) findViewById(R.id.state_activity_netti_logo_txtv);
            genesisView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Fin4Repository)getRepository()).getTokens();
                }
            });

            ImageButton button = (ImageButton) findViewById(R.id.state_activity_search_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Fin4Repository)getRepository()).setCSRF();
                    //penWebLinkInBrowser("http://10.0.2.3:8888");
                }
            });





            fragmentManager = getSupportFragmentManager();


        }

        //final function, also set first view
        handleIntent(getIntent());


    }







    protected void handleIntent(Intent intent){
        Bundle bundle = new Bundle();
        if (intent != null) {
            String action = intent.getAction();
            if(action!=null) {
                switch (action) {
                    case NfcAdapter.ACTION_NDEF_DISCOVERED:
                        break;
                    case Intent.ACTION_SEND:
                       //standard intent sent
                        break;
                }
            }
        }
        /**
         * init Current view
         */
        initView(CURRENT_STATE,bundle);
    }




    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
        //     Toast.makeText(this, "onNewIntent called", Toast.LENGTH_LONG);


    }



    private void setUpSwitcher(ViewSwitcher viewSwitcher) {


    }



    @Override
    public void updateViews() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id
                .state_frame_container);

        FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
        fragTransaction.detach(currentFragment);
        fragTransaction.attach(currentFragment);
        fragTransaction.commit();
    }

    @Override
    public void changeState(int layer) {
        if (layer != CURRENT_STATE) {
            displayView(layer, true);
        }
    }

    @Override
    public IRepository getRepository() {
        if (this.repo == null) {
          //this.repo = new BasicSQLiteRepo(this); // needs to be exchanged if other Database (like Blockchain is added)
            this.repo = new Fin4Repository(this);
        }
        return this.repo;
    }


    @Override
    public void searchItemClicked() {

        if (CURRENT_STATE == BASE_LIST) {
            displayView(StateCallback.DETAIL_LAYER, false);
        }

    }


    /**
     * Finds out started for the first time (ever or in the current version).
     *
     * @return the type of app start
     */
    public AppStart checkAppStart(Context context, SharedPreferences sharedPreferences) {
        PackageInfo pInfo;

        try {
            pInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            int lastVersionCode = sharedPreferences.getInt(
                    LAST_APP_VERSION, -1);
            // String versionName = pInfo.versionName;
            int currentVersionCode = pInfo.versionCode;
            appStart = checkAppStart(currentVersionCode, lastVersionCode);

            // Update version in preferences
            sharedPreferences.edit()
                    .putInt(LAST_APP_VERSION, currentVersionCode).commit(); // must use commit here or app may not update prefs in time and app will loop into walkthrough
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG,
                    "Unable to determine current app version from package manager. Defensively assuming normal app start.");
        }
        return appStart;
    }

    public AppStart checkAppStart(int currentVersionCode, int lastVersionCode) {
        if (lastVersionCode == -1) {
            return AppStart.FIRST_TIME;
        } else if (lastVersionCode < currentVersionCode) {
            return AppStart.FIRST_TIME_VERSION;
        } else if (lastVersionCode > currentVersionCode) {
            Log.w(TAG, "Current version code (" + currentVersionCode
                    + ") is less then the one recognized on last startup ("
                    + lastVersionCode
                    + "). Defensively assuming normal app start.");
            return AppStart.NORMAL;
        } else {
            return AppStart.NORMAL;
        }
    }



    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            Toast.makeText(this, "No Camera", Toast.LENGTH_LONG).show();
            // no camera on this device
            return false;
        }
    }



    public Context getContext() {
        return this;
    }




    @Override
    public void dataBaseModified() {
   //     getSupportLoaderManager().restartLoader(ALL_CONCRETE_OBJECTS_LOADER, null, this);
        for (StateListener listener : myListeners) {
            listener.dataBaseModified(null, null);
        }
    }

    @Override
    public void closeKeyBoard() {
        View view = getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context
                .INPUT_METHOD_SERVICE);

        /*
        did not work in DialogFragment
         */
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if (getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void openKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.toggleSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(),
                    InputMethodManager
                            .SHOW_FORCED, 0);
        }
    }

    @Override
    public void openKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        imm.toggleSoftInputFromWindow(view.getApplicationWindowToken(),
                InputMethodManager
                        .SHOW_FORCED, 0);
    }



    @Override
    public void openWebLinkInBrowser(String url) {
        if (url.startsWith("www.")) {
            url = url.substring(4);
        }
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void call(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        startActivity(intent);
    }

    @Override
    public void text(String number) {

    }

    @Override
    public void email(String email) {

    }


}
