package hawk.ui.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;

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
	private List<String> locations;

	public List<String> getAllowedPlugins() {
		return allowedPlugins;
	}

	public List<String> getRegisteredMetamodels() {
		return registeredMetamodels;
	}

	private HModel(IModelIndexer e, List<String> plugins, boolean r) {
		index = e;
		allowedPlugins=plugins;
		running = r;
		registeredMetamodels=new ArrayList<String>();
		locations=new ArrayList<String>();
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

		return new HModel(m, new ArrayList<String>(), false);

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

			for (IConfigurationElement mparse : HManager.getMps())
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
			HModel hm = new HModel(m, plugins, true);

			HManager.addHawk(hm);
			System.err.println("indexer added");
			return hm;
			// selectIndexer.add(indexerName);
			// selectedIndexer = getIndexerWithName(indexerName);

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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			String metaData = index.getParentFolder().getAbsolutePath()
					+ File.separator + ".metadata_" + index.getName();
			index.shutdown(new File(metaData), false);
			Thread.sleep(1000);
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete() {
		try {
			File f = index.getParentFolder();
			if (this.isRunning()) {
				index.shutdown(null, true);
				running = false;
			}
			Thread.sleep(1000);
			deleteDir(f);
		} catch (Exception e) {
			e.printStackTrace();
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

	public String runEOL(String text) {
		return index.runEOL(text);
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
			if(!locations.contains(loc)){
				IVcsManager mo = HManager.createVCSManager("com.googlecode.hawk.svn.SvnManager");
				mo.run(loc, user, pass, myConsole);
				index.addVCSManager(mo);
				locations.add(loc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addLocal(String loc) {		
		try {
			if(!locations.contains(loc)){
				IVcsManager mo = HManager.createVCSManager("com.googlecode.hawk.localfolder.LocalFolder");
				mo.run(loc, "", "", myConsole);
				index.addVCSManager(mo);
				locations.add(loc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public Collection<String> getLocations() {
		return this.locations;
	}
	
	

}
