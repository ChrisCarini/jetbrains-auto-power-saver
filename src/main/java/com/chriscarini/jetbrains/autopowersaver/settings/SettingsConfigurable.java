package com.chriscarini.jetbrains.autopowersaver.settings;

import com.chriscarini.jetbrains.autopowersaver.messages.Messages;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.FormBuilder;
import javax.swing.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * A {@link Configurable} that provides the user the ability to configure the Iris plugin.
 */
public class SettingsConfigurable implements Configurable {
  @SuppressWarnings("WeakerAccess")
  protected static final int defaultActivatedDelay = 10;
  @SuppressWarnings("WeakerAccess")
  protected static final int defaultDeactivatedDelay = 10;

  @SuppressWarnings("WeakerAccess")
  protected static final int maxDelay = 90;
  private final JPanel mainPanel = new JBPanel<>();

  private final JBCheckBox enableGloballyField = new JBCheckBox();
  private final JBCheckBox enableActivatedField = new JBCheckBox();
  private final JBCheckBox enableDeactivatedField = new JBCheckBox();
  private final JSlider activatedDelaySlider = new JSlider(0, maxDelay);
  private final JSlider deactivatedDelaySlider = new JSlider(0, maxDelay);

  public SettingsConfigurable() {
    buildMainPanel();
  }

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return Messages.get("auto.power.saver.settings.display.name");
  }

  private void buildMainPanel() {
    // Create a simple form to display the user-configurable options
    mainPanel.setLayout(new VerticalFlowLayout(true, false));

    // Setup the sliders
    configureSlider(activatedDelaySlider);
    configureSlider(deactivatedDelaySlider);

    mainPanel.add(FormBuilder.createFormBuilder()
        .addLabeledComponent(Messages.get("auto.power.saver.settings.label.enabled"), enableGloballyField)
        .addSeparator()
        .addLabeledComponent(Messages.get("auto.power.saver.settings.label.enable.on.window.focus"),
            enableActivatedField)
        .addTooltip(Messages.get("auto.power.saver.settings.tooltip.enable.on.window.focus"))
        .addLabeledComponent(Messages.get("auto.power.saver.settings.label.enable.on.window.focus.delay"),
            activatedDelaySlider)
        .addTooltip(Messages.get("auto.power.saver.settings.tooltip.enable.on.window.focus.delay"))
        .addSeparator()
        .addLabeledComponent(Messages.get("auto.power.saver.settings.label.enable.on.window.unfocus"),
            enableDeactivatedField)
        .addTooltip(Messages.get("auto.power.saver.settings.tooltip.enable.on.window.unfocus"))
        .addLabeledComponent(Messages.get("auto.power.saver.settings.label.enable.on.window.unfocus.delay"),
            deactivatedDelaySlider)
        .addTooltip(Messages.get("auto.power.saver.settings.tooltip.enable.on.window.unfocus.delay"))
        .getPanel());
  }

  private void configureSlider(final JSlider slider) {
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(1);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    slider.setSnapToTicks(true);
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    // Set the user input field to contain the currently saved settings
    setUserInputFieldsFromSavedSettings();
    return mainPanel;
  }

  @Override
  public boolean isModified() {
    return !getSettingsFromUserInput().equals(getSettings());
  }

  /**
   * Apply the settings; saves the current user input list to the {@link SettingsManager}, and updates the table.
   */
  @Override
  public void apply() {
    final SettingsManager.PowerSaverState settingsState = getSettingsFromUserInput();
    SettingsManager.getInstance().loadState(settingsState);
    setUserInputFieldsFromSavedSettings();
  }

  @NotNull
  private SettingsManager.PowerSaverState getSettingsFromUserInput() {
    final SettingsManager.PowerSaverState settingsState = new SettingsManager.PowerSaverState();

    settingsState.enabledGlobally = enableGloballyField.isSelected();
    settingsState.enableActivated = enableActivatedField.isSelected();
    settingsState.enableDeactivated = enableDeactivatedField.isSelected();
    settingsState.delayActivated = activatedDelaySlider.getValue();
    settingsState.delayDeactivated = deactivatedDelaySlider.getValue();

    return settingsState;
  }

  /**
   * Get the saved settings and update the user input field.
   */
  private void setUserInputFieldsFromSavedSettings() {
    updateUserInputFields(getSettings());
  }

  /**
   * Update the user input field based on the input value provided by {@code val}
   *
   * @param settings The {@link SettingsManager.PowerSaverState} for the plugin.
   */
  private void updateUserInputFields(@Nullable final SettingsManager.PowerSaverState settings) {
    if (settings == null) {
      return;
    }

    enableGloballyField.setSelected(settings.enabledGlobally);
    enableActivatedField.setSelected(settings.enableActivated);
    enableDeactivatedField.setSelected(settings.enableDeactivated);
    activatedDelaySlider.setValue(settings.delayActivated);
    deactivatedDelaySlider.setValue(settings.delayDeactivated);
  }

  @NotNull
  private SettingsManager.PowerSaverState getSettings() {
    return SettingsManager.getInstance().getState();
  }
}
