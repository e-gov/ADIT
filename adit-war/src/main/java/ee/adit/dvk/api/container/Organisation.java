package ee.adit.dvk.api.container;


/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

/**
 * Class OrganisationType.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Organisation extends XmlBlock implements java.io.Serializable
{
	private java.lang.String organisationName;
	private java.lang.String departmentName;

	public java.lang.String getOrganisationName() {
		return organisationName;
	}

	public void setOrganisationName(java.lang.String organisationName) {
		this.organisationName = organisationName;
	}

	public java.lang.String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(java.lang.String departmentName) {
		this.departmentName = departmentName;
	}
}
