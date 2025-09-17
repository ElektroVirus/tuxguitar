package app.tuxguitar.app.view.dialog.keysignature;

import app.tuxguitar.app.TuxGuitar;
import app.tuxguitar.app.ui.TGApplication;
import app.tuxguitar.app.view.controller.TGViewContext;
import app.tuxguitar.app.view.util.TGDialogUtil;
import app.tuxguitar.document.TGDocumentContextAttributes;
import app.tuxguitar.editor.action.TGActionProcessor;
import app.tuxguitar.editor.action.composition.TGChangeKeySignatureAction;
import app.tuxguitar.song.models.TGMeasure;
import app.tuxguitar.song.models.TGTrack;
import app.tuxguitar.ui.UIFactory;
import app.tuxguitar.ui.event.UISelectionEvent;
import app.tuxguitar.ui.event.UISelectionListener;
import app.tuxguitar.ui.layout.UITableLayout;
import app.tuxguitar.ui.widget.UIButton;
import app.tuxguitar.ui.widget.UICheckBox;
import app.tuxguitar.ui.widget.UIDropDownSelect;
import app.tuxguitar.ui.widget.UILabel;
import app.tuxguitar.ui.widget.UILegendPanel;
import app.tuxguitar.ui.widget.UIPanel;
import app.tuxguitar.ui.widget.UIRadioButton;
import app.tuxguitar.ui.widget.UISelectItem;
import app.tuxguitar.ui.widget.UIWindow;
import app.tuxguitar.util.TGBeatRange;
import app.tuxguitar.util.TGContext;

public class TGKeySignatureDialog {
	private boolean atLeastOneChangeToEnd = false;
	private boolean atLeastOneChangeMarkedMeasures = false;

