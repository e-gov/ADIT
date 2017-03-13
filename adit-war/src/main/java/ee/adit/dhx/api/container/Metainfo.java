package ee.adit.dhx.api.container;

public abstract class Metainfo<Manual, Automatic>
{
	private Manual metaManual;
	private Automatic metaAutomatic;

	public Manual getMetaManual() {
		return metaManual;
	}

	public void setMetaManual(Manual metaManual) {
		this.metaManual = metaManual;
	}

	public Automatic getMetaAutomatic() {
		return metaAutomatic;
	}

	public void setMetaAutomatic(Automatic metaAutomatic) {
		this.metaAutomatic = metaAutomatic;
	}
}
