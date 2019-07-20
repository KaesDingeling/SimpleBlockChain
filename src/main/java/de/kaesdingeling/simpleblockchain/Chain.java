package de.kaesdingeling.simpleblockchain;

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
		
		for (Block block : blocks) {
			System.out.println("Block #" + block.getPosition() + " start calculate");
			block.mine();
			
			if (!block.isBlockFinished()) {
				System.out.println("Block #" + block.getPosition() + " failed to calculate");
				break;
			} else {
				System.out.println("Block #" + block.getPosition() + " calculateing success");
			}
		}
		
		System.out.println("Finish mining");
		System.out.println();
		System.out.println("Results:");
		
		for (Block block : blocks) {
			for (ResultData result : block.getResults()) {
				System.out.println("Block #" + block.getPosition() + " hash: " + result.getHash() + " nonce: " + block.getNonce() + " duration: " + block.getDuration() + "ms");
			}
		}
	}
}