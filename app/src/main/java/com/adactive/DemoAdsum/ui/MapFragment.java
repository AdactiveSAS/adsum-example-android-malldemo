package com.adactive.DemoAdsum.ui;

import android.content.Intent;
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

import com.adactive.DemoAdsum.FloatingActionButtons.FloatingActionButtonsManager;
import com.adactive.DemoAdsum.R;
import com.adactive.DemoAdsum.actions.MapActions;
import com.adactive.DemoAdsum.actions.PathActions;
import com.adactive.DemoAdsum.structure.PoiCollection;
import com.adactive.nativeapi.AdActiveEventListener;
import com.adactive.nativeapi.CheckForUpdatesNotice;
import com.adactive.nativeapi.CheckStartNotice;
import com.adactive.nativeapi.Coordinates3D;
import com.adactive.nativeapi.DataObject.Poi;
import com.adactive.nativeapi.DataObject.Store;
import com.adactive.nativeapi.MapView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.List;


public class MapFragment extends MainActivity.PlaceholderFragment implements View.OnTouchListener, StoreDescriptionDialog.DialogListener, FloatingActionButtonsManager.FABListener {

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
    private FloatingActionButton fabDeletePath;

    private SearchBox search;
    private boolean isMenuEnabled = true;

    private PoiCollection mPoiCollection;

    private AdActiveEventListener adActiveEventListener;

    private int currentBuildingId = -1;

    private MapActions mapActions;
    private PathActions pathActions;
    private FloatingActionButtonsManager fabButtonsManager;

    private int _currentPoi = -1;

    public static MapFragment newInstance(MapView map) {
        MapFragment fragment = new MapFragment();
        fragment.setMap(map);
        return fragment;
    }

    public MapFragment() {
    }

    public void setMap(final MapView m) {
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
                if (!fabButtonsManager.isFloorButtonMapEmpty()) {
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
                    doAfterMapLoaded();
                }
            }


            @Override
            public void OnCheckForUpdatesHandler(int i) {
                if (i == CheckForUpdatesNotice.CHECKFORUPDATES_COMMUNICATIONERROR) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Update Status: Communication Error", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                if (i == CheckForUpdatesNotice.CHECKFORUPDATES_UPDATESFOUND || i == CheckForUpdatesNotice.CHECKFORUPDATES_UPDATESNOTFOUND) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Update Status: Success", Toast.LENGTH_LONG).show();
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
        fabDeletePath = (FloatingActionButton) rootView.findViewById(R.id.delete_path);
        currentCameraMode = MapView.CameraMode.FULL;
        mapContainer = (LinearLayout) rootView.findViewById(R.id.map_container);

        fabButtonsManager = new FloatingActionButtonsManager(getContext(), rootView);


        if (!map.isMapDataAvailable()) {
            rootView.findViewById(R.id.map).setVisibility(View.GONE);
            rootView.findViewById(R.id.progress_container).setVisibility(View.VISIBLE);
            isMenuEnabled = false;
        }

        mapContainer.addView(map);

