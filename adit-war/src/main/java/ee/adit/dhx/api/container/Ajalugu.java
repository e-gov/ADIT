package ee.adit.dhx.api.container;

public abstract class Ajalugu<MINFO, TRANS, MXML>
{
	private MINFO metainfo;
	private TRANS transport;
	private Metaxml metaxml;

	public MINFO getMetainfo() {
		return metainfo;
	}

	public void setMetainfo(MINFO metainfo) {
		this.metainfo = metainfo;
	}

	public TRANS getTransport() {
		return transport;
	}

	public void setTransport(TRANS transport) {
		this.transport = transport;
	}

	public Metaxml getMetaxml() {
		return metaxml;
	}

	public void setMetaxml(Metaxml metaxml) {
		this.metaxml = metaxml;
	}
}
