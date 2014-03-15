package main.jf.catastropherandroid;

import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * MainActivity for Catastropher.
 *
 * @author Johan Stenberg
 */
public class MainActivity extends FragmentActivity {

    private static final LatLng SWEDEN_ROYAL_CASTLE = new LatLng(59.326403, 18.070965);

    private MainLoginFragment mainLoginFragment;

    private boolean hasLoggedIn;

    private ActionBarDrawerToggle drawerToggle;

    private String fbAccessToken;

    private boolean isRefreshingMap;

    private MenuItem refreshMenuItem;

    private boolean receiverRegistrered;

    private final BroadcastReceiver handleGCMMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(GCMUtils.EXTRA_MESSAGE);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(MainActivity.this.getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.main_activity);
        setDrawerLayoutVisible(false);

        Report report = getIntent().getParcelableExtra(getString(R.string.push_report_key));

        if (savedInstanceState == null) {
            mainLoginFragment = new MainLoginFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, mainLoginFragment)
                    .commit();
        } else {
            mainLoginFragment = (MainLoginFragment) getSupportFragmentManager()
                    .findFragmentById(android.R.id.content);
        }

        setupLayout();
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    private void setupLayout() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        String[] menuStrings = getResources().getStringArray(R.array.main_activity_menu_list);

        drawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, menuStrings));

        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_closed) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        setupMap();
    }

    private void setupGCM() {
        registerReceiver(handleGCMMessageReceiver, new IntentFilter(
                GCMUtils.SHOW_GCM_DIALOG_ACTION));
        receiverRegistrered = true;
        boolean valid = validateRegIdVersionCorrectness();

        if (!valid) {
            GCMRegistrar.unregister(this);
        }

        String regId = GCMRegistrar.getRegistrationId(this);

        if (regId.isEmpty()) {
            String googleProjectId = getString(R.string.google_project_id);
            GCMRegistrar.register(this, googleProjectId);
        } else {
            if (!GCMRegistrar.isRegisteredOnServer(this)) {
                AndroidHttpClient httpClient = HttpHandler.getAndroidHttpClient(this);
                HttpHandler httpHandler = new HttpHandler(httpClient);
                httpHandler.registerGCMIdAsync(this, fbAccessToken, regId);
            }
        }
    }

    private boolean validateRegIdVersionCorrectness() {
        String appVersion = GCMUtils.getRegistrationIdAppVersion(this);
        if (appVersion == null) {
            return true;
        } else {
            String packageName = getPackageName();
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(
                        packageName, 0);
                String appVersionCode = Integer.toString(packageInfo.versionCode);

                return appVersionCode.equals(appVersion);
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
    }

    private void setupMap() {
        final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.main_map))
                .getMap();


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(SWEDEN_ROYAL_CASTLE)
                .zoom(9)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        map.setMyLocationEnabled(true);

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location location) {
                CatastroperApplication catastroperApplication = (CatastroperApplication) getApplication();
                catastroperApplication.setCurrentLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(final LatLng latLng) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent(MainActivity.this, NewReportActivity.class);
                                intent.putExtra(MainActivity.this.getString(R.string.user_fb_id_key), fbAccessToken);
                                intent.putExtra(MainActivity.this.getString(R.string.longitude_key), latLng.longitude);
                                intent.putExtra(MainActivity.this.getString(R.string.latitude_key), latLng.latitude);
                                intent.putExtra(MainActivity.this.getString(R.string.zoom_level_key), map.getCameraPosition().zoom);
                                startActivity(intent);
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(getString(R.string.confirm_new_report)).setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();
            }

        });
    }

    void hasLoggedIn(String FBaccessToken) {
        this.fbAccessToken = FBaccessToken;
        setDrawerLayoutVisible(true);
        getSupportFragmentManager().beginTransaction().
                hide(mainLoginFragment).commit();
        if (!hasLoggedIn) {
            invalidateOptionsMenu();
            CatastroperApplication catastroperApplication = (CatastroperApplication) getApplication();
            catastroperApplication.setUserFBAuthToken(FBaccessToken);
            Toast.makeText(this, getString(R.string.fb_logged_in), Toast.LENGTH_SHORT).show();
            hasLoggedIn = true;
            setupGCM();
            refreshMap();
        }
    }

    void hasLoggedOut() {
        invalidateOptionsMenu();
        fbAccessToken = null;
        CatastroperApplication catastroperApplication = (CatastroperApplication) getApplication();
        catastroperApplication.setUserFBAuthToken(null);
        setDrawerLayoutVisible(false);
        getSupportFragmentManager()
                .beginTransaction()
                .show(mainLoginFragment)
                .commit();
        Toast.makeText(this, getString(R.string.fb_logged_out), Toast.LENGTH_SHORT).show();
        hasLoggedIn = false;
    }

    private void setDrawerLayoutVisible(boolean visible) {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        refreshMenuItem = menu.findItem(R.id.main_action_refresh);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        menu.findItem(R.id.main_menu).setVisible(!drawerOpen);
        menu.findItem(R.id.main_menu).setVisible(hasLoggedIn);
        getActionBar().setDisplayHomeAsUpEnabled(hasLoggedIn);
        getActionBar().setHomeButtonEnabled(hasLoggedIn);
        menu.findItem(R.id.main_action_refresh).setVisible(hasLoggedIn);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.main_action_refresh) {
            refreshMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshMap() {
        if (!isRefreshingMap) {
            if (refreshMenuItem != null) {
                refreshMenuItem.setActionView(R.layout.actionbar_progress_menu_item);
            }
            isRefreshingMap = true;
            AndroidHttpClient httpClient = HttpHandler.getAndroidHttpClient(this);
            HttpHandler httpHandler = new HttpHandler(httpClient);
            httpHandler.getReports(this);
        }
    }

    public void handleRefreshMapResults(String json) {
        if (json == null) return;
        final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.main_map))
                .getMap();

        map.clear();

        List<Report> reports = Report.jsonToListOfReports(json);
        if (reports != null) {
            for (Report report : reports) {
                map.addMarker(new MarkerOptions().
                        position(report.getLocation())
                        .title(report.getTitle() + "\n\n" + " date: " + report.getTimestamp())
                        .snippet(report.getText()));
            }
        }

        if (refreshMenuItem != null) {
            refreshMenuItem.setActionView(null);
        }
        isRefreshingMap = false;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerLayout.closeDrawers();

            final ListView drawerList = (ListView) findViewById(R.id.left_drawer);
            drawerList.setChoiceMode(ListView.CHOICE_MODE_NONE);
            drawerList.setAdapter(drawerList.getAdapter());
        }
    }

    private void selectItem(int position) {
        switch (position) {
            case 0: {
                Intent intent = new Intent(this, MyReportsActivity.class);
                startActivity(intent);
            }
            break;
            case 1: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }
            break;
            case 2: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            }
            break;
            case 3: {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                mainLoginFragment.logout();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.logout_prompt)).setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();
            }
            break;
        }
    }

    @Override
    public void onDestroy() {
        if (receiverRegistrered) {
            unregisterReceiver(handleGCMMessageReceiver);
        }

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (hasLoggedIn) {
            refreshMap();
        }
    }
}
