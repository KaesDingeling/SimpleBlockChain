package de.kaesdingeling.simpleblockchain;

import de.kaesdingeling.simpleblockchain.data.Chain;
import de.kaesdingeling.simpleblockchain.data.Config;

public class App {
	public static final int difficulty = 11;
	public static final long maxNonces = 50000000000000L;
	public static final int thread = 7;
	
	public static void main(String[] args) {
		Chain chain = Chain.builder()
				.position(1)
				.config(Config.builder()
						.stopByResult(false)
						.build())
				.build();
		
		chain.addBlock("Bambus");
		
		chain.mine();
	}
}