package com.lakeel.altla.ghost.alpha.mock.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.helper.FragmentHelper;
import com.lakeel.altla.ghost.alpha.mock.lib.animation.helper.OnDismissedListener;
import com.lakeel.altla.ghost.alpha.mock.lib.animation.helper.RevealAnimationHelper;
import com.lakeel.altla.ghost.alpha.mock.lib.animation.helper.RevealAnimationSettings;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class RegisterVirtualObjectFragment extends Fragment implements StepperLayout.StepperListener, OnDismissedListener {

    @BindView(R.id.stepperLayout)
    StepperLayout stepperLayout;

    private static final Log LOG = LogFactory.getLog(RegisterVirtualObjectFragment.class);

    private static final String BUNDLE_ANIMATION_SETTINGS = "animationSettings";

    private RevealAnimationSettings animationSettings;

    public static RegisterVirtualObjectFragment newInstance(int centerX, int centerY, int width, int height) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_ANIMATION_SETTINGS, Parcels.wrap(new RevealAnimationSettings(centerX, centerY, width, height)));

        RegisterVirtualObjectFragment fragment = new RegisterVirtualObjectFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_virtual_object, container, false);

        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        animationSettings = Parcels.unwrap(FragmentHelper.getArguments(this).getParcelable(BUNDLE_ANIMATION_SETTINGS));

        RevealAnimationHelper
                .startCircularRevealAnimation(
                        getContext(),
                        view,
                        animationSettings,
                        ContextCompat.getColor(getContext(), R.color.colorPrimary),
                        ContextCompat.getColor(getContext(), R.color.white));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        stepperLayout.setAdapter(new StepperAdapter(getFragmentManager(), getContext()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getView() != null) {
                    RevealAnimationHelper
                            .startCircularExitAnimation(
                                    getContext(),
                                    getView(),
                                    animationSettings,
                                    ContextCompat.getColor(getContext(), R.color.colorPrimary),
                                    ContextCompat.getColor(getContext(), R.color.white),
                                    this
                            );
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCompleted(View completeButton) {
    }

    @Override
    public void onError(VerificationError verificationError) {
    }

    @Override
    public void onStepSelected(int newStepPosition) {
    }

    @Override
    public void onReturn() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onDismissed() {
        getFragmentManager().popBackStack();
    }

    private static class StepperAdapter extends AbstractFragmentStepAdapter {

        StepperAdapter(@NonNull FragmentManager fm, @NonNull Context context) {
            super(fm, context);
        }

        @Override
        public Step createStep(int position) {
            switch (position) {
                case 0:
                    return InputUrlStepFragment.newInstance();
                case 1:
                    return SelectPlaceFragment.newInstance();
                case 2:
                    return InputUrlStepFragment.newInstance();
                default:
                    throw new RuntimeException("Not create step. Position is wrong:position=" + position);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @NonNull
        @Override
        public StepViewModel getViewModel(@IntRange(from = 0) int position) {
            switch (position) {
                case 0:
                    return new StepViewModel.Builder(context)
                            .setTitle(R.string.textView_type_url)
                            .create();
//                case 1:
//                    return new StepViewModel.Builder(context)
//                            .setTitle(R.string.title_select_place)
//                            .create();
                case 2:
                    return new StepViewModel.Builder(context)
                            .setTitle(R.string.title_create_object)
                            .create();
                default:
                    throw new RuntimeException("Not create view model of stepper. The position is wrong:position=" + position);
            }
        }
    }
}
