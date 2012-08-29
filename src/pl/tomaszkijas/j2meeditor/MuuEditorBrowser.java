package pl.tomaszkijas.j2meeditor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

public class MuuEditorBrowser extends List implements CommandListener{

	protected static final Command OK_COMMAND = new Command("Ok", Command.OK, 0);
	protected static final Command CANCEL_COMMAND = new Command("Cancel", Command.CANCEL, 0);
	
	protected static Image dirImage;
	protected static Image fileImage;
	
	protected static final String UP_DIRECTORY = "..";
	protected static final String ABSOLUTE_ROOT = "/";
	protected final static String FILE_PREFIX = "file:///";
	protected static final char SEP = '/';
	
	protected String currentDirectoryName = ABSOLUTE_ROOT;
	
	protected MuuEditor muuEditor;
		
	public MuuEditorBrowser(MuuEditor muuEditor) {
		super("Browse...", List.IMPLICIT);
		
		this.muuEditor = muuEditor;
		
		addCommand(CANCEL_COMMAND);
		addCommand(OK_COMMAND);
		
		setCommandListener(this);
		setSelectCommand(OK_COMMAND);
		
		showCurrentDir();
	}
	
	public void commandAction(Command command, Displayable d) {
		if (command == CANCEL_COMMAND) {
			muuEditor.displayMainMenu();
		} else if (command == OK_COMMAND) {
			final String currFile = getString(getSelectedIndex());			
            
			new Thread(new Runnable() {
                    public void run() {
                        if (currFile.endsWith("/") || currFile.equals(UP_DIRECTORY)) {
                            traverseDirectory(currFile);
                        } else {
                        	showFile(currFile);                            
                        }
                    }
                }).start();
		}
	}
	
	public void showFile(String fileName) {
		String content = null;
		InputStream in = null;		
		FileConnection fc = null;
		
		try {
			fc = (FileConnection) Connector.open(FILE_PREFIX + currentDirectoryName + fileName);
			
			if (!fc.exists()) {
				muuEditor.displayErrorMessage("File does not exist", MuuEditor.RETURN_TO_MAIN);
				return;
			}
			
			int fileSize = (int) fc.fileSize();
			byte[] data = new byte[fileSize];
			
			in = fc.openInputStream();
			
			int correctSize = in.read(data, 0, fileSize);
			
			if (correctSize > 0) {
				content = new String(data, 0, correctSize);
			}
			
		} catch (IOException e) {
			muuEditor.displayErrorMessage("Unable to open file", MuuEditor.RETURN_TO_MAIN);
			e.printStackTrace();
		} catch (SecurityException e) {
			muuEditor.displayErrorMessage("Permission denied", MuuEditor.RETURN_TO_MAIN);
			e.printStackTrace();
		} finally {
			try {
				in.close();				
				fc.close();
			} catch (Exception e) { }
		}
		
		muuEditor.displayFileContent(content, currentDirectoryName + fileName);
	}
	
	public Enumeration getRootDirs() {
		Enumeration drives = FileSystemRegistry.listRoots();
				
		return drives;
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
			    } else  {
			    	append(fileName, fileImage);			    	
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
	
	public void traverseDirectory(String fileName) {
		if (UP_DIRECTORY.equals(fileName)) {
			int i = currentDirectoryName.lastIndexOf(SEP, currentDirectoryName.length() - 2);

            if (i != -1) {
                currentDirectoryName = currentDirectoryName.substring(0, i + 1);
            } else {
                currentDirectoryName = ABSOLUTE_ROOT;
            }
		} else if (ABSOLUTE_ROOT.equals(currentDirectoryName)) {
			currentDirectoryName = fileName;
		} else {
			currentDirectoryName = currentDirectoryName + fileName;
		}
		
		showCurrentDir();
	}
	
	public void saveFile(String fileName, String fileContent, boolean force) {
		final String name = fileName;
		final String content = fileContent;
		final boolean forceWrite = force;
		
		new Thread(new Runnable() {
            public void run() {
                saveFileInternal(name, content, forceWrite);
            }
        }).start();
	}
	
	protected void saveFileInternal(String fileName, String fileContent, boolean force) {
		FileConnection fc = null;
		OutputStream out = null;
		
		try {
			fc = (FileConnection) Connector.open(FILE_PREFIX + fileName);
			
			if (!fc.exists()) {
				fc.create();				
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
	
	static {
		try {
			dirImage = Image.createImage("/dir.png");
			fileImage = Image.createImage("/file.png");
		} catch (IOException e) {
			dirImage = null;
			fileImage = null;
		}
	}
}
