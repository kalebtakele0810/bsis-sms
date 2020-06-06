import java.util.List;

public class Order {

	List<String> componentsDIN;
	String dispatched;

	public Order(List<String> componentsDIN, String dispatched) {
		super();
		this.componentsDIN = componentsDIN;
		this.dispatched = dispatched;
	}

	public List<String> getComponentsDIN() {
		return componentsDIN;
	}

	public void setComponentsDIN(List<String> componentsDIN) {
		this.componentsDIN = componentsDIN;
	}

	public String getDispatched() {
		return dispatched;
	}

	public void setDispatched(String dispatched) {
		this.dispatched = dispatched;
	}

}
