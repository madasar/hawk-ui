package hawk.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.ecore.EPackage;

import com.googlecode.hawk.core.IMetaModelResourceFactory;
import com.googlecode.hawk.core.IMetaModelUpdater;
import com.googlecode.hawk.core.IModelIndexer;
import com.googlecode.hawk.core.IModelResourceFactory;
import com.googlecode.hawk.core.IModelUpdater;
import com.googlecode.hawk.core.IVcsManager;
import com.googlecode.hawk.core.graph.IGraphDatabase;
import com.googlecode.hawk.core.query.IQueryEngine;
import com.googlecode.hawk.core.runtime.ModelIndexerImpl;

public class HModel {

	private IModelIndexer index;
	private boolean running;
	private List<String> allowedPlugins;
	private List<String> registeredMetamodels;

	public List<String> getAllowedPlugins() {
		return allowedPlugins;
	}

	public ArrayList<String> getRegisteredMetamodels() {
		//return registeredMetamodels;
		return  new ArrayList<String>(EPackage.Registry.INSTANCE.keySet());
	}

	private HModel(IModelIndexer e, boolean r) {
		index = e;
		allowedPlugins=new ArrayList<String>();
		running = r;
		registeredMetamodels=new ArrayList<String>();
	}

	private static HConsole myConsole;

	public static HModel createFromFolder(String indexerName, String folderName) {

		if (myConsole == null)
			myConsole = new HConsole("Hawk Console V2");
		IModelIndexer m = null;

		try {
			m = new ModelIndexerImpl(indexerName, new File(folderName),myConsole);

			for (IConfigurationElement mmparse : HManager.getMmps())
				m.addMetaModelResourceFactory((IMetaModelResourceFactory) mmparse.createExecutableExtension("MetaModelParser"));

			for (IConfigurationElement mparse : HManager.getMps())
				m.addModelResourceFactory((IModelResourceFactory) mparse.createExecutableExtension("ModelParser"));
			
			for (IConfigurationElement ql : HManager.getLanguages())
				m.addQueryEngine((IQueryEngine) ql.createExecutableExtension("query_language"));
			
			for (IConfigurationElement updater : HManager.getUps())
				m.addModelUpdater((IModelUpdater) updater.createExecutableExtension("ModelUpdater"));



			
		} catch (Exception e) {
			System.err
					.println("Exception in trying to add create Indexer from folder:");
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.err.println("Adding of indexer aborted, please try again");
		}

		HModel hm= new HModel(m, false);
		return hm;

	}
	

	private void loadConfig() {
		loadLocalFolderConfig();
	}

	private void saveConfig() {
		saveLocalFolderConfig();
	}
	

	
	private void saveLocalFolderConfig() {
		Properties p = createPropertyList("folder", (List<String>) this.getLocalLocations());
		saveConfig(index.getParentFolder().getAbsolutePath(),".localfolder",p);
	}
	
	private void loadLocalFolderConfig(){
		for(String folder: getPropertyList(loadConfig(this.getFolder(), ".localfolder"), "folder")){
			this.addLocal(folder);
		}
	}

	private static Properties loadConfig(String folderName, String configName) {
		File config = new File(folderName+File.separator+configName);
		Properties p= new Properties();
		if(config.exists() && config.isFile() && config.canRead()){
			try {
				p.load(new FileInputStream(config.getAbsolutePath()));
			} catch (FileNotFoundException e) {
				 e.printStackTrace();
			} catch (IOException e) {
			}
		}
		return p;
	}
	
