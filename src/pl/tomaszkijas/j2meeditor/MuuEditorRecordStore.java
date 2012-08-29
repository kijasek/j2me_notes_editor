package pl.tomaszkijas.j2meeditor;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class MuuEditorRecordStore {

	private static final String RECORD_STORE_NAME = "muu";
	
	private MuuEditor muuEditor;
	private Hashtable records;
	
	public MuuEditorRecordStore(MuuEditor muuEditor) {
		this.muuEditor = muuEditor;
		this.records = new Hashtable();
	}
	
	public void saveRecord(String name, String content, boolean force) {
		MuuEditorRecord record = new MuuEditorRecord(name, content);
		RecordStore rs = null;
		boolean exists = alreadyExists(record);		
		
		if(exists && !force) {
			muuEditor.displayOverwrite(name, content, true);
		} else if (exists && force) {
			replaceRecord(record);
		} else {
		
			try {
				rs = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
				byte[] data = record.toByteArray();
				rs.addRecord(data, 0, data.length);
				
				muuEditor.displayMainMenu();
			} catch (RecordStoreFullException e) {
				muuEditor.displayErrorMessage("Record store is full", MuuEditor.RETURN_TO_EDIT);
				e.printStackTrace();
			} catch (RecordStoreNotFoundException e) {
				muuEditor.displayErrorMessage("Unable to store in record store", MuuEditor.RETURN_TO_EDIT);
				e.printStackTrace();
			} catch (RecordStoreException e) {			
				muuEditor.displayErrorMessage("Unable to store in record store", MuuEditor.RETURN_TO_EDIT);
				e.printStackTrace();
			} catch (IOException e) {
				muuEditor.displayErrorMessage("Unable to store in record store", MuuEditor.RETURN_TO_EDIT);
				e.printStackTrace();
			} finally {
				try {
					rs.closeRecordStore();
				} catch (Exception e) {
					//ignore
				}
			}		
		}
	}
	
	public void replaceRecord (MuuEditorRecord record) {
		RecordStore rs = null;
		
		try {
			rs = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
			byte[] data = record.toByteArray();
			Integer recIndex = (Integer) records.get(record);
			rs.setRecord(recIndex.intValue(), data, 0, data.length);
			
			muuEditor.displayMainMenu();
		} catch (RecordStoreFullException e) {
			muuEditor.displayErrorMessage("Record store is full", MuuEditor.RETURN_TO_EDIT);
			e.printStackTrace();
		} catch (RecordStoreNotFoundException e) {
			muuEditor.displayErrorMessage("Unable to store in record store", MuuEditor.RETURN_TO_EDIT);
			e.printStackTrace();
		} catch (RecordStoreException e) {			
			muuEditor.displayErrorMessage("Unable to store in record store", MuuEditor.RETURN_TO_EDIT);
			e.printStackTrace();
		} catch (IOException e) {
			muuEditor.displayErrorMessage("Unable to store in record store", MuuEditor.RETURN_TO_EDIT);
			e.printStackTrace();
		} finally {
			try {
				rs.closeRecordStore();
			} catch (Exception e) {
				//ignore
			}
		}		
	}
	
	public Enumeration getStoredRecords() {
		Enumeration result = null;
		RecordStore rs = null;
		RecordEnumeration re = null;
		records.clear();
		
		try {
			rs = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
			re = rs.enumerateRecords(null, null, false);
			
			while (re.hasNextElement()) {
				int id = re.nextRecordId();
				byte[] data = rs.getRecord(id);
				
				MuuEditorRecord record = new MuuEditorRecord();
				record.fromByteArray(data);
				
				records.put(record, new Integer(id));				
			}
			
			result = records.keys();
			
		} catch (RecordStoreFullException e) {
			muuEditor.displayErrorMessage("Record store is full", MuuEditor.RETURN_TO_OPEN);
			e.printStackTrace();
		} catch (RecordStoreNotFoundException e) {
			muuEditor.displayErrorMessage("Unable to read record store", MuuEditor.RETURN_TO_OPEN);
			e.printStackTrace();
		} catch (RecordStoreException e) {
			muuEditor.displayErrorMessage("Unable to read record store", MuuEditor.RETURN_TO_OPEN);
			e.printStackTrace();
		} catch (IOException e) {
			muuEditor.displayErrorMessage("Unable to read record store", MuuEditor.RETURN_TO_OPEN);
			e.printStackTrace();
		}
		finally {
			try {
				re.destroy();
				rs.closeRecordStore();
			} catch (Exception e) {
				//ignore
			}
		}
		
		return result;
	}
	
	public MuuEditorRecord openRecord(String name) {
		Enumeration e = records.keys();
		MuuEditorRecord result = null;
				
		while (e.hasMoreElements()) {
			MuuEditorRecord temp = (MuuEditorRecord) e.nextElement();
			if (temp.getName().equals(name)) {
				result = temp;
				break;
			}
		}
		
		return result;
	}
	
	public void deleteRecord(String name) {
		MuuEditorRecord recordToDelete = openRecord(name);
		Integer recordId = (Integer) records.get(recordToDelete);
		
		RecordStore rs = null;
		
		try {
			rs = RecordStore.openRecordStore(RECORD_STORE_NAME, true);			
			rs.deleteRecord(recordId.intValue());
			
			muuEditor.displayMainMenu();
		} catch (RecordStoreFullException e) {
			muuEditor.displayErrorMessage("Record store is full", MuuEditor.RETURN_TO_EDIT);
			e.printStackTrace();
		} catch (RecordStoreNotFoundException e) {
			muuEditor.displayErrorMessage("Unable to delete record", MuuEditor.RETURN_TO_EDIT);
			e.printStackTrace();
		} catch (RecordStoreException e) {			
			muuEditor.displayErrorMessage("Unable to delete record", MuuEditor.RETURN_TO_EDIT);
			e.printStackTrace();
		} finally {
			try {
				rs.closeRecordStore();
			} catch (Exception e) {
				//ignore
			}
		}		
	}
	
	private boolean alreadyExists(MuuEditorRecord record) {
		getStoredRecords();
		return records.containsKey(record);
	}
}
