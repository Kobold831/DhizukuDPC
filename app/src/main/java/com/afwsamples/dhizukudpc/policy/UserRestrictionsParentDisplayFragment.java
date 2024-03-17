package com.afwsamples.dhizukudpc.policy;

import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.afwsamples.dhizukudpc.DevicePolicyManagerGateway;
import com.afwsamples.dhizukudpc.DevicePolicyManagerGatewayImpl;
import com.afwsamples.dhizukudpc.R;
import com.afwsamples.dhizukudpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.dhizukudpc.common.preference.DpcPreferenceBase;
import com.afwsamples.dhizukudpc.common.preference.DpcSwitchPreference;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class UserRestrictionsParentDisplayFragment extends BaseSearchablePolicyPreferenceFragment
    implements Preference.OnPreferenceChangeListener {
  private static final String TAG = "UserRestrictionsParent";

  private DevicePolicyManagerGateway mDevicePolicyManagerGateway;

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  public void onCreate(Bundle savedInstanceState) {
    mDevicePolicyManagerGateway = DevicePolicyManagerGatewayImpl.forParentProfile(requireActivity());
    super.onCreate(savedInstanceState);
    Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.user_restrictions_management_title);
  }

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    PreferenceScreen preferenceScreen =
        getPreferenceManager().createPreferenceScreen(getPreferenceManager().getContext());
    setPreferenceScreen(preferenceScreen);

    final Context preferenceContext = getPreferenceManager().getContext();
    for (UserRestriction restriction : UserRestriction.PROFILE_OWNER_ORG_DEVICE_RESTRICTIONS) {
      DpcSwitchPreference preference = new DpcSwitchPreference(preferenceContext);
      preference.setTitle(restriction.titleResId);
      preference.setKey(restriction.key);
      preference.setOnPreferenceChangeListener(this);
      preferenceScreen.addPreference(preference);
    }

    updateAllUserRestrictions();
    constrainPreferences();
  }

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  public void onResume() {
    super.onResume();
    updateAllUserRestrictions();
  }

  @Override
  public boolean isAvailable(Context context) {
    return true;
  }

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    String restriction = preference.getKey();
    boolean enabled = newValue.equals(true);

    try {
      mDevicePolicyManagerGateway.setUserRestriction(restriction, enabled);
      updateUserRestriction(restriction);
      return true;
    } catch (SecurityException e) {
      Toast.makeText(getActivity(), R.string.user_restriction_error_msg, Toast.LENGTH_SHORT).show();
      Log.e(TAG, "Error occurred while updating user restriction: " + restriction, e);
      return false;
    }
  }

  @RequiresApi(api = VERSION_CODES.R)
  private void updateAllUserRestrictions() {
    for (UserRestriction restriction : UserRestriction.PROFILE_OWNER_ORG_DEVICE_RESTRICTIONS) {
      updateUserRestriction(restriction.key);
    }
  }

  @RequiresApi(api = VERSION_CODES.R)
  private void updateUserRestriction(String userRestriction) {
    DpcSwitchPreference preference = (DpcSwitchPreference) findPreference(userRestriction);
    Set<String> restrictions = mDevicePolicyManagerGateway.getUserRestrictions();
    preference.setChecked(restrictions.contains(userRestriction));
  }

  private void constrainPreferences() {
    for (UserRestriction restriction : UserRestriction.PROFILE_OWNER_ORG_DEVICE_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction.key);
      if (Arrays.stream(UserRestriction.TM_PLUS_RESTRICTIONS).anyMatch(restriction.key::equals)) {
        pref.setMinSdkVersion(VERSION_CODES.TIRAMISU);
      } else if (Arrays.stream(UserRestriction.UDC_PLUS_RESTRICTIONS)
          .anyMatch(restriction.key::equals)) {
        pref.setMinSdkVersion(VERSION_CODES.UPSIDE_DOWN_CAKE);
      } else {
        pref.setMinSdkVersion(VERSION_CODES.R);
      }
    }
  }
}
