package de.kaesdingeling.simpleblockchain.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlockMinerThread {
	private long startNonces;
	private long currentNonces;
	private long endNonces;
	private long minedNonces; // Counter will reset after calc mine speed
	@Builder.Default
	private boolean stop = false; // fallback when thread will not stops
	
	public synchronized void setMinedNonces(long minedNonces) {
		this.minedNonces = minedNonces;
	}
	
	public synchronized void countMinedNoncesUp() {
		setMinedNonces(minedNonces + 1);
	}
	
	public synchronized long getAndResetMinedNonces() {
		final long minedNonces = this.minedNonces;
		setMinedNonces(0L);
		return minedNonces;
	}
}