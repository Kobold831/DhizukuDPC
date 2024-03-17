/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.afwsamples.dhizukudpc.profilepolicy.apprestrictions;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.afwsamples.dhizukudpc.DeviceAdminReceiver;
import com.afwsamples.dhizukudpc.R;
import com.afwsamples.dhizukudpc.common.SelectAppFragment;
import com.afwsamples.dhizukudpc.common.Util;
import com.afwsamples.dhizukudpc.util.Common;
import com.rosan.dhizuku.shared.DhizukuVariables;

import java.util.Objects;

/**
 * This fragment lets the user select an app that can manage application restrictions for the
 * current user.
 *
 * <p>On Android N and after, it allows the selected app to call the DevicePolicyManager APIs using:
 * {@link DevicePolicyManager#setApplicationRestrictionsManagingPackage} {@link
 * DevicePolicyManager#getApplicationRestrictionsManagingPackage}
 *
 * <p>On Android M and before, it allows the selected app to proxy API calls to DevicePolicyManager
 * via TestDPC, see {@link AppRestrictionsProxyHandler}.
 */
public class AppRestrictionsManagingPackageFragment extends SelectAppFragment {

  private DevicePolicyManager mDpm;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDpm = Common.binderWrapperDevicePolicyManager(requireActivity());
  }

  @Override
  public void onResume() {
    super.onResume();
    Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.app_restrictions_managing_package);
  }

  @Override
  protected void setSelectedPackage(String pkgName) {
    // If the input pkgName is an empty string, we clear the app restriction manager.
    if (TextUtils.isEmpty(pkgName)) {
      pkgName = null;
    }
    if (Util.SDK_INT >= VERSION_CODES.N) {
      setApplicationRestrictionsManagingPackage(pkgName);
    } else {
      setApplicationRestrictionsManagingPackageWithProxy(pkgName);
    }
  }

  @Override
  protected void clearSelectedPackage() {
    setSelectedPackage(null);
  }

  @Override
  protected String getSelectedPackage() {
    if (Util.SDK_INT >= VERSION_CODES.N) {
      return getApplicationRestrictionsManagingPackage();
    } else {
      return getApplicationRestrictionsManagingPackageWithProxy();
    }
  }

  @TargetApi(VERSION_CODES.N)
  private void setApplicationRestrictionsManagingPackage(String pkgName) {
    try {
      mDpm.setApplicationRestrictionsManagingPackage(DhizukuVariables.COMPONENT_NAME, pkgName);
    } catch (NameNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private void setApplicationRestrictionsManagingPackageWithProxy(String pkgName) {
    AppRestrictionsProxyHandler.setApplicationRestrictionsManagingPackage(requireActivity(), pkgName);
  }

  @TargetApi(VERSION_CODES.N)
  private String getApplicationRestrictionsManagingPackage() {
    return mDpm.getApplicationRestrictionsManagingPackage(DhizukuVariables.COMPONENT_NAME);
  }

  private String getApplicationRestrictionsManagingPackageWithProxy() {
    return AppRestrictionsProxyHandler.getApplicationRestrictionsManagingPackage(requireActivity());
  }
}
