/*
 * Copyright (C) 2018 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import com.afwsamples.dhizukudpc.DeviceAdminReceiver;
import com.afwsamples.dhizukudpc.R;
import com.afwsamples.dhizukudpc.util.Common;
import com.rosan.dhizuku.shared.DhizukuVariables;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Allows the user to see / edit / delete keep uninstalled packages. See {@link
 * DevicePolicyManager#setKeepUninstalledPackages(ComponentName, List)}
 */
public class ManageKeepUninstalledPackagesFragment extends BaseStringItemsFragment {

  private DevicePolicyManager mDevicePolicyManager;
  private ComponentName mAdminComponent;

  public ManageKeepUninstalledPackagesFragment() {
    super(
        R.string.keep_uninstalled_packages,
        R.string.enter_package_name,
        R.string.package_name_empty_error);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mAdminComponent = DhizukuVariables.COMPONENT_NAME;
    mDevicePolicyManager = Common.binderWrapperDevicePolicyManager(requireActivity());
  }

  @TargetApi(VERSION_CODES.P)
  @Override
  protected Collection<String> loadItems() {
    List<String> packages = mDevicePolicyManager.getKeepUninstalledPackages(mAdminComponent);
    return packages == null ? Collections.emptyList() : packages;
  }

  @TargetApi(VERSION_CODES.P)
  @Override
  protected void saveItems(List<String> items) {
    mDevicePolicyManager.setKeepUninstalledPackages(mAdminComponent, items);
  }
}
