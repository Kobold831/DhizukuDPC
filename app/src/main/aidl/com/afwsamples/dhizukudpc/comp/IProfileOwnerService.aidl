package com.afwsamples.dhizukudpc.comp;

interface IProfileOwnerService {
    oneway void setLauncherIconHidden(boolean hidden);
    boolean isLauncherIconHidden();
    boolean installCaCertificate(in AssetFileDescriptor afd);
}