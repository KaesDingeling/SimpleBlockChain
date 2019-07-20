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
		if (result.getHash().equals("00008a395ab9549f049b50db309c4fafa6369669a840de16ac7c72bcda00adb1") && result.getNonce() == 379716) {
			return true;
		}

		if (result.getHash().equals("00007c350839c019471ad05629fc763bc00d5f6d04a8820f5743f359b2540543") && result.getNonce() == 277542) {
			return true;
		}

		if (result.getHash().equals("000087f72f9e41f9cfe9ddb46e34b210c4227e134301c6eb63a5f1d402e04c5e") && result.getNonce() == 73830) {
			return true;
		}

		if (result.getHash().equals("0000e639831ae8244e7279016c6747ce2b74dc484b36bbc322c92e8e3c674602") && result.getNonce() == 223705) {
			return true;
		}
		
		return false;
	}
}