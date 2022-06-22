package com.autovideo.refactor.datatypes.output;

public enum OutputVideoOrientation {
	PORTRAIT {
		@Override
		public int getReferenceWidth() {
			return VideoResolution.PORTRAIT_FULL_HD.getWidth();
		}
		
		public int getReferenceHeight() {
			return VideoResolution.PORTRAIT_FULL_HD.getHeight();
		}
		
		@Override
		public VideoResolution getHighestResolution() {
			return VideoResolution.PORTRAIT_FULL_HD;
		}
	}, LANDSCAPE {
		@Override
		public int getReferenceWidth() {
			return VideoResolution.LANDSCAPE_FULL_HD.getWidth();
		}
		
		@Override
		public int getReferenceHeight() {
			return VideoResolution.LANDSCAPE_FULL_HD.getHeight();
		}
		
		@Override
		public VideoResolution getHighestResolution() {
			return VideoResolution.LANDSCAPE_FULL_HD;
		}
	}, SQUARE {
		@Override
		public int getReferenceWidth() {
			return VideoResolution.SQUARE_FULL_HD.getWidth();
		}
		
		@Override
		public int getReferenceHeight() {
			return VideoResolution.SQUARE_FULL_HD.getHeight();
		}
		
		@Override
		public VideoResolution getHighestResolution() {
			return VideoResolution.SQUARE_FULL_HD;
		}
	};
	
	/**
	 * Returns the FULL HD resolution width for the given format.
	 * Used to compute proportions for placing elements correctly at different resolutions
	 * @return
	 */
	public abstract int getReferenceWidth();
	
	public abstract int getReferenceHeight();
	
	public abstract VideoResolution getHighestResolution();
}
