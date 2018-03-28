package com.openshift.evg.roadshow.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.Watch;
import io.fabric8.openshift.api.model.Route;

@Component
public class RouteWatcher extends AbstractResourceWatcher<Route> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceWatcher.class);

	private static final String PARKSMAP_BACKEND_LABEL = "type=parksmap-backend";

	@Override
	protected List<Route> listWatchedResources() {
		logger.info("listWatchedResources(): " +getOpenShiftClient().routes().inNamespace(getNamespace()).withLabel(PARKSMAP_BACKEND_LABEL).list().getItems());
		return getOpenShiftClient().routes().inNamespace(getNamespace()).withLabel(PARKSMAP_BACKEND_LABEL).list().getItems();
	}

	@Override
	protected Watch doInit() {
		logger.info("doInit()");
		return getOpenShiftClient().routes().inNamespace(getNamespace()).withLabel(PARKSMAP_BACKEND_LABEL).watch(this);
	}

	@Override
	protected String getUrl(String routeName) {
		logger.info("WK: Getting URL for route: "+routeName);
		List<Route> routes = getOpenShiftClient().routes().inNamespace(getNamespace()).withLabel(PARKSMAP_BACKEND_LABEL)
				.withField("metadata.name", routeName).list().getItems();
		if (routes.isEmpty()) {
			logger.info("WK: routes.isEmpty()=true");
			return null;
		}

		Route route = routes.get(0);
		String routeUrl = "";
		try {
			routeUrl = "http://" + route.getSpec().getHost();
		} catch (Exception e) {
			logger.error("Route {} does not have a port assigned", routeName);
		}

		logger.info("[INFO] Computed route URL: {}", routeUrl);

		return routeUrl;
	}
}
