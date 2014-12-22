package com.googlecode.hawk.emc.dt;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.eclipse.epsilon.eol.models.Model;
import org.eclipse.epsilon.eol.models.ModelReference;
import org.eclipse.epsilon.eol.models.java.JavaModel;

import com.googlecode.hawk.core.graph.IGraphDatabase;
import com.googlecode.hawk.epsilon.emc.EOLQueryEngine;
import com.googlecode.hawk.ui.HawkUIEclipseViewImpl;

public class HawkModel extends ModelReference {

	public static String PROPERTY_INDEXER_NAME = "databaseName";
	protected IGraphDatabase database = null;

	public HawkModel() {
		super(new JavaModel(Collections.emptyList(), new ArrayList<Class<?>>()));
	}

	@Override
	public void load(StringProperties properties, IRelativePathResolver resolver)
			throws EolModelLoadingException {

		System.out.println(">>"+properties.getProperty(Model.PROPERTY_NAME));
		
		this.name = properties.getProperty(Model.PROPERTY_NAME);

		EOLQueryEngine eolQueryEngine = new EOLQueryEngine();
		target = eolQueryEngine;
		eolQueryEngine.setDatabaseConfig(properties);

		//
		database = HawkUIEclipseViewImpl.getIndexerByName(
				properties.getProperty(PROPERTY_INDEXER_NAME)).getGraph();

		// String location = properties.getProperty(PROPERTY_DATABASE_LOCATION);
		//
		// System.out.println("Location: " + location);
		//
		// String name = location.substring(location.lastIndexOf("/") + 1);
		// String loc = location.substring(0, location.lastIndexOf("/"));
		//
		// System.out.println(name);
		// System.out.println(loc);
		//
		// database.run(name, new File(loc), new DefaultConsole());

		eolQueryEngine.load(database);
	}

	// @Override
	// public void dispose() {
	// System.out.println("disposed connection to db");
	// super.dispose();
	// try {
	// database.shutdown(false);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

}
