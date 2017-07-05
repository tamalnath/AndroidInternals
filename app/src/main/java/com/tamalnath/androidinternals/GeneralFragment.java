package com.tamalnath.androidinternals;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;
import java.util.TreeMap;

public class GeneralFragment extends Fragment {

    private static final Map<String, Object> BUILD = Utils.findConstants(Build.class, null, null);
    private Adapter adapter = new Adapter();
    private boolean details = true;
    private Intent batteryStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = getContext().registerReceiver(null, intentFilter);

        final RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleView();
            }
        });
        toggleView();
        return recyclerView;
    }

    void toggleView() {
        adapter.getDataList().clear();
        details = !details;
        if (details) {
            adapter.addHeader(getString(R.string.general_battery));
            adapter.addMap(getBatteryInformation());
            adapter.addHeader(getString(R.string.general_build));
            adapter.addMap(BUILD);
            adapter.addHeader(getString(R.string.general_environment));
            adapter.addMap(System.getenv());
            adapter.addHeader(getString(R.string.general_properties));
            adapter.addMap(System.getProperties());
        } else {
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float charge = level * 100 / scale;
                adapter.addKeyValue(getString(R.string.general_battery), String.valueOf(charge) + "%");
            }
            adapter.addKeyValue("BRAND", Build.BRAND);
            adapter.addKeyValue("MODEL", Build.MODEL);
        }
        adapter.notifyDataSetChanged();
    }

    private Map<String, ?> getBatteryInformation() {
        Map<String, Object> map = new TreeMap<>();
        if (batteryStatus == null) {
            return map;
        }
        map.put(BatteryManager.EXTRA_PRESENT,
                batteryStatus.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false));

        int key = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String value = Utils.findConstant(BatteryManager.class, key, "BATTERY_STATUS_(.*)");
        map.put(BatteryManager.EXTRA_STATUS, value);

        key = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        value = Utils.findConstant(BatteryManager.class, key, "BATTERY_HEALTH_(.*)");
        map.put(BatteryManager.EXTRA_HEALTH, value);

        value = Build.UNKNOWN;
        key = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if (key > 0) {
            value = Utils.findConstant(BatteryManager.class, key, "BATTERY_PLUGGED_(.*)");
        } else if (key == 0) {
            value = "Unplugged";
        }
        map.put(BatteryManager.EXTRA_PLUGGED, value);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        map.put(BatteryManager.EXTRA_LEVEL, level);

        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        map.put(BatteryManager.EXTRA_SCALE, scale);

        int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        map.put(BatteryManager.EXTRA_VOLTAGE, (voltage / 1000f) + "V");

        float temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f;
        map.put(BatteryManager.EXTRA_TEMPERATURE, temperature + getString(R.string.sensor_unit_deg));

        String technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        map.put(BatteryManager.EXTRA_TECHNOLOGY, technology);

        return map;
    }
}
