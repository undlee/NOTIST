package org.techtown.direcord;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class FileManager {

    private static FileManager fileManager = new FileManager();
    private static StorageReference storage;

    private FileManager() {}
    public static FileManager getInstance() {
        return fileManager;
    }

    /**
     *
     * @param uid
     * @param file
     * @return upload 후에 해당 file의 gcsUri 위치
     */
    public static String upload(File file) {

        // Create a storage reference from our app
        StorageReference rootStorage = MainActivity.firebaseStorage.getReference();

        String path = file.getName();

        storage = rootStorage.child(path);

        Uri uri = Uri.fromFile(file);
        storage.putFile(uri);
        return path;
    }

}