        if (isMapLoaded) {
            doAfterMapLoaded();
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
                showWayfindingFragment(_currentPoi);
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

    private void doAfterMapLoaded() {
        //do after map has finished loading
        final boolean isInBuilding = map.getCurrentBuilding() != -1;

        fabButtonsManager.addEventListener(this);
        //set floating buttons
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fabButtonsManager.doFABBehaviourWhetherInBuilding(isInBuilding);
            }
        });

        if (isInBuilding) {
            doBuildingClicked(map.getCurrentBuilding());
        }
        setCurrentFloorOnUser();

        //set map initial state
        mapActions.setInitialState();
        pathActions.resetPathDrawing()
                .setMotionFalse();
        isMapLoaded = true;

    }

    private void doSetSiteView() {
        fabButtonsManager.doSetSiteViewFAB();
        map.setSiteView();
    }

    int lastPoiId = -1;
    int counter = 0;


    private void doPOIClicked(int POI, int place) {
        //Highlight POI
        _currentPoi = POI;
        mapActions.POIClicked(place);

        if (lastPoiId == place)
            counter += 1;

        lastPoiId=place;

        if (counter == 1) {
            lastPoiId=-1;
            counter=0;

            //Launch Dialog
            Bundle args = new Bundle();
            Poi o = mPoiCollection.getById(POI);

            String name = o.getName();

            if (Store.class.isInstance(o)) {
                String description = ((Store) o).getDescription() != null ? ((Store) o).getDescription() : getString(R.string.no_description);
                String logoPath = ((Store) o).getLogoPath();
                args.putString(StoreDescriptionDialog.ARG_STORE_DESCRIPTION, description);
                args.putString(StoreDescriptionDialog.ARG_LOGO_PATH, logoPath);
            }

            args.putString(StoreDescriptionDialog.ARG_STORE_NAME, name);
            args.putInt("PoiID", POI);

            StoreDescriptionDialog storeDialog = new StoreDescriptionDialog();
            storeDialog.setArguments(args);

            storeDialog.show(getFragmentManager(), "storeDescription");
        }
    }


    private void doBuildingClicked(int i) {
        this.currentBuildingId = i;
        final int[] floors = map.getBuildingFloors(i);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map.unLightAll();
                _currentPoi = -1;
                fabButtonsManager.doBuildingClickedFAB(floors, getActivity().getBaseContext(), map);
            }
        });

        map.setCurrentBuilding(i);
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
                fabButtonsManager.updateFloorButtonsFAB(floorId);
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

    private void showWayfindingFragment(Integer poiId) {
        Bundle args = new Bundle();

        args.putInt("currentPoi", _currentPoi);

        args.putStringArrayList(WayfindingDialog.ARG_STORES_NAMES_LIST, (ArrayList<String>) mPoiCollection.getWfNameList());
        args.putIntegerArrayList(WayfindingDialog.ARG_STORES_IDS_LIST, (ArrayList<Integer>) mPoiCollection.getWfIdList());

        WayfindingDialog wayfindingDialog = new WayfindingDialog();
        wayfindingDialog.setArguments(args);
        wayfindingDialog.setMap(map);
        wayfindingDialog.setDeletePath(fabDeletePath);

        wayfindingDialog.show(getFragmentManager(), "wayfinding");
    }

    private void doOpenSearch() {
        map.onPause();
        isMenuEnabled = false;
        search.revealFromMenuItem(R.id.search, getActivity());

        List<String> mPoiNamesSortedList = mPoiCollection.getWfNameList();
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
            map.centerOnPlace(0);
            pathActions.setMotionOn();
            pathActions.drawPathToPoi(poiID);
            _currentPoi = poiID;
        }
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
        //changes the color and icon of the localisationButton
        switch (nextNavigationMode) {
            case FREE:
                enableFreeMode();
                nextNavigationMode = NAVIGATION_MODE.CENTER_ON;
                Log.d("ADSUM:CURENT", String.valueOf(currentNavigationMode));
                Log.d("ADSUM:NEXT", String.valueOf(nextNavigationMode));
                break;
            case CENTER_ON:
                currentNavigationMode = NAVIGATION_MODE.CENTER_ON;
                fabButtonsManager.setColorLocalisationButton(getResources().getColor(R.color.maj));
                Toast.makeText(getActivity(), "Map AutoCentered", Toast.LENGTH_SHORT).show();
                setCurrentFloorOnUser();
                map.centerOnPlace(0, 300, 0.2f);
                nextNavigationMode = NAVIGATION_MODE.CENTER_ON_AND_COMPASS;
                Log.d("ADSUM:CURENT", String.valueOf(currentNavigationMode));
                Log.d("ADSUM:NEXT", String.valueOf(nextNavigationMode));
                break;
            case CENTER_ON_AND_COMPASS:
                currentNavigationMode = NAVIGATION_MODE.CENTER_ON_AND_COMPASS;
                Toast.makeText(getActivity(), "Map AutoCentered And Compass", Toast.LENGTH_SHORT).show();
                fabButtonsManager.setIconLocalisationButton(R.drawable.icon_location_compass);
                mapActions.startCompass();
                nextNavigationMode = NAVIGATION_MODE.FREE;
                Log.d("ADSUM:CURENT", String.valueOf(currentNavigationMode));
                Log.d("ADSUM:NEXT", String.valueOf(nextNavigationMode));
                break;
        }

        // Make the setLevel button visible
        if (map.getCurrentFloor() != -1) {
            fabButtonsManager.setVisibilityFAButtonSetLevel(true);
        }
    }

    private void enableFreeMode() {
        //resets compass mode and centerOnMe
        if (currentNavigationMode != NAVIGATION_MODE.FREE) {
            mapActions.stopCompass();
            fabButtonsManager.setIconLocalisationButton(R.drawable.icon_location);
            fabButtonsManager.setColorLocalisationButton(getResources().getColor(R.color.white));
            currentNavigationMode = NAVIGATION_MODE.FREE;
            nextNavigationMode = NAVIGATION_MODE.CENTER_ON;
        }
    }

    @Override
    public void onDialogClick(android.support.v4.app.DialogFragment dialog, int id) {
        //Function called from Dialog to draw path
        pathActions.drawPathToPoi(id);
    }

    @Override
    public void setSiteViewListener() {
        doSetSiteView();
    }

    @Override
    public void setLevelListener() {

    }

    @Override
    public void deletePathListener() {
        pathActions.resetPathDrawing();
        _currentPoi = -1;
        map.unLightAll();
        fabButtonsManager.setVisibilityFABDeletePath(false);
    }

    @Override
    public void setLocalisationBehaviour() {
        localisationButtonBehaviour();

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