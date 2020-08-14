package org.techtown.direcord;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import java.util.Map;

public class FragmentDataSender {

    public static void sendData(Map<String, String> sendMap, Fragment fragment){
        Bundle bundle = new Bundle();
        for(Map.Entry<String, String> e : sendMap.entrySet())
             bundle.putString(e.getKey(), e.getValue());
        fragment.setArguments(bundle);
    }
}