	public void show(final TGViewContext context) {
		final TGTrack track = context.getAttribute(TGDocumentContextAttributes.ATTRIBUTE_TRACK);
		final TGMeasure measure = context.getAttribute(TGDocumentContextAttributes.ATTRIBUTE_MEASURE);
		final TGBeatRange beatRange = context.getAttribute(TGDocumentContextAttributes.ATTRIBUTE_BEAT_RANGE);
		final Boolean isSelectionActive = context.getAttribute(TGDocumentContextAttributes.ATTRIBUTE_SELECTION_IS_ACTIVE);

		final UIFactory uiFactory = TGApplication.getInstance(context.getContext()).getFactory();
		final UIWindow uiParent = context.getAttribute(TGViewContext.ATTRIBUTE_PARENT);
		final UITableLayout dialogLayout = new UITableLayout();
		final UIWindow dialog = uiFactory.createWindow(uiParent, true, false);
		final Integer oldKeySignature = measure.getKeySignature();
		atLeastOneChangeToEnd = false;
		atLeastOneChangeMarkedMeasures = false;

		dialog.setLayout(dialogLayout);
		dialog.setText(TuxGuitar.getProperty("composition.keysignature"));

		//-------key Signature-------------------------------------
		UITableLayout keySignatureLayout = new UITableLayout();
		UILegendPanel keySignature = uiFactory.createLegendPanel(dialog);
		keySignature.setLayout(keySignatureLayout);
		keySignature.setText(TuxGuitar.getProperty("composition.keysignature"));
		dialogLayout.set(keySignature, 1, 1, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_FILL, true, true);

		UILabel keySignatureLabel = uiFactory.createLabel(keySignature);
		keySignatureLabel.setText(TuxGuitar.getProperty("composition.keysignature") + ":");
		keySignatureLayout.set(keySignatureLabel, 1, 1, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_CENTER, false, true);

		final UIDropDownSelect<Integer> keySignatures = uiFactory.createDropDownSelect(keySignature);

		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.natural"), 0));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.sharp-1"), 1));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.sharp-2"), 2));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.sharp-3"), 3));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.sharp-4"), 4));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.sharp-5"), 5));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.sharp-6"), 6));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.sharp-7"), 7));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.flat-1"), 8));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.flat-2"), 9));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.flat-3"), 10));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.flat-4"), 11));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.flat-5"), 12));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.flat-6"), 13));
		keySignatures.addItem(new UISelectItem<Integer>(TuxGuitar.getProperty("composition.keysignature.flat-7"), 14));
		keySignatures.setSelectedValue(measure.getKeySignature());
		keySignatureLayout.set(keySignatures, 1, 2, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_FILL, true, true, 1, 1, 150f, null, null);

		// If the menu is opened when some bars are selected, we shouldn't be able to choose between
		// applying only for this or for whole selection. In the current implementation the bar which will
		// be changed if only one bar should be affected, it's the one with the caret - usually the last bar.
		// This is counter intuitive, since the user probably selects the bars
		// left to right starting from the bar they want actually to change.
		// Solution: if the user wants to change a single bar, don't offer a checkbox "Apply to selection".
		//
		// If no bars are selected, we offer two radio buttons: 
		//  - only current measure 
		//  - till the end.
		//
		// Still one bug:
		//	- all trials in the drop down menu will go to the undo controller
		//
		// TODO:
		//	- Create a label inside the options when no options are available, to explain the situation
		//

		//-------------------- Option Radio buttons -------------------------------

		// This is the panel where the radio buttons go
		UITableLayout optionsLayout = new UITableLayout();
		UILegendPanel options = uiFactory.createLegendPanel(dialog);
		options.setLayout(optionsLayout);
		options.setText(TuxGuitar.getProperty("options"));
		dialogLayout.set(options, 2, 1, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_FILL, true, true);

		// Only for the current measure
		final UIRadioButton applyOnlyThis = uiFactory.createRadioButton(options);
		// TODO: localize
		applyOnlyThis.setText("Apply Only for the current measure");
		optionsLayout.set(applyOnlyThis, 3, 1, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_FILL, true, true);
		applyOnlyThis.setSelected(true);

		// Apply to end
		final UIRadioButton applyToEnd = uiFactory.createRadioButton(options);
		applyToEnd.setText(TuxGuitar.getProperty("composition.tempo.position-to-end"));
		optionsLayout.set(applyToEnd, 2, 1, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_FILL, true, true);

		boolean dialogOpenedWithSelection = (isSelectionActive && (beatRange != null) && !beatRange.isEmpty());
		if (dialogOpenedWithSelection) {
			// Dialog was created with a selection
			applyOnlyThis.setEnabled(false);
			applyToEnd.setEnabled(false);
		} else {
			// Dialog was created without a selection
			applyOnlyThis.setEnabled(true);
			applyToEnd.setEnabled(true);
		}

		//------------------ BUTTONS --------------------------
		UITableLayout buttonsLayout = new UITableLayout(0f);
		UIPanel buttons = uiFactory.createPanel(dialog, false);
		buttons.setLayout(buttonsLayout);
		dialogLayout.set(buttons, 3, 1, UITableLayout.ALIGN_RIGHT, UITableLayout.ALIGN_FILL, true, true);

		final UIButton buttonOK = uiFactory.createButton(buttons);
		buttonOK.setText(TuxGuitar.getProperty("ok"));
		buttonOK.setDefaultButton();
		buttonOK.addSelectionListener(new UISelectionListener() {
			public void onSelect(UISelectionEvent event) {
				changeKeySignature(context.getContext(), track, measure,
						keySignatures.getSelectedValue(), beatRange,
						dialogOpenedWithSelection, applyToEnd.isSelected());
				dialog.dispose();
			}
		});
		buttonsLayout.set(buttonOK, 1, 1, UITableLayout.ALIGN_FILL,
				UITableLayout.ALIGN_FILL, true, true, 1, 1, 80f, 25f, null);

		UIButton buttonCancel = uiFactory.createButton(buttons);
		buttonCancel.setText(TuxGuitar.getProperty("cancel"));
		buttonCancel.addSelectionListener(new UISelectionListener() {
			public void onSelect(UISelectionEvent event) {
				changeKeySignature(context.getContext(), track, measure,
						oldKeySignature, beatRange, dialogOpenedWithSelection,
						atLeastOneChangeToEnd);
				dialog.dispose();
			}
		});
		buttonsLayout.set(buttonCancel, 1, 2, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_FILL, true, true, 1, 1, 80f, 25f, null);
		buttonsLayout.set(buttonCancel, UITableLayout.MARGIN_RIGHT, 0f);

		TGDialogUtil.openDialog(dialog,TGDialogUtil.OPEN_STYLE_CENTER | TGDialogUtil.OPEN_STYLE_PACK);

		keySignatures.addSelectionListener(new UISelectionListener() {
			public void onSelect(UISelectionEvent event) {
				// If at least once a change 'to end' was made, set the flag so
				// the cancel-button cancels the operation accordingly
				if (applyToEnd.isSelected()) {
					atLeastOneChangeToEnd = true;
				}
				changeKeySignature(context.getContext(), track, measure,
						keySignatures.getSelectedValue(), beatRange,
						dialogOpenedWithSelection, applyToEnd.isSelected());
			}
		});

	}

	public void changeKeySignature(TGContext context, TGTrack track, TGMeasure measure, Integer value, TGBeatRange beatRange, Boolean applyToSelection, Boolean applyToEnd) {
		TGActionProcessor tgActionProcessor = new TGActionProcessor(context, TGChangeKeySignatureAction.NAME);
		tgActionProcessor.setAttribute(TGDocumentContextAttributes.ATTRIBUTE_TRACK, track);
		tgActionProcessor.setAttribute(TGChangeKeySignatureAction.ATTRIBUTE_KEY_SIGNATURE, value);
		tgActionProcessor.setAttribute(TGDocumentContextAttributes.ATTRIBUTE_BEAT_RANGE, beatRange);
		tgActionProcessor.setAttribute(TGChangeKeySignatureAction.ATTRIBUTE_APPLY_TO_SELECTION, applyToSelection);
		if (applyToSelection && (beatRange != null) && !beatRange.isEmpty()) {
			// first measure of selection (start point for undo controller, whatever the caret position in selection)
			tgActionProcessor.setAttribute(TGDocumentContextAttributes.ATTRIBUTE_MEASURE, beatRange.getMeasures().get(0));
			// so that undo controller restores all modified measures
			tgActionProcessor.setAttribute(TGChangeKeySignatureAction.ATTRIBUTE_APPLY_TO_END, Boolean.TRUE);
		} else {
			tgActionProcessor.setAttribute(TGDocumentContextAttributes.ATTRIBUTE_MEASURE, measure);
			tgActionProcessor.setAttribute(TGChangeKeySignatureAction.ATTRIBUTE_APPLY_TO_END, applyToEnd);
		}
		tgActionProcessor.processOnNewThread();
	}
}
