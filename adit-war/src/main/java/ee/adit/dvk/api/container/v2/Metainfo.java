package ee.adit.dvk.api.container.v2;

public class Metainfo extends ee.adit.dvk.api.container.Metainfo<MetaManual, MetaAutomatic>
{
	public void createDescendants(boolean manual, boolean automatic) {
		if (manual) {
			if (getMetaManual() == null) {
				setMetaManual(new MetaManual());
			}
		}

		if (automatic) {
			if (getMetaAutomatic() == null) {
				setMetaAutomatic(new MetaAutomatic());
			}
		}
	}
}
