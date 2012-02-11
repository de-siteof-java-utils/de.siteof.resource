package de.siteof.resource.util.test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;

public class LongCountUpLatch {
	private static final class Sync extends AbstractQueuedLongSynchronizer {
		private static final long serialVersionUID = 4982264981922014374L;

		Sync(long count) {
			setState(count);
		}

		long getCount() {
			return getState();
		}

		void setCount(long count) {
			this.setState(count);
		}

		@Override
		public long tryAcquireShared(long expected) {
			return getState() >= expected ? 1 : -1;
		}

		@Override
		public boolean tryReleaseShared(long expected) {
			// Decrement count; signal when transition to zero
			for (;;) {
				long c = getState();
				if (c >= expected) {
					return false;
				}
				long nextc = c + 1;
				if (compareAndSetState(c, nextc)) {
					return nextc >= expected;
				}
			}
		}
	}

	private final Sync sync;
	private final AtomicLong expected = new AtomicLong(Long.MAX_VALUE);

	/**
	 * Constructs a {@code CountDownLatch} initialized with the given count.
	 *
	 * @param count
	 *            the number of times {@link #countDown} must be invoked before
	 *            threads can pass through {@link #await}
	 * @throws IllegalArgumentException
	 *             if {@code count} is negative
	 */
	public LongCountUpLatch(long count) {
		this.sync = new Sync(count);
	}

	public void reset() {
		this.reset(0);
	}

	public void reset(long count) {
		this.sync.setCount(count);
	}

	public void await(long expected) throws InterruptedException {
		this.expected.set(expected);
		sync.acquireSharedInterruptibly(expected);
		this.expected.set(Long.MAX_VALUE);
	}

	public boolean await(long expected, long timeout, TimeUnit unit)
			throws InterruptedException {
		this.expected.set(expected);
		boolean result = sync.tryAcquireSharedNanos(expected,
				unit.toNanos(timeout));
		this.expected.set(Long.MAX_VALUE);
		return result;
	}

	public void countUp(long increment) {
		while (increment > 0) {
			this.countUp();
			increment--;
		}
	}

	public void countUp() {
		sync.releaseShared(this.expected.get());
	}

	public long getCount() {
		return sync.getCount();
	}

	@Override
	public String toString() {
		return super.toString() + "[Count = " + sync.getCount() + "]";
	}

}
