package it.bugbuster.asilapp.refugee_shelter;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import it.bugbuster.asilapp.entity.RefugeeShelter;
import it.bugbuster.asilapp.utils.JsonUtils;

public class RefugeeShelterFirebase {
    private FirebaseFirestore db;

    public RefugeeShelterFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<RefugeeShelter>> fetchRefugeeShelter(Context context, String filename) {
        MutableLiveData<List<RefugeeShelter>> liveData = new MutableLiveData<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("refugee_shelter") // Collezione Firestore
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<RefugeeShelter> localShelters = JsonUtils.parseRefugeeShelters(context, filename);
                        if (task.isSuccessful()) {
                            List<RefugeeShelter> firestoreShelters = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                RefugeeShelter shelter = document.toObject(RefugeeShelter.class);
                                firestoreShelters.add(shelter);
                            }


                            if (!firestoreShelters.isEmpty()) {
                                if (!firestoreShelters.equals(localShelters)) {
                                    Log.d("Firestore Update", "I dati di Firestore sono aggiornati. Sovrascrivendo i dati locali.");
                                    JsonUtils.overwriteLocalData(context, firestoreShelters, filename);
                                    liveData.setValue(firestoreShelters);
                                } else {
                                    Log.d("Firestore Update", "I dati locali sono già aggiornati.");
                                    liveData.setValue(localShelters);
                                }
                            } else {
                                Log.d("Firestore Update", "Nessun dato locale trovato, usando i dati di Firestore.");
                                liveData.setValue(localShelters);
                            }
                        } else {
                            Log.d("Firestore Error", "Errore nel recupero dei dati");
                            liveData.setValue(localShelters);
                        }
                    }
                });
        return liveData;
    }
}
