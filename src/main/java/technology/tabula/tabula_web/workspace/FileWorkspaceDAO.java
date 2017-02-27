package technology.tabula.tabula_web.workspace;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;


/**
 * An implementation of {@link WorkspaceDAO} that stores data in the file system
 */
public class FileWorkspaceDAO implements WorkspaceDAO {
	
	private String dataDir;
	private Workspace workspace;
	private Path workspacePath;

	public FileWorkspaceDAO(String dataDir) throws JsonIOException, JsonSyntaxException, IOException {
		this.dataDir = dataDir;
		this.workspacePath = Paths.get(this.dataDir, "pdfs", "workspace.json");
		
		// create if doesn't exist
		if (!Files.exists(this.workspacePath)) {
			Files.createDirectories(this.workspacePath.getParent());
			Files.write(this.workspacePath, "[]".getBytes());
			this.workspace = new Workspace();
			//this.flushWorkspace();
		}
		
		this.readWorkspace();
	}
	
	private synchronized void readWorkspace() throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		Type targetClassType = new TypeToken<Workspace>() { }.getType();
	    this.workspace = new Gson().fromJson(new FileReader(workspacePath.toString()), targetClassType);
	}

	/* (non-Javadoc)
	 * @see technology.tabula.tabula_web.workspace.WorkspaceDAO#getWorkspace()
	 */
	@Override
	public Workspace getWorkspace() throws WorkspaceException {
		
		try {
			this.readWorkspace();
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			throw new WorkspaceException(e);
		}
		return this.workspace;
	}
	
	private synchronized void flushWorkspace() throws JsonIOException, IOException {
		Gson gson = new GsonBuilder().create();
		gson.toJson(this.workspace,	 new FileWriter(this.workspacePath.toString()));
	}
	
	/* (non-Javadoc)
	 * @see technology.tabula.tabula_web.workspace.WorkspaceDAO#addToWorkspace(technology.tabula.tabula_web.workspace.WorkspaceEntry, java.util.List)
	 */
	@Override
	public synchronized void addToWorkspace(WorkspaceEntry we, List<DocumentPage> pages) throws WorkspaceException {
		try {
			this.readWorkspace();
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			throw new WorkspaceException(e);
		}
		this.workspace.add(0, we);

		try {
			this.flushWorkspace();
		} catch (JsonIOException | IOException e) {
			throw new WorkspaceException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see technology.tabula.tabula_web.workspace.WorkspaceDAO#getFileMetadata(java.lang.String)
	 */
	@Override
	public WorkspaceEntry getFileMetadata(String id) throws WorkspaceException {
		try {
			this.readWorkspace();
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			throw new WorkspaceException(e);
		}
		
		return this.workspace.stream().filter(we -> we.id.equals(id)).findFirst().get();	
	}
	
	/* (non-Javadoc)
	 * @see technology.tabula.tabula_web.workspace.WorkspaceDAO#getFilePages(java.lang.String)
	 */
	@Override
	public List<DocumentPage> getFilePages(String id) throws WorkspaceException {
		Type targetClassType = new TypeToken<List<DocumentPage>>() { }.getType();
	    try {
			return new Gson().fromJson(new FileReader(Paths.get(this.dataDir, "pdfs", id, "pages.json").toString()), targetClassType);
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			throw new WorkspaceException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see technology.tabula.tabula_web.workspace.WorkspaceDAO#getDocumentPath(java.lang.String)
	 */
	@Override
	public String getDocumentPath(String id) {
		return Paths.get(this.getDataDir(), "pdfs", id, "document.pdf").toString();
	}
	
	@Override
	public String getDocumentDir(String documentId) {
		// TODO Auto-generated method stub
		return Paths.get(this.getDataDir(), "pdfs", documentId).toString();
	}


	/* (non-Javadoc)
	 * @see technology.tabula.tabula_web.workspace.WorkspaceDAO#getDataDir()
	 */
	@Override
	public String getDataDir() {
		return dataDir;
	}

	@Override
	public void addFile(InputStream stream, String documentId, String filename) throws WorkspaceException {
		Path p = Paths.get(this.getDataDir(), "pdfs", documentId);
		
		if (!Files.isDirectory(p)) {
			try {
				Files.createDirectories(p);
			} catch (IOException e) {
				throw new WorkspaceException(e);
			}
		}
		
        try {
			Files.copy(stream, Paths.get(this.getDataDir(), "pdfs", documentId, filename), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new WorkspaceException(e);
		}
	}


}
