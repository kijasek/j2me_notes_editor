package pl.tomaszkijas.j2meeditor;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class MuuEditor extends MIDlet implements CommandListener{

	private static final Command EXIT_COMMAND = new Command("Exit", Command.EXIT, 0);
	private static final Command OK_COMMAND = new Command("Ok", Command.OK, 0);
	private static final Command SAVE_AS_COMMAND = new Command("Save as", Command.SCREEN, 1);
	private static final Command QUIT_COMMAND = new Command("Quit", Command.CANCEL, 0);
	private static final Command YES_COMMAND = new Command("Yes", Command.OK, 0);
	private static final Command NO_COMMAND = new Command("Nope", Command.CANCEL, 0);
	private static final Command BACK_COMMAND = new Command("Back", Command.BACK, 0);
	private static final Command CANCEL_COMMAND = new Command("Cancel", Command.CANCEL, 0);
	private static final Command OPEN_COMMAND = new Command("Open", Command.OK, 0);
	private static final Command SAVE_COMMAND = new Command("Save", Command.SCREEN, 1);
	private static final Command DELETE_COMMAND = new Command("Delete", Command.OK, 0);
	
	private static final String EDITOR_NAME = "MuuEditor";
	
	private static final String NEW_MEMO_LABEL = "New memo";
	private static final String OPEN_MEMO_LABEL = "Open memo";
	private static final String DELETE_MEMO_LABEL = "Delete memo";
	private static final String OPEN_RECORD_LABEL = "Open from record store";
	private static final String OPEN_FILE_LAVEL = "Open from filesystem";
	private static final String DELETE_RECORD_LABEL = "Delete from record store";
	//private static final String DELETE_FILE_LAVEL = "Delete from filesystem";
	private static final String SAVE_RECORD_LABEL = "Save in record store";
	private static final String SAVE_FILE_LAVEL = "Save in filesystem";
	
	private static final int TEXT_BOX_CAPACITY = 2048;
	
	public static final int RETURN_TO_MAIN = 0;
	public static final int RETURN_TO_EDIT = 1;
	public static final int RETURN_TO_OPEN = 2;
	public static final int RETURN_TO_NAME_RECORD = 3;
	public static final int RETURN_TO_FILE_NAME = 4;
	
	private Display display;
	private List mainMenu;
	private TextBox editorArea;
	private Form areYouSureForm;
	private List openFromMenu;
	private List deleteFromMenu;
	private Form saveToRecordStore;
	private List saveToMenu;
	private TextField specifyRecordName;
	private TextField specifyFileName;
	private List openFromStore;
	private Form saveToFileForm;
	private Alert overwriteAlert;
	private List deleteFromStore;
	
	private boolean isNewFile = true;
	private boolean isRecord = true;
	private MuuEditorRecord editedRecord;
	private String editedFile;
	
	private String tempName;
	private String tempContent;
	private boolean isOverwritingRecord = true;
	
	private MuuEditorRecordStore muuRecordStore;
	private MuuEditorBrowser muuEditorBrowser;
	private MuuEditorDirectoryBrowser muuDirecotoryBrowser;
	
	public MuuEditor() {
		display = Display.getDisplay(this);
		
		initializeMainMenu();
		initalizeEditorArea();
		initializeAreYouSureForm();
		initializeOpenFromMenu();
		initializeDeleteFromMenu();
		initializeSaveToMenu();
		initializeSaveToRecordStore();
		initializeOpenFromStore();
		initializeSaveToFileForm();
		initializeOverwriteAlert();
		initializeDeleteFromStore();
		
		muuRecordStore = new MuuEditorRecordStore(this);
		muuEditorBrowser = new MuuEditorBrowser(this);
		muuDirecotoryBrowser = new MuuEditorDirectoryBrowser(this);
	}

	protected void destroyApp(boolean arg0) {
		notifyDestroyed();
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		displayMainMenu();
	}
	
	public void commandAction(Command command, Displayable d) {
		if (mainMenu.isShown()) {		
			handleMainMenu(command, d);
		} else if (editorArea.isShown()) {
			handleEditorArea(command, d);
		} else if (areYouSureForm.isShown()) {
			handeAreYouSureForm(command, d);
		} else if (openFromMenu.isShown()) {
			handleOpenFromMenu(command, d);
		} else if (deleteFromMenu.isShown()) {
			handleDeleteFromMenu(command, d);
		} else if (saveToMenu.isShown()) {
			handleSaveToMenu(command, d);
		} else if (saveToRecordStore.isShown()) {
			handleSaveToRecordStore(command, d);
		} else if (openFromStore.isShown()) {
			handleOpenFromStore(command, d);
		} else if (saveToFileForm.isShown()) {
			handleSaveToFileForm(command, d);
		} else if (overwriteAlert.isShown()) {
			handleOverwriteAlert(command, d);
		} else if (deleteFromStore.isShown()) {
			handleDeleteFromStore(command, d);
		}
	}
	
	private void handleDeleteFromStore(Command command, Displayable d) {
		if (command == BACK_COMMAND) {
			displayDeleteFromMenu();
		} else if (command == DELETE_COMMAND) {
			List l = (List)d;
			
			int index = l.getSelectedIndex();
			String name = l.getString(index);
			
			muuRecordStore.deleteRecord(name);
		}
	}

	private void handleOverwriteAlert(Command command, Displayable d) {
		if ((command == NO_COMMAND) && isOverwritingRecord) {
			displaySaveToRecordStore();
		} else if ((command == NO_COMMAND) && !isOverwritingRecord) {
			displaySaveToFile();
		} else if ((command == YES_COMMAND) && !isOverwritingRecord) {
			muuDirecotoryBrowser.saveFile(tempName, tempContent, true);			
		} else if ((command == YES_COMMAND) && isOverwritingRecord) {
			muuRecordStore.saveRecord(tempName, tempContent, true);
		}
	}

	private void handleSaveToFileForm(Command command, Displayable d) {
		if (command == CANCEL_COMMAND) {
			displayEditorArea();
		} else if (command == SAVE_AS_COMMAND) {
			String fileName = specifyFileName.getString();
			String content = "";
			
			if ((fileName == null) || fileName.equals("")) {
				displayErrorMessage("Invalid name", RETURN_TO_FILE_NAME);
			} else {
				content = editorArea.getString();
				if (content == null) {
					content = "";
				}
				muuDirecotoryBrowser.saveFile(fileName, content, false);				
			}
		}
	}

	private void handleOpenFromStore(Command command, Displayable d) {
		if (command == BACK_COMMAND) {
			displayOpenFromMenu();
		} else if (command == OPEN_COMMAND) {
			List l = (List)d;
			int index = l.getSelectedIndex();
			String name = l.getString(index);
			MuuEditorRecord record = muuRecordStore.openRecord(name);
			
			editorArea.setString(record.getContent());
			isNewFile = false;
			isRecord = true;
			editedRecord = record;
			
			displayEditorArea();
		}
	}

	private void handleSaveToMenu(Command command, Displayable d) {
		if (command == OK_COMMAND) {
			List l = (List) d;
			
			switch (l.getSelectedIndex()) {
			case 0:
				//save to record store
				displaySaveToRecordStore();
				break;

			case 1:
				//save to file system
				displayDirectoryBrowser();
				break;
			}
		} else if (command == CANCEL_COMMAND) {
			displayEditorArea();
		}
	}

	private void handleSaveToRecordStore(Command command, Displayable d) {
		if (command == CANCEL_COMMAND) {
			displaySaveToMenu();
		} else if (command == SAVE_AS_COMMAND) {
			String recordName = specifyRecordName.getString();
			String content = "";
			
			if ((recordName == null) || recordName.equals("")) {
				displayErrorMessage("Invalid name", RETURN_TO_NAME_RECORD);
			} else {
				content = editorArea.getString();
				if (content == null) {
					content = "";
				}
				muuRecordStore.saveRecord(recordName, content, false);				
			}
		}
	}

	private void handleDeleteFromMenu(Command command, Displayable d) {
		if (command == BACK_COMMAND) {
			displayMainMenu();
		} else if (command == OK_COMMAND) {
			List l = (List) d;
			
			switch (l.getSelectedIndex()) {
			case 0:
				//delete from record store
				deleteFromStore.deleteAll();
				fillWithRecords(deleteFromStore);
				displayDeleteFromStore();
				break;

			case 1:
				//delete from filesystem
				break;
			}
		}
	}

	private void handleOpenFromMenu(Command command, Displayable d) {
		if (command == BACK_COMMAND) {
			displayMainMenu();
		} else if (command == OK_COMMAND) {
			List l = (List) d;
			
			switch (l.getSelectedIndex()) {
			case 0:
				//open from record store
				openFromStore.deleteAll();
				fillWithRecords(openFromStore);
				displayOpenFromStore();
				break;

			case 1:
				//open from filesystem
				displayMuuEditorBrowser();
				break;
			}
		}
	}

	private void handeAreYouSureForm(Command command, Displayable d) {
		if (command == YES_COMMAND) {
			displayMainMenu();
		} else if (command == NO_COMMAND) {
			displayEditorArea();
		}
	}

	private void handleEditorArea(Command command, Displayable d) {
		if (command == QUIT_COMMAND) {
			displayAreYouSureForm();
		} else if (command == SAVE_AS_COMMAND) {
			displaySaveToMenu();
		} else if (command == SAVE_COMMAND) {
			if (isNewFile) {
				displaySaveToMenu();
			} else if (isRecord) {
				editedRecord.setContent(editorArea.getString());
				muuRecordStore.replaceRecord(editedRecord);
			} else {
				muuEditorBrowser.saveFile(editedFile, editorArea.getString(), false);				
			}
		}
	}

	private void handleMainMenu(Command command, Displayable d) {
		if (command == EXIT_COMMAND) {
			destroyApp(true);
		} else if (command == OK_COMMAND) {
			List l = (List) d;
			
			switch (l.getSelectedIndex()) {
			case 0:
				//new memo
				isNewFile = true;
				editorArea.setString("");
				displayEditorArea();
				break;
			case 1:
				//open memo
				displayOpenFromMenu();
				break;
			case 2:
				//delete memo
				displayDeleteFromMenu();
				break;
			}
		}
	}

	private void initializeMainMenu() {
		mainMenu = new List(EDITOR_NAME, List.IMPLICIT);
		
		try {
			Image createNewImage = Image.createImage("/document-new.png");
			Image openImage = Image.createImage("/document-open.png");
			Image deleteImage = Image.createImage("/edit-delete.png");
			
			mainMenu.append(NEW_MEMO_LABEL, createNewImage);
			mainMenu.append(OPEN_MEMO_LABEL, openImage);
			mainMenu.append(DELETE_MEMO_LABEL, deleteImage);
		} catch (IOException e) {			
			String[] labels = {NEW_MEMO_LABEL, OPEN_MEMO_LABEL, DELETE_MEMO_LABEL};
			mainMenu = new List(EDITOR_NAME, List.IMPLICIT, labels, null);
		}
		
		mainMenu.setCommandListener(this);
		mainMenu.addCommand(EXIT_COMMAND);
		mainMenu.addCommand(OK_COMMAND);
		mainMenu.setSelectCommand(OK_COMMAND);		
	}
	
	private void initalizeEditorArea() {
		editorArea = new TextBox(EDITOR_NAME, null, TEXT_BOX_CAPACITY, TextField.ANY);
		
		editorArea.addCommand(QUIT_COMMAND);
		editorArea.addCommand(SAVE_AS_COMMAND);
		editorArea.addCommand(SAVE_COMMAND);
		
		editorArea.setCommandListener(this);
	}
	
	private void initializeAreYouSureForm() {
		areYouSureForm = new Form(EDITOR_NAME);
		
		areYouSureForm.append("Close without saving?");
		areYouSureForm.addCommand(YES_COMMAND);
		areYouSureForm.addCommand(NO_COMMAND);
		
		areYouSureForm.setCommandListener(this);
	}
	
	private void initializeOpenFromMenu() {
		openFromMenu = new List(EDITOR_NAME, List.IMPLICIT);
		
		openFromMenu.append(OPEN_RECORD_LABEL, null);
		openFromMenu.append(OPEN_FILE_LAVEL, null);
		
		openFromMenu.addCommand(BACK_COMMAND);
		openFromMenu.addCommand(OK_COMMAND);
		openFromMenu.setSelectCommand(OK_COMMAND);
		
		openFromMenu.setCommandListener(this);
	}
	
	private void initializeDeleteFromMenu() {
		deleteFromMenu = new List(EDITOR_NAME, List.IMPLICIT);
		
		deleteFromMenu.append(DELETE_RECORD_LABEL, null);
		//deleteFromMenu.append(DELETE_FILE_LAVEL, null);
		
		deleteFromMenu.addCommand(BACK_COMMAND);
		deleteFromMenu.addCommand(OK_COMMAND);
		deleteFromMenu.setSelectCommand(OK_COMMAND);
		
		deleteFromMenu.setCommandListener(this);
	}
	
	private void initializeSaveToMenu() {
		saveToMenu = new List(EDITOR_NAME, List.IMPLICIT);
		
		saveToMenu.append(SAVE_RECORD_LABEL, null);
		saveToMenu.append(SAVE_FILE_LAVEL, null);
		
		saveToMenu.addCommand(CANCEL_COMMAND);
		saveToMenu.addCommand(OK_COMMAND);
		saveToMenu.setSelectCommand(OK_COMMAND);
		
		saveToMenu.setCommandListener(this);
	}
	
	private void initializeSaveToRecordStore() {
		saveToRecordStore = new Form(EDITOR_NAME);
		
		saveToRecordStore.addCommand(CANCEL_COMMAND);
		saveToRecordStore.addCommand(SAVE_AS_COMMAND);
		
		specifyRecordName = new TextField("Record name: ", null, 16, TextField.ANY);
		saveToRecordStore.append(specifyRecordName);
		
		saveToRecordStore.setCommandListener(this);
	}
	
	private void initializeOpenFromStore() {
		openFromStore = new List(EDITOR_NAME, List.IMPLICIT);
		
		openFromStore.addCommand(BACK_COMMAND);
		openFromStore.addCommand(OPEN_COMMAND);
		openFromStore.setSelectCommand(OPEN_COMMAND);
		
		openFromStore.setCommandListener(this);		
	}
	
	private void initializeSaveToFileForm() {
		saveToFileForm = new Form(EDITOR_NAME);
		
		saveToFileForm.addCommand(CANCEL_COMMAND);
		saveToFileForm.addCommand(SAVE_AS_COMMAND);
		
		specifyFileName = new TextField("File name: ", null, 16, TextField.ANY);
		saveToFileForm.append(specifyFileName);
		
		saveToFileForm.setCommandListener(this);
	}
	
	private void initializeOverwriteAlert() {
		overwriteAlert = new Alert("Overwrite?", "Already exists, overwrite?", null, AlertType.CONFIRMATION);
		overwriteAlert.setTimeout(Alert.FOREVER);
		overwriteAlert.addCommand(YES_COMMAND);
		overwriteAlert.addCommand(NO_COMMAND);
		
		overwriteAlert.setCommandListener(this);
	}
	
	private void initializeDeleteFromStore() {
		deleteFromStore = new List(EDITOR_NAME, List.IMPLICIT);
		
		deleteFromStore.addCommand(BACK_COMMAND);
		deleteFromStore.addCommand(DELETE_COMMAND);
		deleteFromStore.setSelectCommand(DELETE_COMMAND);
		
		deleteFromStore.setCommandListener(this);		
	}
	
	private void fillWithRecords(List list) {
		Enumeration e = muuRecordStore.getStoredRecords();		
		
		if (e != null) {
			while (e.hasMoreElements()) {
				MuuEditorRecord record = (MuuEditorRecord) e.nextElement();
				list.append(record.getName(), null);
			}
		}
	}
	
	public void displayMainMenu() {
		display.setCurrent(mainMenu);
	}

	public void displayEditorArea() {
		display.setCurrent(editorArea);
	}
	
	public void displayAreYouSureForm() {
		display.setCurrent(areYouSureForm);
	}
	
	public void displayOpenFromMenu() {
		display.setCurrent(openFromMenu);
	}
	
	public void displayDeleteFromMenu() {
		display.setCurrent(deleteFromMenu);
	}
	
	public void displaySaveToMenu() {
		display.setCurrent(saveToMenu);
	}
	
	public void displaySaveToRecordStore() {
		display.setCurrent(saveToRecordStore);
	}
	
	public void displayOpenFromStore() {
		display.setCurrent(openFromStore);
	}
	
	public void displayMuuEditorBrowser() {
		display.setCurrent(muuEditorBrowser);
	}
	
	public void displayFileContent(String fileContent, String fileName) {
		if (fileContent != null) {
			editorArea.setString(fileContent);
		}
		
		isNewFile = false;
		isRecord = false;
		editedFile = fileName;
		displayEditorArea();
	}
	
	public void displayDirectoryBrowser() {
		display.setCurrent(muuDirecotoryBrowser);
	}
	
	public void displaySaveToFile() {
		display.setCurrent(saveToFileForm);
	}
	
	public void displayOverwrite(String name, String content, boolean isRecord) {
		tempName = name;
		tempContent = content;
		isOverwritingRecord = isRecord;
		display.setCurrent(overwriteAlert);
	}
	
	public void displayDeleteFromStore() {
		display.setCurrent(deleteFromStore);
	}
	
	public void displayErrorMessage(String message, int returnTo) {
		Alert alert = new Alert("Error", message, null, AlertType.INFO);
		alert.setTimeout(Alert.FOREVER);		
		
		switch (returnTo) {
		case RETURN_TO_MAIN:
			display.setCurrent(alert, mainMenu);
			break;
		case RETURN_TO_EDIT:
			display.setCurrent(alert, editorArea);
			break;
		case RETURN_TO_NAME_RECORD:
			display.setCurrent(alert, saveToRecordStore);
			break;
		case RETURN_TO_OPEN:
			display.setCurrent(openFromMenu);
			break;
		case RETURN_TO_FILE_NAME:
			display.setCurrent(saveToFileForm);
			break;
		default:
			display.setCurrent(alert, mainMenu);
			break;
		}				
	}
	
}
