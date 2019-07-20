package de.kaesdingeling.simpleblockchain.test;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import de.kaesdingeling.simpleblockchain.data.Block;
import de.kaesdingeling.simpleblockchain.data.Chain;
import de.kaesdingeling.simpleblockchain.data.Config;
import de.kaesdingeling.simpleblockchain.data.ResultData;

public class AppTest {
	
	@Test
	public void simpleTest() {
		Chain chain = Chain.builder()
				.position(1)
				.config(Config.builder()
						.difficulty(4)
						.minNonces(0)
						.maxNonces(500000)
						.thread(1)
						.stopByResult(false)
						.controllerPreLoadDuration(2)
						.controllerUpdateDuration(5)
						.build())
				.build();
		
		chain.addBlock("Bambus");
		
		chain.mine();
		
		Optional<Block> optionalBlock = chain.getBlocks()
			.stream()
			.filter(block -> block.getResults()
					.stream()
					.filter(result -> isMatchAResultInSimpleTest(result))
							.findAny()
							.isPresent()).findAny();
		
		assertTrue(optionalBlock.isPresent());
	}
	
	private boolean isMatchAResultInSimpleTest(ResultData result) {
		if (result.getHash().equals("0000faf8cd931548f22db71dacafcc3044f1450097ff1d6d6f3aeef8ef283d35") && result.getNonce() == 370091) {
			return true;
		}

		if (result.getHash().equals("0000c8f2f0ed74d010ab25f6c836d75274e2780449469caf069fc00f92c6ab3b") && result.getNonce() == 67494) {
			return true;
		}
		
		return false;
	}
}