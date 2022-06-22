package com.autovideo.utils;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BatchedLinesStream extends FixedBatchSpliteratorBase<String> {
	private Iterator<String> it;
	
	public BatchedLinesStream(int batchSize, Iterator<String> it) {
		super(IMMUTABLE | ORDERED | NONNULL, batchSize);
		
		this.it = it;
	}

	public Stream<String> parallelStream() {
		return StreamSupport.stream(this, true);
	}
	
	public Stream<String> stream() {
		return StreamSupport.stream(this, false);
	}
	
	
	@Override
	public boolean tryAdvance(Consumer<? super String> action) {
		if (action == null)
			throw new NullPointerException();
		try {
			if (it.hasNext()) {
				action.accept(it.next());
			} else {
				return false;
			}
			return true;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void forEachRemaining(Consumer<? super String> action) {
		if (action == null)
			throw new NullPointerException();
		try {
			while (it.hasNext()) {
				String wd = it.next();
				action.accept(wd);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
