package com.adactive.DemoAdsum.structure;

import com.adactive.nativeapi.DataObject.Poi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ambroise on 02/09/2016.
 */
public class PoiCollection {

    private List<Poi> pois = null;


    private Map<String, Poi> mPoiName = new HashMap<>();
    private Map<Integer, Poi> mPoiInteger = new HashMap<>();
    private List<String> mNameList = new ArrayList<String>();
    private List<Integer> mIdList = new ArrayList<Integer>();

    public PoiCollection(List<Poi> aPois) {
        pois = aPois;
        for (Poi o : pois) {
            if (o.getName() != null) {
                mPoiName.put(o.getName(), o);
                mPoiInteger.put(o.getId(), o);
                mNameList.add(o.getName());
                mIdList.add(o.getId());
            }
        }
        sortPoiNameMap();
    }

    private void sortPoiNameMap() {
            Collections.sort(mNameList, String.CASE_INSENSITIVE_ORDER);
    }

    public Poi getByName(String name) {return mPoiName.get(name);
    }

    public Poi getById(Integer id) {
        return mPoiInteger.get(id);
    }

    public List<String> getPoiNamesSortedList() {
        return mNameList;
    }

    public List<Integer> getPoiIdList() {
        return mIdList;
    }
}
