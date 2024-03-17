package com.afwsamples.dhizukudpc.comp;

import android.os.IBinder;

public interface ServiceInterfaceConverter<T> {
  T convert(IBinder iBinder);
}
