package main.jf.catastropherandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

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

    private GoogleMap map;

    private String userFBId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.main_activity);
        setVisibile(false);

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

    private void setupMap() {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(SWEDEN_ROYAL_CASTLE)
                .zoom(9)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        map.setMyLocationEnabled(true);

        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                return true;
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
                                intent.putExtra(MainActivity.this.getString(R.string.user_fb_id_key), userFBId);
                                intent.putExtra(MainActivity.this.getString(R.string.longitude_key), latLng.longitude);
                                intent.putExtra(MainActivity.this.getString(R.string.latitude_key), latLng.latitude);
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

    void hasLoggedIn(String userFBId) {
        this.userFBId = userFBId;
        setVisibile(true);
        mainLoginFragment.getView().setVisibility(View.INVISIBLE);
        if (!hasLoggedIn) {
            Toast.makeText(this, getString(R.string.fb_logged_in), Toast.LENGTH_SHORT).show();
            hasLoggedIn = true;
        }
    }

    void hasLoggedOut() {
        userFBId = null;
        setVisibile(false);
        mainLoginFragment.getView().setVisibility(View.VISIBLE);
        Toast.makeText(this, getString(R.string.fb_logged_out), Toast.LENGTH_SHORT).show();
        hasLoggedIn = false;
    }

    private void setVisibile(boolean visible) {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        menu.findItem(R.id.main_menu).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }
            break;
            case 1: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            }
            break;
            case 2: {
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

    public boolean getHasLoggedIn() {
        return hasLoggedIn;
    }
}
