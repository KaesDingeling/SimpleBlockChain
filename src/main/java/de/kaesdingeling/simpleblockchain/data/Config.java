package de.kaesdingeling.simpleblockchain.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Config {
	@Builder.Default
	private int difficulty = 4;
	@Builder.Default
	private long minNonces = 0L;
	@Builder.Default
	private long maxNonces = 500000L;
	@Builder.Default
	private int thread = 4;
	@Builder.Default
	private boolean stopByResult = true;
	@Builder.Default
	private int controllerPreLoadDuration = 5; // seconds (smaller then controllerUpdateDuration and greater then 0)
	@Builder.Default
	private int controllerUpdateDuration = 10; // secounds
}