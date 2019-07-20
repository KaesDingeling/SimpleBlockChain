package de.kaesdingeling.simpleblockchain.data;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;

import de.kaesdingeling.simpleblockchain.utils.CONSTANTS;
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
	private long duration;
	private List<ResultData> results;
	
	/* Internal */
	private ExecutorService threadPool;
	private ExecutorService controllThread;
	private List<BlockMinerThread> threadMinerPools;
	@Builder.Default
	private boolean controllerRunning = false;
	
	public void mine(final Config config) {
		checkThreadPool(config);
		checkResults();
		
		final String pattern = calcPattern(config);
		final long startTime = System.currentTimeMillis();
		final long blockSize = getBlockSize(config);
		final Block previous = chain.getPrevious(this);

		System.out.println(getLogHead() + "Total-size: " + (config.getMaxNonces() - config.getMinNonces()));
		System.out.println(getLogHead() + "Block-size: " + blockSize);
		
		for (int i = 0; i < config.getThread(); i++) {
			final long startNonces = getStartNonces(i, blockSize, config);
			final long endNonces = getEndNonces(i, blockSize, config);
			
			System.out.println(getLogHead() + " Thread: " + (i + 1) + " Start-nonces: " + startNonces);
			System.out.println(getLogHead() + " Thread: " + (i + 1) + " End-nonces: " + endNonces);
			
			final BlockMinerThread blockMinerThread = BlockMinerThread.builder()
					.startNonces(startNonces)
					.currentNonces(startNonces)
					.endNonces(endNonces)
					.build();
			
			threadMinerPools.add(blockMinerThread);
			
			threadPool.execute(() -> {
				for (long j = blockMinerThread.getStartNonces(); j < blockMinerThread.getEndNonces(); j++) {
					String hash = calcSha256(j, previous);
					
					blockMinerThread.setCurrentNonces(j);
					blockMinerThread.countMinedNoncesUp();
					
					if (hash.startsWith(pattern)) {
						results.add(ResultData.builder()
								.nonce(j)
								.hash(hash)
								.build());
						
						System.out.println(getLogHead() + "Found result: " + hash + " Nonce: " + j + " Duration: " + (System.currentTimeMillis() + startTime));
						
						if (config.isStopByResult()) {
							stopAllThreads();
						}
						
						break;
					}
					
					if (blockMinerThread.isStop()) {
						break;
					}
				}
				
				blockMinerThread.setStop(true);
			});
		}
		
		controllThread = Executors.newSingleThreadExecutor();
		controllThread.execute(() -> {
			try {
				controllerRunning = true;
				
				System.out.println(getLogHead() + "Controller started");
				
				while (isRunning()) {
					long minedSpeedInSecondsSum = 0L;
					long todo = 0L;
					
					int waitCounter = 0;
					
					while (isRunning() && waitCounter < config.getControllerPreLoadDuration()) {
						try {
							for (BlockMinerThread blockMinerThread : threadMinerPools) {
								minedSpeedInSecondsSum = minedSpeedInSecondsSum + blockMinerThread.getAndResetMinedNonces();
							}
							
							TimeUnit.SECONDS.sleep(1);
						} catch (Exception e) {
							// do nothing
						} finally {
							waitCounter++;
						}
					}
					
					for (BlockMinerThread blockMinerThread : threadMinerPools) {
						todo = todo + (blockMinerThread.getEndNonces() - blockMinerThread.getCurrentNonces());
					}
					
					final long minedSpeedInSeconds = (minedSpeedInSecondsSum / waitCounter);
					
					String hashRate = "";
					
					if (minedSpeedInSeconds >= 1000L && minedSpeedInSeconds < 1000000L) {
						hashRate = (minedSpeedInSeconds / 1000L) + " k/s";
					} else if (minedSpeedInSeconds >= 1000000L && minedSpeedInSeconds < 1000000000L) {
						hashRate = (minedSpeedInSeconds / 1000000L) + " m/s";
					} else if (minedSpeedInSeconds >= 1000000000L && minedSpeedInSeconds < 1000000000000L) {
						hashRate = (minedSpeedInSeconds / 1000000000L) + " g/s";
					} else if (minedSpeedInSeconds >= 1000000000000L && minedSpeedInSeconds < 1000000000000000L) {
						hashRate = (minedSpeedInSeconds / 1000000000000L) + " t/s";
					} else {
						hashRate = minedSpeedInSeconds + " per secounds";
					}
					
					System.out.println(getLogHead() + "Controller >> Current hash rate: " + hashRate + " Todo: " + todo);
					
					try {
						TimeUnit.SECONDS.sleep((config.getControllerUpdateDuration() - config.getControllerPreLoadDuration()));
					} catch (Exception e) {
						// do nothing
					}
				}
				
				System.out.println(getLogHead() + "Controller stopped");
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				controllerRunning = false;
			}
		});
		
		while (controllerRunning) {
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		duration = (System.currentTimeMillis() - startTime);
	}
	
	private String calcSha256(final long nonce, final Block previous) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(position);
		builder.append(nonce);
		builder.append(data);
		builder.append((previous != null ? previous.getLastHash() : CONSTANTS.zeroHash));
		
		return Hashing.sha256()
				  .hashString(builder.toString(), StandardCharsets.UTF_8)
				  .toString();
	}
	
	public String getLastHash() {
		Optional<ResultData> resultData = results.stream().findAny();
		
		if (resultData.isPresent()) {
			return resultData.get().getHash();
		} else {
			return CONSTANTS.zeroHash;
		}
	}
	
	public boolean isBlockFinished() {
		return (results != null && results.size() > 0);
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
	
	public String getLogHead() {
		return chain.getLogHead() + "Block #" + position + " | ";
	}
	
	/* Internal */
	private long getBlockSize(Config config) {
		return ((Double) Math.ceil(((config.getMaxNonces() - config.getMinNonces()) / config.getThread()))).longValue();
	}
	
	private void checkResults() {
		if (results == null) {
			results = Lists.newLinkedList();
		} else {
			results.clear();
		}
	}
	
	private long getStartNonces(final int block, final long blockSize, final Config config) {
		long startSize = config.getMinNonces();
		
		for (int i = 0; i < block; i++) {
			startSize = startSize + blockSize;
		}

		return startSize;
	}
	
	private long getEndNonces(final int block, final long blockSize, final Config config) {
		long endSize = config.getMinNonces();
		
		for (int i = 0; i < block; i++) {
			endSize = endSize + blockSize;
		}

		return (endSize + blockSize);
	}
	
	private void checkThreadPool(Config config) {
		stopAllThreads();
		
		threadPool = Executors.newFixedThreadPool(config.getThread());
		threadMinerPools = Lists.newLinkedList();
	}
	
	private String calcPattern(Config config) {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < config.getDifficulty(); i++) {
			builder.append("0");
		}
		
		return builder.toString();
	}
}