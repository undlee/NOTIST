package org.techtown.direcord;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FragmentRecordSetting extends Fragment {

    FragmentRecord fragmentRecord;
    View view;
    EditText edit_filename ;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_recordsetting,container,false);
        edit_filename = (EditText) view.findViewById(R.id.edit_filename);
        Button button = (Button) view.findViewById(R.id.makeroom);
        ToggleButton languageBtn = view.findViewById(R.id.toggleButton);
        final String lang = languageBtn.getText().toString().equals("영어") ? "en-US" : "ko-KR";

        fragmentRecord = new FragmentRecord();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = edit_filename.getText().toString();

                File file = new File(((BottomMainActivity)getActivity()).getFilesDir(), fileName);
                String path = file.getAbsolutePath();
                Map<String, String> sendMap = new HashMap<>();
                sendMap.put(ResourceConst.FILE_NAME_KEY, path);
                sendMap.put(ResourceConst.LANGUAGE_KEY, lang);
                FragmentDataSender.sendData(sendMap, fragmentRecord);
                Log.d("path ", path);
                Log.d("lang ", lang);
                permissionCheck();
                ((BottomMainActivity)getActivity()).replaceFragment(fragmentRecord);
            }
        });

        return view;
    }


    public void permissionCheck() {
        if (ContextCompat.checkSelfPermission(((BottomMainActivity)getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(((BottomMainActivity)getActivity()), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(((BottomMainActivity)getActivity()), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

}
