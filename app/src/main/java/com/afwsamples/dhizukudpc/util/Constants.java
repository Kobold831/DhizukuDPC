package com.afwsamples.dhizukudpc.util;

import android.app.admin.DevicePolicyManager;
import android.os.BatteryManager;
import android.os.Build;
import android.util.SparseIntArray;

import com.afwsamples.dhizukudpc.R;
import com.afwsamples.dhizukudpc.common.Util;

public class Constants {

    public static final String FRAGMENT_TAG = "PolicyManagementFragment";

    public static final String LOG_TAG = "TestDPC";

    public static final int INSTALL_KEY_CERTIFICATE_REQUEST_CODE = 7689;
    public static final int INSTALL_CA_CERTIFICATE_REQUEST_CODE = 7690;
    public static final int CAPTURE_IMAGE_REQUEST_CODE = 7691;
    public static final int CAPTURE_VIDEO_REQUEST_CODE = 7692;
    public static final int INSTALL_APK_PACKAGE_REQUEST_CODE = 7693;
    public static final int REQUEST_MANAGE_CREDENTIALS_REQUEST_CODE = 7694;

    public static final String X509_CERT_TYPE = "X.509";
    public static final String TAG = "PolicyManagement";

    public static final String OVERRIDE_KEY_SELECTION_KEY = "override_key_selection";

