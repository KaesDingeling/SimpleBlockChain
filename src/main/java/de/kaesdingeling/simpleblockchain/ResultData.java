package de.kaesdingeling.simpleblockchain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultData {
	private String hash;
	private long nonce;
}