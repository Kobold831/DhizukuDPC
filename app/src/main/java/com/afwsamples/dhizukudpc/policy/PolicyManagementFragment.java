/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.afwsamples.dhizukudpc.policy;

import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;
import static com.afwsamples.dhizukudpc.common.Util.onErrorLog;
import static com.afwsamples.dhizukudpc.common.Util.onSuccessLog;
import static com.afwsamples.dhizukudpc.common.preference.DpcPreferenceHelper.NO_CUSTOM_CONSTRAINT;
import static com.afwsamples.dhizukudpc.util.Constants.*;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyManager.InstallSystemUpdateCallback;
import android.app.admin.SystemUpdateInfo;
import android.app.admin.WifiSsidPolicy;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiSsid;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.security.AppUriAuthenticationPolicy;
import android.security.KeyChain;
import android.service.notification.NotificationListenerService;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.afwsamples.dhizukudpc.AddAccountActivity;
import com.afwsamples.dhizukudpc.CrossProfileAppsAllowlistFragment;
import com.afwsamples.dhizukudpc.CrossProfileAppsFragment;
import com.afwsamples.dhizukudpc.DeviceAdminReceiver;
import com.afwsamples.dhizukudpc.DevicePolicyManagerGateway;
import com.afwsamples.dhizukudpc.DevicePolicyManagerGateway.FailedOperationException;
import com.afwsamples.dhizukudpc.DevicePolicyManagerGatewayImpl;
import com.afwsamples.dhizukudpc.R;
import com.afwsamples.dhizukudpc.SetupManagementActivity;
import com.afwsamples.dhizukudpc.common.AccountArrayAdapter;
import com.afwsamples.dhizukudpc.common.AppInfoArrayAdapter;
import com.afwsamples.dhizukudpc.common.CertificateUtil;
import com.afwsamples.dhizukudpc.common.MediaDisplayFragment;
import com.afwsamples.dhizukudpc.common.PackageInstallationUtils;
import com.afwsamples.dhizukudpc.common.ReflectionUtil;
import com.afwsamples.dhizukudpc.common.ReflectionUtil.ReflectionIsTemporaryException;
import com.afwsamples.dhizukudpc.common.UserArrayAdapter;
import com.afwsamples.dhizukudpc.common.Util;
import com.afwsamples.dhizukudpc.common.preference.DpcEditTextPreference;
import com.afwsamples.dhizukudpc.common.preference.DpcListPreference;
import com.afwsamples.dhizukudpc.common.preference.DpcPreference;
import com.afwsamples.dhizukudpc.common.preference.DpcPreferenceBase;
import com.afwsamples.dhizukudpc.common.preference.DpcPreferenceHelper;
import com.afwsamples.dhizukudpc.common.preference.DpcSwitchPreference;
import com.afwsamples.dhizukudpc.comp.BindDeviceAdminFragment;
import com.afwsamples.dhizukudpc.policy.blockuninstallation.BlockUninstallationInfoArrayAdapter;
import com.afwsamples.dhizukudpc.policy.certificate.DelegatedCertInstallerFragment;
import com.afwsamples.dhizukudpc.policy.keyguard.LockScreenPolicyFragment;
import com.afwsamples.dhizukudpc.policy.keyguard.PasswordConstraintsFragment;
import com.afwsamples.dhizukudpc.policy.keymanagement.GenerateKeyAndCertificateTask;
import com.afwsamples.dhizukudpc.policy.keymanagement.KeyGenerationParameters;
import com.afwsamples.dhizukudpc.policy.keymanagement.SignAndVerifyTask;
import com.afwsamples.dhizukudpc.policy.locktask.KioskModeActivity;
import com.afwsamples.dhizukudpc.policy.locktask.LockTaskAppInfoArrayAdapter;
import com.afwsamples.dhizukudpc.policy.locktask.SetLockTaskFeaturesFragment;
import com.afwsamples.dhizukudpc.policy.networking.AlwaysOnVpnFragment;
import com.afwsamples.dhizukudpc.policy.networking.NetworkUsageStatsFragment;
import com.afwsamples.dhizukudpc.policy.networking.PrivateDnsModeFragment;
import com.afwsamples.dhizukudpc.policy.resetpassword.ResetPasswordWithTokenFragment;
import com.afwsamples.dhizukudpc.policy.systemupdatepolicy.SystemUpdatePolicyFragment;
import com.afwsamples.dhizukudpc.policy.wifimanagement.WifiConfigCreationDialog;
import com.afwsamples.dhizukudpc.policy.wifimanagement.WifiEapTlsCreateDialogFragment;
import com.afwsamples.dhizukudpc.policy.wifimanagement.WifiModificationFragment;
import com.afwsamples.dhizukudpc.profilepolicy.ProfilePolicyManagementFragment;
import com.afwsamples.dhizukudpc.profilepolicy.addsystemapps.EnableSystemAppsByIntentFragment;
import com.afwsamples.dhizukudpc.profilepolicy.apprestrictions.AppRestrictionsManagingPackageFragment;
import com.afwsamples.dhizukudpc.profilepolicy.apprestrictions.ManagedConfigurationsFragment;
import com.afwsamples.dhizukudpc.profilepolicy.delegation.DelegationFragment;
import com.afwsamples.dhizukudpc.profilepolicy.permission.ManageAppPermissionsFragment;
import com.afwsamples.dhizukudpc.transferownership.PickTransferComponentFragment;
import com.afwsamples.dhizukudpc.util.Common;
import com.afwsamples.dhizukudpc.util.MainThreadExecutor;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener;
import com.rosan.dhizuku.shared.DhizukuVariables;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressLint("NewApi")
public class PolicyManagementFragment extends PreferenceFragmentCompat {

  private DevicePolicyManager mDevicePolicyManager;
  private DevicePolicyManagerGateway mDevicePolicyManagerGateway;
  private PackageManager mPackageManager;
  private String mPackageName;
  private ComponentName mAdminComponentName;
  private UserManager mUserManager;
  private TelephonyManager mTelephonyManager;
  private AccountManager mAccountManager;
  private GetAccessibilityServicesTask mGetAccessibilityServicesTask = null;
  private GetInputMethodsTask mGetInputMethodsTask = null;
  private GetNotificationListenersTask mGetNotificationListenersTask = null;
  private ShowCaCertificateListTask mShowCaCertificateListTask = null;
  private Uri mImageUri;
  private Uri mVideoUri;
  private boolean mIsProfileOwner;

  DpcPreference preAppStatus,
          preSecurityPatch,
          preAccessibility,
          preTime,
          preTimeZone,
          preScreenBrightness,
          preScreenOffTimeout,
          preProfileName,
          preOrganizationId,
          preManageOverrideApn,
          preCrossProfileCalendar,
          preDisableAccountManagement,
          preGetDisableAccountManagement,
          preAddAccount,
          preRemoveAccount,
          preEnableSystemApps,
          preEnableSystemAppsByPkgName,
          preEnableSystemAppsByIntent,
          preInstallExistingPkg,
          preInstallApkPkg,
          preUninstallPkg,
          preHideApps,
          preHideAppsParentControl,
          preUnHideApps,
          preUnHideAppsParentControl,
          preSuspendApps,
          preUnSuspendApps,
          preClearAppData,
          preKeepUninstalledPkg,
          preManagedConfigurations,
          preDisableMeteredData,
          preAppRestrictionsManagingPackage,
          preManageCertInstaller,
          preGenericDelegation,
          preBlockUninstallationBypPkg,
          preBlockUninstallationList,
          preCaptureImage,
          preCaptureVideo,
          preRequestManageCredentials,
          preInstallKeyCertificate,
          preRemoveKeyCertificate,
          preGenerateKeyAndCertificate,
          preTestKeyUsability,
          preInstallCaCertificate,
          preGetCaCertificates,
          preRemoveAllCaCertificates,
          preGrantKeyPairToApp,
          preCreateWifiConfiguration,
          preCreateEapTlsWifiConfiguration,
          preModifyWifiConfiguration,
          preModifyOwnedWifiConfiguration,
          preRemoveNotOwnedWifiConfigurations,
          preShowWifiMacAddress,
          preSetWifiMinSecurityLevel,
          preSetWifiSsidRestriction,
          preSetInputMethods,
          preSetInputMethodsOnParent,
          preSetNotificationListeners,
          preSetNotificationListenersText,
          prePasswordComplexity,
          preRequiredPasswordComplexity,
          prePasswordCompliant,
          preSeparateChallenge,
          preLockScreenPolicy,
          prePasswordConstraints,
          preResetPassword,
          preLockNow,
          preSetNewPassword,
          preSetProfileParentNewPassword,
          preSetProfileParentNewPasswordDeviceRequirement,
          preManageLockTask,
          preCheckLockTaskPermitted,
          preSetLockTaskFeatures,
          preStartLockTask,
          preRelaunchInLockTask,
          preStopLockTask,
          preManagedProfilePolicies,
          preBindDeviceAdminPolicies,
          preNetworkStats,
          preSetAlwaysOnVpn,
          preEnterpriseSlice,
          preSetGlobalHttpProxy,
          preClearGlobalHttpProxy,
          preSetPrivateDnsMode,
          preSetPermissionPolicy,
          preManageAppPermissions,
          preDisableStatusBar,
          preReEnableStatusBar,
          preDisableKeyguard,
          preReEnableKeyguard,
          preStartKioskMode,
          preSystemUpdatePolicy,
          preSystemUpdatePending,
          preManagedSystemUpdates,
          preCreateManagedProfile,
          preCreateAndManageUser,
          preRemoveUser,
          preSwitchUser,
          preStartUserInBackground,
          preStopUser,
          preLogoutUser,
          preSetUserSessionMessage,
          preSetAffiliationIds,
          preAffiliatedUser,
          preEphemeralUser,
          preSetUserRestrictions,
          preSetUserRestrictionsParent,
          preSetShortSupportMessage,
          preSetLongSupportMessage,
          preRequestSecurityLogs,
          preRequestPreRebootSecurityLogs,
          preRequestNetworkLogs,
          preRequestBugreport,
          preWipeData,
          preRemoveDeviceOwner,
          preReboot,
          preSetFactoryResetProtectionPolicy,
          preFactoryResetOrgOwnedDevice,
          preTransferOwnershipToComponent,
          preCrossProfileApps,
          preCrossProfileAppsAllowList,
          preNearbyNotificationStreaming,
          preNearbyAppStreaming,
          preMtePolicy;

  DpcSwitchPreference swAutoBrightness,
          swAppFeedbackNotification,
          swDisableCamera,
          swDisableCameraOnParent,
          swDisableScreenCapture,
          swDisableScreenCaptureOnParent,
          swMuteAudio,
          swEnableWifiConfigLockdown,
          swPreferentialNetworkServiceStatus,
          swEnableLogout,
          swStayOnWhilePluggedIn,
          swInstallNonMarketApps,
          swLocationEnabled,
          swLocationMode,
          swAutoTimeRequired,
          swAutoTime,
          swAutoTimeZone,
          swSecurityLogging,
          swNetworkLogging,
          swBackupService,
          swCriteriaMode,
          swUsbDataSignaling,
          swSuspendPersonalApps;

  DpcEditTextPreference etOverrideKeySelection,
          etDeviceOrganizationName,
          etProfileMaxTimeOff;

  DpcListPreference listNewPasswordWithComplexity,
          listRequiredPasswordComplexity,
          listRequiredPasswordComplexityOnParent;

  interface ManageLockTaskListCallback {
    void onPositiveButtonClicked(String[] lockTaskArray);
  }

  private void onSuccessShowToast(String method, int msgId, Object... args) {
    showToast(msgId, args);
  }

  private void onSuccessShowToastWithHardcodedMessage(String format, Object... args) {
    showToast(String.format(format, args));
  }

  private void onErrorShowToast(int msgId, Object... args) {
    showToast(msgId, args);
  }

  private void onErrorShowToast(String method, Exception e, int msgId, Object... args) {
    showToast(msgId, args);
  }

  private void onErrorOrFailureShowToast(String method, Exception e, int failureMsgId, int errorMsgId) {
    if (e instanceof FailedOperationException) {
      showToast(failureMsgId);
    } else {
      showToast(errorMsgId);
    }
  }

  private void showToast(int msgId, Object... args) {
    showToast(getString(msgId, args));
  }

  private void showToast(String msg) {
    showToast(msg, Toast.LENGTH_SHORT);
  }

  private void showToast(String msg, int duration) {
    if (requireActivity().isFinishing()) {
      return;
    }

    Toast.makeText(requireActivity(), msg, duration).show();
  }

  /* フラグメント切り替え */
  private void showFragment(final Fragment fragment) {
    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
    fragmentManager
            .beginTransaction()
            .addToBackStack(PolicyManagementFragment.class.getName())
            .replace(R.id.container, fragment)
            .commit();
  }

  /* フラグメント切り替え */
  private void showFragment(final Fragment fragment, String tag) {
    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
    fragmentManager
            .beginTransaction()
            .addToBackStack(PolicyManagementFragment.class.getName())
            .replace(R.id.container, fragment, tag)
            .commit();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    if (!Common.isDhizukuActive(requireActivity())) {
      DevicePolicyManager dpm = requireActivity().getSystemService(DevicePolicyManager.class);
      if (dpm.isDeviceOwnerApp(requireActivity().getPackageName())) {
        dpm.clearDeviceOwnerApp(requireActivity().getPackageName());
        showToast("デバイスオーナーがDhizuku DPCに設定されていたため解除しました");
      }
      super.onCreate(savedInstanceState);
      return;
    }

    if (!((DevicePolicyManager) requireActivity().getSystemService(Context.DEVICE_POLICY_SERVICE)).isAdminActive(new ComponentName(requireActivity(), DeviceAdminReceiver.class))) {
      new MaterialAlertDialogBuilder(requireActivity())
              .setCancelable(false)
              .setMessage("Dhizuku DPCを端末管理アプリに設定してください")
              .show();
    }

    Context context = requireActivity();

    mAdminComponentName = DhizukuVariables.COMPONENT_NAME;
    mDevicePolicyManager = Common.binderWrapperDevicePolicyManager(requireActivity());
    mUserManager = context.getSystemService(UserManager.class);
    mPackageManager = context.getPackageManager();
    mDevicePolicyManagerGateway = new DevicePolicyManagerGatewayImpl(mDevicePolicyManager, mUserManager, mPackageManager, context.getSystemService(LocationManager.class), mAdminComponentName);
    mIsProfileOwner = mDevicePolicyManagerGateway.isProfileOwnerApp();
    mTelephonyManager = context.getSystemService(TelephonyManager.class);
    mAccountManager = AccountManager.get(context);
    mPackageName = DhizukuVariables.PACKAGE_NAME;

    mImageUri = getStorageUri("image.jpg");
    mVideoUri = getStorageUri("video.mp4");

    super.onCreate(savedInstanceState);
  }

