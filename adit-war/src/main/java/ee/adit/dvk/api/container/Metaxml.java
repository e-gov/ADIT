package ee.adit.dvk.api.container;

import java.util.ArrayList;
import java.util.List;

public class Metaxml {
	private List<AddresseeInfo> addressees;
	private AuthorInfo authorInfo;
	private List<Compilator> compilators;
	private LetterMetaData letterMetaData;
	private ArrayOfSignature signatures;
    private String signeddoc;

	public List<AddresseeInfo> getAddressees() {
		return addressees;
	}

	public void setAddressees(List<AddresseeInfo> addressees) {
		this.addressees = addressees;
	}

	public boolean hasAddresses() {
		return addressees != null && addressees.size() > 0;
	}

	public void printAddressees() {
		if (!hasAddresses()) {
			return;
		}

		for (AddresseeInfo info : addressees) {
			System.out.print("Addressee: \n\t");
			if (info.hasOrganisation()) {
				System.out.println("Organization: " + info.getOrganisation());
			}
			if (info.hasPerson()) {
				System.out.print("\n\tPerson: ");
				System.out.println(info.getPerson());
			}
		}
	}

	public AuthorInfo getAuthorInfo() {
		return authorInfo;
	}

	public void setAuthorInfo(AuthorInfo author) {
		this.authorInfo = author;
	}

	public boolean hasAuthor() {
		return authorInfo != null;
	}

	public List<Compilator> getCompilators() {
		return compilators;
	}

	public void setCompilators(List<Compilator> compilators) {
		this.compilators = compilators;
	}

	public boolean hasCompilators() {
		return compilators != null && compilators.size() > 0;
	}

	public LetterMetaData getLetterMetaData() {
		return letterMetaData;
	}

	public void setLetterMetaData(LetterMetaData letterMetaData) {
		this.letterMetaData = letterMetaData;
	}

	public boolean hasLetterMetaData() {
		return letterMetaData != null;
	}

	public void printCompilators() {
		if (!hasCompilators()) {
			return;
		}

		for (Compilator c : compilators) {
			System.out.println("\tCompilator: " + c);
		}
	}

    public ArrayOfSignature getSignatures() {
        return signatures;
    }

    public void setSignatures(ArrayOfSignature signatures) {
        this.signatures = signatures;
    }

    public boolean hasSignatures() {
        return signatures != null && signatures.getSignature().size() > 0;
    }

    public void createDescendants(boolean addressees, boolean authorInfo, boolean compilators, boolean letterMetaData) {
        createDescendants(addressees, authorInfo, compilators, letterMetaData, false);
    }

	public void createDescendants(boolean addressees, boolean authorInfo,
	    boolean compilators, boolean letterMetaData, boolean signatures) {
		if (addressees) {
			if (this.addressees == null) {
				this.addressees = new ArrayList<AddresseeInfo>();
			}
		}

		if (authorInfo) {
			if (this.authorInfo == null) {
				this.authorInfo = new AuthorInfo();
			}
		}

		if (compilators) {
			if (this.compilators == null) {
				this.compilators = new ArrayList<Compilator>();
			}
		}

		if (letterMetaData) {
			if (this.letterMetaData == null) {
				this.letterMetaData = new LetterMetaData();
			}
		}

        if (signatures) {
            if (this.signatures == null) {
                this.signatures = new ArrayOfSignature();
            }
        }
	}

    public String getSigneddoc() {
        return signeddoc;
    }

    public void setSigneddoc(String signeddoc) {
        this.signeddoc = signeddoc;
    }
}
