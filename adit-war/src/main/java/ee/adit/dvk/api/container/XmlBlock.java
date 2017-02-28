package ee.adit.dvk.api.container;

import ee.adit.dvk.api.ml.Util;

public class XmlBlock
{
	@Override
	public String toString() {
		return Util.getDump(this);
	}
}
