package de.kaesdingeling.simpleblockchain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlockMinerThread {
	private long startNonces;
	private long currentNonces;
	private long endNonces;
	private long duration;
	@Builder.Default
	private boolean stop = false; // fallback#
}