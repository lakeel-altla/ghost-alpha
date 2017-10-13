package com.lakeel.altla.ghost.alpha.location;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import android.app.Activity;
import android.content.IntentSender;
import android.support.annotation.NonNull;

public class LocationSettingsChecker {

    private LocationSettingsChecker() {
    }

    public static void checkLocationSettings(@NonNull final Activity activity,
                                             @NonNull final LocationSettingsRequest locationSettingsRequest,
                                             final int resolutionRequestCode,
                                             @NonNull final LocationSettingsCallbacks callbacks) {
        // See: https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(activity);
        settingsClient.checkLocationSettings(locationSettingsRequest)
                      .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                          @Override
                          public void onSuccess(LocationSettingsResponse response) {
                              // All location settings are satisfied. The client can initialize location
                              // requests here.
                              callbacks.onLocationSettingsSatisfied();
                          }
                      })
                      .addOnFailureListener(activity, new OnFailureListener() {
                          @Override
                          public void onFailure(@NonNull Exception e) {
                              int statusCode = ((ApiException) e).getStatusCode();
                              switch (statusCode) {
                                  case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                      // Location settings are not satisfied. But could be fixed by showing the
                                      // user a dialog.
                                      try {
                                          // Show the dialog by calling startResolutionForResult(),
                                          // and check the result in onActivityResult().
                                          ResolvableApiException rae = (ResolvableApiException) e;
                                          rae.startResolutionForResult(activity, resolutionRequestCode);
                                      } catch (IntentSender.SendIntentException sie) {
                                          // Ignore the error.
                                      }
                                      break;
                                  default:
                                      // Location settings are not satisfied. However, we have no way to fix the
                                      // settings so we won't show the dialog.
                                      callbacks.onLocationSettingsNeverFixed();
                                      break;
                              }
                          }
                      });
    }

    public interface LocationSettingsCallbacks {

        void onLocationSettingsSatisfied();

        void onLocationSettingsNeverFixed();
    }
}
