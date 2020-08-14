package org.techtown.direcord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FragmentRecord extends Fragment {

    public MediaRecorder recorder;
    Thread mRecordThread = null;
    boolean isRecording = false;
    AudioRecord mAudioRecord;
    public MediaPlayer player;
    private String path;
    private String fileName;
    String mFilepath;
    int source = MediaRecorder.AudioSource.MIC;
    int rate = 44100;
    int cCnt = AudioFormat.CHANNEL_IN_STEREO;
    int format = AudioFormat.ENCODING_PCM_FLOAT;
    int bSize;
    boolean isStop = true;

    ViewGroup viewGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.activity_record, container, false);


        TextView filename_textView = (TextView) viewGroup.findViewById(R.id.filename_recordpage);
        final String path = getArguments().getString(ResourceConst.FILE_NAME_KEY) + ".pcm";
        final String lang = getArguments().getString(ResourceConst.LANGUAGE_KEY);
        Log.d("pppthhh ", path);
        if (path != null) {
            fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
            filename_textView.setText(fileName);
        }
        viewGroup.findViewById(R.id.record_btn).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)//stopRecording때문
            @Override
            public void onClick(View view) {
                view.setSelected(!view.isSelected());

                if (view.isSelected()) {


                    onRecord(viewGroup);
                    //Handle selected state change//녹음시작
                    //   recordAudio(path);
                } else {
                    onRecord(viewGroup);
                    //Handle de-select state change//녹음멈춤
                    // stopRecording();
                }
            }

        });


        Button save = (Button) viewGroup.findViewById(R.id.save);
        audioInit();
        audioRecord(path);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stopandsave();
                isStop = false;
                mAudioRecord.release();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                File recordFile = new File(path);
                final String uid = MainActivity.getUid();
                String uploadPath = FileManager.getInstance().upload(recordFile);
                Log.d("file Name ", uploadPath);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String baseUrl = "https://direcord-283711.dt.r.appspot.com/speech/analysis/";
                            String url = baseUrl + fileName + "/";
                            Log.d("[url] ", url);

                            Http sttRequest = new Http(url);

                            Map<String, String> paramMap = new HashMap<>();
                            paramMap.put("minSpeakerCnt", "1");
                            paramMap.put("maxSpeakerCnt", "8");
                            paramMap.put("language", lang);
                            Log.d("lang ", lang);
                            sttRequest.setParameters(paramMap);

                            String folder = path.substring(0, path.lastIndexOf("/"));
                            File file = new File(folder + "/" + fileName + ".txt");
                            FileOutputStream fos = new FileOutputStream(file);
                            String stt = sttRequest.executeGet();
                            Log.d("[stt] ", stt);
                            fos.write(stt.getBytes());
                            fos.close();

                        } catch (InterruptedException | FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                Toast.makeText((BottomMainActivity) getActivity(), "저장했습니다:)", Toast.LENGTH_LONG).show();
                close();
                ((BottomMainActivity) getActivity()).replaceFragment(new FragmentRecordSetting());
            }
        });
        return viewGroup;
    }

    private void close() {
        path = "";
        fileName = "";
    }

    private void audioInit() {
        recorder = new MediaRecorder();


        bSize = AudioRecord.getMinBufferSize(rate, cCnt, format);
        mAudioRecord = new AudioRecord(source, rate, cCnt, format, bSize);
        mAudioRecord.startRecording();
    }

    private void audioRecord(final String path) {
        mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[bSize];
                mFilepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record.pcm";
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(path);
                    Log.d("fox is null. ", "" + (fos == null));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


                while (isStop) {
                    mAudioRecord.startRecording();
                    while (isRecording) {
                        int ret = mAudioRecord.read(readData, 0, bSize);  //  AudioRecord의 read 함수를 통해 pcm data 를 읽어옴
                        Log.d("MainActivity", "read bytes is " + ret);

                        try {
                            fos.write(readData, 0, bSize);    //  읽어온 readData 를 파일에 write 함
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mAudioRecord.stop();
                }

                mAudioRecord = null;

                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void recordAudio(String path) {


        recorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 어디에서 음성 데이터를 받을 것인지
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 압축 형식 설정
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        Log.d("path is null", (path == null) + "");
        recorder.setOutputFile(path);

        try {
            recorder.prepare();
            recorder.start();

            Toast.makeText(((BottomMainActivity) getActivity()), "녹음 시작됨.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)//? pause땜에..
    public void stopRecording() {


        if (recorder != null) {
            recorder.pause(); //됨..?

            Toast.makeText(((BottomMainActivity) getActivity()), "녹음 중지됨.", Toast.LENGTH_LONG).show();
        }
    }

    public void stopandsave() {

        if (recorder != null) {
            recorder.stop(); //
            recorder.release();
            recorder = null;

//            Toast.makeText(getApplicationContext(), "녹음 중지됨.", Toast.LENGTH_LONG).show();
        }


    }

    public void playAudio() {
        try {
            closePlayer(); //항상 처음시작에 죽임

            player = new MediaPlayer();
            //player.setDataSource(url); //인터넷에서 가져와서 플레이
            player.setDataSource(((BottomMainActivity) getActivity()), Uri.parse(path)); //녹음된파일 플레이
            player.prepare();
            player.start();

            Toast.makeText(((BottomMainActivity) getActivity()), "재생 시작됨.", Toast.LENGTH_LONG).show(); //메세지띄움
        } catch (Exception e) {
            Log.e("SampleAudioRecorder", "Audio play failed.", e);
        }
    }

    public void stopAudio() { //재시작
        if (player != null && player.isPlaying()) {
            closePlayer();

            Toast.makeText(((BottomMainActivity) getActivity()), "정지됨.", Toast.LENGTH_LONG).show();
        }
    }

    public void closePlayer() { //항상 처음시작에 죽임
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
    int idx = 0;
    public void onRecord(View view) {
        if (isRecording == true) {
            isRecording = false;
        } else {
            isRecording = true;

            if(idx == 0) {
                if (mAudioRecord == null) {
                    mAudioRecord = new AudioRecord(source, rate, cCnt, format, bSize);
                    mAudioRecord.startRecording();
                }
                mRecordThread.start();
                idx++;
            }
        }

    }
}
//package org.techtown.direcord;
//
//import android.media.MediaPlayer;
//import android.media.MediaRecorder;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
//import androidx.fragment.app.Fragment;
//
//import java.io.IOException;
//
//public class FragmentRecord extends Fragment {
//
//    public MediaRecorder recorder;
//    public MediaPlayer player;
//    String path;
//
//    ViewGroup viewGroup;
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        viewGroup = (ViewGroup) inflater.inflate(R.layout.activity_record,container,false);
//
//
//        TextView filename_textView = (TextView) viewGroup.findViewById(R.id.filename_recordpage);
//        final String path =  getArguments().getString(ResourceConst.FILE_NAME_KEY);
//        if(path != null){
//            String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
//            filename_textView.setText(fileName);
//        }
//        viewGroup.findViewById(R.id.record_btn).setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.N)//stopRecording때문
//            @Override
//            public void onClick(View view) {
//                view.setSelected(!view.isSelected());
//
//                if (view.isSelected()) {
//                    //Handle selected state change//녹음시작
//                    recordAudio(path);
//                }
//                else {
//                    //Handle de-select state change//녹음멈춤
//                    stopRecording();
//                }
//            }
//
//        });
//
//
//        Button save = (Button) viewGroup.findViewById(R.id.save);
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                stopandsave();
//                ((BottomMainActivity)getActivity()).replaceFragment(new FragmentRecordSetting());
//
//                String baseUrl = "https://direcord-283711.dt.r.appspot.com/";
////                String urlPath = "speech/analysis/meeting.mp3/?";
////                urlPath += "gs://direcord-283711.appspot.com/?";
////                String parameter = "minSpeakerCnt=4&maxSpeakerCnt=4";
//                String urlPath = "/login/";
////                String url = baseUrl + urlPath + parameter;
//                String url = baseUrl + urlPath;
//                Log.d("url : ", url);
//                Http sttRequest = new Http(url);
//                try {
//                    sttRequest.executeGet();
//                    Log.d("stt result ", sttRequest.getResponse());
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Toast.makeText((BottomMainActivity)getActivity(), "저장했습니다:)", Toast.LENGTH_LONG).show();
//
//            }
//        });
//        return viewGroup;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public void recordAudio(String path) {
//        recorder = new MediaRecorder();
//
//        recorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 어디에서 음성 데이터를 받을 것인지
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 압축 형식 설정
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//
//        Log.d("path is null", (path == null) + "");
//        recorder.setOutputFile(path);
//
//        try {
//            recorder.prepare();
//            recorder.start();
//
//            Toast.makeText(((BottomMainActivity)getActivity()), "녹음 시작됨.", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)//? pause땜에..
//    public void stopRecording() {
//
//
//        if (recorder != null) {
//            recorder.pause(); //됨..?
//
//            Toast.makeText(((BottomMainActivity)getActivity()), "녹음 중지됨.", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    public void stopandsave() {
//
//        if (recorder != null) {
//            recorder.stop(); //
//            recorder.release();
//            recorder = null;
//
////            Toast.makeText(getApplicationContext(), "녹음 중지됨.", Toast.LENGTH_LONG).show();
//        }
//
//
//    }
//
//    public void playAudio() {
//        try {
//            closePlayer(); //항상 처음시작에 죽임
//
//            player = new MediaPlayer();
//            //player.setDataSource(url); //인터넷에서 가져와서 플레이
//            player.setDataSource(((BottomMainActivity)getActivity()), Uri.parse(path)); //녹음된파일 플레이
//            player.prepare();
//            player.start();
//
//            Toast.makeText(((BottomMainActivity)getActivity()), "재생 시작됨.", Toast.LENGTH_LONG).show(); //메세지띄움
//        } catch (Exception e) {
//            Log.e("SampleAudioRecorder", "Audio play failed.", e);
//        }
//    }
//
//    public void stopAudio() { //재시작
//        if (player != null && player.isPlaying()) {
//            closePlayer();
//
//            Toast.makeText(((BottomMainActivity)getActivity()), "정지됨.", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    public void closePlayer() { //항상 처음시작에 죽임
//        if (player != null) {
//            player.stop();
//            player.release();
//            player = null;
//        }
//    }
//
//}


