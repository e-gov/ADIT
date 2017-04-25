package ee.adit.dhx.api.container;

import ee.adit.dhx.api.ml.Util;

public class XmlBlock
{
	@Override
	public String toString() {
		return Util.getDump(this);
	}
}
