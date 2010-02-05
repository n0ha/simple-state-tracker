package net.n0ha.sst;

public enum MockButton implements Button {

	SAVE(1, "SAVE"), CANCEL(2, "CANCEL"), APPROVE(3, "APPROVE"), UNVERIFY(4, "UNVERIFY");

	private final String value;

	int id;

	private MockButton(int id, String value) {
		this.id = id;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public long getId() {
		return this.id;
	}

}
