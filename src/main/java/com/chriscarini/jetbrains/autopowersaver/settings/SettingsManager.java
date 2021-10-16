package com.chriscarini.jetbrains.autopowersaver.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


/**
 * The {@link SettingsManager} for this plugin; settings will be stored out to and read from {@code auto-power-saver.xml}.
 */
@State(name = "autoPowerSaver", storages = @Storage(value = "auto-power-saver.xml"))
public class SettingsManager implements PersistentStateComponent<SettingsManager.PowerSaverState> {
  private PowerSaverState myState;

  public static SettingsManager getInstance() {
    return ApplicationManager.getApplication().getService(SettingsManager.class);
  }

  @NotNull
  @Override
  public PowerSaverState getState() {
    if (myState == null) {
      myState = new PowerSaverState();
    }
    return myState;
  }

  @Override
  public void loadState(@NotNull final PowerSaverState powerSaverState) {
    myState = powerSaverState;
  }

  /**
   * Representation of the Iris Settings {@link State}.
   */
  public static class PowerSaverState {
    public boolean enabledGlobally;

    public boolean enableActivated;
    public boolean enableDeactivated;

    public int delayActivated;
    public int delayDeactivated;

    @SuppressWarnings("WeakerAccess")
    public PowerSaverState() {
      this.enabledGlobally = true;
      this.enableActivated = true;
      this.enableDeactivated = true;
      this.delayActivated = SettingsConfigurable.defaultActivatedDelay;
      this.delayDeactivated = SettingsConfigurable.defaultDeactivatedDelay;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PowerSaverState that = (PowerSaverState) o;
      return enabledGlobally == that.enabledGlobally && enableActivated == that.enableActivated
          && enableDeactivated == that.enableDeactivated && delayActivated == that.delayActivated
          && delayDeactivated == that.delayDeactivated;
    }

    @Override
    public int hashCode() {
      return Objects.hash(enabledGlobally, enableActivated, enableDeactivated, delayActivated, delayDeactivated);
    }
  }
}
