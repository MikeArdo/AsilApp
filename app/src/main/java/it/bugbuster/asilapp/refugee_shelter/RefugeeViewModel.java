package it.bugbuster.asilapp.refugee_shelter;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.bugbuster.asilapp.entity.RefugeeShelter;

public class RefugeeViewModel extends ViewModel {
    private RefugeeShelterFirebase refugeeShelterFirebase;
    private LiveData<List<RefugeeShelter>> refugeeShelters;

    public RefugeeViewModel() {
        refugeeShelterFirebase = new RefugeeShelterFirebase();
    }

    public LiveData<List<RefugeeShelter>> getRefugeeShelter(Context context, String filename) {
        return refugeeShelterFirebase.fetchRefugeeShelter(context, filename);
    }


}