    public static final String GENERIC_DELEGATION_KEY = "generic_delegation";
    public static final String APP_RESTRICTIONS_MANAGING_PACKAGE_KEY =
            "app_restrictions_managing_package";
    public static final String BLOCK_UNINSTALLATION_BY_PKG_KEY = "block_uninstallation_by_pkg";
    public static final String BLOCK_UNINSTALLATION_LIST_KEY = "block_uninstallation_list";
    public static final String CAPTURE_IMAGE_KEY = "capture_image";
    public static final String CAPTURE_VIDEO_KEY = "capture_video";
    public static final String CHECK_LOCK_TASK_PERMITTED_KEY = "check_lock_task_permitted";
    public static final String CREATE_MANAGED_PROFILE_KEY = "create_managed_profile";
    public static final String CREATE_AND_MANAGE_USER_KEY = "create_and_manage_user";
    public static final String SET_AFFILIATION_IDS_KEY = "set_affiliation_ids";
    public static final String DELEGATED_CERT_INSTALLER_KEY = "manage_cert_installer";
    public static final String APP_STATUS_KEY = "app_status";
    public static final String SECURITY_PATCH_KEY = "security_patch";
    public static final String PASSWORD_COMPLIANT_KEY = "password_compliant";
    public static final String PASSWORD_COMPLEXITY_KEY = "password_complexity";
    public static final String REQUIRED_PASSWORD_COMPLEXITY_KEY = "required_password_complexity";
    public static final String REQUIRED_PASSWORD_COMPLEXITY_ON_PARENT_KEY =
            "required_password_complexity_on_parent";
    public static final String SEPARATE_CHALLENGE_KEY = "separate_challenge";
    public static final String DISABLE_CAMERA_KEY = "disable_camera";
    public static final String DISABLE_CAMERA_ON_PARENT_KEY = "disable_camera_on_parent";
    public static final String DISABLE_KEYGUARD = "disable_keyguard";
    public static final String DISABLE_METERED_DATA_KEY = "disable_metered_data";
    public static final String DISABLE_SCREEN_CAPTURE_KEY = "disable_screen_capture";
    public static final String DISABLE_SCREEN_CAPTURE_ON_PARENT_KEY =
            "disable_screen_capture_on_parent";
    public static final String DISABLE_STATUS_BAR = "disable_status_bar";
    public static final String ENABLE_BACKUP_SERVICE = "enable_backup_service";
    public static final String APP_FEEDBACK_NOTIFICATIONS = "app_feedback_notifications";
    public static final String ENABLE_SECURITY_LOGGING = "enable_security_logging";
    public static final String ENABLE_NETWORK_LOGGING = "enable_network_logging";
    public static final String ENABLE_SYSTEM_APPS_BY_INTENT_KEY = "enable_system_apps_by_intent";
    public static final String ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY =
            "enable_system_apps_by_package_name";
    public static final String ENABLE_SYSTEM_APPS_KEY = "enable_system_apps";
    public static final String INSTALL_EXISTING_PACKAGE_KEY = "install_existing_packages";
    public static final String INSTALL_APK_PACKAGE_KEY = "install_apk_package";
    public static final String UNINSTALL_PACKAGE_KEY = "uninstall_package";
    public static final String GENERATE_KEY_CERTIFICATE_KEY = "generate_key_and_certificate";
    public static final String GET_CA_CERTIFICATES_KEY = "get_ca_certificates";
    public static final String GET_DISABLE_ACCOUNT_MANAGEMENT_KEY = "get_disable_account_management";
    public static final String ADD_ACCOUNT_KEY = "add_account";
    public static final String REMOVE_ACCOUNT_KEY = "remove_account";
    public static final String HIDE_APPS_KEY = "hide_apps";
    public static final String HIDE_APPS_PARENT_KEY = "hide_apps_parent";
    public static final String REQUEST_MANAGE_CREDENTIALS_KEY = "request_manage_credentials";
    public static final String INSTALL_CA_CERTIFICATE_KEY = "install_ca_certificate";
    public static final String INSTALL_KEY_CERTIFICATE_KEY = "install_key_certificate";
    public static final String INSTALL_NONMARKET_APPS_KEY = "install_nonmarket_apps";
    public static final String LOCK_SCREEN_POLICY_KEY = "lock_screen_policy";
    public static final String MANAGE_APP_PERMISSIONS_KEY = "manage_app_permissions";
    public static final String MANAGED_CONFIGURATIONS_KEY = "managed_configurations";
    public static final String MANAGED_PROFILE_SPECIFIC_POLICIES_KEY = "managed_profile_policies";
    public static final String MANAGE_LOCK_TASK_LIST_KEY = "manage_lock_task";
    public static final String MUTE_AUDIO_KEY = "mute_audio";
    public static final String NETWORK_STATS_KEY = "network_stats";
    public static final String PASSWORD_CONSTRAINTS_KEY = "password_constraints";
    public static final String REBOOT_KEY = "reboot";
    public static final String REENABLE_KEYGUARD = "reenable_keyguard";
    public static final String REENABLE_STATUS_BAR = "reenable_status_bar";
    public static final String RELAUNCH_IN_LOCK_TASK = "relaunch_in_lock_task";
    public static final String REMOVE_ALL_CERTIFICATES_KEY = "remove_all_ca_certificates";
    public static final String REMOVE_DEVICE_OWNER_KEY = "remove_device_owner";
    public static final String REMOVE_KEY_CERTIFICATE_KEY = "remove_key_certificate";
    public static final String REMOVE_USER_KEY = "remove_user";
    public static final String SWITCH_USER_KEY = "switch_user";
    public static final String START_USER_IN_BACKGROUND_KEY = "start_user_in_background";
    public static final String STOP_USER_KEY = "stop_user";
    public static final String LOGOUT_USER_KEY = "logout_user";
    public static final String ENABLE_LOGOUT_KEY = "enable_logout";
    public static final String SET_USER_SESSION_MESSAGE_KEY = "set_user_session_message";
    public static final String AFFILIATED_USER_KEY = "affiliated_user";
    public static final String EPHEMERAL_USER_KEY = "ephemeral_user";
    public static final String REQUEST_BUGREPORT_KEY = "request_bugreport";
    public static final String REQUEST_NETWORK_LOGS = "request_network_logs";
    public static final String REQUEST_SECURITY_LOGS = "request_security_logs";
    public static final String REQUEST_PRE_REBOOT_SECURITY_LOGS = "request_pre_reboot_security_logs";
    public static final String RESET_PASSWORD_KEY = "reset_password";
    public static final String LOCK_NOW_KEY = "lock_now";
    public static final String SET_ACCESSIBILITY_SERVICES_KEY = "set_accessibility_services";
    public static final String SET_ALWAYS_ON_VPN_KEY = "set_always_on_vpn";
    public static final String SET_GET_PREFERENTIAL_NETWORK_SERVICE_STATUS =
            "set_get_preferential_network_service_status";
    public static final String SET_GLOBAL_HTTP_PROXY_KEY = "set_global_http_proxy";
    public static final String SET_LOCK_TASK_FEATURES_KEY = "set_lock_task_features";
    public static final String CLEAR_GLOBAL_HTTP_PROXY_KEY = "clear_global_http_proxy";
    public static final String SET_DEVICE_ORGANIZATION_NAME_KEY = "set_device_organization_name";
    public static final String SET_AUTO_TIME_REQUIRED_KEY = "set_auto_time_required";
    public static final String SET_AUTO_TIME_KEY = "set_auto_time";
    public static final String SET_AUTO_TIME_ZONE_KEY = "set_auto_time_zone";
    public static final String SET_DISABLE_ACCOUNT_MANAGEMENT_KEY = "set_disable_account_management";
    public static final String SET_INPUT_METHODS_KEY = "set_input_methods";
    public static final String SET_INPUT_METHODS_ON_PARENT_KEY = "set_input_methods_on_parent";
    public static final String SET_NOTIFICATION_LISTENERS_KEY = "set_notification_listeners";
    public static final String SET_NOTIFICATION_LISTENERS_TEXT_KEY =
            "set_notification_listeners_text";
    public static final String SET_LONG_SUPPORT_MESSAGE_KEY = "set_long_support_message";
    public static final String SET_PERMISSION_POLICY_KEY = "set_permission_policy";
    public static final String SET_SHORT_SUPPORT_MESSAGE_KEY = "set_short_support_message";
    public static final String SET_USER_RESTRICTIONS_KEY = "set_user_restrictions";
    public static final String SET_USER_RESTRICTIONS_PARENT_KEY = "set_user_restrictions_parent";
    public static final String SHOW_WIFI_MAC_ADDRESS_KEY = "show_wifi_mac_address";
    public static final String START_KIOSK_MODE = "start_kiosk_mode";
    public static final String START_LOCK_TASK = "start_lock_task";
    public static final String STAY_ON_WHILE_PLUGGED_IN = "stay_on_while_plugged_in";
    public static final String STOP_LOCK_TASK = "stop_lock_task";
    public static final String SUSPEND_APPS_KEY = "suspend_apps";
    public static final String SYSTEM_UPDATE_POLICY_KEY = "system_update_policy";
    public static final String SYSTEM_UPDATE_PENDING_KEY = "system_update_pending";
    public static final String TEST_KEY_USABILITY_KEY = "test_key_usability";
    public static final String UNHIDE_APPS_KEY = "unhide_apps";
    public static final String UNHIDE_APPS_PARENT_KEY = "unhide_apps_parent";
    public static final String UNSUSPEND_APPS_KEY = "unsuspend_apps";
    public static final String CLEAR_APP_DATA_KEY = "clear_app_data";
    public static final String KEEP_UNINSTALLED_PACKAGES = "keep_uninstalled_packages";
    public static final String WIPE_DATA_KEY = "wipe_data";
    public static final String CREATE_WIFI_CONFIGURATION_KEY = "create_wifi_configuration";
    public static final String CREATE_EAP_TLS_WIFI_CONFIGURATION_KEY =
            "create_eap_tls_wifi_configuration";
    public static final String WIFI_CONFIG_LOCKDOWN_ENABLE_KEY = "enable_wifi_config_lockdown";
    public static final String MODIFY_WIFI_CONFIGURATION_KEY = "modify_wifi_configuration";
    public static final String MODIFY_OWNED_WIFI_CONFIGURATION_KEY =
            "modify_owned_wifi_configuration";
    public static final String REMOVE_NOT_OWNED_WIFI_CONFIGURATION_KEY =
            "remove_not_owned_wifi_configurations";
    public static final String TRANSFER_OWNERSHIP_KEY = "transfer_ownership_to_component";
    public static final String TAG_WIFI_CONFIG_CREATION = "wifi_config_creation";
    public static final String SECURITY_PATCH_FORMAT = "yyyy-MM-dd";
    public static final String SET_NEW_PASSWORD = "set_new_password";
    public static final String SET_NEW_PASSWORD_WITH_COMPLEXITY = "set_new_password_with_complexity";
    public static final String SET_REQUIRED_PASSWORD_COMPLEXITY = "set_required_password_complexity";
    public static final String SET_REQUIRED_PASSWORD_COMPLEXITY_ON_PARENT =
            "set_required_password_complexity_on_parent";
    public static final String SET_PROFILE_PARENT_NEW_PASSWORD = "set_profile_parent_new_password";
    public static final String SET_PROFILE_PARENT_NEW_PASSWORD_DEVICE_REQUIREMENT =
            "set_profile_parent_new_password_device_requirement";
    public static final String BIND_DEVICE_ADMIN_POLICIES = "bind_device_admin_policies";
    public static final String CROSS_PROFILE_APPS = "cross_profile_apps";
    public static final String CROSS_PROFILE_APPS_ALLOWLIST = "cross_profile_apps_allowlist";
    public static final String SET_SCREEN_BRIGHTNESS_KEY = "set_screen_brightness";
    public static final String AUTO_BRIGHTNESS_KEY = "auto_brightness";
    public static final String CROSS_PROFILE_CALENDAR_KEY = "cross_profile_calendar";
    public static final String ENTERPRISE_SLICE_KEY = "enterprise_slice";
    public static final String SET_SCREEN_OFF_TIMEOUT_KEY = "set_screen_off_timeout";
    public static final String SET_TIME_KEY = "set_time";
    public static final String SET_TIME_ZONE_KEY = "set_time_zone";
    public static final String SET_PROFILE_NAME_KEY = "set_profile_name";
    public static final String MANAGE_OVERRIDE_APN_KEY = "manage_override_apn";
    public static final String MANAGED_SYSTEM_UPDATES_KEY = "managed_system_updates";
    public static final String SET_PRIVATE_DNS_MODE_KEY = "set_private_dns_mode";
    public static final String FACTORY_RESET_ORG_OWNED_DEVICE = "factory_reset_org_owned_device";
    public static final String SET_FACTORY_RESET_PROTECTION_POLICY_KEY =
            "set_factory_reset_protection_policy";
    public static final String SET_LOCATION_ENABLED_KEY = "set_location_enabled";
    public static final String SET_LOCATION_MODE_KEY = "set_location_mode";
    public static final String SUSPEND_PERSONAL_APPS_KEY = "suspend_personal_apps";
    public static final String PROFILE_MAX_TIME_OFF_KEY = "profile_max_time_off";
    public static final String COMMON_CRITERIA_MODE_KEY = "common_criteria_mode";
    public static final String SET_ORGANIZATION_ID_KEY = "set_organization_id";
    public static final String ENROLLMENT_SPECIFIC_ID_KEY = "enrollment_specific_id";
    public static final String ENABLE_USB_DATA_SIGNALING_KEY = "enable_usb_data_signaling";
    public static final String NEARBY_NOTIFICATION_STREAMING_KEY = "nearby_notification_streaming";
    public static final String NEARBY_APP_STREAMING_KEY = "nearby_app_streaming";
    public static final String GRANT_KEY_PAIR_TO_APP_KEY = "grant_key_pair_to_app";
    public static final String SET_WIFI_MIN_SECURITY_LEVEL_KEY = "set_wifi_min_security_level";
    public static final String SET_WIFI_SSID_RESTRICTION_KEY = "set_wifi_ssid_restriction";
    public static final String MTE_POLICY_KEY = "mte_policy";