	private static void saveConfig(String folderName, String configName, Properties p) {
		File config = new File(folderName+File.separator+configName);
		
		if(config.exists() && config.isFile() && config.canWrite()){
			config.delete();
		}
		
		if(!config.exists()){
			try {
				p.store(new FileOutputStream(config.getAbsolutePath()), "");
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}
	
	
	private static List<String> getPropertyList(Properties properties, String name) 
	{
	    List<String> result = new ArrayList<String>();
	    for (Entry<Object, Object> entry : properties.entrySet()){
	        if (((String)entry.getKey()).matches("^" + Pattern.quote(name) + "\\.\\d+$")){
	            result.add((String) entry.getValue());
	        }
	    }
	    return result;
	}
	
	private static Properties createPropertyList(String key, List<String> values) 
	{
		Properties p = new Properties();
		for (int i=0; i<values.size();i++){
			p.setProperty(key+"."+i, values.get(i));
		}
	    return p;
	}

	public static HModel create(String indexerName, String folderName,
			List<String> plugins) {

		if (myConsole == null)
			myConsole = new HConsole("Hawk Console V2");

		IModelIndexer m;
		try {
			m = new ModelIndexerImpl(indexerName, new File(folderName),
					myConsole);

			// create the indexer with relevant database


			// set up plugins
			// first get all of type (static callto HawkOSGIConfigManager)
			// check each one has the an ID that was selected
			// create VCS
			// call m.add

			for (IConfigurationElement mmparse : HManager.getMmps())
				m.addMetaModelResourceFactory((IMetaModelResourceFactory) mmparse.createExecutableExtension("MetaModelParser"));

			for(IConfigurationElement mparse : HManager.getMps())
				m.addModelResourceFactory((IModelResourceFactory) mparse.createExecutableExtension("ModelParser"));
			
			for (IConfigurationElement ql : HManager.getLanguages())
				m.addQueryEngine((IQueryEngine) ql.createExecutableExtension("query_language"));
			
			for (IConfigurationElement updater : HManager.getUps())
				m.addModelUpdater((IModelUpdater) updater.createExecutableExtension("ModelUpdater"));
			
			IGraphDatabase db = HManager
					.createGraph("com.googlecode.hawk.neo4j_v2.Neo4JDatabase");
			db.run(indexerName, new File(folderName), myConsole);
			m.setDB(db);

			// hard coded metamodel updater?
			IMetaModelUpdater metaModelUpdater = HManager.getMetaModelUpdater();
			m.setMetaModelUpdater(metaModelUpdater);	
			
			m.init("".toCharArray());
			HModel hm = new HModel(m, true);

			HManager.addHawk(hm);
			//System.err.println("indexer added");
			return hm;

		} catch (Exception e) {
			System.err.println("Exception in trying to add new Indexer:");
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.err.println("Adding of indexer aborted, please try again");
		}

		return null;
	}

	public String getName() {
		return index.getName();
	}

	public String getFolder() {
		return index.getParentFolder().toString();
	}

	public boolean isRunning() {
		return running;
	}

	public void start() {
		try {
			// create the indexer with relevant database
			IGraphDatabase db = HManager
					.createGraph("com.googlecode.hawk.neo4j_v2.Neo4JDatabase");
			db.run(this.getName(), new File(this.getFolder()), myConsole);
			index.setDB(db);

			// hard coded metamodel updater?
			IMetaModelUpdater metaModelUpdater = HManager.getMetaModelUpdater();
			index.setMetaModelUpdater(metaModelUpdater);

			index.init("".toCharArray());
			running = true;
			
			this.loadConfig();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			this.saveConfig();
			String metaData = index.getParentFolder().getAbsolutePath()
					+ File.separator + ".metadata_" + index.getName();
			index.shutdown(new File(metaData), false);

			running = false;
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public void delete() {
		
			File f = index.getParentFolder();
			while (this.isRunning()) {
				try {	
					index.shutdown(null, true);
					running = false;		
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			for (int i =0; i<5 || deleteDir(f);i++){
				
			}

	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	public String toString() {
		return this.getName()
				+ (this.isRunning() ? " (running) " : " (paused) ") + " ["
				+ this.getFolder() + "] ";
	}

	public String query(String query) {
		return query(query, "com.googlecode.hawk.epsilon.emc.GraphEpsilonModel");
	}
	
	public String query(String query, String ql) {
		IQueryEngine q = index.getKnownQueryLanguages()
				.get(ql);

		Object ret = q.contextlessQuery(index.getGraph(), query);

		return ret.toString();
	}

	public boolean registerMeta(File f) {
		if(registeredMetamodels.contains(f.getAbsolutePath()))
			return true;
		try {
			index.registerMetamodel(f);
		} catch (Exception e) {
			return false;
		}
		this.registeredMetamodels.add(f.getAbsolutePath());
		return true;
	}
	
	public Collection<String> getVCSTypeNames(){
		return HManager.getVCSTypes();
	}

	public void addSVN(String loc, String user, String pass) {
		try {
			if(!this.getLocations().contains(loc)){
				IVcsManager mo = HManager.createVCSManager("com.googlecode.hawk.svn.SvnManager");
				mo.run(loc, user, pass, myConsole);
				index.addVCSManager(mo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addLocal(String loc) {		
		try {
			if(!this.getLocations().contains(loc)){
				IVcsManager mo = HManager.createVCSManager("com.googlecode.hawk.localfolder.LocalFolder");
				mo.run(loc, "", "", myConsole);
				index.addVCSManager(mo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public Collection<String> getLocations() {

		List<String> locations=new ArrayList<String>();;
		for(IVcsManager o: index.getRunningVCSManagers()){
			locations.add(o.getLocation());
		}
		return locations;
	}
	
	public Collection<String> getLocalLocations() {

		List<String> locations=new ArrayList<String>();;
		for(IVcsManager o: index.getRunningVCSManagers()){
			if(o.getType().contains("localfolder"))
				locations.add(o.getLocation());
		}
		return locations;
	}

	public IGraphDatabase getGraph() {
		return index.getGraph();
	}
	
	

}
