
public class State {

	/**
	 * @param args
	 */
	private String name;
	private int sum;
	
	public State(String name, int sum) {
		this.setName(name);
		this.setSum(sum);
	}

	public int getSum() {
		return sum;
	}

	public void setSum(int sum) {
		this.sum = sum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