    public static final String BATTERY_PLUGGED_ANY =
            Integer.toString(
                    BatteryManager.BATTERY_PLUGGED_AC
                            | BatteryManager.BATTERY_PLUGGED_USB
                            | BatteryManager.BATTERY_PLUGGED_WIRELESS);
    public static final String DONT_STAY_ON = "0";

    public static final int USER_OPERATION_ERROR_UNKNOWN = 1;
    public static final int USER_OPERATION_SUCCESS = 0;

    public static final SparseIntArray PASSWORD_COMPLEXITY = new SparseIntArray(4);

    static {
        if (Util.SDK_INT >= Build.VERSION_CODES.Q) {
            final int[] complexityIds =
                    new int[]{
                            DevicePolicyManager.PASSWORD_COMPLEXITY_NONE,
                            DevicePolicyManager.PASSWORD_COMPLEXITY_LOW,
                            DevicePolicyManager.PASSWORD_COMPLEXITY_MEDIUM,
                            DevicePolicyManager.PASSWORD_COMPLEXITY_HIGH
                    };

            // Strings to show for each password complexity setting.
            final int[] complexityNames =
                    new int[]{
                            R.string.password_complexity_none,
                            R.string.password_complexity_low,
                            R.string.password_complexity_medium,
                            R.string.password_complexity_high
                    };
            for (int i = 0; i < complexityIds.length; i++) {
                PASSWORD_COMPLEXITY.put(complexityIds[i], complexityNames[i]);
            }
        }
    }
}