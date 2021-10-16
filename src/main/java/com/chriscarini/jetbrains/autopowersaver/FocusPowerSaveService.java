package com.chriscarini.jetbrains.autopowersaver;

import com.chriscarini.jetbrains.autopowersaver.productivity.AutoPowerSaverProductivityFeaturesProvider;
import com.chriscarini.jetbrains.autopowersaver.settings.SettingsManager;
import com.intellij.application.Topics;
import com.intellij.concurrency.JobScheduler;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.FrameStateListener;
import com.intellij.ide.PowerSaveMode;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NonNls;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class FocusPowerSaveService implements Disposable {
  @NonNls
  private static final Logger LOG = Logger.getInstance(FocusPowerSaveService.class);

  private static ScheduledFuture<?> frameActivatedJob;
  private static ScheduledFuture<?> frameDeactivatedJob;

  public FocusPowerSaveService() {
    Disposer.register(ApplicationManager.getApplication(), this);

    Topics.subscribe(FrameStateListener.TOPIC, ApplicationManager.getApplication(),
        new IdeFrameStatePowerSaveListener());
  }

  private static void cancelJobs() {
    if (frameDeactivatedJob != null) {
      LOG.debug("Cancel any existing frameDeactivated job...");
      frameDeactivatedJob.cancel(false);
    }
    if (frameActivatedJob != null) {
      LOG.debug("Cancel any existing frameActivated job...");
      frameActivatedJob.cancel(false);
    }
  }

  @Override
  public void dispose() {
    LOG.debug("Disposing FocusPowerSaveService...");
    cancelJobs();
  }

  /**
   * Listener that enables/disables {@link PowerSaveMode} on frame activate/deactivate.
   */
  private static class IdeFrameStatePowerSaveListener implements FrameStateListener {
    @Override
    public void onFrameActivated() {
      LOG.debug("IDE Frame Activated...");
      if (ApplicationManager.getApplication().isDisposed()) {
        return;
      }

      final SettingsManager.PowerSaverState settings = SettingsManager.getInstance().getState();
      // Early exit if we're disabled (globally, or just the frame activated action)
      if (!(settings.enabledGlobally || settings.enableActivated)) {
        LOG.debug("Not enabled; stopping.");
        return;
      }

      cancelJobs();

      // Next, schedule a new activated job
      LOG.debug("Schedule new job to be run in " + settings.delayActivated + " seconds...");
      frameActivatedJob = JobScheduler.getScheduler().schedule(() -> {
        if (ApplicationManager.getApplication().isDisposed()) {
          return;
        }

        if (PowerSaveMode.isEnabled()) {
          LOG.debug("Frame Activated, Power Save Mode disabled; enable!");
          ApplicationManager.getApplication().invokeLater(() -> PowerSaveMode.setEnabled(false), ModalityState.any());
          FeatureUsageTracker.getInstance()
              .triggerFeatureUsed(AutoPowerSaverProductivityFeaturesProvider.AUTO_POWER_SAVER_FEATURE);
        }
      }, settings.delayActivated, TimeUnit.SECONDS);
    }

    @Override
    public void onFrameDeactivated() {
      LOG.debug("IDE Frame Deactivated...");
      if (ApplicationManager.getApplication().isDisposed()) {
        return;
      }

      final SettingsManager.PowerSaverState settings = SettingsManager.getInstance().getState();
      // Early exit if we're disabled (globally, or just the frame activated action)
      if (!(settings.enabledGlobally || settings.enableDeactivated)) {
        LOG.debug("Not enabled; stopping.");
        return;
      }

      cancelJobs();

      // Next, schedule a new deactivated job
      LOG.debug("Schedule new job to be run in " + settings.delayDeactivated + " seconds...");
      frameDeactivatedJob = JobScheduler.getScheduler().schedule(() -> {
        if (ApplicationManager.getApplication().isDisposed()) {
          return;
        }

        if (!PowerSaveMode.isEnabled()) {
          LOG.debug("Frame Deactivated, Power Save Mode enabledGlobally; disable!");
          ApplicationManager.getApplication().invokeLater(() -> PowerSaveMode.setEnabled(true), ModalityState.any());
          FeatureUsageTracker.getInstance()
              .triggerFeatureUsed(AutoPowerSaverProductivityFeaturesProvider.AUTO_POWER_SAVER_FEATURE);
        }
      }, settings.delayDeactivated, TimeUnit.SECONDS);
    }
  }
}
