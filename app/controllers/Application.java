package controllers;

import java.util.ArrayList;
import java.util.List;

import models.JSONConfiguration;
import models.Node;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.ow2.play.metadata.api.Data;
import org.ow2.play.metadata.api.MetaResource;
import org.ow2.play.metadata.api.Metadata;
import org.ow2.play.metadata.api.MetadataException;
import org.ow2.play.metadata.api.Resource;
import org.ow2.play.metadata.api.service.MetadataBootstrap;
import org.ow2.play.metadata.api.service.MetadataService;

import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import utils.Locator;

public class Application extends Controller {

	@Before
	public static void init() {
		Node n = Node.getCurrentNode();
		if (n != null) {
			renderArgs.put("currentNode", n);
		}
	}

	public static void connect() {
		List<Node> nodes = Node.all().fetch();
		render(nodes);
	}

	public static void nodeConnect(Long id) {
		Node n = Node.findById(id);
		if (n != null) {
			session.put("node", n.id);
			flash.success("Connected to node %s", n.name);
		} else {
			flash.error("No such node");
		}
		connect();
	}

	public static void nodeDisconnect(Long id) {
		session.remove("node");
		flash.success("Disconnected...");
		connect();
	}

	private static Node getNode() {
		Node n = Node.getCurrentNode();
		if (n == null) {
			flash.error("Please connect to a node!");
			connect();
		}
		return n;
	}

	public static void index() {
		Node n = Node.getCurrentNode();
		if (n == null) {
			connect();
		}
		list();
	}

	/**
	 * @GET
	 */
	public static void list() {
		String url = Locator.getMetaService(getNode());
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setAddress(url);
		factory.setServiceClass(MetadataService.class);
		Object o = factory.create();
		MetadataService client = MetadataService.class.cast(o);

		List<MetaResource> list = null;
		try {
			list = client.list();
		} catch (Exception e) {
			flash.error("Unable to get metadata list '%s'", e.getMessage());
		}
		render(list);
	}

	/**
	 * @POST
	 * 
	 * @param store
	 */
	public static void loadResources(@Required String store) {
		String url = Locator.getBoostrapService(getNode());

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setAddress(url);
		factory.setServiceClass(MetadataBootstrap.class);
		Object o = factory.create();

		MetadataBootstrap client = MetadataBootstrap.class.cast(o);

		List<String> l = new ArrayList<String>();
		l.add(store);
		try {
			// TODO : let's do it asynchronously...
			client.init(l);
			flash.success("Data loaded");
		} catch (Exception e) {
			flash.error("Unable to get metadata list '%s'", e.getMessage());
		}
		load();
	}

	/**
	 * @GET
	 */
	public static void load() {
		List<JSONConfiguration> jsonconfig = JSONConfiguration.all().fetch();
		render(jsonconfig);
	}

	/**
	 * @GET
	 * 
	 * @param name
	 * @param url
	 */
	public static void resource(String name, String url) {
		String service = Locator.getMetaService(getNode());
		Resource resource = new Resource(name, url);

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setAddress(service);
		factory.setServiceClass(MetadataService.class);
		Object o = factory.create();

		MetadataService client = MetadataService.class.cast(o);
		List<Metadata> meta = null;
		try {
			meta = client.getMetaData(resource);
		} catch (MetadataException e) {
			flash.error(e.getMessage());
		}
		render(resource, meta);
	}

	/**
	 * @GET
	 */
	public static void download() {
		String service = Locator.getMetaService(getNode());
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setAddress(service);
		factory.setServiceClass(MetadataService.class);
		Object o = factory.create();

		MetadataService client = MetadataService.class.cast(o);

		renderJSON(client.list());
	}

	/**
	 * @POST
	 * 
	 * @param name
	 * @param url
	 */
	public static void createResource(String name, String url) {
		if (name == null || url == null || name.length() == 0
				|| url.length() == 0) {
			flash.error("Null or empty values are not allowed");
			create();
		}

		String service = Locator.getMetaService(getNode());
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setAddress(service);
		factory.setServiceClass(MetadataService.class);
		Object o = factory.create();

		MetadataService client = MetadataService.class.cast(o);
		try {
			client.addMetadata(new Resource(name, url), null);
		} catch (MetadataException e) {
			flash.error(e.getMessage());
		}

		resource(name, url);
	}

	public static void metadata() {
		index();
	}

	public static void create() {
		render();
	}

	public static void addMeta(String name, String url, String mname,
			String mtype, String mvalue) {
		if (mname == null || mtype == null || mvalue == null
				|| mname.length() == 0 || mtype.length() == 0
				|| mvalue.length() == 0) {
			flash.error("Null values are not allowed");
			resource(name, url);
		}

		String service = Locator.getMetaService(getNode());
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setAddress(service);
		factory.setServiceClass(MetadataService.class);
		Object o = factory.create();

		MetadataService client = MetadataService.class.cast(o);
		try {
			client.addMetadata(new Resource(name, url), new Metadata(mname,
					new Data(mtype, mvalue)));
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		resource(name, url);
	}

	public static void removeMeta() {
		flash.error("Remove is not implemented");
		index();
	}
}