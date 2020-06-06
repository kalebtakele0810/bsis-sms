
public class Donor {

	String id;
	String fullName;
	String phoneNumber;
	String donorNumber;
	String bloodAbo;
	String bloodRh;
	boolean isReleased;

	public Donor(String id, String fullName, String phoneNumber, String donorNumber, String bloodAbo, String bloodRh,
			boolean isReleased) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.phoneNumber = phoneNumber;
		this.donorNumber = donorNumber;
		this.bloodAbo = bloodAbo;
		this.bloodRh = bloodRh;
		this.isReleased = isReleased;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getDonorNumber() {
		return donorNumber;
	}

	public void setDonorNumber(String donorNumber) {
		this.donorNumber = donorNumber;
	}

	public String getBloodAbo() {
		return bloodAbo;
	}

	public void setBloodAbo(String bloodAbo) {
		this.bloodAbo = bloodAbo;
	}

	public String getBloodRh() {
		return bloodRh;
	}

	public void setBloodRh(String bloodRh) {
		this.bloodRh = bloodRh;
	}

	public boolean isReleased() {
		return isReleased;
	}

	public void setReleased(boolean isReleased) {
		this.isReleased = isReleased;
	}

}
