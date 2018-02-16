/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - Initial
 * implementation and API
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.dialog;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.opal.commons.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Instances of this class are message areas
 */
public class FooterArea extends DialogArea {
	private static final int BUTTON_WIDTH = 70;
	private Image icon;
	private String footerText;

	private List<String> buttonLabels;
	private int defaultButtonIndex;

	private int timer;
	private int timerIndexButton;

	int selectedButtonIndex;

	private String collapsedLabelText;
	private String expandedLabelText;
	private boolean expanded;
	private String detailText;
	private boolean details;
	private Button disabledButton;

	private String checkBoxLabel;
	private boolean checkBoxValue;

	private Text expandedPanel;
	private Composite composite;

	/**
	 * Constructor
	 *
	 * @param parent dialog that is composed of this footer area
	 */
	public FooterArea(final Dialog parent) {
		super(parent);
		selectedButtonIndex = -1;
		expandedLabelText = ResourceManager.getLabel(ResourceManager.FEWER_DETAILS);
		collapsedLabelText = ResourceManager.getLabel(ResourceManager.MORE_DETAILS);
		timer = -1;
		timerIndexButton = -1;
	}

	/**
	 * Add a check box
	 *
	 * @param label label to display
	 * @param selection default value of the check box
	 * @return this footer area
	 */
	public FooterArea addCheckBox(final String label, final boolean selection) {
		checkBoxLabel = label;
		checkBoxValue = selection;
		setInitialised(true);
		return this;
	}

	/**
	 * @see org.eclipse.nebula.widgets.opal.dialog.DialogArea#render()
	 */
	@Override
	void render() {
		if (!isInitialised()) {
			return;
		}

		createSeparator();

		composite = new Composite(parent.shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		composite.setBackground(getGreyColor());

		int numberOfColumns = buttonLabels == null ? 0 : buttonLabels.size();
		if (details) {
			numberOfColumns += 2;
		}

		final GridLayout gridLayout = new GridLayout(numberOfColumns, false);
		gridLayout.marginHeight = gridLayout.marginWidth = 10;
		composite.setLayout(gridLayout);

		if (details) {
			createDetails(numberOfColumns);
		}

		if (buttonLabels != null) {
			createButtons();
		}

		if (details && parent.getMessageArea().getException() == null && expanded) {
			createExpandedPanel(numberOfColumns);
		}

		if (checkBoxLabel != null) {
			createCheckBox(numberOfColumns);
		}

		if (footerText != null) {
			createFooter();

		}

	}

	/**
	 * Create the buttons
	 */
	private void createButtons() {
		Button defaultButton = null;
		for (int i = 0; i < buttonLabels.size(); i++) {
			final Button button = new Button(composite, SWT.PUSH);
			button.setText(buttonLabels.get(i));

			final GridData gd = new GridData(GridData.END, GridData.CENTER, i == 0, false);
			final int defaultWidth = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			gd.minimumWidth = Math.max(BUTTON_WIDTH, defaultWidth);
			gd.widthHint = Math.max(BUTTON_WIDTH, defaultWidth);
			button.setLayoutData(gd);

			if (i == defaultButtonIndex) {
				defaultButton = button;
			}

			final Integer integer = Integer.valueOf(i);
			button.addSelectionListener(new SelectionAdapter() {

				/**
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				@Override
				public void widgetSelected(final SelectionEvent e) {
					FooterArea.this.parent.shell.dispose();
					selectedButtonIndex = integer.intValue();
				}

			});

			if (i == timerIndexButton && timer != -1) {
				disabledButton = button;
				button.setData(button.getText());
				button.setText(button.getText() + " (" + timer + ")");
				button.setEnabled(false);
			}

		}

		if (timerIndexButton != -1 && timer != -1) {
			Display.getCurrent().timerExec(1000, new Runnable() {

				@Override
				public void run() {
					timer--;
					if (disabledButton.isDisposed()) {
						return;
					}

					if (timer == 0) {
						disabledButton.setText((String) disabledButton.getData());
						disabledButton.setEnabled(true);
					} else {
						disabledButton.setText(disabledButton.getData() + " (" + timer + ")");
						Display.getCurrent().timerExec(1000, this);
					}

				}
			});
		}

		parent.shell.setDefaultButton(defaultButton);
	}

	/**
	 * Create the details section
	 *
	 * @param numberOfColumns
	 */
	private void createDetails(final int numberOfColumns) {
		final Label icon = new Label(composite, SWT.NONE);
		icon.setBackground(getGreyColor());
		icon.setImage(isExpanded() ? getFewerDetailsImage() : getMoreDetailsImage());
		icon.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, false, false));

		final Label label = new Label(composite, SWT.NONE);
		label.setBackground(getGreyColor());
		label.setText(isExpanded() ? expandedLabelText : collapsedLabelText);
		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));

		final int numberOfColumnsParam = numberOfColumns;

		final Listener listener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (FooterArea.this.parent.getMessageArea().getException() != null) {
					if (label.getText().equals(expandedLabelText)) {
						label.setText(collapsedLabelText);
						icon.setImage(FooterArea.this.getMoreDetailsImage());
						FooterArea.this.parent.getMessageArea().hideException();
					} else {
						label.setText(expandedLabelText);
						icon.setImage(FooterArea.this.getFewerDetailsImage());
						FooterArea.this.parent.getMessageArea().showException();
					}

				} else {
					if (label.getText().equals(expandedLabelText)) {
						label.setText(collapsedLabelText);
						icon.setImage(FooterArea.this.getMoreDetailsImage());
						expandedPanel.dispose();
						FooterArea.this.parent.pack();
					} else {
						label.setText(expandedLabelText);
						icon.setImage(FooterArea.this.getFewerDetailsImage());
						FooterArea.this.createExpandedPanel(numberOfColumnsParam);
						FooterArea.this.parent.pack();
					}
				}
			}
		};

