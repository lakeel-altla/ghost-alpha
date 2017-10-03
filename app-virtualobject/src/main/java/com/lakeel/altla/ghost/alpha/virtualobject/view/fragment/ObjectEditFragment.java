package com.lakeel.altla.ghost.alpha.virtualobject.view.fragment;

import com.lakeel.altla.ghost.alpha.virtualobject.R;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class ObjectEditFragment extends Fragment {

    private FragmentContext fragmentContext;

    @NonNull
    public static ObjectEditFragment newInstance() {
        return new ObjectEditFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentContext = (FragmentContext) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentContext = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_object_edit, container, false);
    }

    public interface FragmentContext {

    }
}
