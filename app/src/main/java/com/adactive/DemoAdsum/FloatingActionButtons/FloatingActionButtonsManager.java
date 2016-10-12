package com.adactive.DemoAdsum.FloatingActionButtons;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.adactive.DemoAdsum.R;
import com.adactive.nativeapi.MapView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ambroise on 04/10/2016.
 */

public class FloatingActionButtonsManager extends com.getbase.floatingactionbutton.FloatingActionsMenu{

    private FloatingActionsMenu fabSetLevel;
    private FloatingActionButton fabSetSiteView;
    private FloatingActionButton fabDeletePath;
    private FloatingActionButton fabSetLocalisationBehaviour;
    private FloatingActionButton preSelectedFloorButton;

    private Map<Integer, FloatingActionButton> floorButtonsMap = new HashMap<>();

    private FABListener floatingActionButtonListener;

    public FloatingActionButtonsManager(Context context, View aRootView) {
        super(context);
        fabSetSiteView = (FloatingActionButton) aRootView.findViewById(R.id.set_site_view);
        fabSetLevel = (FloatingActionsMenu) aRootView.findViewById(R.id.set_level);
        fabDeletePath = (FloatingActionButton) aRootView.findViewById(R.id.delete_path);
        fabSetLocalisationBehaviour = (FloatingActionButton) aRootView.findViewById(R.id.set_follow_location);
    }

    public FloatingActionButtonsManager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingActionButtonsManager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }


    public interface FABListener {
        void setSiteViewListener();
        void setLevelListener();
        void deletePathListener();
        void setLocalisationBehaviour();
    }

    public void addEventListener(FABListener listener) {
        floatingActionButtonListener=listener;
        startListening();
            }


    private void startListening(){
        fabSetLocalisationBehaviour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionButtonListener.setLocalisationBehaviour();
            }
        });
        fabSetLevel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                floatingActionButtonListener.setLevelListener();
            }
        });
        fabSetSiteView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                floatingActionButtonListener.setSiteViewListener();
            }
        });
        fabDeletePath.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                floatingActionButtonListener.deletePathListener();
            }
        });

    }


    public boolean isFloorButtonMapEmpty() {
        return floorButtonsMap.isEmpty();
    }

    public void doFABBehaviourWhetherInBuilding(boolean isInBuilding) {
        if (isInBuilding) {
            fabSetSiteView.setIcon(R.drawable.ic_chevron_left_black_48dp);
            fabSetLevel.setVisibility(View.VISIBLE);
        } else {
            fabSetSiteView.setIcon(R.drawable.ic_home_black_48dp);
            fabSetLevel.setVisibility(View.GONE);
        }
        fabSetSiteView.setVisibility(View.VISIBLE);
    }


    public void doSetSiteViewFAB() {
        // Collapse the fabSetLevel menu
        fabSetLevel.collapse();
        // Change the icon of the fabSetSiteView button (into home)
        fabSetSiteView.setIcon(R.drawable.ic_home_black_48dp);
        // Make the fabSetLevel button invisible
        fabSetLevel.setVisibility(View.GONE);
    }


    public void doBuildingClickedFAB(int[] floors, Context ctx,MapView map) {
        // Remove all the former floorButtons of the menu
        for (Integer floorId : floorButtonsMap.keySet()) {
            fabSetLevel.removeButton(floorButtonsMap.get(floorId));
        }
        floorButtonsMap.clear();

        // Add all the new floorButtons on the menu
        FloatingActionButton floorButton = null;
        for (int i = 0; i < floors.length; ++i) {
            floorButton = createFloorButton(i, floors[i],ctx,map);
            //floorButtons.add(floorButton);
            floorButtonsMap.put(floors[i], floorButton);
            fabSetLevel.addButton(floorButton);
        }

        // Disable the current floor button
            preSelectedFloorButton = floorButton;
            floorButton.setEnabled(false);


        // Change the icon of the fabSetSiteView button (into arrow)
        fabSetSiteView.setIcon(R.drawable.ic_chevron_left_black_48dp);

        // Make the fabSetLevel button visible
        fabSetLevel.setVisibility(View.VISIBLE);

    }


    private FloatingActionButton createFloorButton(int level, final int floorId, Context ctx, final MapView map) {
        FloatingActionButton floorButton = new FloatingActionButton(ctx);
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
                updateFloorButtonsFAB(floorId);
                Log.i("floorID", String.valueOf(floorId));
                map.setCurrentFloor(floorId);

            }
        });

        return floorButton;
    }

    public void updateFloorButtonsFAB(final int floorId) {
        if (preSelectedFloorButton != null) {
            preSelectedFloorButton.setEnabled(true);
        }

        // Disable the current floor button
        preSelectedFloorButton = floorButtonsMap.get(floorId);
        if (preSelectedFloorButton != null) {
            preSelectedFloorButton.setEnabled(false);
        }
    }


    public void setColorLocalisationButton(int color) {
        fabSetLocalisationBehaviour.setColorNormal(color);

    }

    public void setIconLocalisationButton(int icon) {
        fabSetLocalisationBehaviour.setIcon(icon);
    }

    public void setVisibilityFAButtonSetLevel(boolean isVisible) {
        if (isVisible) {
            fabSetLevel.setVisibility(View.VISIBLE);
        } else {
            fabSetLevel.setVisibility(View.INVISIBLE);
        }

    }

    public void setVisibilityFABDeletePath(boolean isVisible) {
        if (isVisible) {
            fabSetLevel.setVisibility(View.VISIBLE);
        } else {
            fabDeletePath.setVisibility(View.GONE);
        }

    }
}
