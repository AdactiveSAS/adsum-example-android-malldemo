package com.adactive.DemoAdsum.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.adactive.DemoAdsum.R;
import com.adactive.DemoAdsum.actions.MapActions;
import com.adactive.DemoAdsum.actions.PathActions;
import com.adactive.nativeapi.MapView;
import com.quinny898.library.persistentsearch.SearchBox;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MapView map;
    private Toolbar toolbar;
    private PathActions pathActions;
    private MapActions mapActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Force local to english
        Locale locale2 = new Locale("en");
        Locale.setDefault(locale2);
        Configuration config2 = new Configuration();
        config2.locale = locale2;
        getBaseContext().getResources().updateConfiguration(
                config2, getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

        map = new MapView(getApplicationContext());

        mapActions = new MapActions(map);
        mapActions.startMap();

        //Drawer implementation
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Start containing Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(1, map)).commit();

        //Prevent the screen from locking
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public void onDestroy() {
        map.destroy();
        super.onDestroy();
    }

    public void onSectionAttached(int number) {
    }

    public SearchBox getSearchBox() {
        return (SearchBox) findViewById(R.id.searchbox);
    }

    @Override
    public void onBackPressed() {
        SearchBox search = (SearchBox) findViewById(R.id.searchbox);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        boolean handled = false;

        if (search.isShown()) {
            search.toggleSearch();
            handled = true;
        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            handled = true;
        }

        if (!handled) super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.drawer_synchroAdsum) {
            Toast.makeText(this, "Updating Map", Toast.LENGTH_LONG).show();
            map.update(true);

        } else if (id == R.id.drawer_resetPath) {
            Toast.makeText(this, "Path Reset", Toast.LENGTH_LONG).show();
            pathActions = new PathActions(map);
            pathActions.resetPathDrawing();


        } else if (id == R.id.drawer_about) {
            Toast.makeText(this, "Demo", Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class PlaceholderFragment extends android.support.v4.app.Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber, MapView map) {
            PlaceholderFragment fragment;
            fragment = MapFragment.newInstance(map);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


    // when the fragment call requestPermissions, the onRequestPermissionsResuls is called on the activity
    /*public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.e("dbg", "permission results");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.container);

        if (requestCode == MapFragment.PERMISSION_REQUEST_CODE) {
            boolean allRequestsAccepted = false;
            if (grantResults.length == permissions.length) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allRequestsAccepted = false;
                        break;
                    } else {
                        allRequestsAccepted = true;
                    }
                }
                if (allRequestsAccepted) {
                    Log.e("dbg", "permission accpeted");
                } else {
                    Log.e("dbg", "permission refused");
                    fragment.onPermissionsRefused();
                }
            }
        }
    }*/
}