		label.addListener(SWT.MouseUp, listener);
		icon.addListener(SWT.MouseUp, listener);
	}

	/**
	 * Create a check box
	 *
	 * @param numberOfColumns
	 */
	private void createCheckBox(final int numberOfColumns) {
		final Button button = new Button(composite, SWT.CHECK);
		button.setText(checkBoxLabel);
		button.setSelection(checkBoxValue);
		button.setBackground(getGreyColor());
		button.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false, numberOfColumns, 1));
		button.addSelectionListener(new SelectionAdapter() {

			/**
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(final SelectionEvent e) {
				checkBoxValue = button.getSelection();
			}

		});
	}

	/**
	 * Create footer section
	 */
	private void createFooter() {
		createSeparator();

		final Composite informationComposite = new Composite(parent.shell, SWT.NONE);
		informationComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
		informationComposite.setBackground(getGreyColor());

		informationComposite.setLayout(new GridLayout(icon == null ? 1 : 2, false));

		if (icon != null) {
			final Label labelIcon = new Label(informationComposite, SWT.NONE);
			labelIcon.setBackground(getGreyColor());
			labelIcon.setImage(icon);
			labelIcon.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, false, false));
		}
		final Label labelText = new Label(informationComposite, SWT.NONE);
		labelText.setBackground(getGreyColor());
		labelText.setText(footerText);
		labelText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
	}

	/**
	 * Create the expanded panel
	 *
	 * @param numberOfColumns
	 */
	private void createExpandedPanel(final int numberOfColumns) {
		expandedPanel = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		expandedPanel.setText(detailText);
		expandedPanel.setBackground(getGreyColor());
		final GridData gd = new GridData(GridData.FILL, GridData.FILL, false, false, numberOfColumns, 1);
		gd.minimumHeight = gd.heightHint = 150;
		expandedPanel.setLayoutData(gd);
	}

	/**
	 * Create a separator
	 */
	private void createSeparator() {
		final Composite c = new Composite(parent.shell, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		c.setBackground(getGreyColor());

		final GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		c.setLayout(gridLayout);

		final Label separator = new Label(c, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

	}

	// ------------------------------------------- Getters & Setters

	/**
	 * @return the icon
	 */
	public Image getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 * @return this footer area
	 */
	public FooterArea setIcon(final Image icon) {
		this.icon = icon;
		setInitialised(true);
		return this;
	}

	/**
	 * @return the text
	 */
	public String getFooterText() {
		return footerText;
	}

	/**
	 * @param text the text to set
	 * @return this footer area
	 */
	public FooterArea setFooterText(final String text) {
		footerText = text;
		setInitialised(true);
		return this;
	}

	/**
	 * @return the button labels
	 */
	public List<String> getButtonLabels() {
		return buttonLabels;
	}

	/**
	 * @param buttonLabels the button labels to set
	 * @return this footer area
	 */
	public FooterArea setButtonLabels(final List<String> buttonLabels) {
		this.buttonLabels = buttonLabels;
		setInitialised(true);
		return this;
	}

	/**
	 * @param buttonLabels the button labels to set
	 * @return this footer area
	 */
	public FooterArea setButtonLabels(final String... buttonLabels) {
		this.buttonLabels = Arrays.asList(buttonLabels);
		setInitialised(true);
		return this;
	}

	/**
	 * @return the default button index
	 */
	public int getDefaultButtonIndex() {
		return defaultButtonIndex;
	}

	/**
	 * @param defaultButtonIndex the default button index to set
	 * @return this footer area
	 */
	public FooterArea setDefaultButtonIndex(final int defaultButtonIndex) {
		this.defaultButtonIndex = defaultButtonIndex;
		setInitialised(true);
		return this;
	}

	/**
	 * @return the timer value
	 */
	public int getTimer() {
		return timer;
	}

	/**
	 * @param timer the timer value to set
	 * @return this footer area
	 */
	public FooterArea setTimer(final int timer) {
		this.timer = timer;
		setInitialised(true);
		return this;
	}

	/**
	 * @return the timer index button
	 */
	public int getTimerIndexButton() {
		return timerIndexButton;
	}

	/**
	 * @param timerIndexButton the timer index button to set
	 * @return this footer area
	 */
	public FooterArea setTimerIndexButton(final int timerIndexButton) {
		this.timerIndexButton = timerIndexButton;
		setInitialised(true);
		return this;
	}

	/**
	 * @return the selected button
	 */
	int getSelectedButton() {
		return selectedButtonIndex;
	}

	/**
	 * @return the collapsed label text
	 */
	public String getCollapsedLabelText() {
		return collapsedLabelText;
	}

	/**
	 * @param collapsedLabelText the collapsed label text to set
	 * @return this footer area
	 */
	public FooterArea setCollapsedLabelText(final String collapsedLabelText) {
		details = true;
		this.collapsedLabelText = collapsedLabelText;
		setInitialised(true);
		return this;
	}

	/**
	 * @return the expanded label text
	 */
	public String getExpandedLabelText() {
		return expandedLabelText;
	}

	/**
	 * @param expandedLabelText the expanded label text to set
	 * @return this footer area
	 */
	public FooterArea setExpandedLabelText(final String expandedLabelText) {
		details = true;
		this.expandedLabelText = expandedLabelText;
		setInitialised(true);
		return this;
	}

	/**
	 * @return the expanded flag
	 */
	public boolean isExpanded() {
		return expanded;
	}

	/**
	 * @param expanded the expanded flag to set
	 * @return this footer area
	 */
	public FooterArea setExpanded(final boolean expanded) {
		details = true;
		this.expanded = expanded;
		setInitialised(true);
		return this;
	}

	/**
	 * @return the detail text
	 */
	public String getDetailText() {
		return detailText;
	}

	/**
	 * @param detailText the detail text to set
	 * @return this footer area
	 */
	public FooterArea setDetailText(final String detailText) {
		details = true;
		this.detailText = detailText;
		setInitialised(true);
		return this;
	}

	/**
	 * @return the check box vqlue
	 */
	public boolean getCheckBoxValue() {
		return checkBoxValue;
	}

}
