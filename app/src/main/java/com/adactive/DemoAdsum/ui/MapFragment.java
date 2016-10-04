package com.adactive.DemoAdsum.ui;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.adactive.DemoAdsum.R;
import com.adactive.DemoAdsum.actions.MapActions;
import com.adactive.DemoAdsum.actions.PathActions;
import com.adactive.DemoAdsum.structure.PoiCollection;
import com.adactive.DemoAdsum.utils.PermissionsUtils;
import com.adactive.nativeapi.AdActiveEventListener;
import com.adactive.nativeapi.CheckForUpdatesNotice;
import com.adactive.nativeapi.CheckStartNotice;
import com.adactive.nativeapi.Coordinates3D;
import com.adactive.nativeapi.MapView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapFragment extends MainActivity.PlaceholderFragment implements View.OnTouchListener, StoreDescriptionDialog.DialogListener
        {

    static private boolean isMapLoaded = false;
    static private MapView.CameraMode currentCameraMode = MapView.CameraMode.FULL;


    public enum NAVIGATION_MODE {
        FREE,
        CENTER_ON,
        CENTER_ON_AND_COMPASS
    }

    private NAVIGATION_MODE currentNavigationMode = NAVIGATION_MODE.FREE;
    private NAVIGATION_MODE nextNavigationMode = NAVIGATION_MODE.CENTER_ON;


    private View rootView;
    private MapView map;
    private LinearLayout mapContainer;

    private FloatingActionsMenu setLevel;
    private FloatingActionButton setSiteView;
    private FloatingActionButton deletePath;
    private FloatingActionButton setLocalisationBehaviour;

    private FloatingActionButton preSelectedFloorButton;
    private Map<Integer, FloatingActionButton> floorButtonsMap = new HashMap<>();

    private SearchBox search;
    private boolean isMenuEnabled = true;

    private PoiCollection mPoiCollection;

    private AdActiveEventListener adActiveEventListener;

    private int currentBuildingId = -1;

    private MapActions mapActions;
    private PathActions pathActions;

    public static MapFragment newInstance(MapView map) {
        MapFragment fragment = new MapFragment();
        fragment.setMap(map);
        return fragment;
    }

    public MapFragment() {
    }

    public void setMap(MapView m) {
        map = m;

        adActiveEventListener = new AdActiveEventListener() {
            @Override
            public void OnPOIClickedHandler(int[] POIs, int place) {
                doPOIClicked(POIs[0], place);

            }

            @Override
            public void OnBuildingClickedHandler(int i) {
                doBuildingClicked(i);
            }

            @Override
            public void OnFloorChangedHandler(int floorId) {
                int nBuidlingid = map.getFloorBuilding(floorId);
                if (nBuidlingid != currentBuildingId) {
                    doBuildingClicked(nBuidlingid);
                }
                if (!floorButtonsMap.isEmpty()) {
                    doFloorButtonsChanged(floorId);
                }
            }


            @Override
            public void OnFloorClickedHandler(int i) {
            }

            @Override
            public void OnTextClickedHandler(int[] POIs, int place) {
            }

            @Override
            public void OnMapLoadedHandler() {
            }

            @Override
            public void OnAdActiveViewStartHandler(int stateId) {
                if (stateId == CheckStartNotice.ADACTIVEVIEW_DID_START) {
                    mapActions = new MapActions(map);
                    pathActions = new PathActions(map);
                    mPoiCollection = new PoiCollection(map.getDataManager().getAllPois());
                    doMapLoaded();
                }
            }


            @Override
            public void OnCheckForUpdatesHandler(int i) {
                Log.i("update", "done");
                if (i == CheckForUpdatesNotice.CHECKFORUPDATES_UPDATESFOUND || i == CheckForUpdatesNotice.CHECKFORUPDATES_UPDATESNOTFOUND) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootView.findViewById(R.id.map).setVisibility(View.VISIBLE);
                            rootView.findViewById(R.id.progress_container).setVisibility(View.GONE);
                            isMenuEnabled = true;
                        }
                    });

                    map.start();
                }
            }

            @Override
            public void OnFloorIntersectedAtPositionHandler(int i, Coordinates3D coordinates3D) {

            }
        };


        map.addEventListener(adActiveEventListener);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        currentCameraMode = MapView.CameraMode.FULL;

        mapContainer = (LinearLayout) rootView.findViewById(R.id.map_container);
        setSiteView = (FloatingActionButton) rootView.findViewById(R.id.set_site_view);
        setLevel = (FloatingActionsMenu) rootView.findViewById(R.id.set_level);
        deletePath = (FloatingActionButton) rootView.findViewById(R.id.delete_path);
        setLocalisationBehaviour = (FloatingActionButton) rootView.findViewById(R.id.set_follow_location);

        setLocalisationBehaviour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localisationButtonBehaviour();
            }
        });

        if (!map.isMapDataAvailable()) {
            rootView.findViewById(R.id.map).setVisibility(View.GONE);
            rootView.findViewById(R.id.progress_container).setVisibility(View.VISIBLE);
            isMenuEnabled = false;
        }

        mapContainer.addView(map);

        if (isMapLoaded) {
            doMapLoaded();
        }

        search = ((MainActivity) getActivity()).getSearchBox();
        search.enableVoiceRecognition(this);

        map.setOnTouchListener(this);

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);

        if (currentCameraMode == MapView.CameraMode.FULL) {
            menu.findItem(R.id.switch_camera).setTitle(getString(R.string.action_switch_2D));
        } else if (currentCameraMode == MapView.CameraMode.ORTHO) {
            menu.findItem(R.id.switch_camera).setTitle(getString(R.string.action_switch_3D));
        }

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (isMenuEnabled) {
            // Change the icon in the action bar and the camera mode
            if (id == R.id.switch_camera) {
                doSwitchCamera(item);
                return true;
            }

            // Show the wayfinding dialog
            if (id == R.id.wayfinding) {
                showWayfindingFragment();
                return true;
            }

            // Open the search menu
            if (id == R.id.search) {
                doOpenSearch();
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapContainer.removeView(map);
        map.removeEventListener(adActiveEventListener);

        if (!isMenuEnabled) {
            search.toggleSearch();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == getActivity().RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            search.populateEditText(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doMapLoaded() {

        final boolean isInBuilding = map.getCurrentBuilding() != -1;

        setSiteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSetSiteView();
            }
        });

        deletePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pathActions.resetPathDrawing();
                map.unLightAll();
                deletePath.setVisibility(View.GONE);
            }
        });


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //if (checkPermissions()) {
                //}
                if (isInBuilding) {
                    setSiteView.setIcon(R.drawable.ic_chevron_left_black_48dp);
                    setLevel.setVisibility(View.VISIBLE);
                } else {
                    setSiteView.setIcon(R.drawable.ic_home_black_48dp);
                    setLevel.setVisibility(View.GONE);
                }

                setSiteView.setVisibility(View.VISIBLE);
            }
        });

        if (isInBuilding) {
            doBuildingClicked(map.getCurrentBuilding());
        }
        setCurrentFloorOnUser();

        mapActions.setInitialState();
        pathActions.resetPathDrawing()
                .setMotionFalse();
        isMapLoaded = true;

    }

    private void doSetSiteView() {
        // Collapse the setLevel menu
        setLevel.collapse();
        // Change the icon of the setSiteView button (into home)
        setSiteView.setIcon(R.drawable.ic_home_black_48dp);
        // Make the setLevel button invisible
        setLevel.setVisibility(View.GONE);
        map.setSiteView();
    }


    private void doPOIClicked(int POI, int place) {
        //Highlight POI
        mapActions.POIClicked(place);

        //Launch Dialog
        Bundle args = new Bundle();
        String name = map.getDataManager().getPoi(POI).getName();
        args.putString(StoreDescriptionDialog.ARG_STORE_NAME, name);
        args.putInt("PoiID", POI);
        //args.putString(StoreDescriptionDialog.ARG_STORE_DESCRIPTION, description);

        StoreDescriptionDialog storeDialog = new StoreDescriptionDialog();
        storeDialog.setArguments(args);

        storeDialog.show(getFragmentManager(), "storeDescription");
    }


    private void doBuildingClicked(int i) {
        this.currentBuildingId = i;
        final int[] floors = map.getBuildingFloors(i);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map.unLightAll();

                // Remove all the former floorButtons of the menu */
                for (Integer floorId : floorButtonsMap.keySet()) {
                    setLevel.removeButton(floorButtonsMap.get(floorId));
                }
                floorButtonsMap.clear();

                // Add all the new floorButtons on the menu
                FloatingActionButton floorButton = null;
                for (int i = 0; i < floors.length; ++i) {
                    floorButton = createFloorButton(i, floors[i]);
                    //floorButtons.add(floorButton);
                    floorButtonsMap.put(floors[i], floorButton);
                    setLevel.addButton(floorButton);
                }

                // Disable the current floor button
                if (floorButton != null) {
                    preSelectedFloorButton = floorButton;
                    floorButton.setEnabled(false);
                }

                // Change the icon of the setSiteView button (into arrow)
                setSiteView.setIcon(R.drawable.ic_chevron_left_black_48dp);

                // Make the setLevel button visible
                setLevel.setVisibility(View.VISIBLE);
            }
        });

        map.setCurrentBuilding(i);
    }

    private FloatingActionButton createFloorButton(int level, final int floorId) {
        FloatingActionButton floorButton = new FloatingActionButton(getActivity().getBaseContext());
        floorButton.setSize(FloatingActionButton.SIZE_MINI);
        floorButton.setColorNormalResId(R.color.white);
        floorButton.setColorPressedResId(R.color.white_pressed);

        TextDrawable floor_icon = TextDrawable.builder()
                .beginConfig()
                .fontSize(30)
                .textColor(Color.BLACK)
                .endConfig()
                .buildRound(Integer.toString(level), Color.TRANSPARENT);

        floorButton.setIconDrawable(floor_icon);

        floorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFloorButtonsChanged(floorId);
                Log.i("floorID", String.valueOf(floorId));
                map.setCurrentFloor(floorId);
            }
        });

        return floorButton;
    }

    private void setCurrentFloorOnUser() {
        int floorID = map.getPlaceFloor(0);
        mapActions.setCurrentFloorOnUser(floorID);
        doFloorButtonsChanged(floorID);
    }

    public void doFloorButtonsChanged(final int floorId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (preSelectedFloorButton != null && floorId > -1) {
                    preSelectedFloorButton.setEnabled(true);
                }

                // Disable the current floor button
                preSelectedFloorButton = floorButtonsMap.get(floorId);
                if (preSelectedFloorButton != null) {
                    preSelectedFloorButton.setEnabled(false);
                }
            }
        });

    }

    private void doSwitchCamera(MenuItem item) {
        if (currentCameraMode == MapView.CameraMode.FULL) {
            currentCameraMode = MapView.CameraMode.ORTHO;
            item.setTitle(getString(R.string.action_switch_3D));
        } else if (currentCameraMode == MapView.CameraMode.ORTHO) {
            currentCameraMode = MapView.CameraMode.FULL;
            item.setTitle(getString(R.string.action_switch_2D));
        }

        map.setCameraMode(currentCameraMode);
    }

    private void showWayfindingFragment() {
        Bundle args = new Bundle();

        args.putStringArrayList(WayfindingDialog.ARG_STORES_NAMES_LIST, (ArrayList<String>) mPoiCollection.getPoiNamesSortedList());
        args.putIntegerArrayList(WayfindingDialog.ARG_STORES_IDS_LIST, (ArrayList<Integer>) mPoiCollection.getPoiIdList());

        WayfindingDialog wayfindingDialog = new WayfindingDialog();
        wayfindingDialog.setArguments(args);
        wayfindingDialog.setMap(map);
        wayfindingDialog.setDeletePath(deletePath);

        wayfindingDialog.show(getFragmentManager(), "wayfinding");
    }

    private void doOpenSearch() {
        map.onPause();
        isMenuEnabled = false;
        search.revealFromMenuItem(R.id.search, getActivity());

        List<String> mPoiNamesSortedList = mPoiCollection.getPoiNamesSortedList();
        for (String n : mPoiNamesSortedList) {

            if (map.getPOIPlaces(mPoiCollection.getByName(n).getId()).length != 0) {
                SearchResult option = new SearchResult(n, getResources().getDrawable(R.drawable.ic_store_black_48dp));

                search.addSearchable(option);
            }
        }


        search.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {

            }

            @Override
            public void onSearchClosed() {
                doCloseSearch();
            }

            @Override
            public void onSearchTermChanged() {
            }

            @Override
            public void onSearch(String searchTerm) {
                doSearch(searchTerm);
            }

            @Override
            public void onSearchCleared() {
            }
        });

    }

    private void doCloseSearch() {
        map.onResume();
        isMenuEnabled = true;
        search.clearSearchable();
        search.clearResults();
        search.setSearchString("");
        search.hideCircularly(getActivity());
    }

    private void doSearch(String searchTerm) {

        if (mPoiCollection.getByName(searchTerm) == null) {
            Toast.makeText(getActivity(), searchTerm + getString(R.string.search_error), Toast.LENGTH_SHORT).show();
        } else {
            int poiID = (mPoiCollection.getByName(searchTerm)).getId();
            map.unLightAll();
            map.highLightPOI(poiID, getString(R.string.highlight_color));
            map.centerOnPOI(poiID);
            pathActions.drawPathToPoi(poiID);
        }
    }


    public void notifyUser(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Touching the Map will deactivate CenterOnMe if it was active
        enableFreeMode();
        // dispatch the touch event to the adsum map
        map.onTouch(v, event);
        return true;
    }

    private void localisationButtonBehaviour() {
        Log.d("ADSUM:CURENT", String.valueOf(currentNavigationMode));
        Log.d("ADSUM:NEXT", String.valueOf(nextNavigationMode));

        //changes the color of setLocalisation the floating button and behaves accordingly
        switch (nextNavigationMode) {
            case FREE:

                enableFreeMode();
                nextNavigationMode = NAVIGATION_MODE.CENTER_ON;
                break;
            case CENTER_ON:
                currentNavigationMode = NAVIGATION_MODE.CENTER_ON;
                setLocalisationBehaviour.setColorNormal(getResources().getColor(R.color.maj));
                Toast.makeText(getActivity(), "Map AutoCentered", Toast.LENGTH_SHORT).show();
                setCurrentFloorOnUser();
                map.centerOnPlace(0, 300, 0.2f);
                nextNavigationMode = NAVIGATION_MODE.CENTER_ON_AND_COMPASS;
                break;
            case CENTER_ON_AND_COMPASS:
                currentNavigationMode = NAVIGATION_MODE.CENTER_ON_AND_COMPASS;
                Toast.makeText(getActivity(), "Map AutoCentered And Compass", Toast.LENGTH_SHORT).show();
                setLocalisationBehaviour.setIcon(R.drawable.icon_location_compass);
                mapActions.startCompass();
                nextNavigationMode = NAVIGATION_MODE.FREE;
                break;
        }

        // Make the setLevel button visible
        if (map.getCurrentFloor() != -1) {
            setLevel.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onDialogNeutralClick(android.support.v4.app.DialogFragment dialog, int id) {
        //Function called from Dialog to draw path
        pathActions.drawPathToPoi(id);
    }


    private void enableFreeMode() {
        if (currentNavigationMode != NAVIGATION_MODE.FREE) {
            mapActions.stopCompass();
            setLocalisationBehaviour.setIcon(R.drawable.icon_location);
            setLocalisationBehaviour.setColorNormal(getResources().getColor(R.color.white));
            currentNavigationMode = NAVIGATION_MODE.FREE;
            nextNavigationMode = NAVIGATION_MODE.CENTER_ON;
        }
    }


    // Permissions Check
/*
    private static final int PERMISSION_REQUEST_CODE = 0;

    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.BLUETOOTH
    };

    protected boolean checkPermissions() {
        return PermissionsUtils.checkAndRequest(this.getActivity(), NEEDED_PERMISSIONS, getResources().getString(R.string.permission_msg), PERMISSION_REQUEST_CODE,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPermissionsRefused();
                    }
                });
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
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

                } else {
                    onPermissionsRefused();
                }
            }
        }
    }*/


    public void onPermissionsRefused() {
        notifyUser("Cannot run the service because permissions have been denied");
    }


            @Override
            public void onPause() {
                if (map != null)
                    map.onPause();
                super.onPause();
            }


            @Override
            public void onResume() {
                if (map != null)
                    map.onResume();
                super.onResume();
            }


        }