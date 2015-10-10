package com.adr.bigdata.search.handler.getfilterconsumer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.search.SolrCache;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkerPool;
import com.nhb.common.Loggable;


public class CacheRegenerator implements Loggable, ExceptionHandler {

	private String workerName;
	private final int ringBufferSize = Integer.parseInt(System.getProperty("ringbuffersize", "4096"));
	private final int workerPoolSize = Integer.parseInt(System.getProperty("workerpoolsize", "8"));
	protected RingBuffer<CacheEvent> ringBuffer;
	protected WorkerPool<CacheEvent> workerPool;

	public CacheRegenerator(String workerName) {
		this.workerName = workerName;
		initWorkerPool();
	}

	public void publish(SolrCache solrCache) {
		long sequence = this.ringBuffer.next();
		try {
			CacheEvent event = this.ringBuffer.get(sequence);
			event.setCache(solrCache);
		} finally {
			this.ringBuffer.publish(sequence);
		}
	}

	private void initWorkerPool() {
		CacheWorker[] workers = new CacheWorker[workerPoolSize];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new CacheWorker();
		}

		RingBuffer<CacheEvent> _ringBuffer = RingBuffer.createMultiProducer(new EventFactory<CacheEvent>() {
			@Override
			public CacheEvent newInstance() {
				return new CacheEvent();
			}
		}, ringBufferSize);

		this.workerPool = new WorkerPool<CacheEvent>(_ringBuffer, _ringBuffer.newBarrier(), this, workers);

		_ringBuffer.addGatingSequences(this.workerPool.getWorkerSequences());

		this.ringBuffer = this.workerPool.start(new ThreadPoolExecutor(workerPoolSize, workerPoolSize, 60,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

					final AtomicInteger threadNumber = new AtomicInteger(1);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, String.format(workerName, threadNumber.getAndIncrement()));
					}
				}));
	}

	@Override
	public void handleEventException(Throwable ex, long sequence, Object event) {
		getLogger().error("error while handling: ", ex);
	}

	@Override
	public void handleOnShutdownException(Throwable ex) {
		getLogger().error("error while shutting down: ", ex);
	}

	@Override
	public void handleOnStartException(Throwable ex) {
		getLogger().error("error while starting: ", ex);
	}

}