  /* 再表示 */
  @Override
  public void onResume() {
    super.onResume();
    Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.policies_management);
    /* プレファレンス初期化 */
    initializePre();
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.device_policy_header);

    preAppStatus = findPreference(APP_STATUS_KEY);
    preSecurityPatch = findPreference(SECURITY_PATCH_KEY);
    preAccessibility = findPreference(SET_ACCESSIBILITY_SERVICES_KEY);
    preTime = findPreference(SET_TIME_KEY);
    preTimeZone = findPreference(SET_TIME_ZONE_KEY);
    preScreenBrightness = findPreference(SET_SCREEN_BRIGHTNESS_KEY);
    swAutoBrightness = findPreference(AUTO_BRIGHTNESS_KEY);
    preScreenOffTimeout = findPreference(SET_SCREEN_OFF_TIMEOUT_KEY);
    preProfileName = findPreference(SET_PROFILE_NAME_KEY);
    preOrganizationId = findPreference(SET_ORGANIZATION_ID_KEY);
    preManageOverrideApn = findPreference(MANAGE_OVERRIDE_APN_KEY);
    preCrossProfileCalendar = findPreference(CROSS_PROFILE_CALENDAR_KEY);
    preDisableAccountManagement = findPreference(SET_DISABLE_ACCOUNT_MANAGEMENT_KEY);
    preGetDisableAccountManagement = findPreference(GET_DISABLE_ACCOUNT_MANAGEMENT_KEY);
    preAddAccount = findPreference(ADD_ACCOUNT_KEY);
    preRemoveAccount = findPreference(REMOVE_ACCOUNT_KEY);
    preEnableSystemApps = findPreference(ENABLE_SYSTEM_APPS_KEY);
    preEnableSystemAppsByPkgName = findPreference(ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY);
    preEnableSystemAppsByIntent = findPreference(ENABLE_SYSTEM_APPS_BY_INTENT_KEY);
    preInstallExistingPkg = findPreference(INSTALL_EXISTING_PACKAGE_KEY);
    preInstallApkPkg = findPreference(INSTALL_APK_PACKAGE_KEY);
    preUninstallPkg = findPreference(UNINSTALL_PACKAGE_KEY);
    preHideApps = findPreference(HIDE_APPS_KEY);
    preHideAppsParentControl = findPreference(HIDE_APPS_PARENT_KEY);
    preUnHideApps = findPreference(UNHIDE_APPS_KEY);
    preUnHideAppsParentControl = findPreference(UNHIDE_APPS_PARENT_KEY);
    preSuspendApps = findPreference(SUSPEND_APPS_KEY);
    preUnSuspendApps = findPreference(UNSUSPEND_APPS_KEY);
    preClearAppData = findPreference(CLEAR_APP_DATA_KEY);
    preKeepUninstalledPkg = findPreference(KEEP_UNINSTALLED_PACKAGES);
    preManagedConfigurations = findPreference(MANAGED_CONFIGURATIONS_KEY);
    preDisableMeteredData = findPreference(DISABLE_METERED_DATA_KEY);
    swAppFeedbackNotification = findPreference(APP_FEEDBACK_NOTIFICATIONS);
    preAppRestrictionsManagingPackage = findPreference(APP_RESTRICTIONS_MANAGING_PACKAGE_KEY);
    preManageCertInstaller = findPreference(DELEGATED_CERT_INSTALLER_KEY);
    preGenericDelegation = findPreference(GENERIC_DELEGATION_KEY);
    preBlockUninstallationBypPkg = findPreference(BLOCK_UNINSTALLATION_BY_PKG_KEY);
    preBlockUninstallationList = findPreference(BLOCK_UNINSTALLATION_LIST_KEY);
    swDisableCamera = findPreference(DISABLE_CAMERA_KEY);
    swDisableCameraOnParent = findPreference(DISABLE_CAMERA_ON_PARENT_KEY);
    preCaptureImage = findPreference(CAPTURE_IMAGE_KEY);
    preCaptureVideo = findPreference(CAPTURE_VIDEO_KEY);
    swDisableScreenCapture = findPreference(DISABLE_SCREEN_CAPTURE_KEY);
    swDisableScreenCaptureOnParent = findPreference(DISABLE_SCREEN_CAPTURE_ON_PARENT_KEY);
    swMuteAudio = findPreference(MUTE_AUDIO_KEY);
    preRequestManageCredentials = findPreference(REQUEST_MANAGE_CREDENTIALS_KEY);
    preInstallKeyCertificate = findPreference(INSTALL_KEY_CERTIFICATE_KEY);
    preRemoveKeyCertificate = findPreference(REMOVE_KEY_CERTIFICATE_KEY);
    etOverrideKeySelection = findPreference(OVERRIDE_KEY_SELECTION_KEY);
    preGenerateKeyAndCertificate = findPreference(GENERATE_KEY_CERTIFICATE_KEY);
    preTestKeyUsability = findPreference(TEST_KEY_USABILITY_KEY);
    preInstallCaCertificate = findPreference(INSTALL_CA_CERTIFICATE_KEY);
    preGetCaCertificates = findPreference(GET_CA_CERTIFICATES_KEY);
    preRemoveAllCaCertificates = findPreference(REMOVE_ALL_CERTIFICATES_KEY);
    preGrantKeyPairToApp = findPreference(GRANT_KEY_PAIR_TO_APP_KEY);
    preCreateWifiConfiguration = findPreference(CREATE_WIFI_CONFIGURATION_KEY);
    preCreateEapTlsWifiConfiguration = findPreference(CREATE_EAP_TLS_WIFI_CONFIGURATION_KEY);
    swEnableWifiConfigLockdown = findPreference(WIFI_CONFIG_LOCKDOWN_ENABLE_KEY);
    preModifyWifiConfiguration = findPreference(MODIFY_WIFI_CONFIGURATION_KEY);
    preModifyOwnedWifiConfiguration = findPreference(MODIFY_OWNED_WIFI_CONFIGURATION_KEY);
    preRemoveNotOwnedWifiConfigurations = findPreference(REMOVE_NOT_OWNED_WIFI_CONFIGURATION_KEY);
    preShowWifiMacAddress = findPreference(SHOW_WIFI_MAC_ADDRESS_KEY);
    preSetWifiMinSecurityLevel = findPreference(SET_WIFI_MIN_SECURITY_LEVEL_KEY);
    preSetWifiSsidRestriction = findPreference(SET_WIFI_SSID_RESTRICTION_KEY);
    preSetInputMethods = findPreference(SET_INPUT_METHODS_KEY);
    preSetInputMethodsOnParent = findPreference(SET_INPUT_METHODS_ON_PARENT_KEY);
    preSetNotificationListeners = findPreference(SET_NOTIFICATION_LISTENERS_KEY);
    preSetNotificationListenersText = findPreference(SET_NOTIFICATION_LISTENERS_TEXT_KEY);
    prePasswordComplexity = findPreference(PASSWORD_COMPLEXITY_KEY);
    preRequiredPasswordComplexity = findPreference(REQUIRED_PASSWORD_COMPLEXITY_KEY);
    prePasswordCompliant = findPreference(PASSWORD_COMPLIANT_KEY);
    preSeparateChallenge = findPreference(SEPARATE_CHALLENGE_KEY);
    preLockScreenPolicy = findPreference(LOCK_SCREEN_POLICY_KEY);
    prePasswordConstraints = findPreference(PASSWORD_CONSTRAINTS_KEY);
    preResetPassword = findPreference(RESET_PASSWORD_KEY);
    preLockNow = findPreference(LOCK_NOW_KEY);
    preSetNewPassword = findPreference(SET_NEW_PASSWORD);
    listNewPasswordWithComplexity = findPreference(SET_NEW_PASSWORD_WITH_COMPLEXITY);
    listRequiredPasswordComplexity = findPreference(SET_REQUIRED_PASSWORD_COMPLEXITY);
    listRequiredPasswordComplexityOnParent = findPreference(SET_REQUIRED_PASSWORD_COMPLEXITY_ON_PARENT);
    preSetProfileParentNewPassword = findPreference(SET_PROFILE_PARENT_NEW_PASSWORD);
    preSetProfileParentNewPasswordDeviceRequirement = findPreference(SET_PROFILE_PARENT_NEW_PASSWORD_DEVICE_REQUIREMENT);
    preManageLockTask = findPreference(MANAGE_LOCK_TASK_LIST_KEY);
    preCheckLockTaskPermitted = findPreference(CHECK_LOCK_TASK_PERMITTED_KEY);
    preSetLockTaskFeatures = findPreference(SET_LOCK_TASK_FEATURES_KEY);
    preStartLockTask = findPreference(START_LOCK_TASK);
    preRelaunchInLockTask = findPreference(RELAUNCH_IN_LOCK_TASK);
    preStopLockTask = findPreference(STOP_LOCK_TASK);
    preManagedProfilePolicies = findPreference(MANAGED_PROFILE_SPECIFIC_POLICIES_KEY);
    preBindDeviceAdminPolicies = findPreference(BIND_DEVICE_ADMIN_POLICIES);
    preNetworkStats = findPreference(NETWORK_STATS_KEY);
    preSetAlwaysOnVpn = findPreference(SET_ALWAYS_ON_VPN_KEY);
    swPreferentialNetworkServiceStatus = findPreference(SET_GET_PREFERENTIAL_NETWORK_SERVICE_STATUS);
    preEnterpriseSlice = findPreference(ENTERPRISE_SLICE_KEY);
    preSetGlobalHttpProxy = findPreference(SET_GLOBAL_HTTP_PROXY_KEY);
    preClearGlobalHttpProxy = findPreference(CLEAR_GLOBAL_HTTP_PROXY_KEY);
    preSetPrivateDnsMode = findPreference(SET_PRIVATE_DNS_MODE_KEY);
    preSetPermissionPolicy = findPreference(SET_PERMISSION_POLICY_KEY);
    preManageAppPermissions = findPreference(MANAGE_APP_PERMISSIONS_KEY);
    preDisableStatusBar = findPreference(DISABLE_STATUS_BAR);
    preReEnableStatusBar = findPreference(REENABLE_STATUS_BAR);
    preDisableKeyguard = findPreference(DISABLE_KEYGUARD);
    preReEnableKeyguard = findPreference(REENABLE_KEYGUARD);
    preStartKioskMode = findPreference(START_KIOSK_MODE);
    preSystemUpdatePolicy = findPreference(SYSTEM_UPDATE_POLICY_KEY);
    preSystemUpdatePending = findPreference(SYSTEM_UPDATE_PENDING_KEY);
    preManagedSystemUpdates = findPreference(MANAGED_SYSTEM_UPDATES_KEY);
    preCreateManagedProfile = findPreference(CREATE_MANAGED_PROFILE_KEY);
    preCreateAndManageUser = findPreference(CREATE_AND_MANAGE_USER_KEY);
    preRemoveUser = findPreference(REMOVE_USER_KEY);
    preSwitchUser = findPreference(SWITCH_USER_KEY);
    preStartUserInBackground = findPreference(START_USER_IN_BACKGROUND_KEY);
    preStopUser = findPreference(STOP_USER_KEY);
    preLogoutUser = findPreference(LOGOUT_USER_KEY);
    swEnableLogout = findPreference(ENABLE_LOGOUT_KEY);
    preSetUserSessionMessage = findPreference(SET_USER_SESSION_MESSAGE_KEY);
    preSetAffiliationIds = findPreference(SET_AFFILIATION_IDS_KEY);
    preAffiliatedUser = findPreference(AFFILIATED_USER_KEY);
    preEphemeralUser = findPreference(EPHEMERAL_USER_KEY);
    preSetUserRestrictions = findPreference(SET_USER_RESTRICTIONS_KEY);
    preSetUserRestrictionsParent = findPreference(SET_USER_RESTRICTIONS_PARENT_KEY);
    swStayOnWhilePluggedIn = findPreference(STAY_ON_WHILE_PLUGGED_IN);
    swInstallNonMarketApps = findPreference(INSTALL_NONMARKET_APPS_KEY);
    swLocationEnabled = findPreference(SET_LOCATION_ENABLED_KEY);
    swLocationMode = findPreference(SET_LOCATION_MODE_KEY);
    preSetShortSupportMessage = findPreference(SET_SHORT_SUPPORT_MESSAGE_KEY);
    preSetLongSupportMessage = findPreference(SET_LONG_SUPPORT_MESSAGE_KEY);
    etDeviceOrganizationName = findPreference(SET_DEVICE_ORGANIZATION_NAME_KEY);
    swAutoTimeRequired = findPreference(SET_AUTO_TIME_REQUIRED_KEY);
    swAutoTime = findPreference(SET_AUTO_TIME_KEY);
    swAutoTimeZone = findPreference(SET_AUTO_TIME_ZONE_KEY);
    swSecurityLogging = findPreference(ENABLE_SECURITY_LOGGING);
    preRequestSecurityLogs = findPreference(REQUEST_SECURITY_LOGS);
    preRequestPreRebootSecurityLogs = findPreference(REQUEST_PRE_REBOOT_SECURITY_LOGS);
    swNetworkLogging = findPreference(ENABLE_NETWORK_LOGGING);
    preRequestNetworkLogs = findPreference(REQUEST_NETWORK_LOGS);
    preRequestBugreport = findPreference(REQUEST_BUGREPORT_KEY);
    swBackupService = findPreference(ENABLE_BACKUP_SERVICE);
    swCriteriaMode = findPreference(COMMON_CRITERIA_MODE_KEY);
    swUsbDataSignaling = findPreference(ENABLE_USB_DATA_SIGNALING_KEY);
    preWipeData = findPreference(WIPE_DATA_KEY);
    preRemoveDeviceOwner = findPreference(REMOVE_DEVICE_OWNER_KEY);
    preReboot = findPreference(REBOOT_KEY);
    preSetFactoryResetProtectionPolicy = findPreference(SET_FACTORY_RESET_PROTECTION_POLICY_KEY);
    preFactoryResetOrgOwnedDevice = findPreference(FACTORY_RESET_ORG_OWNED_DEVICE);
    swSuspendPersonalApps = findPreference(SUSPEND_PERSONAL_APPS_KEY);
    etProfileMaxTimeOff = findPreference(PROFILE_MAX_TIME_OFF_KEY);
    preTransferOwnershipToComponent = findPreference(TRANSFER_OWNERSHIP_KEY);
    preCrossProfileApps = findPreference(CROSS_PROFILE_APPS);
    preCrossProfileAppsAllowList = findPreference(CROSS_PROFILE_APPS_ALLOWLIST);
    preNearbyNotificationStreaming = findPreference(NEARBY_NOTIFICATION_STREAMING_KEY);
    preNearbyAppStreaming = findPreference(NEARBY_APP_STREAMING_KEY);
    preMtePolicy = findPreference(MTE_POLICY_KEY);

    /* アクセシビリティサービス */
    preAccessibility.setOnPreferenceClickListener(preference -> {
      if (mGetAccessibilityServicesTask != null && !mGetAccessibilityServicesTask.isCancelled()) {
        mGetAccessibilityServicesTask.cancel(true);
      }
      mGetAccessibilityServicesTask = new GetAccessibilityServicesTask();
      mGetAccessibilityServicesTask.execute();
      return false;
    });

    /* 時間 */
    preTime.setOnPreferenceClickListener(preference -> {
      if (Util.SDK_INT >= VERSION_CODES.R) {
        setAutoTimeEnabled(false);
      } else {
        mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.AUTO_TIME, "0");
      }
      showSetTimeDialog();
      return false;
    });

    /* タイムゾーン */
    preTimeZone.setOnPreferenceClickListener(preference -> {
      if (Util.SDK_INT >= VERSION_CODES.R) {
        setAutoTimeZoneEnabled(false);
      } else {
        mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.AUTO_TIME_ZONE, "0");
      }
      showSetTimeZoneDialog();
      return false;
    });

    /* 画面輝度 */
    preScreenBrightness.setOnPreferenceClickListener(preference -> {
      showSetScreenBrightnessDialog();
      return false;
    });

    swAutoBrightness.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManager.setSystemSetting(mAdminComponentName, Settings.System.SCREEN_BRIGHTNESS_MODE, newValue.equals(true) ? "1" : "0");
      reloadAutoBrightnessUi();
      return false;
    });

    /* スクリーンタイムアウト時間 */
    preScreenOffTimeout.setOnPreferenceClickListener(preference -> {
      showSetScreenOffTimeoutDialog();
      return false;
    });

    /* プロファイル名 */
    preProfileName.setOnPreferenceClickListener(preference -> {
      showSetProfileNameDialog();
      return false;
    });

    /* 組織識別子 */
    preOrganizationId.setOnPreferenceClickListener(preference -> {
      showSetOrganizationIdDialog();
      return false;
    });

    /* APN上書き */
    preManageOverrideApn.setOnPreferenceClickListener(preference -> {
      showFragment(new OverrideApnFragment());
      return false;
    });

    /* カレンダープロファイル共有 */
    preCrossProfileCalendar.setOnPreferenceClickListener(preference -> {
      showFragment(new CrossProfileCalendarFragment());
      return false;
    });

    preDisableAccountManagement.setOnPreferenceClickListener(preference -> {
      showSetDisableAccountManagementPrompt();
      return false;
    });

    preGetDisableAccountManagement.setOnPreferenceClickListener(preference -> {
      showDisableAccountTypeList();
      return false;
    });

    preAddAccount.setOnPreferenceClickListener(preference -> {
      requireActivity().startActivity(new Intent(requireActivity(), AddAccountActivity.class));
      return false;
    });

    preRemoveAccount.setOnPreferenceClickListener(preference -> {
      chooseAccount();
      return false;
    });

    preEnableSystemApps.setOnPreferenceClickListener(preference -> {
      showEnableSystemAppsPrompt();
      return false;
    });

    preEnableSystemAppsByPkgName.setOnPreferenceClickListener(preference -> {
      showEnableSystemAppByPackageNamePrompt();
      return false;
    });

    preEnableSystemAppsByIntent.setOnPreferenceClickListener(preference -> {
      showFragment(new EnableSystemAppsByIntentFragment());
      return false;
    });

    preInstallExistingPkg.setOnPreferenceClickListener(preference -> {
      showInstallExistingPackagePrompt();
      return false;
    });

    preInstallExistingPkg.setCustomConstraint(this::validateAffiliatedUserAfterP);

    preInstallApkPkg.setOnPreferenceClickListener(preference -> {
      Util.showFileViewer(this, INSTALL_APK_PACKAGE_REQUEST_CODE);
      return false;
    });

    preUninstallPkg.setOnPreferenceClickListener(preference -> {
      showUninstallPackagePrompt();
      return false;
    });

    preHideApps.setOnPreferenceClickListener(preference -> {
      showHideAppsPrompt(false);
      return false;
    });

    preHideAppsParentControl.setOnPreferenceClickListener(preference -> {
      showHideAppsOnParentPrompt(false);
      return false;
    });

    preUnHideApps.setOnPreferenceClickListener(preference -> {
      showHideAppsPrompt(true);
      return false;
    });

    preHideAppsParentControl.setOnPreferenceClickListener(preference -> {
      showHideAppsOnParentPrompt(true);
      return false;
    });

    preSuspendApps.setOnPreferenceClickListener(preference -> {
      showSuspendAppsPrompt(false);
      return false;
    });

    preUnSuspendApps.setOnPreferenceClickListener(preference -> {
      showSuspendAppsPrompt(true);
      return false;
    });

    preClearAppData.setOnPreferenceClickListener(preference -> {
      showClearAppDataPrompt();
      return false;
    });

    preKeepUninstalledPkg.setOnPreferenceClickListener(preference -> {
      showFragment(new ManageKeepUninstalledPackagesFragment());
      return false;
    });

    preManagedConfigurations.setOnPreferenceClickListener(preference -> {
      showFragment(new ManagedConfigurationsFragment());
      return false;
    });

    preDisableMeteredData.setOnPreferenceClickListener(preference -> {
      showSetMeteredDataPrompt();
      return false;
    });

    swAppFeedbackNotification.setOnPreferenceChangeListener((preference, newValue) -> {
      SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit();
      editor.putBoolean(getString(R.string.app_feedback_notifications), newValue.equals(true));
      editor.apply();
      return false;
    });

    preAppRestrictionsManagingPackage.setOnPreferenceClickListener(preference -> {
      showFragment(new AppRestrictionsManagingPackageFragment());
      return false;
    });

    preManageCertInstaller.setOnPreferenceClickListener(preference -> {
      showFragment(new DelegatedCertInstallerFragment());
      return false;
    });

    preGenericDelegation.setOnPreferenceClickListener(preference -> {
      showFragment(new DelegationFragment());
      return false;
    });

    preBlockUninstallationBypPkg.setOnPreferenceClickListener(preference -> {
      showBlockUninstallationByPackageNamePrompt();
      return false;
    });

    preBlockUninstallationList.setOnPreferenceClickListener(preference -> {
      showBlockUninstallationPrompt();
      return false;
    });

    swDisableCamera.setOnPreferenceChangeListener((preference, newValue) -> {
      setCameraDisabled((Boolean) newValue);
      reloadCameraDisableUi();
      return false;
    });

    swDisableCameraOnParent.setOnPreferenceChangeListener((preference, newValue) -> {
      setCameraDisabledOnParent((Boolean) newValue);
      reloadCameraDisableOnParentUi();
      return false;
    });

    preCaptureImage.setOnPreferenceClickListener(preference -> {
      dispatchCaptureIntent(MediaStore.ACTION_IMAGE_CAPTURE, CAPTURE_IMAGE_REQUEST_CODE, mImageUri);
      return false;
    });

    preCaptureVideo.setOnPreferenceClickListener(preference -> {
      dispatchCaptureIntent(MediaStore.ACTION_VIDEO_CAPTURE, CAPTURE_VIDEO_REQUEST_CODE, mVideoUri);
      return false;
    });

    swDisableScreenCapture.setOnPreferenceChangeListener((preference, newValue) -> {
      setScreenCaptureDisabled((Boolean) newValue);
      reloadScreenCaptureDisableUi();
      return false;
    });

    swDisableScreenCaptureOnParent.setOnPreferenceChangeListener((preference, newValue) -> {
      setScreenCaptureDisabledOnParent((Boolean) newValue);
      reloadScreenCaptureDisableOnParentUi();
      return false;
    });

    swMuteAudio.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManager.setMasterVolumeMuted(mAdminComponentName, (Boolean) newValue);
      reloadMuteAudioUi();
      return false;
    });

    preRequestManageCredentials.setOnPreferenceClickListener(preference -> {
      showConfigurePolicyAndManageCredentialsPrompt();
      return false;
    });

    preInstallKeyCertificate.setOnPreferenceClickListener(preference -> {
      Util.showFileViewer(this, INSTALL_KEY_CERTIFICATE_REQUEST_CODE);
      return false;
    });

    preRemoveKeyCertificate.setOnPreferenceClickListener(preference -> {
      choosePrivateKeyForRemoval();
      return false;
    });

    etOverrideKeySelection.setOnPreferenceChangeListener((preference, newValue) -> {
      preference.setSummary((String) newValue);
      return false;
    });

    etOverrideKeySelection.setSummary(etOverrideKeySelection.getText());

    preGenerateKeyAndCertificate.setOnPreferenceClickListener(preference -> {
      showPromptForGeneratedKeyAlias("generated-key-testdpc-1");
      return false;
    });

    preTestKeyUsability.setOnPreferenceClickListener(preference -> {
      testKeyCanBeUsedForSigning();
      return false;
    });

    preInstallCaCertificate.setOnPreferenceClickListener(preference -> {
      Util.showFileViewer(this, INSTALL_CA_CERTIFICATE_REQUEST_CODE);
      return false;
    });

    preGetCaCertificates.setOnPreferenceClickListener(preference -> {
      showCaCertificateList();
      return false;
    });

    preRemoveAllCaCertificates.setOnPreferenceClickListener(preference -> {
      mDevicePolicyManager.uninstallAllUserCaCerts(mAdminComponentName);
      showToast(R.string.all_ca_certificates_removed);
      return false;
    });

    preGrantKeyPairToApp.setOnPreferenceClickListener(preference -> {
      showGrantKeyPairToAppDialog();
      return false;
    });

    preCreateWifiConfiguration.setOnPreferenceClickListener(preference -> {
      showWifiConfigCreationDialog();
      return false;
    });

    preCreateEapTlsWifiConfiguration.setOnPreferenceClickListener(preference -> {
      showEapTlsWifiConfigCreationDialog();
      return false;
    });

    swEnableWifiConfigLockdown.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManager.setConfiguredNetworksLockdownState(mAdminComponentName, newValue.equals(true));
      reloadLockdownAdminConfiguredNetworksUi();
      return false;
    });

    preModifyWifiConfiguration.setOnPreferenceClickListener(preference -> {
      showFragment(WifiModificationFragment.createFragment(false));
      return false;
    });

    preModifyOwnedWifiConfiguration.setOnPreferenceClickListener(preference -> {
      showFragment(WifiModificationFragment.createFragment(true));
      return false;
    });

    preRemoveNotOwnedWifiConfigurations.setOnPreferenceClickListener(preference -> {
      boolean removed = requireActivity().getSystemService(WifiManager.class).removeNonCallerConfiguredNetworks();
      if (removed) {
        showToast("One or more networks are removed");
      } else {
        showToast("No network is removed");
      }
      return false;
    });

    preShowWifiMacAddress.setOnPreferenceClickListener(preference -> {
      showWifiMacAddress();
      return false;
    });

    preSetWifiMinSecurityLevel.setOnPreferenceClickListener(preference -> {
      showSetWifiMinSecurityLevelDialog();
      return false;
    });

    preSetWifiSsidRestriction.setOnPreferenceClickListener(preference -> {
      showSetWifiSsidRestrictionDialog();
      return false;
    });

    preSetInputMethods.setOnPreferenceClickListener(preference -> {
      if (mGetInputMethodsTask != null && !mGetInputMethodsTask.isCancelled()) {
        mGetInputMethodsTask.cancel(true);
      }
      mGetInputMethodsTask = new GetInputMethodsTask();
      mGetInputMethodsTask.execute();
      return false;
    });

    preSetInputMethodsOnParent.setOnPreferenceClickListener(preference -> {
      setPermittedInputMethodsOnParent();
      return false;
    });

    preSetNotificationListeners.setOnPreferenceClickListener(preference -> {
      if (mGetNotificationListenersTask != null && !mGetNotificationListenersTask.isCancelled()) {
        mGetNotificationListenersTask.cancel(true);
      }
      mGetNotificationListenersTask = new GetNotificationListenersTask();
      mGetNotificationListenersTask.execute();
      return false;
    });

    preSetNotificationListenersText.setOnPreferenceClickListener(preference -> {
      setNotificationAllowlistEditBox();
      return false;
    });

    prePasswordComplexity.setOnPreferenceClickListener(preference -> {
      /* None */
      return false;
    });

    preRequiredPasswordComplexity.setOnPreferenceClickListener(preference -> {
      /* None */
      return false;
    });

    prePasswordCompliant.setOnPreferenceClickListener(preference -> {
      /* None */
      return false;
    });

    preSeparateChallenge.setOnPreferenceClickListener(preference -> {
      /* None */
      return false;
    });

    preLockScreenPolicy.setOnPreferenceClickListener(preference -> {
      showFragment(new LockScreenPolicyFragment.Container());
      return false;
    });

    prePasswordConstraints.setOnPreferenceClickListener(preference -> {
      showFragment(new PasswordConstraintsFragment.Container());
      return false;
    });

    preResetPassword.setOnPreferenceClickListener(preference -> {
      if (Util.SDK_INT >= VERSION_CODES.O) {
        showFragment(new ResetPasswordWithTokenFragment());
        return true;
      } else {
        showResetPasswordPrompt();
      }
      return false;
    });

    preLockNow.setOnPreferenceClickListener(preference -> {
      lockNow();
      return false;
    });

    preSetNewPassword.setOnPreferenceClickListener(preference -> {
      startActivity(new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD));
      return false;
    });

    listNewPasswordWithComplexity.setOnPreferenceChangeListener((preference, newValue) -> {
      Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
      intent.putExtra(DevicePolicyManager.EXTRA_PASSWORD_COMPLEXITY, Integer.parseInt((String) newValue));
      startActivity(intent);
      return false;
    });

    listRequiredPasswordComplexity.setOnPreferenceChangeListener((preference, newValue) -> {
      int requiredComplexity = Integer.parseInt((String) newValue);
      setRequiredPasswordComplexity(requiredComplexity);
      return false;
    });

    listRequiredPasswordComplexityOnParent.setOnPreferenceChangeListener((preference, newValue) -> {
      int requiredParentComplexity = Integer.parseInt((String) newValue);
      setRequiredPasswordComplexityOnParent(requiredParentComplexity);
      return false;
    });

    preSetProfileParentNewPassword.setOnPreferenceClickListener(preference -> {
      startActivity(new Intent(DevicePolicyManager.ACTION_SET_NEW_PARENT_PROFILE_PASSWORD));
      return false;
    });

    preSetProfileParentNewPasswordDeviceRequirement.setOnPreferenceClickListener(preference -> {
      startActivity(new Intent(DevicePolicyManager.ACTION_SET_NEW_PARENT_PROFILE_PASSWORD).putExtra("android.app.extra.DEVICE_PASSWORD_REQUIREMENT_ONLY", true));
      return false;
    });

    preManageLockTask.setOnPreferenceClickListener(preference -> {
      showManageLockTaskListPrompt(R.string.lock_task_title, (packages) -> mDevicePolicyManagerGateway.setLockTaskPackages(packages,
              (v) -> onSuccessLog("setLockTaskPackages()"),
              (e) -> onErrorShowToast("setLockTaskPackages()", e, R.string.lock_task_unavailable)));
      return false;
    });

    preManageLockTask.setCustomConstraint(this::validateAffiliatedUserAfterP);

    preCheckLockTaskPermitted.setOnPreferenceClickListener(preference -> {
      showCheckLockTaskPermittedPrompt();
      return false;
    });

    preSetLockTaskFeatures.setOnPreferenceClickListener(preference -> {
      showFragment(new SetLockTaskFeaturesFragment());
      return false;
    });

    preSetLockTaskFeatures.setCustomConstraint(this::validateAffiliatedUserAfterP);

    preStartLockTask.setOnPreferenceClickListener(preference -> {
      requireActivity().startLockTask();
      return false;
    });

    preRelaunchInLockTask.setOnPreferenceClickListener(preference -> {
      relaunchInLockTaskMode();
      return false;
    });

    preStopLockTask.setOnPreferenceClickListener(preference -> {
      try {
        requireActivity().stopLockTask();
      } catch (IllegalStateException ignored) {
      }
      return false;
    });

    preManagedProfilePolicies.setOnPreferenceClickListener(preference -> {
      showFragment(new ProfilePolicyManagementFragment(), ProfilePolicyManagementFragment.FRAGMENT_TAG);
      return false;
    });

    preBindDeviceAdminPolicies.setOnPreferenceClickListener(preference -> {
      showFragment(new BindDeviceAdminFragment());
      return false;
    });

    preBindDeviceAdminPolicies.setCustomConstraint(() ->
            (Util.getBindDeviceAdminTargetUsers(requireActivity()).size() == 1) ? NO_CUSTOM_CONSTRAINT : R.string.require_one_po_to_bind);

    preNetworkStats.setOnPreferenceClickListener(preference -> {
      showFragment(new NetworkUsageStatsFragment());
      return false;
    });

    preSetAlwaysOnVpn.setOnPreferenceClickListener(preference -> {
      showFragment(new AlwaysOnVpnFragment());
      return false;
    });

    swPreferentialNetworkServiceStatus.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManagerGateway.setPreferentialNetworkServiceEnabled((Boolean) newValue,
              (v) -> onSuccessShowToastWithHardcodedMessage("setPreferentialNetworkServiceEnabled(%b)", mDevicePolicyManagerGateway.isPreferentialNetworkServiceEnabled()),
              (e) -> onErrorLog("setPreferentialNetworkServiceEnabled", e));
      return false;
    });

    if (Common.isDhizukuActive(requireActivity()) && (isManagedProfileOwner() || isDeviceOwner()) && Util.SDK_INT >= VERSION_CODES.S) {
      swPreferentialNetworkServiceStatus.setChecked(mDevicePolicyManagerGateway.isPreferentialNetworkServiceEnabled());
    }

    preEnterpriseSlice.setOnPreferenceClickListener(preference -> {
      showFragment(new EnterpriseSliceFragment());
      return false;
    });

    preSetGlobalHttpProxy.setOnPreferenceClickListener(preference -> {
      showSetGlobalHttpProxyDialog();
      return false;
    });

    preClearGlobalHttpProxy.setOnPreferenceClickListener(preference -> {
      mDevicePolicyManager.setRecommendedGlobalProxy(mAdminComponentName, null /* proxyInfo */);
      return false;
    });

    preSetPrivateDnsMode.setOnPreferenceClickListener(preference -> {
      showFragment(new PrivateDnsModeFragment());
      return false;
    });

    preSetPermissionPolicy.setOnPreferenceClickListener(preference -> {
      showSetPermissionPolicyDialog();
      return false;
    });

    preManageAppPermissions.setOnPreferenceClickListener(preference -> {
      showFragment(new ManageAppPermissionsFragment());
      return false;
    });

    preDisableStatusBar.setOnPreferenceClickListener(preference -> {
      setStatusBarDisabled(true);
      return false;
    });

    preDisableStatusBar.setCustomConstraint(this::validateAffiliatedUserAfterP);
    preDisableStatusBar.addCustomConstraint(this::validateDeviceOwnerBeforeP);

    preReEnableStatusBar.setOnPreferenceClickListener(preference -> {
      setStatusBarDisabled(false);
      return false;
    });

    preReEnableStatusBar.setCustomConstraint(this::validateAffiliatedUserAfterP);
    preReEnableStatusBar.addCustomConstraint(this::validateDeviceOwnerBeforeP);

    preDisableKeyguard.setOnPreferenceClickListener(preference -> {
      setKeyGuardDisabled(true);
      return false;
    });

    preDisableKeyguard.setCustomConstraint(this::validateAffiliatedUserAfterP);
    preDisableKeyguard.addCustomConstraint(this::validateDeviceOwnerBeforeP);

    preReEnableKeyguard.setOnPreferenceClickListener(preference -> {
      setKeyGuardDisabled(false);
      return false;
    });

    preReEnableKeyguard.setCustomConstraint(this::validateAffiliatedUserAfterP);
    preReEnableKeyguard.addCustomConstraint(this::validateDeviceOwnerBeforeP);

    preStartKioskMode.setOnPreferenceClickListener(preference -> {
      showManageLockTaskListPrompt(R.string.kiosk_select_title, this::startKioskMode);
      return false;
    });

    preSystemUpdatePolicy.setOnPreferenceClickListener(preference -> {
      showFragment(new SystemUpdatePolicyFragment());
      return false;
    });

    preSystemUpdatePending.setOnPreferenceClickListener(preference -> {
      showPendingSystemUpdate();
      return false;
    });

    preManagedSystemUpdates.setOnPreferenceClickListener(preference -> {
      promptInstallUpdate();
      return false;
    });

    preCreateManagedProfile.setOnPreferenceClickListener(preference -> {
      requireActivity().startActivity(new Intent(requireActivity(), SetupManagementActivity.class));
      return false;
    });

    preCreateAndManageUser.setOnPreferenceClickListener(preference -> {
      showCreateAndManageUserPrompt();
      return false;
    });

    preRemoveUser.setOnPreferenceClickListener(preference -> {
      showRemoveUserPrompt();
      return false;
    });

    preSwitchUser.setOnPreferenceClickListener(preference -> {
      showSwitchUserPrompt();
      return false;
    });

    preStartUserInBackground.setOnPreferenceClickListener(preference -> {
      showStartUserInBackgroundPrompt();
      return false;
    });

    preStopUser.setOnPreferenceClickListener(preference -> {
      showStopUserPrompt();
      return false;
    });

    preLogoutUser.setOnPreferenceClickListener(preference -> {
      logoutUser();
      return false;
    });

    preLogoutUser.setCustomConstraint(this::validateAffiliatedUserAfterP);

    swEnableLogout.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManager.setLogoutEnabled(mAdminComponentName, (Boolean) newValue);
      reloadEnableLogoutUi();
      return false;
    });

    preSetUserSessionMessage.setOnPreferenceClickListener(preference -> {
      showFragment(new SetUserSessionMessageFragment());
      return false;
    });

    preSetAffiliationIds.setOnPreferenceClickListener(preference -> {
      showFragment(new ManageAffiliationIdsFragment());
      return false;
    });

    preAffiliatedUser.setOnPreferenceClickListener(preference -> {
      /* None */
      return false;
    });

    preEphemeralUser.setOnPreferenceClickListener(preference -> {
      /* None */
      return false;
    });

    preSetUserRestrictions.setOnPreferenceClickListener(preference -> {
      showFragment(new UserRestrictionsDisplayFragment());
      return false;
    });

    preSetUserRestrictionsParent.setOnPreferenceClickListener(preference -> {
      showFragment(new UserRestrictionsParentDisplayFragment());
      return false;
    });

    swStayOnWhilePluggedIn.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, newValue.equals(true) ? BATTERY_PLUGGED_ANY : DONT_STAY_ON);
      updateStayOnWhilePluggedInPreference();
      return false;
    });

    swInstallNonMarketApps.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManager.setSecureSetting(mAdminComponentName, Settings.Secure.INSTALL_NON_MARKET_APPS, newValue.equals(true) ? "1" : "0");
      updateInstallNonMarketAppsPreference();
      return false;
    });

    swInstallNonMarketApps.setCustomConstraint(this::validateInstallNonMarketApps);

    swLocationEnabled.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManager.setLocationEnabled(mAdminComponentName, newValue.equals(true));
      reloadLocationEnabledUi();
      reloadLocationModeUi();
      return false;
    });

    swLocationMode.setOnPreferenceChangeListener((preference, newValue) -> {
      final int locationMode;
      if (newValue.equals(true)) {
        locationMode = Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
      } else {
        locationMode = Settings.Secure.LOCATION_MODE_OFF;
      }
      mDevicePolicyManager.setSecureSetting(mAdminComponentName, Settings.Secure.LOCATION_MODE, String.format(Locale.getDefault(), "%d", locationMode));
      reloadLocationEnabledUi();
      reloadLocationModeUi();
      return false;
    });

    preSetShortSupportMessage.setOnPreferenceClickListener(preference -> {
      showFragment(SetSupportMessageFragment.newInstance(SetSupportMessageFragment.TYPE_SHORT));
      return false;
    });

    preSetLongSupportMessage.setOnPreferenceClickListener(preference -> {
      showFragment(SetSupportMessageFragment.newInstance(SetSupportMessageFragment.TYPE_LONG));
      return false;
    });

    etDeviceOrganizationName.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManagerGateway.setOrganizationName((String) newValue,
              (v) -> onSuccessLog("setOrganizationName"),
              (e) -> onErrorLog("setOrganizationName", e));
      etDeviceOrganizationName.setSummary((String) newValue);
      return false;
    });

    swAutoTimeRequired.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManager.setAutoTimeRequired(mAdminComponentName, newValue.equals(true));
      reloadSetAutoTimeRequiredUi();
      return false;
    });

    swAutoTimeRequired.addCustomConstraint(this::validateDeviceOwnerBeforeO);

    swAutoTime.setOnPreferenceChangeListener((preference, newValue) -> {
      setAutoTimeEnabled(newValue.equals(true));
      reloadSetAutoTimeUi();
      return false;
    });

    swAutoTimeZone.setOnPreferenceChangeListener((preference, newValue) -> {
      setAutoTimeZoneEnabled(newValue.equals(true));
      reloadSetAutoTimeZoneUi();
      return false;
    });

    swSecurityLogging.setOnPreferenceChangeListener((preference, newValue) -> {
      setSecurityLoggingEnabled((Boolean) newValue);
      reloadEnableSecurityLoggingUi();
      return false;
    });

    preRequestSecurityLogs.setOnPreferenceClickListener(preference -> {
      showFragment(SecurityLogsFragment.newInstance(false /* preReboot */));
      return false;
    });

    preRequestSecurityLogs.setCustomConstraint(() -> isSecurityLoggingEnabled() ? NO_CUSTOM_CONSTRAINT : R.string.requires_security_logs);

    preRequestPreRebootSecurityLogs.setOnPreferenceClickListener(preference -> {
      showFragment(SecurityLogsFragment.newInstance(true /* preReboot */));
      return false;
    });

    preRequestPreRebootSecurityLogs.setCustomConstraint(() -> isSecurityLoggingEnabled() ? NO_CUSTOM_CONSTRAINT : R.string.requires_security_logs);

    swNetworkLogging.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManagerGateway.setNetworkLoggingEnabled((Boolean) newValue);
      reloadEnableNetworkLoggingUi();
      return false;
    });

    swNetworkLogging.addCustomConstraint(this::validateDeviceOwnerOrDelegationNetworkLoggingBeforeS);

    preRequestNetworkLogs.setOnPreferenceClickListener(preference -> {
      showFragment(new NetworkLogsFragment());
      return false;
    });

    preRequestNetworkLogs.setCustomConstraint(() -> isNetworkLoggingEnabled() ? NO_CUSTOM_CONSTRAINT : R.string.requires_network_logs);

    preRequestBugreport.setOnPreferenceClickListener(preference -> {
      requestBugReport();
      return false;
    });

    swBackupService.setOnPreferenceChangeListener((preference, newValue) -> {
      setBackupServiceEnabled((Boolean) newValue);
      reloadEnableBackupServiceUi();
      return false;
    });

    swBackupService.setCustomConstraint(this::validateDeviceOwnerBeforeQ);

    swCriteriaMode.setOnPreferenceChangeListener((preference, newValue) -> {
      setCommonCriteriaModeEnabled((Boolean) newValue);
      reloadCommonCriteriaModeUi();
      return false;
    });

    swUsbDataSignaling.setOnPreferenceChangeListener((preference, newValue) -> {
      setUsbDataSignalingEnabled((Boolean) newValue);
      reloadEnableUsbDataSignalingUi();
      return false;
    });

    preWipeData.setOnPreferenceClickListener(preference -> {
      showWipeDataPrompt();
      return false;
    });

    preRemoveDeviceOwner.setOnPreferenceClickListener(preference -> {
      showRemoveDeviceOwnerPrompt();
      return false;
    });

    preReboot.setOnPreferenceClickListener(preference -> {
      reboot();
      return false;
    });

    preSetFactoryResetProtectionPolicy.setOnPreferenceClickListener(preference -> {
      showFragment(new FactoryResetProtectionPolicyFragment());
      return false;
    });

    preFactoryResetOrgOwnedDevice.setOnPreferenceClickListener(preference -> {
      factoryResetOrgOwnedDevice();
      return false;
    });

    swSuspendPersonalApps.setOnPreferenceChangeListener((preference, newValue) -> {
      mDevicePolicyManager.setPersonalAppsSuspended(mAdminComponentName, (Boolean) newValue);
      reloadPersonalAppsSuspendedUi();
      return false;
    });

    etProfileMaxTimeOff.setOnPreferenceChangeListener((preference, newValue) -> {
      final long timeoutSec = Long.parseLong((String) newValue);
      mDevicePolicyManager.setManagedProfileMaximumTimeOff(mAdminComponentName, TimeUnit.SECONDS.toMillis(timeoutSec));
      maybeUpdateProfileMaxTimeOff();
      return false;
    });

    preTransferOwnershipToComponent.setOnPreferenceClickListener(preference -> {
      showFragment(new PickTransferComponentFragment());
      return false;
    });

    preCrossProfileApps.setOnPreferenceClickListener(preference -> {
      showFragment(new CrossProfileAppsFragment());
      return false;
    });

    preCrossProfileAppsAllowList.setOnPreferenceClickListener(preference -> {
      showFragment(new CrossProfileAppsAllowlistFragment());
      return false;
    });

    preNearbyNotificationStreaming.setOnPreferenceClickListener(preference -> {
      showNearbyNotificationStreamingDialog();
      return false;
    });

    preNearbyAppStreaming.setOnPreferenceClickListener(preference -> {
      showNearbyAppStreamingDialog();
      return false;
    });

    preMtePolicy.setOnPreferenceClickListener(preference -> {
      showMtePolicyDialog();
      return false;
    });

    /* プレファレンス初期化 */
    initializePre();
  }

  /* プレファレンス初期化 */
  private void initializePre() {
    if (((DevicePolicyManager) requireActivity().getSystemService(Context.DEVICE_POLICY_SERVICE)).isAdminActive(new ComponentName(requireActivity(), DeviceAdminReceiver.class))) {
      if (Common.isDhizukuActive(requireActivity())) {
        maybeUpdateProfileMaxTimeOff();
        onCreateSetNewPasswordWithComplexityPreference();
        onCreateSetRequiredPasswordComplexityPreference();
        onCreateSetRequiredPasswordComplexityOnParentPreference();
        constrainSpecialCasePreferences();
        maybeDisableLockTaskPreferences();
        loadAppFeedbackNotifications();
        loadPreAppStatus();
        loadPreSecurityPatch();
        loadEnrollmentSpecificId();
        loadIsEphemeralUserUi();
        reloadCameraDisableUi();
        reloadScreenCaptureDisableUi();
        reloadMuteAudioUi();
        reloadEnableBackupServiceUi();
        reloadCommonCriteriaModeUi();
        reloadEnableUsbDataSignalingUi();
        reloadEnableSecurityLoggingUi();
        reloadEnableNetworkLoggingUi();
        reloadSetAutoTimeRequiredUi();
        reloadSetAutoTimeUi();
        reloadSetAutoTimeZoneUi();
        reloadEnableLogoutUi();
        reloadAutoBrightnessUi();
        reloadPersonalAppsSuspendedUi();
        updateStayOnWhilePluggedInPreference();
        updateInstallNonMarketAppsPreference();
        loadPasswordCompliant();
        loadPasswordComplexity();
        loadRequiredPasswordComplexity();
        loadSeparateChallenge();
        reloadAffiliatedApis();
        return;
      }
    }
    loadPreAppStatus();
  }

  private void maybeUpdateProfileMaxTimeOff() {
    if (etProfileMaxTimeOff.isEnabled()) {
      final String currentValueAsString = Long.toString(TimeUnit.MILLISECONDS.toSeconds(mDevicePolicyManager.getManagedProfileMaximumTimeOff(mAdminComponentName)));
      etProfileMaxTimeOff.setText(currentValueAsString);
      etProfileMaxTimeOff.setSummary(currentValueAsString);
    }
  }

  private void onCreateSetNewPasswordWithComplexityPreference() {
    ListPreference complexityPref = (ListPreference) findPreference(SET_NEW_PASSWORD_WITH_COMPLEXITY);
    addPasswordComplexityListToPreference(complexityPref);
  }

  private void onCreateSetRequiredPasswordComplexityPreference() {
    ListPreference requiredComplexityPref = (ListPreference) findPreference(SET_REQUIRED_PASSWORD_COMPLEXITY);
    addPasswordComplexityListToPreference(requiredComplexityPref);
  }

  private void onCreateSetRequiredPasswordComplexityOnParentPreference() {
    ListPreference requiredParentComplexityPref = (ListPreference) findPreference(SET_REQUIRED_PASSWORD_COMPLEXITY_ON_PARENT);
    addPasswordComplexityListToPreference(requiredParentComplexityPref);
  }

  private void constrainSpecialCasePreferences() {
    // Reset password can be used in all contexts since N
    if (Util.SDK_INT >= VERSION_CODES.N) {
      ((DpcPreference) findPreference(RESET_PASSWORD_KEY)).clearNonCustomConstraints();
    }
  }

  private void maybeDisableLockTaskPreferences() {
    if (Util.SDK_INT < VERSION_CODES.O) {
      String[] lockTaskPreferences = {
              MANAGE_LOCK_TASK_LIST_KEY, CHECK_LOCK_TASK_PERMITTED_KEY, START_LOCK_TASK, STOP_LOCK_TASK
      };
      for (String preference : lockTaskPreferences) {
        ((DpcPreferenceBase) findPreference(preference)).setAdminConstraint(DpcPreferenceHelper.ADMIN_DEVICE_OWNER);
      }
    }
  }

  @TargetApi(VERSION_CODES.N)
  private void loadAppFeedbackNotifications() {
    if (Util.SDK_INT < VERSION_CODES.N) {
      return;
    }

    swAppFeedbackNotification.setChecked(PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean(getString(R.string.app_feedback_notifications), false));
  }

  /* アプリステータス読み込み */
  private void loadPreAppStatus() {
    if (Common.isDhizukuActive(requireActivity())) {
      preAppStatus.setSummary(R.string.this_is_a_device_owner);
    } else {
      if (!Dhizuku.init(requireActivity())) {
        preAppStatus.setSummary(R.string.this_is_not_an_admin);
        return;
      }

      if (!Dhizuku.isPermissionGranted()) {
        Dhizuku.requestPermission(new DhizukuRequestPermissionListener() {
          @Override
          public void onRequestPermission(int grantResult) {
            requireActivity().runOnUiThread(() -> {
              if (grantResult == PackageManager.PERMISSION_GRANTED) {
                preAppStatus.setSummary(R.string.this_is_a_device_owner);
                requireActivity().finish();
                requireActivity().overridePendingTransition(0, 0);
                startActivity(requireActivity().getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
              } else {
                preAppStatus.setSummary(R.string.this_is_not_an_admin);
              }
            });
          }
        });
      }
    }
  }

  /* セキュリティパッチレベル読み込み */
  @SuppressLint("SimpleDateFormat")
  private void loadPreSecurityPatch() {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M || !preSecurityPatch.isEnabled()) {
      return;
    }

    try {
      Date date = new SimpleDateFormat(SECURITY_PATCH_FORMAT).parse(Build.VERSION.SECURITY_PATCH);

      if (date != null) {
        preSecurityPatch.setSummary(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
      }
    } catch (ParseException ignored) {
      preSecurityPatch.setSummary(getString(R.string.invalid_security_patch, Build.VERSION.SECURITY_PATCH));
    }
  }

  @TargetApi(VERSION_CODES.S)
  private void loadEnrollmentSpecificId() {
    Preference enrollmentSpecificIdPreference = findPreference(ENROLLMENT_SPECIFIC_ID_KEY);
    if (!enrollmentSpecificIdPreference.isEnabled()) {
      return;
    }

    enrollmentSpecificIdPreference.setSummary(mDevicePolicyManager.getEnrollmentSpecificId());
  }

  @TargetApi(VERSION_CODES.P)
  private void loadIsEphemeralUserUi() {
    if (preEphemeralUser.isEnabled()) {
      boolean isEphemeralUser = mDevicePolicyManager.isEphemeralUser(mAdminComponentName);
      preEphemeralUser.setSummary(isEphemeralUser ? R.string.yes : R.string.no);
    }
  }

  private void reloadCameraDisableUi() {
    boolean isCameraDisabled = mDevicePolicyManager.getCameraDisabled(mAdminComponentName);
    swDisableCamera.setChecked(isCameraDisabled);
  }

  private void reloadScreenCaptureDisableUi() {
    boolean isScreenCaptureDisabled = mDevicePolicyManager.getScreenCaptureDisabled(mAdminComponentName);
    swDisableScreenCapture.setChecked(isScreenCaptureDisabled);
  }

  private void reloadMuteAudioUi() {
    if (swMuteAudio.isEnabled()) {
      final boolean isAudioMuted = mDevicePolicyManager.isMasterVolumeMuted(mAdminComponentName);
      swMuteAudio.setChecked(isAudioMuted);
    }
  }

  @TargetApi(VERSION_CODES.O)
  private void reloadEnableBackupServiceUi() {
    if (swBackupService.isEnabled()) {
      swBackupService.setChecked(mDevicePolicyManager.isBackupServiceEnabled(mAdminComponentName));
    }
  }

  @TargetApi(VERSION_CODES.R)
  private void reloadCommonCriteriaModeUi() {
    if (swCriteriaMode.isEnabled()) {
      swCriteriaMode.setChecked(mDevicePolicyManager.isCommonCriteriaModeEnabled(mAdminComponentName));
    }
  }

  @TargetApi(VERSION_CODES.S)
  private void reloadEnableUsbDataSignalingUi() {
    if (swUsbDataSignaling.isEnabled()) {
      boolean enabled = mDevicePolicyManager.isUsbDataSignalingEnabled();
      swUsbDataSignaling.setChecked(enabled);
    }
  }

  @TargetApi(VERSION_CODES.N)
  private void reloadEnableSecurityLoggingUi() {
    if (swSecurityLogging.isEnabled()) {
      boolean securityLoggingEnabled =
              mDevicePolicyManager.isSecurityLoggingEnabled(mAdminComponentName);
      swSecurityLogging.setChecked(securityLoggingEnabled);
      preRequestSecurityLogs.refreshEnabledState();
      preRequestPreRebootSecurityLogs.refreshEnabledState();
    }
  }

  @TargetApi(VERSION_CODES.O)
  private void reloadEnableNetworkLoggingUi() {
    if (swNetworkLogging.isEnabled()) {
      boolean isNetworkLoggingEnabled = isNetworkLoggingEnabled();
      swNetworkLogging.setChecked(isNetworkLoggingEnabled);
      preRequestNetworkLogs.refreshEnabledState();
    }
  }

  private void reloadSetAutoTimeRequiredUi() {
    boolean isAutoTimeRequired = mDevicePolicyManager.getAutoTimeRequired();
    swAutoTimeRequired.setChecked(isAutoTimeRequired);
  }

  @TargetApi(VERSION_CODES.R)
  private void reloadSetAutoTimeUi() {
    if (Util.SDK_INT < VERSION_CODES.R) {
      return;
    }
    if (isOrganizationOwnedDevice()) {
      boolean isAutoTime = mDevicePolicyManager.getAutoTimeEnabled(mAdminComponentName);
      swAutoTime.setChecked(isAutoTime);
    }
  }

  @TargetApi(VERSION_CODES.R)
  private void reloadSetAutoTimeZoneUi() {
    if (Util.SDK_INT < VERSION_CODES.R) {
      return;
    }
    if (isOrganizationOwnedDevice()) {
      boolean isAutoTimeZone = mDevicePolicyManager.getAutoTimeZoneEnabled(mAdminComponentName);
      swAutoTimeZone.setChecked(isAutoTimeZone);
    }
  }

  @TargetApi(VERSION_CODES.P)
  private void reloadEnableLogoutUi() {
    if (swEnableLogout.isEnabled()) {
      swEnableLogout.setChecked(mDevicePolicyManager.isLogoutEnabled());
    }
  }

  @TargetApi(VERSION_CODES.P)
  private void reloadAutoBrightnessUi() {
    if (swAutoBrightness.isEnabled()) {
      final String brightnessMode = Settings.System.getString(requireActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
      swAutoBrightness.setChecked(parseInt(brightnessMode, /* defaultValue= */ 0) == 1);
    }
  }

  @TargetApi(VERSION_CODES.R)
  private void reloadPersonalAppsSuspendedUi() {
    if (swSuspendPersonalApps.isEnabled()) {
      int suspendReasons = mDevicePolicyManager.getPersonalAppsSuspendedReasons(mAdminComponentName);
      swSuspendPersonalApps.setChecked(suspendReasons != 0);
    }
  }

  private void updateStayOnWhilePluggedInPreference() {
    if (!swStayOnWhilePluggedIn.isEnabled()) {
      return;
    }

    boolean checked;
    final int currentState = Settings.Global.getInt(requireActivity().getContentResolver(), Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);
    checked = (currentState & (BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB | BatteryManager.BATTERY_PLUGGED_WIRELESS)) != 0;
    mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, checked ? BATTERY_PLUGGED_ANY : DONT_STAY_ON);
    swStayOnWhilePluggedIn.setChecked(checked);
  }

  public void updateInstallNonMarketAppsPreference() {
    int isInstallNonMarketAppsAllowed = Settings.Secure.getInt(requireActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
    swInstallNonMarketApps.setChecked(isInstallNonMarketAppsAllowed != 0);
  }

  @TargetApi(VERSION_CODES.N)
  private void loadPasswordCompliant() {
    Preference passwordCompliantPreference = findPreference(PASSWORD_COMPLIANT_KEY);
    if (!passwordCompliantPreference.isEnabled()) {
      return;
    }

    String summary;
    DevicePolicyManager dpm = requireActivity().getSystemService(DevicePolicyManager.class);
    boolean compliant = dpm.isActivePasswordSufficient();
    if (isManagedProfileOwner()) {
      DevicePolicyManager parentDpm = dpm.getParentProfileInstance(DeviceAdminReceiver.getComponentName(requireActivity()));
      boolean parentCompliant = parentDpm.isActivePasswordSufficient();
      final String deviceCompliant;
      if (Util.SDK_INT < VERSION_CODES.S) {
        deviceCompliant = "N/A";
      } else {
        deviceCompliant = Boolean.toString(parentDpm.isActivePasswordSufficientForDeviceRequirement());
      }
      summary =
              String.format(
                      getString(R.string.password_compliant_profile_summary),
                      Boolean.toString(parentCompliant),
                      deviceCompliant,
                      Boolean.toString(compliant));
    } else {
      summary =
              String.format(
                      getString(R.string.password_compliant_summary), Boolean.toString(compliant));
    }
    passwordCompliantPreference.setSummary(summary);
  }

  private void loadPasswordComplexity() {
    Preference passwordComplexityPreference = findPreference(PASSWORD_COMPLEXITY_KEY);
    if (!passwordComplexityPreference.isEnabled()) {
      return;
    }

    String summary;
    int complexity = PASSWORD_COMPLEXITY.get(mDevicePolicyManager.getPasswordComplexity());
    if (isManagedProfileOwner() && Util.SDK_INT >= VERSION_CODES.R) {
      DevicePolicyManager parentDpm =
              mDevicePolicyManager.getParentProfileInstance(mAdminComponentName);
      int parentComplexity = PASSWORD_COMPLEXITY.get(parentDpm.getPasswordComplexity());
      summary =
              String.format(
                      getString(R.string.password_complexity_profile_summary),
                      getString(parentComplexity),
                      getString(complexity));
    } else {
      summary = getString(complexity);
    }
    passwordComplexityPreference.setSummary(summary);
  }

  private void loadRequiredPasswordComplexity() {
    Preference requiredPasswordComplexityPreference =
            findPreference(REQUIRED_PASSWORD_COMPLEXITY_KEY);
    if (!requiredPasswordComplexityPreference.isEnabled()) {
      return;
    }

    String summary;
    int complexity = PASSWORD_COMPLEXITY.get(getRequiredComplexity(mDevicePolicyManager));
    if (isManagedProfileOwner() && Util.SDK_INT >= VERSION_CODES.S) {
      DevicePolicyManager parentDpm =
              mDevicePolicyManager.getParentProfileInstance(mAdminComponentName);
      int parentComplexity = PASSWORD_COMPLEXITY.get(getRequiredComplexity(parentDpm));
      summary =
              String.format(
                      getString(R.string.password_complexity_profile_summary),
                      getString(parentComplexity),
                      getString(complexity));
    } else {
      summary = getString(complexity);
    }

    requiredPasswordComplexityPreference.setSummary(summary);
  }

  @TargetApi(VERSION_CODES.P)
  private void loadSeparateChallenge() {
    final Preference separateChallengePreference = findPreference(SEPARATE_CHALLENGE_KEY);
    if (!separateChallengePreference.isEnabled()) {
      return;
    }

    final Boolean separate = !mDevicePolicyManager.isUsingUnifiedPassword(mAdminComponentName);
    separateChallengePreference.setSummary(
            String.format(getString(R.string.separate_challenge_summary), Boolean.toString(separate)));
  }

  @TargetApi(VERSION_CODES.P)
  private void reloadAffiliatedApis() {
    if (preAffiliatedUser.isEnabled()) {
      preAffiliatedUser.setSummary(mDevicePolicyManager.isAffiliatedUser() ? R.string.yes : R.string.no);
    }

    preInstallExistingPkg.refreshEnabledState();
    preManageLockTask.refreshEnabledState();
    preSetLockTaskFeatures.refreshEnabledState();
    preLogoutUser.refreshEnabledState();
    preDisableStatusBar.refreshEnabledState();
    preReEnableStatusBar.refreshEnabledState();
    preDisableKeyguard.refreshEnabledState();
    preReEnableKeyguard.refreshEnabledState();
  }

  @RequiresApi(VERSION_CODES.R)
  private void setAutoTimeEnabled(boolean enabled) {
    mDevicePolicyManager.setAutoTimeEnabled(mAdminComponentName, enabled);
  }

  @TargetApi(VERSION_CODES.P)
  private void showSetTimeDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    final View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText timeEditText = dialogView.findViewById(R.id.input);
    final String currentTime = Long.toString(System.currentTimeMillis());

    timeEditText.setText(currentTime);

    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.pre_main_title_time)
            .setView(dialogView)
            .setPositiveButton(
                    android.R.string.ok,
                    (dialogInterface, i) -> {
                      final String newTimeString = timeEditText.getText().toString();
                      if (newTimeString.isEmpty()) {
                        showToast(R.string.no_set_time);
                        return;
                      }
                      long newTime = 0;
                      try {
                        newTime = Long.parseLong(newTimeString);
                      } catch (NumberFormatException e) {
                        showToast(R.string.invalid_set_time);
                        return;
                      }
                      mDevicePolicyManager.setTime(mAdminComponentName, newTime);
                    })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
  }

  @RequiresApi(VERSION_CODES.R)
  private void setAutoTimeZoneEnabled(boolean enabled) {
    mDevicePolicyManager.setAutoTimeZoneEnabled(mAdminComponentName, enabled);
  }

  @TargetApi(VERSION_CODES.P)
  private void showSetTimeZoneDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    final View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText timezoneEditText = dialogView.findViewById(R.id.input);
    final String currentTimezone = Calendar.getInstance().getTimeZone().getID();

    timezoneEditText.setText(currentTimezone);

    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.pre_main_title_time_zone)
            .setView(dialogView)
            .setPositiveButton(
                    android.R.string.ok,
                    (dialogInterface, i) -> {
                      final String newTimezone = timezoneEditText.getText().toString();
                      if (newTimezone.isEmpty()) {
                        showToast(R.string.no_timezone);
                        return;
                      }
                      final String[] ids = TimeZone.getAvailableIDs();
                      if (!Arrays.asList(ids).contains(newTimezone)) {
                        showToast(R.string.invalid_timezone);
                        return;
                      }
                      mDevicePolicyManager.setTimeZone(mAdminComponentName, newTimezone);
                    })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
  }

  @TargetApi(VERSION_CODES.P)
  private void showSetScreenBrightnessDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    final View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText brightnessEditText = dialogView.findViewById(R.id.input);
    final String oldBrightness = Settings.System.getString(requireActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);

    brightnessEditText.setHint(R.string.set_screen_brightness_hint);

    if (!TextUtils.isEmpty(oldBrightness)) {
      brightnessEditText.setText(oldBrightness);
    }

    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.set_screen_brightness)
            .setView(dialogView)
            .setPositiveButton(
                    android.R.string.ok,
                    (dialogInterface, i) -> {
                      final String brightness = brightnessEditText.getText().toString();
                      if (brightness.isEmpty()) {
                        showToast(R.string.no_screen_brightness);
                        return;
                      }
                      final int brightnessValue = Integer.parseInt(brightness);
                      if (brightnessValue > 255 || brightnessValue < 0) {
                        showToast(R.string.invalid_screen_brightness);
                        return;
                      }
                      mDevicePolicyManager.setSystemSetting(
                              mAdminComponentName, Settings.System.SCREEN_BRIGHTNESS, brightness);
                    })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
  }

  @SuppressLint("SetTextI18n")
  @TargetApi(VERSION_CODES.P)
  private void showSetScreenOffTimeoutDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    final View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText timeoutEditText = dialogView.findViewById(R.id.input);
    final String oldTimeout = Settings.System.getString(requireActivity().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
    final int oldTimeoutValue = Integer.parseInt(oldTimeout);

    timeoutEditText.setHint(R.string.set_screen_off_timeout_hint);

    if (!TextUtils.isEmpty(oldTimeout)) {
      timeoutEditText.setText(Integer.toString(oldTimeoutValue / 1000));
    }

    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.set_screen_off_timeout)
            .setView(dialogView)
            .setPositiveButton(
                    android.R.string.ok,
                    (dialogInterface, i) -> {
                      final String screenTimeout = timeoutEditText.getText().toString();
                      if (screenTimeout.isEmpty()) {
                        showToast(R.string.no_screen_off_timeout);
                        return;
                      }
                      final int screenTimeoutVaue = Integer.parseInt(screenTimeout);
                      if (screenTimeoutVaue < 0) {
                        showToast(R.string.invalid_screen_off_timeout);
                        return;
                      }
                      mDevicePolicyManager.setSystemSetting(
                              mAdminComponentName,
                              Settings.System.SCREEN_OFF_TIMEOUT,
                              Integer.toString(screenTimeoutVaue * 1000));
                    })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
  }

  @TargetApi(VERSION_CODES.P)
  private void showSetProfileNameDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    final View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText profileNameEditText = (EditText) dialogView.findViewById(R.id.input);
    profileNameEditText.setText("");

    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.set_profile_name)
            .setView(dialogView)
            .setPositiveButton(
                    android.R.string.ok,
                    (dialogInterface, i) -> {
                      final String newProfileName = profileNameEditText.getText().toString();
                      if (newProfileName.isEmpty()) {
                        showToast(R.string.no_profile_name);
                        return;
                      }
                      mDevicePolicyManager.setProfileName(mAdminComponentName, newProfileName);
                    })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
  }

  @TargetApi(VERSION_CODES.S)
  private void showSetOrganizationIdDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    final View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText organizationIdTextEdit = (EditText) dialogView.findViewById(R.id.input);
    organizationIdTextEdit.setText("");

    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.set_organization_id)
            .setView(dialogView)
            .setPositiveButton(
                    android.R.string.ok,
                    (dialogInterface, i) -> {
                      final String organizationId = organizationIdTextEdit.getText().toString();
                      if (organizationId.isEmpty()) {
                        showToast(R.string.organization_id_empty);
                        return;
                      }
                      setOrganizationId(organizationId);
                    })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
  }

  private void setOrganizationId(String organizationId) {
    try {
      ReflectionUtil.invoke(mDevicePolicyManager, "setOrganizationId", organizationId);
    } catch (ReflectionIsTemporaryException e) {
      showToast("Error setting organization ID");
    }

    loadEnrollmentSpecificId();
  }

  private void showSetDisableAccountManagementPrompt() {
    if (requireActivity().isFinishing()) {
      return;
    }

    View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
    final EditText input = (EditText) view.findViewById(R.id.input);
    input.setHint(R.string.account_type_hint);

    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.set_disable_account_management)
            .setView(view)
            .setPositiveButton(R.string.disable, (dialogInterface, i) -> {
              String accountType = input.getText().toString();
              setDisableAccountManagement(accountType, true);
            })
            .setNeutralButton(R.string.enable, (dialogInterface, i) -> {
              String accountType = input.getText().toString();
              setDisableAccountManagement(accountType, false);
            })
            .setNegativeButton(android.R.string.cancel, null /* Nothing to do */)
            .show();
  }

  private void setDisableAccountManagement(String accountType, boolean disabled) {
    if (!TextUtils.isEmpty(accountType)) {
      mDevicePolicyManager.setAccountManagementDisabled(mAdminComponentName, accountType, disabled);
      showToast(
              disabled ? R.string.account_management_disabled : R.string.account_management_enabled,
              accountType);
      return;
    }

    showToast(R.string.fail_to_set_account_management);
  }

  private void showDisableAccountTypeList() {
    if (requireActivity().isFinishing()) {
      return;
    }

    String[] disabledAccountTypeList = mDevicePolicyManager.getAccountTypesWithManagementDisabled();

    if (disabledAccountTypeList != null) {
      Arrays.sort(disabledAccountTypeList, String.CASE_INSENSITIVE_ORDER);
    }

    if (disabledAccountTypeList == null || disabledAccountTypeList.length == 0) {
      showToast(R.string.no_disabled_account);
    } else {
      new MaterialAlertDialogBuilder(requireActivity())
              .setTitle(R.string.list_of_disabled_account_types)
              .setAdapter(
                      new ArrayAdapter<>(
                              requireActivity(),
                              android.R.layout.simple_list_item_1,
                              android.R.id.text1,
                              disabledAccountTypeList),
                      null)
              .setPositiveButton(android.R.string.ok, null)
              .show();
    }
  }

  private void chooseAccount() {
    if (requireActivity().isFinishing()) {
      return;
    }

    List<Account> accounts = Arrays.asList(mAccountManager.getAccounts());

    if (accounts.isEmpty()) {
      showToast(R.string.no_accounts_available);
    } else {
      AccountArrayAdapter accountArrayAdapter = new AccountArrayAdapter(getActivity(), R.id.account_name, accounts);
      new MaterialAlertDialogBuilder(requireActivity())
              .setTitle(R.string.remove_account)
              .setAdapter(accountArrayAdapter, (dialog, position) -> removeAccount(accounts.get(position)))
              .show();
    }
  }

  private void removeAccount(Account account) {
    mAccountManager.removeAccount(
            account,
            requireActivity(),
            future -> {
              try {
                Bundle result = future.getResult();
                boolean success = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
                if (success) {
                  showToast(R.string.success_remove_account, account);
                } else {
                  showToast(R.string.fail_to_remove_account, account);
                }
              } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                showToast(R.string.fail_to_remove_account, account);
              }
            },
            null);
  }

  private void showEnableSystemAppsPrompt() {
    final List<String> disabledSystemApps = mDevicePolicyManagerGateway.getDisabledSystemApps();

    if (disabledSystemApps.isEmpty()) {
      showToast(R.string.no_disabled_system_apps);
    } else {
      AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(), R.id.pkg_name, disabledSystemApps, true);
      new MaterialAlertDialogBuilder(requireActivity())
              .setTitle(getString(R.string.enable_system_apps_title))
              .setAdapter(
                      appInfoArrayAdapter,
                      (dialog, position) -> {
                        String packageName = disabledSystemApps.get(position);
                        mDevicePolicyManagerGateway.enableSystemApp(
                                packageName,
                                (v) -> onSuccessShowToast("enableSystemApp", R.string.enable_system_apps_by_package_name_success_msg, packageName),
                                (e) -> onErrorLog("enableSystemApp(%s)", packageName));
                      })
              .show();
    }
  }

  private void showEnableSystemAppByPackageNamePrompt() {
    if (requireActivity().isFinishing()) {
      return;
    }

    LinearLayout inputContainer = (LinearLayout) requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText editText = (EditText) inputContainer.findViewById(R.id.input);

    editText.setHint(getString(R.string.package_name_hints));

    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.enable_system_apps_title))
            .setView(inputContainer)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                      String packageName = editText.getText().toString();
                      mDevicePolicyManagerGateway.enableSystemApp(
                              packageName,
                              (v) -> onSuccessShowToast("enableSystemApp", R.string.enable_system_apps_by_package_name_success_msg, packageName),
                              (e) -> onErrorShowToast("enableSystemApp", e, R.string.package_name_error, packageName));
                    })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
  }

  private void logAndShowToast(String message, Exception e) {
    showToast(message + ": " + e.getMessage());
  }

  private void addPasswordComplexityListToPreference(ListPreference pref) {
    List<CharSequence> entries = new ArrayList<>();
    List<CharSequence> values = new ArrayList<>();
    int size = PASSWORD_COMPLEXITY.size();
    for (int i = 0; i < size; i++) {
      entries.add(getString(PASSWORD_COMPLEXITY.valueAt(i)));
      values.add(Integer.toString(PASSWORD_COMPLEXITY.keyAt(i)));
    }
    pref.setEntries(entries.toArray(new CharSequence[size]));
    pref.setEntryValues(values.toArray(new CharSequence[size]));
  }

  /**
   * Pre O, lock task APIs were only available to the Device Owner. From O, they are also available
   * to affiliated profile owners. The XML file sets a deviceowner|profileowner restriction for
   * those restriction so further restricting them, if necessary
   */

  private boolean isDelegatedApp() {
    return false;
  }

  private boolean isCredentialManagerApp() {
    return false;
  }

  @TargetApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  private void showMtePolicyDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    int policy = mDevicePolicyManager.getMtePolicy();
    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.mte_policy)
        .setSingleChoiceItems(
            R.array.mte_policy_options,
            /* checkedItem= */ policy,
            (dialogInterface, i) -> mDevicePolicyManager.setMtePolicy(i))
        .setNegativeButton(R.string.close, null)
        .show();
  }

  @TargetApi(VERSION_CODES.Q)
  private void promptInstallUpdate() {
    new MaterialAlertDialogBuilder(requireActivity())
        .setMessage(R.string.install_update_prompt)
        .setTitle(R.string.install_update)
        .setPositiveButton(
            R.string.install_update_prompt_yes, (dialogInterface, i) -> installUpdate())
        .setNegativeButton(R.string.install_update_prompt_no, (dialogInterface, i) -> {})
        .create()
        .show();
  }

  @TargetApi(VERSION_CODES.Q)
  private void installUpdate() {
    File file = new File(requireActivity().getFilesDir(), "ota.zip");
    Uri uri = FileProvider.getUriForFile(requireActivity(), requireActivity().getPackageName() + ".fileprovider", file);
    mDevicePolicyManager.installSystemUpdate(
        mAdminComponentName,
        uri,
        new MainThreadExecutor(),
        new InstallSystemUpdateCallback() {
          @Override
          public void onInstallUpdateError(int errorCode, String errorMessage) {
            showToast("Error code: " + errorCode);
          }
        });
  }

  @RequiresApi(api = VERSION_CODES.M)
  private void testKeyCanBeUsedForSigning() {
    KeyChain.choosePrivateKeyAlias(requireActivity(), alias -> {
      if (alias == null) {
        // No value was chosen.
        showToast("No key chosen.");
        return;
      }

      new SignAndVerifyTask(
              getContext(),
              this::showToast)
          .execute(alias);
    },
        null,
        null,
        null,
        null);
  }

  @TargetApi(VERSION_CODES.O)
  private void showPendingSystemUpdate() {
    final SystemUpdateInfo updateInfo = mDevicePolicyManager.getPendingSystemUpdate(mAdminComponentName);
    if (updateInfo == null) {
      showToast(getString(R.string.update_info_no_update_toast));
    } else {
      final long timestamp = updateInfo.getReceivedTime();
      final String date = DateFormat.getDateTimeInstance().format(new Date(timestamp));
      final int securityState = updateInfo.getSecurityPatchState();
      final String securityText =
          securityState == SystemUpdateInfo.SECURITY_PATCH_STATE_FALSE
              ? getString(R.string.update_info_security_false)
              : (securityState == SystemUpdateInfo.SECURITY_PATCH_STATE_TRUE
                  ? getString(R.string.update_info_security_true)
                  : getString(R.string.update_info_security_unknown));

      new MaterialAlertDialogBuilder(requireActivity())
          .setTitle(R.string.update_info_title)
          .setMessage(getString(R.string.update_info_received, date, securityText))
          .setPositiveButton(android.R.string.ok, null)
          .show();
    }
  }

  private boolean isManagedProfileOwner() {
    return Util.isManagedProfileOwner(requireActivity());
  }

  @TargetApi(VERSION_CODES.O)
  private void lockNow() {
    if (Util.SDK_INT >= VERSION_CODES.O && isManagedProfileOwner()) {
      showLockNowPrompt();
      return;
    }
    DevicePolicyManagerGateway gateway = mDevicePolicyManagerGateway;
    if (Util.SDK_INT >= VERSION_CODES.N && isManagedProfileOwner()) {
      // Always call lock now on the parent for managed profile on N
      gateway = DevicePolicyManagerGatewayImpl.forParentProfile(requireActivity());
    }
    gateway.lockNow((v) -> onSuccessLog("lockNow"), (e) -> onErrorLog("lockNow", String.valueOf(e)));
  }

  /** Shows a prompt to ask for any flags to pass to lockNow. */
  @TargetApi(VERSION_CODES.O)
  private void showLockNowPrompt() {
    final LayoutInflater inflater = requireActivity().getLayoutInflater();
    final View dialogView = inflater.inflate(R.layout.lock_now_dialog_prompt, null);
    final CheckBox lockParentCheckBox = (CheckBox) dialogView.findViewById(R.id.lock_parent_checkbox);
    final CheckBox evictKeyCheckBox = (CheckBox) dialogView.findViewById(R.id.evict_ce_key_checkbox);

    lockParentCheckBox.setOnCheckedChangeListener((button, checked) -> evictKeyCheckBox.setEnabled(!checked));
    evictKeyCheckBox.setOnCheckedChangeListener((button, checked) -> lockParentCheckBox.setEnabled(!checked));

    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.lock_now)
            .setView(dialogView)
            .setPositiveButton(
                    android.R.string.ok,
                    (d, i) -> {
                      final int flags =
                              evictKeyCheckBox.isChecked()
                                      ? DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY
                                      : 0;
                      final DevicePolicyManagerGateway gateway =
                              lockParentCheckBox.isChecked()
                                      ? DevicePolicyManagerGatewayImpl.forParentProfile(requireActivity())
                                      : mDevicePolicyManagerGateway;
                    })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
  }

  @TargetApi(VERSION_CODES.M)
  private void setCameraDisabled(boolean disabled) {
    mDevicePolicyManager.setCameraDisabled(mAdminComponentName, disabled);
  }

  @TargetApi(VERSION_CODES.R)
  private void setCameraDisabledOnParent(boolean disabled) {
    DevicePolicyManager parentDpm = mDevicePolicyManager.getParentProfileInstance(mAdminComponentName);
    parentDpm.setCameraDisabled(mAdminComponentName, disabled);
  }

  @TargetApi(VERSION_CODES.N)
  private void setSecurityLoggingEnabled(boolean enabled) {
    mDevicePolicyManager.setSecurityLoggingEnabled(mAdminComponentName, enabled);
  }

  @TargetApi(VERSION_CODES.O)
  private void setBackupServiceEnabled(boolean enabled) {
    mDevicePolicyManager.setBackupServiceEnabled(mAdminComponentName, enabled);
  }

  @TargetApi(VERSION_CODES.R)
  private void setCommonCriteriaModeEnabled(boolean enabled) {
    mDevicePolicyManager.setCommonCriteriaModeEnabled(mAdminComponentName, enabled);
  }

  @TargetApi(VERSION_CODES.S)
  private void setUsbDataSignalingEnabled(boolean enabled) {
    mDevicePolicyManagerGateway.setUsbDataSignalingEnabled(enabled);
  }

  @TargetApi(VERSION_CODES.M)
  private void setKeyGuardDisabled(boolean disabled) {
    mDevicePolicyManagerGateway.setKeyguardDisabled(
            disabled,
            (v) -> onSuccessLog("setKeyGuardDisabled(%b)", disabled),
            (e) ->
                    showToast(
                            disabled ? R.string.unable_disable_keyguard : R.string.unable_enable_keyguard));

    if (!mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, disabled)) {
      // this should not happen
      if (disabled) {
        showToast(R.string.unable_disable_keyguard);
      } else {
        showToast(R.string.unable_enable_keyguard);
      }
    }
  }

  private void setScreenCaptureDisabled(boolean disabled) {
    mDevicePolicyManager.setScreenCaptureDisabled(mAdminComponentName, disabled);
  }

  @TargetApi(VERSION_CODES.R)
  private void setScreenCaptureDisabledOnParent(boolean disabled) {
    DevicePolicyManager parentDpm = mDevicePolicyManager.getParentProfileInstance(mAdminComponentName);
    parentDpm.setScreenCaptureDisabled(mAdminComponentName, disabled);
  }

  private boolean isDeviceOwner() {
    return mDevicePolicyManager.isDeviceOwnerApp(DhizukuVariables.PACKAGE_NAME);
  }

  @TargetApi(VERSION_CODES.O)
  private boolean isNetworkLoggingEnabled() {
    if (Util.SDK_INT < VERSION_CODES.S) {
      if (!(isDeviceOwner() || hasNetworkLoggingDelegation())) {
        return false;
      }
    } else {
      if (!(isDeviceOwner() || isManagedProfileOwner() || hasNetworkLoggingDelegation())) {
        return false;
      }
    }
    return mDevicePolicyManager.isNetworkLoggingEnabled(mAdminComponentName);
  }

  private boolean hasNetworkLoggingDelegation() {
    return Util.hasDelegation(requireActivity(), DevicePolicyManager.DELEGATION_NETWORK_LOGGING);
  }

  @TargetApi(VERSION_CODES.O)
  private boolean isSecurityLoggingEnabled() {
    return mDevicePolicyManager.isSecurityLoggingEnabled(mAdminComponentName);
  }

  @TargetApi(VERSION_CODES.N)
  private void requestBugReport() {
    mDevicePolicyManagerGateway.requestBugreport(
            (v) -> onSuccessLog("requestBugreport"),
            (e) ->
                    onErrorOrFailureShowToast(
                            "requestBugreport",
                            e,
                            R.string.bugreport_failure_throttled,
                            R.string.bugreport_failure_exception));
  }

  @TargetApi(VERSION_CODES.M)
  private void setStatusBarDisabled(boolean disable) {
    if (!mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, disable)) {
      if (disable) {
        showToast("Unable to disable status bar when lock password is set.");
      }
    }
  }

  @TargetApi(VERSION_CODES.P)
  private boolean installKeyPair(final PrivateKey key, final Certificate cert, final String alias, boolean isUserSelectable) {
    try {
      if (Util.SDK_INT >= VERSION_CODES.P) {

        return mDevicePolicyManager.installKeyPair(mAdminComponentName, key, new Certificate[] {cert}, alias, isUserSelectable ? DevicePolicyManager.INSTALLKEY_SET_USER_SELECTABLE : 0);
      } else {
        if (!isUserSelectable) {
          throw new IllegalArgumentException("Cannot set key as non-user-selectable prior to P");
        }
        return mDevicePolicyManager.installKeyPair(mAdminComponentName, key, cert, alias);
      }
    } catch (SecurityException e) {
      return false;
    }
  }

  private void generateKeyPair(final KeyGenerationParameters params) {
    new GenerateKeyAndCertificateTask(params, requireActivity(), mAdminComponentName).execute();
  }

  /** Dispatches an intent to capture image or video. */
  private void dispatchCaptureIntent(String action, int requestCode, Uri storageUri) {
    final Intent captureIntent = new Intent(action);
    if (captureIntent.resolveActivity(mPackageManager) != null) {
      captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, storageUri);
      startActivityForResult(captureIntent, requestCode);
    } else {
      showToast(R.string.camera_app_not_found);
    }
  }

  /** Creates a content uri to be used with the capture intent. */
  private Uri getStorageUri(String fileName) {
    final String filePath = requireActivity().getFilesDir() + File.separator + "media" + File.separator + fileName;
    final File file = new File(filePath);
    // Create the folder if it doesn't exist.
    file.getParentFile().mkdirs();
    return FileProvider.getUriForFile(requireActivity(), requireActivity().getPackageName() + ".fileprovider", file);
  }

  /**
   * Shows a list of primary user apps in a dialog.
   *
   * @param dialogTitle the title to show for the dialog
   * @param callback will be called with the list apps that the user has selected when he closes the
   *     dialog. The callback is not fired if the user cancels.
   */
  private void showManageLockTaskListPrompt(int dialogTitle, final ManageLockTaskListCallback callback) {
    if (requireActivity().isFinishing()) {
      return;
    }
    Intent launcherIntent = Util.getLauncherIntent(requireActivity());
    final List<ResolveInfo> primaryUserAppList = mPackageManager.queryIntentActivities(launcherIntent, 0);
    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
    homeIntent.addCategory(Intent.CATEGORY_HOME);
    // Also show the default launcher in this list
    final ResolveInfo defaultLauncher = mPackageManager.resolveActivity(homeIntent, 0);
    primaryUserAppList.add(defaultLauncher);
    if (primaryUserAppList.isEmpty()) {
      showToast(R.string.no_primary_app_available);
    } else {
      Collections.sort(primaryUserAppList, new ResolveInfo.DisplayNameComparator(mPackageManager));
      final LockTaskAppInfoArrayAdapter appInfoArrayAdapter = new LockTaskAppInfoArrayAdapter(requireActivity(), R.id.pkg_name, primaryUserAppList);
      ListView listView = new ListView(requireActivity());
      listView.setAdapter(appInfoArrayAdapter);
      listView.setOnItemClickListener((parent, view, position, id) -> appInfoArrayAdapter.onItemClick(parent, view, position, id));

      new MaterialAlertDialogBuilder(requireActivity())
          .setTitle(getString(dialogTitle))
          .setView(listView)
          .setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String[] lockTaskEnabledArray = appInfoArrayAdapter.getLockTaskList();
            callback.onPositiveButtonClicked(lockTaskEnabledArray);
          })
          .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
          .show();
    }
  }

  /**
   * Shows a prompt to collect a package name and checks whether the lock task for the corresponding
   * app is permitted or not.
   */
  private void showCheckLockTaskPermittedPrompt() {
    if (requireActivity().isFinishing()) {
      return;
    }
    View view = requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText input = (EditText) view.findViewById(R.id.input);
    input.setHint(getString(R.string.input_package_name_hints));

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.check_lock_task_permitted))
        .setView(view)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          String packageName = input.getText().toString();
          boolean isLockTaskPermitted =
              mDevicePolicyManagerGateway.isLockTaskPermitted(packageName);
          showToast(
              isLockTaskPermitted
                  ? R.string.check_lock_task_permitted_result_permitted
                  : R.string.check_lock_task_permitted_result_not_permitted);
          dialog.dismiss();
        })
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
        .show();
  }

  private void showResetPasswordPrompt() {
    View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.reset_password_dialog, null);

    final EditText passwordView = (EditText) dialogView.findViewById(R.id.password);
    final CheckBox requireEntry =
        (CheckBox) dialogView.findViewById(R.id.require_password_entry_checkbox);
    final CheckBox dontRequireOnBoot =
        (CheckBox) dialogView.findViewById(R.id.dont_require_password_on_boot_checkbox);

    DialogInterface.OnClickListener resetListener = (dialogInterface, which) -> {
      String password = passwordView.getText().toString();
      if (TextUtils.isEmpty(password)) {
        password = null;
      }

      int flags = 0;
      flags |=
          requireEntry.isChecked() ? DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY : 0;
      flags |=
          dontRequireOnBoot.isChecked()
              ? DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT
              : 0;

      boolean ok = false;
      try {
        ok = mDevicePolicyManager.resetPassword(password, flags);
      } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
        // Not allowed to set password or trying to set a bad password, eg. 2 characters
        // where system minimum length is 4.
        Log.w(TAG, "Failed to reset password", e);
      }
      showToast(ok ? R.string.password_reset_success : R.string.password_reset_failed);
    };

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.reset_password)
        .setView(dialogView)
        .setPositiveButton(android.R.string.ok, resetListener)
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  /**
   * Shows a prompt to ask for confirmation on wiping the data and also provide an option to set if
   * external storage and factory reset protection data also needs to wiped.
   */
  private void showWipeDataPrompt() {
    final LayoutInflater inflater = requireActivity().getLayoutInflater();
    final View dialogView = inflater.inflate(R.layout.wipe_data_dialog_prompt, null);
    final CheckBox externalStorageCheckBox =
        (CheckBox) dialogView.findViewById(R.id.external_storage_checkbox);
    final CheckBox resetProtectionCheckBox =
        (CheckBox) dialogView.findViewById(R.id.reset_protection_checkbox);

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.wipe_data_title)
        .setView(dialogView)
        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
          int flags = 0;
          flags |= (externalStorageCheckBox.isChecked() ? DevicePolicyManager.WIPE_EXTERNAL_STORAGE : 0);
          flags |= (resetProtectionCheckBox.isChecked() ? DevicePolicyManager.WIPE_RESET_PROTECTION_DATA : 0);
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  /** Shows a prompt to ask for confirmation on removing device owner. */
  private void showRemoveDeviceOwnerPrompt() {
    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.remove_device_owner_title)
            .setMessage(R.string.remove_device_owner_confirmation)
            .setPositiveButton(android.R.string.ok, (d, i) -> mDevicePolicyManagerGateway.clearDeviceOwnerApp(
                                    (v) -> {
                                      if (requireActivity().isFinishing()) {
                                        showToast(R.string.device_owner_removed);
                                        requireActivity().finish();
                                      }
                                    },
                                    (e) -> onErrorLog("clearDeviceOwnerApp", e)))
            .setNegativeButton(android.R.string.cancel, null)
            .show();
  }

  /** Shows a message box with the device wifi mac address. */
  @TargetApi(VERSION_CODES.N)
  private void showWifiMacAddress() {
    final String macAddress = mDevicePolicyManager.getWifiMacAddress(mAdminComponentName);
    final String message = macAddress != null ? macAddress : getString(R.string.show_wifi_mac_address_not_available_msg);
    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.show_wifi_mac_address_title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
        .show();
  }

  @TargetApi(VERSION_CODES.M)
  private void showSetPermissionPolicyDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }
    View setPermissionPolicyView = requireActivity().getLayoutInflater().inflate(R.layout.set_permission_policy, null);
    final RadioGroup permissionGroup = (RadioGroup) setPermissionPolicyView.findViewById(R.id.set_permission_group);

    int permissionPolicy = mDevicePolicyManager.getPermissionPolicy(mAdminComponentName);
    switch (permissionPolicy) {
      case DevicePolicyManager.PERMISSION_POLICY_PROMPT ->
              ((RadioButton) permissionGroup.findViewById(R.id.prompt)).toggle();
      case DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT ->
              ((RadioButton) permissionGroup.findViewById(R.id.accept)).toggle();
      case DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY ->
              ((RadioButton) permissionGroup.findViewById(R.id.deny)).toggle();
    }

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.set_default_permission_policy))
        .setView(setPermissionPolicyView)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          int policy = 0;
          int checked = permissionGroup.getCheckedRadioButtonId();
          if (checked == R.id.prompt) {
            policy = DevicePolicyManager.PERMISSION_POLICY_PROMPT;
          } else if (checked == R.id.accept) {
            policy = DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT;
          } else if (checked == R.id.deny) {
            policy = DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY;
          }
          mDevicePolicyManager.setPermissionPolicy(mAdminComponentName, policy);
          dialog.dismiss();
        })
        .show();
  }

  /**
   * For user creation: Shows a prompt asking for the username of the new user and whether the setup
   * wizard should be skipped.
   */
  @TargetApi(VERSION_CODES.N)
  private void showCreateAndManageUserPrompt() {
    if (requireActivity().isFinishing()) {
      return;
    }

    final View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.create_and_manage_user_dialog_prompt, null);

    final EditText userNameEditText = (EditText) dialogView.findViewById(R.id.user_name);
    userNameEditText.setHint(R.string.enter_username_hint);
    final CheckBox skipSetupWizardCheckBox =
        (CheckBox) dialogView.findViewById(R.id.skip_setup_wizard_checkbox);
    final CheckBox makeUserEphemeralCheckBox =
        (CheckBox) dialogView.findViewById(R.id.make_user_ephemeral_checkbox);
    final CheckBox leaveAllSystemAppsEnabled =
        (CheckBox) dialogView.findViewById(R.id.leave_all_system_apps_enabled_checkbox);
    if (Util.SDK_INT < VERSION_CODES.P) {
      makeUserEphemeralCheckBox.setEnabled(false);
      leaveAllSystemAppsEnabled.setEnabled(false);
    }

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.create_and_manage_user)
        .setView(dialogView)
        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
          String name = userNameEditText.getText().toString();
          if (!TextUtils.isEmpty(name)) {
            int flags = 0;
            if (skipSetupWizardCheckBox.isChecked()) {
              flags |= DevicePolicyManager.SKIP_SETUP_WIZARD;
            }
            if (makeUserEphemeralCheckBox.isChecked()) {
              flags |= DevicePolicyManager.MAKE_USER_EPHEMERAL;
            }
            if (leaveAllSystemAppsEnabled.isChecked()) {
              flags |= DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED;
            }

            mDevicePolicyManagerGateway.createAndManageUser(
                name,
                flags,
                (u) ->
                    showToast(R.string.user_created, mUserManager.getSerialNumberForUser(u)),
                (e) -> showToast(R.string.failed_to_create_user));
          }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  /**
   * For user removal: Shows a prompt for a user serial number. The associated user will be removed.
   */
  private void showRemoveUserPromptLegacy() {
    if (getActivity() == null || getActivity().isFinishing()) {
      return;
    }
    View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
    final EditText input = (EditText) view.findViewById(R.id.input);
    input.setHint(R.string.enter_user_id);
    input.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.remove_user)
        .setView(view)
        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
          long serialNumber = -1;
          try {
            serialNumber = Long.parseLong(input.getText().toString());
            removeUser(mDevicePolicyManagerGateway.getUserHandle(serialNumber));
          } catch (NumberFormatException e) {
            // Error message is printed in the next line.
          }
        })
        .show();
  }

  private void removeUser(UserHandle userHandle) {
    mDevicePolicyManagerGateway.removeUser(
        userHandle,
        (u) -> onSuccessShowToast("removeUser()", R.string.user_removed),
        (e) -> onErrorShowToast("removeUser()", e, R.string.failed_to_remove_user));
  }

  /**
   * For user removal: If the device is P or above, shows a prompt for choosing a user to be
   * removed. Otherwise, shows a prompt for user to enter a serial number, as {@link
   * DevicePolicyManager#getSecondaryUsers} is not available.
   */
  private void showRemoveUserPrompt() {
    if (Util.SDK_INT >= VERSION_CODES.P) {
      showChooseUserPrompt(R.string.remove_user, this::removeUser);
    } else {
      showRemoveUserPromptLegacy();
    }
  }

  /** For user switch: Shows a prompt for choosing a user to be switched to. */
  @TargetApi(VERSION_CODES.P)
  private void showSwitchUserPrompt() {
    showChooseUserPrompt(
        R.string.switch_user,
        userHandle -> {
          mDevicePolicyManagerGateway.switchUser(
              userHandle,
              (v) -> onSuccessShowToast("switchUser", R.string.user_switched),
              (e) -> onErrorShowToast("switchUser", e, R.string.failed_to_switch_user));
        });
  }

  /**
   * For starting user in background: Shows a prompt for choosing a user to be started in
   * background.
   */
  @TargetApi(VERSION_CODES.P)
  private void showStartUserInBackgroundPrompt() {
    showChooseUserPrompt(
        R.string.start_user_in_background,
        userHandle -> {
          mDevicePolicyManagerGateway.startUserInBackground(
              userHandle,
              (v) ->
                  onSuccessShowToast("startUserInBackground", R.string.user_started_in_background),
              (e) ->
                  onErrorShowToast(
                      "startUserInBackground", e, R.string.failed_to_start_user_in_background));
        });
  }

  /** For user stop: Shows a prompt for choosing a user to be stopped. */
  @TargetApi(VERSION_CODES.P)
  private void showStopUserPrompt() {
    showChooseUserPrompt(
        R.string.stop_user,
        userHandle -> {
          mDevicePolicyManagerGateway.stopUser(
              userHandle,
              (v) -> onSuccessShowToast("stopUser", R.string.user_stopped),
              (e) -> onErrorShowToast("stopUser", e, R.string.failed_to_stop_user));
        });
  }

  private interface UserCallback {
    void onUserChosen(UserHandle userHandle);
  }

  /** Shows a prompt for choosing a user. The callback will be invoked with chosen user. */
  @TargetApi(VERSION_CODES.P)
  private void showChooseUserPrompt(int titleResId, UserCallback callback) {
    if (requireActivity().isFinishing()) {
      return;
    }

    List<UserHandle> secondaryUsers = mDevicePolicyManager.getSecondaryUsers(mAdminComponentName);
    if (secondaryUsers.isEmpty()) {
      showToast(R.string.no_secondary_users_available);
    } else {
      UserArrayAdapter userArrayAdapter = new UserArrayAdapter(getActivity(), R.id.user_name, secondaryUsers);
      new MaterialAlertDialogBuilder(requireActivity())
          .setTitle(titleResId)
          .setAdapter(
              userArrayAdapter,
              (dialog, position) -> callback.onUserChosen(secondaryUsers.get(position)))
          .show();
    }
  }

  /** Logout the current user. */
  @TargetApi(VERSION_CODES.P)
  private void logoutUser() {
    int status = mDevicePolicyManager.logoutUser(mAdminComponentName);
    showToast(status == USER_OPERATION_SUCCESS ? R.string.user_logouted : R.string.failed_to_logout_user);
  }

  /** Asks for the package name whose uninstallation should be blocked / unblocked. */
  private void showBlockUninstallationByPackageNamePrompt() {
    if (requireActivity().isFinishing()) {
      return;
    }
    View view = LayoutInflater.from(requireActivity()).inflate(R.layout.simple_edittext, null);
    final EditText input = (EditText) view.findViewById(R.id.input);
    input.setHint(getString(R.string.input_package_name_hints));
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
    builder
        .setTitle(R.string.block_uninstallation_title)
        .setView(view)
        .setPositiveButton(R.string.block, (dialogInterface, i) -> {
          String pkgName = input.getText().toString();
          if (!TextUtils.isEmpty(pkgName)) {
            mDevicePolicyManager.setUninstallBlocked(mAdminComponentName, pkgName, true);
            showToast(R.string.uninstallation_blocked, pkgName);
          } else {
            showToast(R.string.block_uninstallation_failed_invalid_pkgname);
          }
        })
        .setNeutralButton(R.string.unblock, (dialogInterface, i) -> {
          String pkgName = input.getText().toString();
          if (!TextUtils.isEmpty(pkgName)) {
            mDevicePolicyManager.setUninstallBlocked(mAdminComponentName, pkgName, false);
            showToast(R.string.uninstallation_allowed, pkgName);
          } else {
            showToast(R.string.block_uninstallation_failed_invalid_pkgname);
          }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  @TargetApi(VERSION_CODES.S)
  private int getRequiredComplexity(DevicePolicyManager dpm) {
    return dpm.getRequiredPasswordComplexity();
  }

  // NOTE: The setRequiredPasswordComplexity call is gated by a check in device_policy_header.xml,
  // where the minSdkVersion for it is specified. That prevents it from being callable on devices
  // running older releases and obviates the need for a target sdk check here.
  @TargetApi(VERSION_CODES.S)
  private void setRequiredPasswordComplexity(int complexity) {
    setRequiredPasswordComplexity(mDevicePolicyManager, complexity);
  }

  // NOTE: The setRequiredPasswordComplexity call is gated by a check in device_policy_header.xml,
  // where the minSdkVersion for it is specified. That prevents it from being callable on devices
  // running older releases and obviates the need for a target sdk check here.
  @TargetApi(VERSION_CODES.S)
  private void setRequiredPasswordComplexityOnParent(int complexity) {
    setRequiredPasswordComplexity(mDevicePolicyManager.getParentProfileInstance(mAdminComponentName), complexity);
  }

  // NOTE: The setRequiredPasswordComplexity call is gated by a check in device_policy_header.xml,
  // where the minSdkVersion for it is specified. That prevents it from being callable on devices
  // running older releases and obviates the need for a target sdk check here.
  @TargetApi(VERSION_CODES.S)
  private void setRequiredPasswordComplexity(DevicePolicyManager dpm, int complexity) {
    dpm.setRequiredPasswordComplexity(complexity);
    loadPasswordCompliant();
    loadPasswordComplexity();
    loadRequiredPasswordComplexity();
  }

  private static int parseInt(String str, int defaultValue) {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private void showConfigurePolicyAndManageCredentialsPrompt() {
    if (requireActivity().isFinishing()) {
      return;
    }
    final String appUriPolicyName = "appUriPolicy";
    final String defaultPolicy =
            """
                    com.android.chrome#client.badssl.com:443#testAlias
                    com.android.chrome#prod.idrix.eu/secure#testAlias
                    de.blinkt.openvpn#192.168.0.1#vpnAlias""";
    LinearLayout inputContainer = (LinearLayout) requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText editText = (EditText) inputContainer.findViewById(R.id.input);
    editText.setSingleLine(false);
    editText.setHint(defaultPolicy);
    editText.setText(
        PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .getString(appUriPolicyName, defaultPolicy));

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.request_manage_credentials))
        .setView(inputContainer)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          String policy = editText.getText().toString();
          if (TextUtils.isEmpty(policy)) policy = defaultPolicy;
          try {
            requestToManageCredentials(policy);
            SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putString(appUriPolicyName, policy);
            editor.apply();
          } finally {
            dialog.dismiss();
          }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void requestToManageCredentials(String policyStr) {
    AppUriAuthenticationPolicy.Builder builder = new AppUriAuthenticationPolicy.Builder();
    String[] policies = policyStr.split("\n");
    for (String policy : policies) {
      String[] segments = policy.split("#");
      if (segments.length != 3) {
        showToast(String.format(getString(R.string.invalid_app_uri_policy), policy));
        return;
      }
      builder.addAppAndUriMapping(
              segments[0], new Uri.Builder().authority(segments[1]).build(), segments[2]);
    }
    startActivityForResult(
        KeyChain.createManageCredentialsIntent(builder.build()),
        REQUEST_MANAGE_CREDENTIALS_REQUEST_CODE);
  }

  /**
   * Imports a certificate to the managed profile. If the provided password failed to decrypt the
   * given certificate, shows a try again prompt. Otherwise, shows a prompt for the certificate
   * alias.
   *
   * @param intent Intent that contains the certificate data uri.
   * @param password The password to decrypt the certificate.
   */
  private void importKeyCertificateFromIntent(Intent intent, String password) {
    importKeyCertificateFromIntent(intent, password, 0 /* first try */);
  }

  /**
   * Imports a certificate to the managed profile. If the provided decryption password is incorrect,
   * shows a try again prompt. Otherwise, shows a prompt for the certificate alias.
   *
   * @param intent Intent that contains the certificate data uri.
   * @param password The password to decrypt the certificate.
   * @param attempts The number of times user entered incorrect password.
   */
  private void importKeyCertificateFromIntent(Intent intent, String password, int attempts) {
    if (requireActivity().isFinishing()) {
      return;
    }

    if (intent != null && intent.getData() != null) {
      // If the password is null, try to decrypt the certificate with an empty password.
      if (password == null) {
        password = "";
      }
      try {
        CertificateUtil.PKCS12ParseInfo parseInfo = CertificateUtil.parsePKCS12Certificate(requireActivity().getContentResolver(), intent.getData(), password);
        showPromptForKeyCertificateAlias(parseInfo.privateKey, parseInfo.certificate, parseInfo.alias);
      } catch (KeyStoreException
          | FileNotFoundException
          | CertificateException
          | UnrecoverableKeyException
          | NoSuchAlgorithmException e) {
        Log.e(TAG, "Unable to load key", e);
      } catch (IOException e) {
        showPromptForCertificatePassword(intent, ++attempts);
      } catch (ClassCastException e) {
        showToast(R.string.not_a_key_certificate);
      }
    }
  }

  /**
   * Shows a prompt to ask for the certificate password. If the certificate password is correct,
   * import the private key and certificate.
   *
   * @param intent Intent that contains the certificate data uri.
   * @param attempts The number of times user entered incorrect password.
   */
  private void showPromptForCertificatePassword(final Intent intent, final int attempts) {
    if (requireActivity().isFinishing()) {
      return;
    }
    View passwordInputView = requireActivity().getLayoutInflater().inflate(R.layout.certificate_password_prompt, null);
    final EditText input = (EditText) passwordInputView.findViewById(R.id.password_input);
    if (attempts > 1) {
      passwordInputView.findViewById(R.id.incorrect_password).setVisibility(View.VISIBLE);
    }
    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.certificate_password_prompt_title))
        .setView(passwordInputView)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          String userPassword = input.getText().toString();
          importKeyCertificateFromIntent(intent, userPassword, attempts);
          dialog.dismiss();
        })
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
        .show();
  }

  /**
   * Shows a prompt to ask for the certificate alias. This alias will be imported together with the
   * private key and certificate.
   *
   * @param key The private key of a certificate.
   * @param certificate The certificate will be imported.
   * @param alias A name that represents the certificate in the profile.
   */
  private void showPromptForKeyCertificateAlias(final PrivateKey key, final Certificate certificate, String alias) {
    if (requireActivity().isFinishing() || key == null || certificate == null) {
      return;
    }
    View passwordInputView = requireActivity().getLayoutInflater().inflate(R.layout.certificate_alias_prompt, null);
    final EditText input = (EditText) passwordInputView.findViewById(R.id.alias_input);
    if (!TextUtils.isEmpty(alias)) {
      input.setText(alias);
      input.selectAll();
    }

    final CheckBox userSelectableCheckbox =
        passwordInputView.findViewById(R.id.alias_user_selectable);
    userSelectableCheckbox.setEnabled(Util.SDK_INT >= VERSION_CODES.P);
    userSelectableCheckbox.setChecked(Util.SDK_INT < VERSION_CODES.P);

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.certificate_alias_prompt_title))
        .setView(passwordInputView)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          String alias1 = input.getText().toString();
          boolean isUserSelectable = userSelectableCheckbox.isChecked();
          if (installKeyPair(key, certificate, alias1, isUserSelectable)) {
            showToast(R.string.certificate_added, alias1);
          } else {
            showToast(R.string.certificate_add_failed, alias1);
          }
          dialog.dismiss();
        })
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
        .show();
  }

  /**
   * Shows a prompt to ask for the certificate alias. A key will be generated for this alias.
   *
   * @param alias A name that represents the certificate in the profile.
   */
  private void showPromptForGeneratedKeyAlias(String alias) {
    if (requireActivity().isFinishing()) {
      return;
    }

    View aliasNamingView = requireActivity().getLayoutInflater().inflate(R.layout.key_generation_prompt, null);
    final EditText input = (EditText) aliasNamingView.findViewById(R.id.alias_input);
    if (!TextUtils.isEmpty(alias)) {
      input.setText(alias);
      input.selectAll();
    }

    final CheckBox userSelectableCheckbox =
        aliasNamingView.findViewById(R.id.alias_user_selectable);
    userSelectableCheckbox.setChecked(Util.SDK_INT < VERSION_CODES.P);

    final CheckBox ecKeyCheckbox = aliasNamingView.findViewById(R.id.generate_ec_key);

    // Attestation check-boxes
    final CheckBox includeAttestationChallengeCheckbox =
        aliasNamingView.findViewById(R.id.include_key_attestation_challenge);
    final CheckBox deviceBrandAttestationCheckbox =
        aliasNamingView.findViewById(R.id.include_device_brand_attestation);
    final CheckBox deviceSerialAttestationCheckbox =
        aliasNamingView.findViewById(R.id.include_device_serial_in_attestation);
    final CheckBox deviceImeiAttestationCheckbox =
        aliasNamingView.findViewById(R.id.include_device_imei_in_attestation);
    final CheckBox deviceMeidAttestationCheckbox =
        aliasNamingView.findViewById(R.id.include_device_meid_in_attestation);
    final CheckBox useStrongBoxCheckbox = aliasNamingView.findViewById(R.id.use_strongbox);
    final CheckBox useIndividualAttestationCheckbox =
        aliasNamingView.findViewById(R.id.use_individual_attestation);
    useIndividualAttestationCheckbox.setEnabled(Util.SDK_INT >= VERSION_CODES.R);

    // Custom Challenge input
    final EditText customChallengeInput = aliasNamingView.findViewById(R.id.custom_challenge_input);

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.certificate_alias_prompt_title))
        .setView(aliasNamingView)
        .setPositiveButton(
            android.R.string.ok, (dialog, which) -> {
              KeyGenerationParameters.Builder paramsBuilder =
                  new KeyGenerationParameters.Builder();
              paramsBuilder.setAlias(input.getText().toString());
              paramsBuilder.setIsUserSelectable(userSelectableCheckbox.isChecked());

              if (includeAttestationChallengeCheckbox.isChecked()) {
                String customChallenge = customChallengeInput.getText().toString().trim();
                byte[] decodedChallenge = Base64.decode(customChallenge, Base64.DEFAULT);
                paramsBuilder.setAttestationChallenge(decodedChallenge);
              }

              int idAttestationFlags = 0;
              if (deviceBrandAttestationCheckbox.isChecked()) {
                idAttestationFlags |= DevicePolicyManager.ID_TYPE_BASE_INFO;
              }
              if (deviceSerialAttestationCheckbox.isChecked()) {
                idAttestationFlags |= DevicePolicyManager.ID_TYPE_SERIAL;
              }
              if (deviceImeiAttestationCheckbox.isChecked()) {
                idAttestationFlags |= DevicePolicyManager.ID_TYPE_IMEI;
              }
              if (deviceMeidAttestationCheckbox.isChecked()) {
                idAttestationFlags |= DevicePolicyManager.ID_TYPE_MEID;
              }
              if (useIndividualAttestationCheckbox.isChecked()) {
                idAttestationFlags |= DevicePolicyManager.ID_TYPE_INDIVIDUAL_ATTESTATION;
              }
              paramsBuilder.setIdAttestationFlags(idAttestationFlags);
              paramsBuilder.setUseStrongBox(useStrongBoxCheckbox.isChecked());
              paramsBuilder.setGenerateEcKey(ecKeyCheckbox.isChecked());

              generateKeyPair(paramsBuilder.build());
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  /**
   * Selects a private/public key pair to uninstall, using the system dialog to choose an alias.
   *
   * <p>Once the alias is chosen and deleted, a {@link Toast} shows status- success or failure.
   */
  @TargetApi(VERSION_CODES.N)
  private void choosePrivateKeyForRemoval() {
    KeyChain.choosePrivateKeyAlias(requireActivity(), alias -> {
      if (alias == null) {
        // No value was chosen.
        return;
      }

      final boolean removed = mDevicePolicyManager.removeKeyPair(mAdminComponentName, alias);

      requireActivity().runOnUiThread(() -> {
        if (removed) {
          showToast(R.string.remove_keypair_successfully);
        } else {
          showToast(R.string.remove_keypair_fail);
        }
      });
    }, /* keyTypes[] */
        null, /* issuers[] */
        null, /* uri */
        null, /* alias */
        null);
  }

  /**
   * Imports a CA certificate from the given data URI.
   *
   * @param intent Intent that contains the CA data URI.
   */
  private void importCaCertificateFromIntent(Intent intent) {
    if (requireActivity().isFinishing()) {
      return;
    }

    if (intent != null && intent.getData() != null) {
      ContentResolver cr = requireActivity().getContentResolver();
      boolean isCaInstalled = false;
      try {
        InputStream certificateInputStream = cr.openInputStream(intent.getData());
        isCaInstalled =
            Util.installCaCertificate(
                certificateInputStream, mDevicePolicyManager, mAdminComponentName);
      } catch (FileNotFoundException e) {
        Log.e(TAG, "importCaCertificateFromIntent: ", e);
      }
      showToast(isCaInstalled ? R.string.install_ca_successfully : R.string.install_ca_fail);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == Activity.RESULT_OK) {
      switch (requestCode) {
        case INSTALL_KEY_CERTIFICATE_REQUEST_CODE -> importKeyCertificateFromIntent(data, "");
        case INSTALL_CA_CERTIFICATE_REQUEST_CODE -> importCaCertificateFromIntent(data);
        case CAPTURE_IMAGE_REQUEST_CODE -> showFragment(
                MediaDisplayFragment.newInstance(
                        MediaDisplayFragment.REQUEST_DISPLAY_IMAGE, mImageUri));
        case CAPTURE_VIDEO_REQUEST_CODE -> showFragment(
                MediaDisplayFragment.newInstance(
                        MediaDisplayFragment.REQUEST_DISPLAY_VIDEO, mVideoUri));
        case INSTALL_APK_PACKAGE_REQUEST_CODE -> installApkPackageFromIntent(data);
      }
    }
  }

  /** Shows a list of installed CA certificates. */
  private void showCaCertificateList() {
    if (getActivity() == null || getActivity().isFinishing()) {
      return;
    }
    // Avoid starting the same task twice.
    if (mShowCaCertificateListTask != null && !mShowCaCertificateListTask.isCancelled()) {
      mShowCaCertificateListTask.cancel(true);
    }
    mShowCaCertificateListTask = new ShowCaCertificateListTask();
    mShowCaCertificateListTask.execute();
  }

  /**
   * Shows a dialog that asks the user for a host and port, then sets the recommended global proxy
   * to these values.
   */
  private void showSetGlobalHttpProxyDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    final View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.proxy_config_dialog, null);
    final EditText hostEditText = (EditText) dialogView.findViewById(R.id.proxy_host);
    final EditText portEditText = (EditText) dialogView.findViewById(R.id.proxy_port);
    final String host = System.getProperty("http.proxyHost");
    if (!TextUtils.isEmpty(host)) {
      hostEditText.setText(host);
      portEditText.setText(System.getProperty("http.proxyPort"));
    }

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.set_global_http_proxy)
        .setView(dialogView)
        .setPositiveButton(
            android.R.string.ok,
            (dialogInterface, i) -> {
              final String hostString = hostEditText.getText().toString();
              if (hostString.isEmpty()) {
                showToast(R.string.no_host);
                return;
              }
              final String portString = portEditText.getText().toString();
              if (portString.isEmpty()) {
                showToast(R.string.no_port);
                return;
              }
              final int port = Integer.parseInt(portString);
              if (port > 65535) {
                showToast(R.string.port_out_of_range);
                return;
              }
              mDevicePolicyManager.setRecommendedGlobalProxy(
                  mAdminComponentName, ProxyInfo.buildDirectProxy(hostString, port));
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  /**
   * Displays an alert dialog that allows the user to select applications from all non-system
   * applications installed on the current profile. After the user selects an app, this app can't be
   * uninstallation.
   */
  private void showBlockUninstallationPrompt() {
    if (requireActivity().isFinishing()) {
      return;
    }

    List<ApplicationInfo> applicationInfoList = mPackageManager.getInstalledApplications(0 /* No flag */);
    List<ResolveInfo> resolveInfoList = new ArrayList<ResolveInfo>();
    Collections.sort(applicationInfoList, new ApplicationInfo.DisplayNameComparator(mPackageManager));
    for (ApplicationInfo applicationInfo : applicationInfoList) {
      // Ignore system apps because they can't be uninstalled.
      if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.resolvePackageName = applicationInfo.packageName;
        resolveInfoList.add(resolveInfo);
      }
    }

    final BlockUninstallationInfoArrayAdapter blockUninstallationInfoArrayAdapter = new BlockUninstallationInfoArrayAdapter(requireActivity(), R.id.pkg_name, resolveInfoList, mAdminComponentName);
    ListView listview = new ListView(requireActivity());
    listview.setAdapter(blockUninstallationInfoArrayAdapter);
    listview.setOnItemClickListener(blockUninstallationInfoArrayAdapter);

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.block_uninstallation_title)
        .setView(listview)
        .setPositiveButton(R.string.close, null /* Nothing to do */)
        .show();
  }



  /** Shows a prompt to ask for package name which is used to install an existing package. */
  @TargetApi(VERSION_CODES.P)
  private void showInstallExistingPackagePrompt() {
    if (requireActivity().isFinishing()) {
      return;
    }
    LinearLayout inputContainer = (LinearLayout) requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText editText = inputContainer.findViewById(R.id.input);
    editText.setHint(getString(R.string.package_name_hints));

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.install_existing_packages_title))
        .setView(inputContainer)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
              final String packageName = editText.getText().toString();
              boolean success =
                  mDevicePolicyManager.installExistingPackage(mAdminComponentName, packageName);
              showToast(
                  success
                      ? R.string.install_existing_packages_success_msg
                      : R.string.package_name_error,
                  packageName);
              dialog.dismiss();
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  @TargetApi(VERSION_CODES.M)
  private void installApkPackageFromIntent(Intent intent) {
    if (requireActivity().isFinishing()) {
      return;
    }

    if (intent != null && intent.getData() != null) {
      try {
        InputStream inputStream = requireActivity().getContentResolver().openInputStream(intent.getData());
        PackageInstallationUtils.installPackage(requireActivity(), inputStream, null);
      } catch (IOException e) {
        showToast("Failed to open APK file");
        Log.e(TAG, "Failed to open APK file", e);
      }
    }
  }

  @TargetApi(VERSION_CODES.M)
  private void showUninstallPackagePrompt() {
    final List<String> installedApps = new ArrayList<>();
    for (ResolveInfo res : getAllLauncherIntentResolversSorted()) {
      if (!installedApps.contains(res.activityInfo.packageName)) { // O(N^2) but not critical
        installedApps.add(res.activityInfo.packageName);
      }
    }
    AppInfoArrayAdapter appInfoArrayAdapter =
        new AppInfoArrayAdapter(getActivity(), R.id.pkg_name, installedApps, true);
    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.uninstall_packages_title))
        .setAdapter(appInfoArrayAdapter, (dialog, position) -> {
          String packageName = installedApps.get(position);
          PackageInstallationUtils.uninstallPackage(requireActivity(), packageName);
        })
        .show();
  }

  /**
   * Shows an alert dialog which displays a list hidden / non-hidden apps. Clicking an app in the
   * dialog enables the app.
   */
  private void showHideAppsPrompt(final boolean showHiddenApps) {
    final List<String> showApps = new ArrayList<>();
    if (showHiddenApps) {
      // Find all hidden packages using the GET_UNINSTALLED_PACKAGES flag
      for (ApplicationInfo applicationInfo : getAllInstalledApplicationsSorted()) {
        if (mDevicePolicyManager.isApplicationHidden(
            mAdminComponentName, applicationInfo.packageName)) {
          showApps.add(applicationInfo.packageName);
        }
      }
    } else {
      // Find all non-hidden apps with a launcher icon
      for (ResolveInfo res : getAllLauncherIntentResolversSorted()) {
        if (!showApps.contains(res.activityInfo.packageName)
            && !mDevicePolicyManager.isApplicationHidden(
                mAdminComponentName, res.activityInfo.packageName)) {
          showApps.add(res.activityInfo.packageName);
        }
      }
    }

    if (showApps.isEmpty()) {
      showToast(showHiddenApps ? R.string.unhide_apps_empty : R.string.hide_apps_empty);
    } else {
      AppInfoArrayAdapter appInfoArrayAdapter =
          new AppInfoArrayAdapter(getActivity(), R.id.pkg_name, showApps, true);
      final int dialogTitleResId;
      final int successResId;
      final int failureResId;
      if (showHiddenApps) {
        // showing a dialog to unhide an app
        dialogTitleResId = R.string.unhide_apps_title;
        successResId = R.string.unhide_apps_success;
        failureResId = R.string.unhide_apps_failure;
      } else {
        // showing a dialog to hide an app
        dialogTitleResId = R.string.hide_apps_title;
        successResId = R.string.hide_apps_success;
        failureResId = R.string.hide_apps_failure;
      }
      new MaterialAlertDialogBuilder(requireActivity())
          .setTitle(getString(dialogTitleResId))
          .setAdapter(appInfoArrayAdapter, (dialog, position) -> {
            String packageName = showApps.get(position);
            if (mDevicePolicyManager.setApplicationHidden(
                mAdminComponentName, packageName, !showHiddenApps)) {
              showToast(successResId, packageName);
            } else {
              showToast(getString(failureResId, packageName), Toast.LENGTH_LONG);
            }
          })
          .show();
    }
  }

  @RequiresApi(api = VERSION_CODES.R)
  private void showHideAppsOnParentPrompt(final boolean showHiddenApps) {
    final int dialogTitleResId;
    final int successResId;
    final int failureResId;
    final int failureSystemResId;
    if (showHiddenApps) {
      // showing a dialog to unhide an app
      dialogTitleResId = R.string.unhide_apps_parent_title;
      successResId = R.string.unhide_apps_success;
      failureResId = R.string.unhide_apps_failure;
      failureSystemResId = R.string.unhide_apps_system_failure;
    } else {
      // showing a dialog to hide an app
      dialogTitleResId = R.string.hide_apps_parent_title;
      successResId = R.string.hide_apps_success;
      failureResId = R.string.hide_apps_failure;
      failureSystemResId = R.string.hide_apps_system_failure;
    }

    View view = requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText input = view.findViewById(R.id.input);
    input.setHint(getString(R.string.input_package_name_hints));

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(dialogTitleResId))
        .setView(view)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
              String packageName = input.getText().toString();
              try {
                if (mDevicePolicyManager
                    .getParentProfileInstance(mAdminComponentName)
                    .setApplicationHidden(mAdminComponentName, packageName, !showHiddenApps)) {
                  showToast(successResId, packageName);
                } else {
                  showToast(getString(failureResId, packageName), Toast.LENGTH_LONG);
                }
              } catch (IllegalArgumentException e) {
                showToast(getString(failureSystemResId, packageName), Toast.LENGTH_LONG);
              }
              dialog.dismiss();
            })
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
        .show();
  }

  /** Shows an alert dialog which displays a list of suspended/non-suspended apps. */
  @TargetApi(VERSION_CODES.N)
  private void showSuspendAppsPrompt(final boolean forUnsuspending) {
    final List<String> showApps = new ArrayList<>();
    if (forUnsuspending) {
      // Find all suspended packages using the GET_UNINSTALLED_PACKAGES flag.
      for (ApplicationInfo applicationInfo : getAllInstalledApplicationsSorted()) {
        if (isPackageSuspended(applicationInfo.packageName)) {
          showApps.add(applicationInfo.packageName);
        }
      }
    } else {
      // Find all non-suspended apps with a launcher icon.
      for (ResolveInfo res : getAllLauncherIntentResolversSorted()) {
        if (!showApps.contains(res.activityInfo.packageName)
            && !isPackageSuspended(res.activityInfo.packageName)) {
          showApps.add(res.activityInfo.packageName);
        }
      }
    }

    if (showApps.isEmpty()) {
      showToast(forUnsuspending ? R.string.unsuspend_apps_empty : R.string.suspend_apps_empty);
    } else {
      AppInfoArrayAdapter appInfoArrayAdapter =
          new AppInfoArrayAdapter(getActivity(), R.id.pkg_name, showApps, true);
      final int dialogTitleResId;
      final int successResId;
      final int failureResId;
      if (forUnsuspending) {
        // Showing a dialog to unsuspend an app.
        dialogTitleResId = R.string.unsuspend_apps_title;
        successResId = R.string.unsuspend_apps_success;
        failureResId = R.string.unsuspend_apps_failure;
      } else {
        // Showing a dialog to suspend an app.
        dialogTitleResId = R.string.suspend_apps_title;
        successResId = R.string.suspend_apps_success;
        failureResId = R.string.suspend_apps_failure;
      }
      new MaterialAlertDialogBuilder(requireActivity())
          .setTitle(getString(dialogTitleResId))
          .setAdapter(appInfoArrayAdapter, (dialog, position) -> {
            String packageName = showApps.get(position);
            mDevicePolicyManagerGateway.setPackagesSuspended(
                new String[] {packageName},
                !forUnsuspending,
                (failed) -> {
                  if (failed.length == 0) {
                    onSuccessShowToast("setPackagesSuspended", successResId, packageName);
                  } else {
                    onErrorShowToast(failureResId, packageName);
                  }
                },
                (e) ->
                    onErrorShowToast("setPackagesSuspended", e, failureResId, packageName));
          })
          .show();
    }
  }

  /** Shows an alert dialog with a list of packages with metered data disabled. */
  @TargetApi(VERSION_CODES.P)
  private void showSetMeteredDataPrompt() {
    if (requireActivity().isFinishing()) {
      return;
    }

    final List<ApplicationInfo> applicationInfos = mPackageManager.getInstalledApplications(0 /* flags */);
    final List<ResolveInfo> resolveInfos = new ArrayList<>();
    Collections.sort(applicationInfos, new ApplicationInfo.DisplayNameComparator(mPackageManager));
    for (ApplicationInfo applicationInfo : applicationInfos) {
      final ResolveInfo resolveInfo = new ResolveInfo();
      resolveInfo.resolvePackageName = applicationInfo.packageName;
      resolveInfos.add(resolveInfo);
    }
    final MeteredDataRestrictionInfoAdapter meteredDataRestrictionInfoAdapter =
        new MeteredDataRestrictionInfoAdapter(requireActivity(), resolveInfos, getMeteredDataRestrictedPkgs());
    final ListView listView = new ListView(requireActivity());
    listView.setAdapter(meteredDataRestrictionInfoAdapter);
    listView.setOnItemClickListener(meteredDataRestrictionInfoAdapter);

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.metered_data_restriction)
        .setView(listView)
        .setPositiveButton(R.string.update_pkgs, meteredDataRestrictionInfoAdapter)
        .setNegativeButton(R.string.close, null /* Nothing to do */)
        .show();
  }

  @TargetApi(VERSION_CODES.P)
  private List<String> getMeteredDataRestrictedPkgs() {
    return mDevicePolicyManagerGateway.getMeteredDataDisabledPackages();
  }

  /**
   * Shows an alert dialog which displays a list of apps. Clicking an app in the dialog clear the
   * app data.
   */
  @TargetApi(VERSION_CODES.P)
  private void showClearAppDataPrompt() {
    final List<String> packageNameList =
        getAllInstalledApplicationsSorted().stream()
            .map(applicationInfo -> applicationInfo.packageName)
            .collect(Collectors.toList());
    if (packageNameList.isEmpty()) {
      showToast(R.string.clear_app_data_empty);
    } else {
      AppInfoArrayAdapter appInfoArrayAdapter =
          new AppInfoArrayAdapter(requireActivity(), R.id.pkg_name, packageNameList, true);
      new MaterialAlertDialogBuilder(requireActivity())
          .setTitle(getString(R.string.clear_app_data_title))
          .setAdapter(
              appInfoArrayAdapter,
              (dialog, position) -> clearApplicationUserData(packageNameList.get(position)))
          .show();
    }
  }

  @TargetApi(VERSION_CODES.P)
  private void clearApplicationUserData(String packageName) {
    mDevicePolicyManager.clearApplicationUserData(
        mAdminComponentName,
        packageName,
        new MainThreadExecutor(),
        (__, succeed) ->
            showToast(
                succeed ? R.string.clear_app_data_success : R.string.clear_app_data_failure,
                packageName));
  }

  @TargetApi(VERSION_CODES.N)
  private boolean isPackageSuspended(String packageName) {
    try {
      return mDevicePolicyManagerGateway.isPackageSuspended(packageName);
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Unable check if package is suspended", e);
      return false;
    }
  }

  private List<ResolveInfo> getAllLauncherIntentResolversSorted() {
    final Intent launcherIntent = Util.getLauncherIntent(getActivity());
    final List<ResolveInfo> launcherIntentResolvers =
        mPackageManager.queryIntentActivities(launcherIntent, 0);
    Collections.sort(
        launcherIntentResolvers, new ResolveInfo.DisplayNameComparator(mPackageManager));
    return launcherIntentResolvers;
  }

  private List<ApplicationInfo> getAllInstalledApplicationsSorted() {
    List<ApplicationInfo> allApps =
        mPackageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
    Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(mPackageManager));
    return allApps;
  }



  /**
   * Gets all the accessibility services. After all the accessibility services are retrieved, the
   * result is displayed in a popup.
   */
  private class GetAccessibilityServicesTask extends GetAvailableComponentsTask<AccessibilityServiceInfo> {
    private AccessibilityManager mAccessibilityManager;

    public GetAccessibilityServicesTask() {
      super(requireActivity(), R.string.pre_main_title_accessibility);
      mAccessibilityManager = (AccessibilityManager) requireActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    @Override
    protected List<AccessibilityServiceInfo> doInBackground(Void... voids) {
      return mAccessibilityManager.getInstalledAccessibilityServiceList();
    }

    @Override
    protected List<ResolveInfo> getResolveInfoListFromAvailableComponents(List<AccessibilityServiceInfo> accessibilityServiceInfoList) {
      HashSet<String> packageSet = new HashSet<>();
      List<ResolveInfo> resolveInfoList = new ArrayList<>();
      for (AccessibilityServiceInfo accessibilityServiceInfo : accessibilityServiceInfoList) {
        ResolveInfo resolveInfo = accessibilityServiceInfo.getResolveInfo();
        // Some apps may contain multiple accessibility services. Make sure that the package
        // name is unique in the return list.
        if (!packageSet.contains(resolveInfo.serviceInfo.packageName)) {
          resolveInfoList.add(resolveInfo);
          packageSet.add(resolveInfo.serviceInfo.packageName);
        }
      }
      return resolveInfoList;
    }

    @Override
    protected List<String> getPermittedComponentsList() {
      return mDevicePolicyManager.getPermittedAccessibilityServices(mAdminComponentName);
    }

    @Override
    protected void setPermittedComponentsList(List<String> permittedAccessibilityServices) {
      boolean result = mDevicePolicyManager.setPermittedAccessibilityServices(mAdminComponentName, permittedAccessibilityServices);
      int successMsgId =
              (permittedAccessibilityServices == null)
                      ? R.string.all_accessibility_services_enabled
                      : R.string.set_accessibility_services_successful;
      showToast(result ? successMsgId : R.string.set_accessibility_services_fail);
    }
  }

  /** Gets all the input methods and displays them in a prompt. */
  private class GetInputMethodsTask extends GetAvailableComponentsTask<InputMethodInfo> {
    private InputMethodManager mInputMethodManager;

    public GetInputMethodsTask() {
      super(requireActivity(), R.string.set_input_methods);
      mInputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected List<InputMethodInfo> doInBackground(Void... voids) {
      return mInputMethodManager.getInputMethodList();
    }

    @Override
    protected List<ResolveInfo> getResolveInfoListFromAvailableComponents(List<InputMethodInfo> inputMethodsInfoList) {
      List<ResolveInfo> inputMethodsResolveInfoList = new ArrayList<>();
      for (InputMethodInfo inputMethodInfo : inputMethodsInfoList) {
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.serviceInfo = inputMethodInfo.getServiceInfo();
        resolveInfo.resolvePackageName = inputMethodInfo.getPackageName();
        inputMethodsResolveInfoList.add(resolveInfo);
      }
      return inputMethodsResolveInfoList;
    }

    @Override
    protected List<String> getPermittedComponentsList() {
      return mDevicePolicyManager.getPermittedInputMethods(mAdminComponentName);
    }

    @Override
    protected void setPermittedComponentsList(List<String> permittedInputMethods) {
      boolean result =
          mDevicePolicyManager.setPermittedInputMethods(mAdminComponentName, permittedInputMethods);
      int successMsgId =
          (permittedInputMethods == null)
              ? R.string.all_input_methods_enabled
              : R.string.set_input_methods_successful;
      showToast(result ? successMsgId : R.string.set_input_methods_fail);
    }
  }

  @RequiresApi(api = VERSION_CODES.S)
  private void setPermittedInputMethodsOnParent() {
    if (requireActivity().isFinishing()) {
      return;
    }
    DevicePolicyManagerGateway parentDpmGateway = DevicePolicyManagerGatewayImpl.forParentProfile(requireActivity());
    View view = requireActivity().getLayoutInflater().inflate(R.layout.permitted_input_methods_on_parent, null);

    Button allInputMethodsButton = view.findViewById(R.id.all_input_methods_button);
    allInputMethodsButton.setOnClickListener(
        v -> {
          boolean result = parentDpmGateway.setPermittedInputMethods(null);
          showToast(
              result
                  ? R.string.all_input_methods_on_parent
                  : R.string.add_input_method_on_parent_fail);
        });
    Button systemInputMethodsButton = view.findViewById(R.id.system_input_methods_button);
    systemInputMethodsButton.setOnClickListener(
        v -> {
          boolean result = parentDpmGateway.setPermittedInputMethods(new ArrayList<>());
          showToast(
              result
                  ? R.string.system_input_methods_on_parent
                  : R.string.add_input_method_on_parent_fail);
        });

    new AlertDialog.Builder(getActivity()).setView(view).show();
  }

  @SuppressLint("SetTextI18n")
  @TargetApi(VERSION_CODES.O)
  private void setNotificationAllowlistEditBox() {
    if (requireActivity().isFinishing()) {
      return;
    }
    View view = requireActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText input = (EditText) view.findViewById(R.id.input);
    input.setHint(getString(R.string.set_notification_listener_text_hint));
    List<String> enabledComponents =
        mDevicePolicyManager.getPermittedCrossProfileNotificationListeners(mAdminComponentName);
    if (enabledComponents == null) {
      input.setText("null");
    } else {
      input.setText(TextUtils.join(", ", enabledComponents));
    }

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.set_notification_listener_text_hint))
        .setView(view)
        .setPositiveButton(
            android.R.string.ok,
            (DialogInterface dialog, int which) -> {
              String packageNames = input.getText().toString();
              if (packageNames.trim().equals("null")) {
                setPermittedNotificationListeners(null);
              } else {
                List<String> items = Arrays.asList(packageNames.trim().split("\\s*,\\s*"));
                setPermittedNotificationListeners(items);
              }
              dialog.dismiss();
            })
        .setNegativeButton(
            android.R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
        .show();
  }

  /** Gets all the NotificationListenerServices and displays them in a prompt. */
  private class GetNotificationListenersTask extends GetAvailableComponentsTask<ResolveInfo> {
    public GetNotificationListenersTask() {
      super(requireActivity(), R.string.set_notification_listeners);
    }

    @Override
    protected List<ResolveInfo> doInBackground(Void... voids) {
      return mPackageManager.queryIntentServices(
          new Intent(NotificationListenerService.SERVICE_INTERFACE),
          PackageManager.GET_META_DATA | PackageManager.MATCH_UNINSTALLED_PACKAGES);
    }

    @Override
    protected List<ResolveInfo> getResolveInfoListFromAvailableComponents(
        List<ResolveInfo> notificationListenerServices) {
      return notificationListenerServices;
    }

    @Override
    @TargetApi(VERSION_CODES.O)
    protected List<String> getPermittedComponentsList() {
      return mDevicePolicyManager.getPermittedCrossProfileNotificationListeners(
          mAdminComponentName);
    }

    @Override
    protected void setPermittedComponentsList(List<String> permittedNotificationListeners) {
      setPermittedNotificationListeners(permittedNotificationListeners);
    }
  }

  @TargetApi(VERSION_CODES.O)
  private void setPermittedNotificationListeners(List<String> permittedNotificationListeners) {
    boolean result =
        mDevicePolicyManager.setPermittedCrossProfileNotificationListeners(
            mAdminComponentName, permittedNotificationListeners);
    int successMsgId =
        (permittedNotificationListeners == null)
            ? R.string.all_notification_listeners_enabled
            : R.string.set_notification_listeners_successful;
    showToast(result ? successMsgId : R.string.set_notification_listeners_fail);
  }

  /** Gets all CA certificates and displays them in a prompt. */
  private class ShowCaCertificateListTask extends AsyncTask<Void, Void, String[]> {

    @Override
    protected String[] doInBackground(Void... params) {
      return getCaCertificateSubjectDnList();
    }

    @Override
    protected void onPostExecute(String[] installedCaCertificateDnList) {
      if (requireActivity().isFinishing()) {
        return;
      }
      if (installedCaCertificateDnList == null) {
        showToast(R.string.no_ca_certificate);
      } else {
        new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.installed_ca_title))
            .setItems(installedCaCertificateDnList, null)
            .show();
      }
    }

    private String[] getCaCertificateSubjectDnList() {
      List<byte[]> installedCaCerts = mDevicePolicyManager.getInstalledCaCerts(mAdminComponentName);
      String[] caSubjectDnList = null;
      if (installedCaCerts.size() > 0) {
        caSubjectDnList = new String[installedCaCerts.size()];
        int i = 0;
        for (byte[] installedCaCert : installedCaCerts) {
          try {
            X509Certificate certificate =
                (X509Certificate)
                    CertificateFactory.getInstance(X509_CERT_TYPE)
                        .generateCertificate(new ByteArrayInputStream(installedCaCert));
            caSubjectDnList[i++] = certificate.getSubjectDN().getName();
          } catch (CertificateException e) {
            Log.e(TAG, "getCaCertificateSubjectDnList: ", e);
          }
        }
      }
      return caSubjectDnList;
    }
  }

  @TargetApi(VERSION_CODES.P)
  private void relaunchInLockTaskMode() {
    ActivityManager activityManager = requireActivity().getSystemService(ActivityManager.class);

    final Intent intent = new Intent(requireActivity(), requireActivity().getClass());
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    // Ensure a new task is actually created if not already running in lock task mode
    if (!activityManager.isInLockTaskMode()) {
      intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    }
    final ActivityOptions options = ActivityOptions.makeBasic();
    options.setLockTaskEnabled(true);

    try {
      startActivity(intent, options.toBundle());
      requireActivity().finish();
    } catch (SecurityException e) {
      showToast("You must first allow-list the TestDPC package for LockTask");
    }
  }

  private void startKioskMode(String[] lockTaskArray) {
    final ComponentName customLauncher = new ComponentName(requireActivity(), KioskModeActivity.class);

    // enable custom launcher (it's disabled by default in manifest)
    mPackageManager.setComponentEnabledSetting(
        customLauncher,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP);

    // set custom launcher as default home activity
    mDevicePolicyManager.addPersistentPreferredActivity(
        mAdminComponentName, Util.getHomeIntentFilter(), customLauncher);
    Intent launchIntent = Util.getHomeIntent();
    launchIntent.putExtra(KioskModeActivity.LOCKED_APP_PACKAGE_LIST, lockTaskArray);

    startActivity(launchIntent);
    requireActivity().finish();
  }

  private void showWifiConfigCreationDialog() {
    WifiConfigCreationDialog dialog = WifiConfigCreationDialog.newInstance();
    dialog.show(requireActivity().getSupportFragmentManager(), TAG_WIFI_CONFIG_CREATION);
  }

  private void showEapTlsWifiConfigCreationDialog() {
    DialogFragment fragment = WifiEapTlsCreateDialogFragment.newInstance(null);
    fragment.show(requireActivity().getSupportFragmentManager(), WifiEapTlsCreateDialogFragment.class.getName());
  }

  @TargetApi(VERSION_CODES.N)
  private void reboot() {
    if (mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
      showToast(R.string.reboot_error_msg);
      return;
    }
    mDevicePolicyManagerGateway.reboot(
            (v) -> onSuccessLog("reboot"), (e) -> onErrorLog("reboot", String.valueOf(e)));
  }



  private void showNearbyNotificationStreamingDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    int policy = mDevicePolicyManager.getNearbyNotificationStreamingPolicy();
    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.nearby_notification_streaming)
        .setSingleChoiceItems(
            R.array.nearby_streaming_policies,
            /* checkedItem= */ policy,
            (dialogInterface, i) -> mDevicePolicyManager.setNearbyNotificationStreamingPolicy(i))
        .setNegativeButton(R.string.close, null)
        .show();
  }

  private void showNearbyAppStreamingDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    int policy = mDevicePolicyManager.getNearbyAppStreamingPolicy();
    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.nearby_app_streaming)
        .setSingleChoiceItems(
            R.array.nearby_streaming_policies,
            /* checkedItem= */ policy,
            (dialogInterface, i) -> mDevicePolicyManager.setNearbyAppStreamingPolicy(i))
        .setNegativeButton(R.string.close, null)
        .show();
  }





  @TargetApi(VERSION_CODES.R)
  private void showGrantKeyPairToAppDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }

    View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.grant_key_pair_to_app_prompt, null);

    final EditText keyPairAliasTextEdit = (EditText) dialogView.findViewById(R.id.keyPairAlias);
    keyPairAliasTextEdit.setText("");
    final EditText packageNameTextEdit = (EditText) dialogView.findViewById(R.id.packageName);
    packageNameTextEdit.setText("");

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.grant_key_pair_title)
        .setView(dialogView)
        .setPositiveButton(
            R.string.grant_button,
            (dialogInterface, i) -> {
              final String keyPairAlias = keyPairAliasTextEdit.getText().toString();
              if (keyPairAlias.isEmpty()) {
                showToast(R.string.key_pair_alias_empty);
                return;
              }

              final String packagename = packageNameTextEdit.getText().toString();
              if (packagename.isEmpty()) {
                showToast(R.string.grant_to_package_name_empty);
                return;
              }

              grantKeyPairToApp(keyPairAlias, packagename);
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  @RequiresApi(api = VERSION_CODES.R)
  private void grantKeyPairToApp(String keyAlias, String packageName) {
    boolean status = false;
    try {
      status = mDevicePolicyManager.grantKeyPairToApp(mAdminComponentName, keyAlias, packageName);
    } catch (SecurityException | IllegalArgumentException e) {
      Log.e(TAG, "Error invoking grantKeyPairToApp", e);
    }
    if (status) {
      showToast("KeyPair granted successfully");
    } else {
      showToast("KeyPair grant failed");
    }
  }

  /**
   * Shows the current minimum Wi-Fi security level and lets the user change this value
   */
  @TargetApi(VERSION_CODES.TIRAMISU)
  private void showSetWifiMinSecurityLevelDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }
    View setSecurityLevelView =
        requireActivity().getLayoutInflater().inflate(R.layout.set_wifi_min_security_level, null);
    final RadioGroup securityLevelGroup =
        (RadioGroup) setSecurityLevelView.findViewById(R.id.set_security_level_group);

    int securityLevel = mDevicePolicyManager.getMinimumRequiredWifiSecurityLevel();
    switch (securityLevel) {
      case DevicePolicyManager.WIFI_SECURITY_OPEN ->
              ((RadioButton) securityLevelGroup.findViewById(R.id.open)).toggle();
      case DevicePolicyManager.WIFI_SECURITY_PERSONAL ->
              ((RadioButton) securityLevelGroup.findViewById(R.id.personal)).toggle();
      case DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP ->
              ((RadioButton) securityLevelGroup.findViewById(R.id.enterprise_eap)).toggle();
      case DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192 ->
              ((RadioButton) securityLevelGroup.findViewById(R.id.enterprise_192)).toggle();
    }

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.set_wifi_min_security_level))
        .setView(setSecurityLevelView)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          int level = -1;
          int checked = securityLevelGroup.getCheckedRadioButtonId();
          if (checked == R.id.open) {
            level = DevicePolicyManager.WIFI_SECURITY_OPEN;
          } else if (checked == R.id.personal) {
            level = DevicePolicyManager.WIFI_SECURITY_PERSONAL;
          } else if (checked == R.id.enterprise_eap) {
            level = DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP;
          } else if (checked == R.id.enterprise_192) {
            level = DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192;
          }
          mDevicePolicyManager.setMinimumRequiredWifiSecurityLevel(level);
          dialog.dismiss();
        })
        .show();
  }

  /**
   * Lets the user set the Wi-Fi SSID restriction
   */
  @TargetApi(VERSION_CODES.TIRAMISU)
  private void showSetWifiSsidRestrictionDialog() {
    if (requireActivity().isFinishing()) {
      return;
    }
    View setSsidRestrictionView =
        requireActivity().getLayoutInflater().inflate(R.layout.set_wifi_ssid_restriction, null);
    final RadioGroup listTypeGroup =
        (RadioGroup) setSsidRestrictionView.findViewById(R.id.set_list_type_group);
    final EditText ssidsTextEdit = (EditText) setSsidRestrictionView.findViewById(R.id.ssids);
    ssidsTextEdit.setText("");

    new MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.set_wifi_ssid_restriction))
            .setView(setSsidRestrictionView)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
              final String ssids = ssidsTextEdit.getText().toString();
              if (ssids.isEmpty()) {
                mDevicePolicyManager.setWifiSsidPolicy(null);
                showToast("SSID restriction removed");
                return;
              }

              String[] ssidsArray = ssids.split(",");
              Set<WifiSsid> ssidList = new HashSet<>();
              for (String ssid : ssidsArray) {
                ssidList.add(WifiSsid.fromBytes(ssid.getBytes(StandardCharsets.UTF_8)));
              }

              int type = -1;
              int checked = listTypeGroup.getCheckedRadioButtonId();
              if (checked == R.id.allow) {
                type = WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST;
              } else if (checked == R.id.deny) {
                type = WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_DENYLIST;
              }

              WifiSsidPolicy policy = new WifiSsidPolicy(type, ssidList);
              mDevicePolicyManager.setWifiSsidPolicy(policy);
              showToast("SSID restriction set");
              dialog.dismiss();
            })
            .show();
  }



  @TargetApi(VERSION_CODES.P)
  private int validateAffiliatedUserAfterP() {
    if (Util.SDK_INT >= VERSION_CODES.P) {
      if (!mDevicePolicyManager.isAffiliatedUser()) {
        return R.string.require_affiliated_user;
      }
    }
    return NO_CUSTOM_CONSTRAINT;
  }

  @TargetApi(VERSION_CODES.R)
  private void factoryResetOrgOwnedDevice() {
    DevicePolicyManagerGatewayImpl.forParentProfile(requireActivity())
            .wipeData(
                    /* flags= */ 0, (v) -> onSuccessLog("wipeData"), (e) -> onErrorLog("wipeData", e));
  }

  private boolean isOrganizationOwnedDevice() {
    return mDevicePolicyManager.isDeviceOwnerApp(mPackageName)
        || (mDevicePolicyManager.isProfileOwnerApp(mPackageName)
            && mDevicePolicyManagerGateway.isOrganizationOwnedDeviceWithManagedProfile());
  }

  private int validateDeviceOwnerBeforeO() {
    if (Util.SDK_INT < VERSION_CODES.O) {
      if (!mDevicePolicyManager.isDeviceOwnerApp(mPackageName)) {
        return R.string.requires_device_owner;
      }
    }
    return NO_CUSTOM_CONSTRAINT;
  }

  private int validateDeviceOwnerBeforeP() {
    if (Util.SDK_INT < VERSION_CODES.P) {
      if (!mDevicePolicyManager.isDeviceOwnerApp(mPackageName)) {
        return R.string.requires_device_owner;
      }
    }
    return NO_CUSTOM_CONSTRAINT;
  }

  private int validateDeviceOwnerBeforeQ() {
    if (Util.SDK_INT < VERSION_CODES.Q) {
      if (!mDevicePolicyManager.isDeviceOwnerApp(mPackageName)) {
        return R.string.requires_device_owner;
      }
    }
    return NO_CUSTOM_CONSTRAINT;
  }

  private int validateDeviceOwnerOrDelegationNetworkLoggingBeforeS() {
    if (Util.SDK_INT < VERSION_CODES.S && (isDeviceOwner() || hasNetworkLoggingDelegation())) {
      return R.string.requires_device_owner_or_delegation_network_logging;
    }
    return NO_CUSTOM_CONSTRAINT;
  }

  private int validateInstallNonMarketApps() {
    if (Util.SDK_INT >= VERSION_CODES.O
        && requireActivity().getApplicationInfo().targetSdkVersion >= VERSION_CODES.O) {
      return R.string.deprecated_since_oreo;
    }
    if (mUserManager.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY)
        || mUserManager.hasUserRestriction(DISALLOW_INSTALL_UNKNOWN_SOURCES)) {
      return R.string.user_restricted;
    }
    return NO_CUSTOM_CONSTRAINT;
  }

  @TargetApi(VERSION_CODES.R)
  private void reloadCameraDisableOnParentUi() {
    DevicePolicyManager parentDpm = mDevicePolicyManager.getParentProfileInstance(mAdminComponentName);
    boolean isCameraDisabled = parentDpm.getCameraDisabled(mAdminComponentName);
    swDisableCameraOnParent.setChecked(isCameraDisabled);
  }

  @TargetApi(VERSION_CODES.R)
  private void reloadScreenCaptureDisableOnParentUi() {
    DevicePolicyManager parentDpm =
            mDevicePolicyManager.getParentProfileInstance(mAdminComponentName);
    boolean isScreenCaptureDisabled = parentDpm.getScreenCaptureDisabled(mAdminComponentName);
    swDisableScreenCaptureOnParent.setChecked(isScreenCaptureDisabled);
  }

  @TargetApi(VERSION_CODES.R)
  private void reloadLockdownAdminConfiguredNetworksUi() {
    boolean lockdown = mDevicePolicyManager.hasLockdownAdminConfiguredNetworks(mAdminComponentName);
    swEnableWifiConfigLockdown.setChecked(lockdown);
  }

  @TargetApi(VERSION_CODES.R)
  private void reloadLocationEnabledUi() {
    LocationManager locationManager = requireActivity().getSystemService(LocationManager.class);
    swLocationEnabled.setChecked(locationManager.isLocationEnabled());
  }

  @TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
  private void reloadLocationModeUi() {
    final String locationMode =
            Settings.System.getString(requireActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
    swLocationMode.setChecked(parseInt(locationMode, 0) != Settings.Secure.LOCATION_MODE_OFF);
  }
}