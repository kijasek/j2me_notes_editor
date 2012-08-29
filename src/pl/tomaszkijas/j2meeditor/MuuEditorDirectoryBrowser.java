package pl.tomaszkijas.j2meeditor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

public class MuuEditorDirectoryBrowser extends MuuEditorBrowser {
	
	private static final Command CD_COMMAND = new Command("cd", Command.OK, 0);
	
	public MuuEditorDirectoryBrowser(MuuEditor muuEditor) {
		super(muuEditor);		
		
		setTitle("Choose directory...");		
		addCommand(CD_COMMAND);
		setSelectCommand(CD_COMMAND);
	}
	
	public void commandAction(Command command, Displayable d) {
		if (command == CANCEL_COMMAND) {
			muuEditor.displayEditorArea();
		} else if (command == OK_COMMAND) {
			userClicked(false);
		} else if (command == CD_COMMAND) {
			userClicked(true);
		}
	}
	
	protected void showCurrentDir() {
		Enumeration e = null;
		FileConnection currentDir = null;
		deleteAll();
		
		try {
			if (ABSOLUTE_ROOT.equals(currentDirectoryName)) {
				e = getRootDirs();
			} else {
				currentDir = (FileConnection)Connector.open(FILE_PREFIX + currentDirectoryName);
				e = currentDir.list();
				append(UP_DIRECTORY, dirImage);
			}		
			
			while (e.hasMoreElements()) {
			    String fileName = (String)e.nextElement();

			    if (fileName.charAt(fileName.length() - 1) == SEP) {
			    	append(fileName, dirImage);
			    } 
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (currentDir != null) {
					currentDir.close();
				}
			} catch (IOException e1) { }				
		}		
	}
	
	private void userClicked(final boolean isCdCommand) {
		final String currFile = getString(getSelectedIndex());			
        
		new Thread(new Runnable() {
                public void run() {
                    if ((currFile.endsWith("/") || currFile.equals(UP_DIRECTORY)) && isCdCommand) {
                        traverseDirectory(currFile);
                    } else {
                    	if (UP_DIRECTORY.equals(currFile)) {
                			int i = currentDirectoryName.lastIndexOf(SEP, currentDirectoryName.length() - 2);

                            if (i != -1) {
                                currentDirectoryName = currentDirectoryName.substring(0, i + 1);
                            } else {
                                currentDirectoryName = ABSOLUTE_ROOT;
                            }
                		} else if (ABSOLUTE_ROOT.equals(currentDirectoryName)) {
                			currentDirectoryName = currFile;
                		} else {
                			currentDirectoryName = currentDirectoryName + currFile;
                		}
                    	
                    	muuEditor.displaySaveToFile();
                    }
                }
            }).start();
	}
	
	protected void saveFileInternal(String fileName, String fileContent, boolean force) {
		FileConnection fc = null;
		OutputStream out = null;
		
		try {
			fc = (FileConnection) Connector.open(FILE_PREFIX + currentDirectoryName + fileName);
			System.out.println("dUAPPAPAPAPAAPAP");
			
			if (!fc.exists()) {
				fc.create();				
			} else if (!force) {
				muuEditor.displayOverwrite(fileName, fileContent, false);
				return;
			}
			
			out = fc.openOutputStream();
			byte[] bytes = fileContent.getBytes();
			out.write(bytes);
			
		} catch (IOException e) {
			muuEditor.displayErrorMessage("Unable to save file", MuuEditor.RETURN_TO_MAIN);
			e.printStackTrace();
		} catch (SecurityException e) {
			muuEditor.displayErrorMessage("Permission denied", MuuEditor.RETURN_TO_MAIN);
			e.printStackTrace();
		} finally {
			try {
				out.flush();
				out.close();				
				fc.close();
			} catch (Exception e) { }
		}
		
		muuEditor.displayMainMenu();		
	}
}
