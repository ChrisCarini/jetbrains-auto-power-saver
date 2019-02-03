package com.chriscarini.jetbrains.autopowersaver.productivity;

import com.chriscarini.jetbrains.autopowersaver.messages.Messages;
import com.intellij.featureStatistics.ApplicabilityFilter;
import com.intellij.featureStatistics.FeatureDescriptor;
import com.intellij.featureStatistics.GroupDescriptor;
import com.intellij.featureStatistics.ProductivityFeaturesProvider;
import com.intellij.ide.util.TipAndTrickBean;
import java.util.Collections;
import org.jetbrains.annotations.NonNls;


public class AutoPowerSaverProductivityFeaturesProvider extends ProductivityFeaturesProvider {
  @NonNls
  private static final String TIP_FILE_NAME = "autoPowerSaver.html";
  private static final TipAndTrickBean TIP_AND_TRICK_BEAN = TipAndTrickBean.findByFileName(TIP_FILE_NAME);
  @NonNls
  public static final String AUTO_POWER_SAVER_FEATURE =
      TIP_AND_TRICK_BEAN != null ? TIP_AND_TRICK_BEAN.featureId : "auto.power.saver";

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
