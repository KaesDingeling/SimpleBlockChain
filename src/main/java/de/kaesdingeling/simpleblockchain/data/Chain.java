package de.kaesdingeling.simpleblockchain.data;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Chain {
	private int position;
	@Builder.Default
	private List<Block> blocks = Lists.newArrayList();
	private Config config;
	
	public void addBlock(String data) {
		blocks.add(Block.builder()
				.chain(this)
				.position(blocks.size() + 1)
				.data(data)
				.build());
	}
	
	public Block getPrevious(Block current) {
		int position = 0;
		
		for (Block block : blocks) {
			if (block.equals(current) && position > 0) {
				return blocks.get(position - 1);
			}
			
			position++;
		}
		
		return null;
	}
	
	public void mine() {
		System.out.println("Start mining");
		
		if (config != null) {
			for (Block block : blocks) {
				System.out.println("Block #" + block.getPosition() + " start calculate");
				block.mine(config);
				
				if (!block.isBlockFinished()) {
					System.out.println(block.getLogHead() + "Failed to calculate");
					break;
				} else {
					System.out.println(block.getLogHead() + "Calculateing success");
				}
			}
		
			System.out.println("Finish mining");
			System.out.println();
			System.out.println("Results:");
			
			for (Block block : blocks) {
				for (ResultData result : block.getResults()) {
					System.out.println(block.getLogHead() + "Hash: " + result.getHash() + " Nonce: " + result.getNonce() + " Duration: " + block.getDuration() + "ms");
				}
			}
		} else {
			System.out.println("Stop mining! No config defined");
		}
	}
	
	public String getLogHead() {
		return "Chain #" + position + " ";
	}
}