package ee.adit.dvk.api.container.v2;

import ee.adit.dvk.api.container.Metaxml;

public class Ajalugu extends ee.adit.dvk.api.container.Ajalugu<Metainfo, Transport, Metaxml>
{
	public void createDescendants(boolean metainfo, boolean transport, boolean metaxml) {
		if (metainfo) {
			if (getMetainfo() == null) {
				setMetainfo(new Metainfo());
			}
		}

		if (transport) {
			if (getTransport() == null) {
				setTransport(new Transport());
			}
		}

		if (metaxml) {
			if (getMetaxml() == null) {
				setMetaxml(new Metaxml());
			}
		}
	}
}
