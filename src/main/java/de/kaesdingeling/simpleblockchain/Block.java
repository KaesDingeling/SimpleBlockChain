package de.kaesdingeling.simpleblockchain;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Block {
	/* Data to set */
	private String data;
	private int position;
	private Chain chain;
	
	/* Generated data */
	private long nonce;
	private boolean blockFinished;
	private long maxNonces;
	private String pattern;
	private long duration;
	private List<ResultData> results;
	
	// Default for block 1
	private static final String zeroHash = "0000000000000000000000000000000000000000000000000000000000000000";
	
	/* Internal */
	private ExecutorService threadPool;
	private ExecutorService controllThread;
	private List<BlockMinerThread> threadMinerPools;
	
	private String calcSha256(long nonce) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(position);
		builder.append(nonce);
		builder.append(data);
		
		Block previous = chain.getPrevious(this);
		
		if (previous != null) {
			//builder.append(previous.getHash());
		} else {
			builder.append(zeroHash);
		}
		
		return Hashing.sha256()
				  .hashString(builder.toString(), StandardCharsets.UTF_8)
				  .toString();
	}
	
	public void mine() {
		nonce = 0;
		blockFinished = false;
		maxNonces = calcMaxNonces();
		pattern = calcPattern();
		
		checkThreadPool();
		results = Lists.newLinkedList();
		
		long startTime = System.currentTimeMillis();
		
		final long blockSize = (App.maxNonces / App.thread);
		
		System.out.println("Block #" + position + " blocksize: " + blockSize);
		
		for (int i = 0; i < App.thread; i++) {
			final long startNonces = getStartNonces(i, blockSize);
			final BlockMinerThread blockMinerThread = BlockMinerThread.builder()
					.startNonces(startNonces)
					.currentNonces(startNonces)
					.endNonces(getEndNonces(startNonces, blockSize))
					.build();
			
			threadMinerPools.add(blockMinerThread);
			
			System.out.println("Block #" + position + " thread: " + (i + 1) + " start: " + blockMinerThread.getStartNonces());
			System.out.println("Block #" + position + " thread: " + (i + 1) + " end: " + blockMinerThread.getEndNonces());
			
			threadPool.execute(() -> {
				long counter = 0L;
				long counterSet = System.currentTimeMillis();
				
				for (long j = blockMinerThread.getStartNonces(); j < blockMinerThread.getEndNonces(); j++) {
					nonce++;
					
					String hash = calcSha256(j);
					
					if (hash.startsWith(pattern)) {
						results.add(ResultData.builder()
								.nonce(j)
								.hash(hash)
								.build());
						
						System.out.println("Block #" + position + " Found result: " + hash + " nonce: " + j);
						/*
						blockFinished = true;
						this.nonce = j;
						this.hash = hash;
						stopAllThreads();
						break;
						*/
					}
					
					// Log with hash speed
					if (counter == 100000) {
						blockMinerThread.setCurrentNonces(j);
						blockMinerThread.setDuration(System.currentTimeMillis() - counterSet);
						
						counter = 0L;
						counterSet = System.currentTimeMillis();
					}
					
					if (blockMinerThread.isStop()) {
						break;
					}
					
					counter++;
				}
			});
		}
		
		controllThread = Executors.newSingleThreadExecutor();
		controllThread.execute(() -> {
			System.out.println("Block #" + position + " controller started");
			
			while (isRunning()) {
				long allDurations = 0L;
				long todo = 0L; // TODO fix calculation
				
				for (BlockMinerThread blockMinerThread : threadMinerPools) {
					allDurations =+ blockMinerThread.getDuration();
					todo =+ (blockMinerThread.getEndNonces() - blockMinerThread.getCurrentNonces());
				}
				
				System.out.println("Block #" + position + " speed every " + (100000 * App.thread) + ": " + (allDurations / App.thread) + " todo: " + todo);
				
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			System.out.println("Block #" + position + " controller stopped");
		});
		
		while (isRunning()) {
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		duration = (System.currentTimeMillis() - startTime);
	}
	
	public boolean isRunning() {
		return (threadMinerPools != null && threadMinerPools.stream().filter(i -> !i.isStop()).findAny().isPresent());
	}
	
	public void stopAllThreads() {
		if (threadPool != null && !threadPool.isShutdown()) {
			threadPool.shutdownNow();
		}
		
		if (threadMinerPools != null && threadMinerPools.size() > 0) {
			for (BlockMinerThread blockMinerThread : threadMinerPools) {
				blockMinerThread.setStop(true);
			}
		}
		
		if (controllThread != null && !controllThread.isShutdown()) {
			controllThread.shutdownNow();
		}
	}
	
	private long getStartNonces(int block, long blockSize) {
		return (blockSize * block);
	}
	
	private long getEndNonces(long startNonces, long blockSize) {
		return (startNonces + blockSize);
	}
	
	private void checkThreadPool() {
		stopAllThreads();
		
		threadPool = Executors.newFixedThreadPool(App.thread);
		threadMinerPools = Lists.newLinkedList();
	}
	
	private long calcMaxNonces() {
		return App.maxNonces;
	}
	
	private String calcPattern() {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < App.difficulty; i++) {
			builder.append("0");
		}
		
		return builder.toString();
	}
}