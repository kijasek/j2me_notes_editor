package pl.tomaszkijas.j2meeditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MuuEditorRecord {

	private String name;
	private String content;
	
	public MuuEditorRecord() {
		
	}
	
	public MuuEditorRecord(String name, String content) {
		this.name = name;
		this.content = content;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public void fromByteArray(byte[] data) throws IOException {
	    ByteArrayInputStream bin = new ByteArrayInputStream(data);
	    DataInputStream din = new DataInputStream(bin);

	    name = din.readUTF();
	    content = din.readUTF();	    

	    din.close();
	}

	public byte[] toByteArray() throws IOException {
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    DataOutputStream dout = new DataOutputStream(bout);

	    dout.writeUTF(getName());
	    dout.writeUTF(getContent());	    

	    dout.close();

	    return bout.toByteArray();
	}
	
	public boolean equals(Object obj) {
		boolean result = false;		
		
		if (super.equals(obj)) {
			result = true;			
		} else if ((obj != null) && name.equals(((MuuEditorRecord)obj).getName())) {
			result = true;
		}
		
		return result;
	}
	
	public int hashCode() {
		return name.hashCode();
	}
	
}
