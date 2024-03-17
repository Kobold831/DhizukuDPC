package com.afwsamples.dhizukudpc.comp;

interface IDeviceOwnerService {
    /**
     * Notify device owner that work profile is unlocked.
     */
    oneway void notifyUserIsUnlocked(in UserHandle callingUserHandle);
}