package com.chriscarini.jetbrains.autopowersaver.productivity;

import com.chriscarini.jetbrains.autopowersaver.messages.Messages;
import com.intellij.featureStatistics.ApplicabilityFilter;
import com.intellij.featureStatistics.FeatureDescriptor;
import com.intellij.featureStatistics.GroupDescriptor;
import com.intellij.featureStatistics.ProductivityFeaturesProvider;
import org.jetbrains.annotations.NonNls;

import java.util.Collections;


public class AutoPowerSaverProductivityFeaturesProvider extends ProductivityFeaturesProvider {
  @NonNls
  private static final String TIP_FILE_NAME = "autoPowerSaver.html";
  @NonNls
  public static final String AUTO_POWER_SAVER_FEATURE = "auto.power.saver";

  @Override
  public FeatureDescriptor[] getFeatureDescriptors() {
    return new FeatureDescriptor[]{new FeatureDescriptor(AUTO_POWER_SAVER_FEATURE, "ui", TIP_FILE_NAME,
        Messages.get("auto.power.saver.productivity.features.provider"), 0, 1, Collections.emptySet(), 3, this)};
  }

  @Override
  public GroupDescriptor[] getGroupDescriptors() {
    return new GroupDescriptor[0];
  }

  @Override
  public ApplicabilityFilter[] getApplicabilityFilters() {
    return new ApplicabilityFilter[0];
  }
}
