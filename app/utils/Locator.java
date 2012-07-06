/**
 * 
 */
package utils;

import models.Node;

/**
 * Deprecated...
 * 
 * @author chamerling
 * 
 */
public class Locator {

	/**
	 * Try to find a business service which endpoint contains the given input
	 * string. Dummy way to lookup service until we have a reall governance
	 * tool...
	 * 
	 * @param pattern
	 * @param node
	 * @return
	 */
	public static String getBusinessService(String pattern, Node node) {
		return node.baseURL + "/" + pattern;
		/*
		 * String result = null; if (node == null || pattern == null) { return
		 * null; }
		 * 
		 * ServiceInformation information = CXFHelper.getClient(node.baseURL,
		 * ServiceInformation.class);
		 * 
		 * try { Set<String> businessServices =
		 * information.getExposedWebServices(); Iterator<String> iter =
		 * businessServices.iterator(); boolean found = false; while
		 * (iter.hasNext() && !found) { String url = iter.next(); if (url !=
		 * null && url.contains(pattern)) { found = true; result = url; } } }
		 * catch (Exception e) { }
		 * 
		 * return result;
		 */
	}

	public static String getBoostrapService(Node node) {
		return node.baseURL.endsWith("/") ? node.baseURL + "BootService" : node.baseURL + "/BootService";
	}

	public static String getMetaService(Node node) {
		return node.baseURL.endsWith("/") ? node.baseURL + "MetadataService" : node.baseURL + "/MetadataService";
	}

}
