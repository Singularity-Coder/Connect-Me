package com.singularitycoder.connectme.helpers;

import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = ForegroundLocationService.class
)
@GeneratedEntryPoint
@InstallIn(ServiceComponent.class)
public interface ForegroundLocationService_GeneratedInjector {
  void injectForegroundLocationService(ForegroundLocationService foregroundLocationService);
}
