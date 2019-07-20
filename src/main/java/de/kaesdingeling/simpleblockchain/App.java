package de.kaesdingeling.simpleblockchain;

public class App {
	public static final int difficulty = 11;
	public static final long maxNonces = 50000000000000L;
	public static final int thread = 7;
	
	public static void main(String[] args) {
		Chain chain = Chain.builder()
				.position(1)
				.build();
		
		chain.addBlock("Bambus");
		
		chain.mine();
	}
}