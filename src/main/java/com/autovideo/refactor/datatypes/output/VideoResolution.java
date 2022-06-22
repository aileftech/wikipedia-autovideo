package com.autovideo.refactor.datatypes.output;

public enum VideoResolution {
	LANDSCAPE_FULL_HD {
		@Override
		public int getWidth() {
			return 1920;
		}

		@Override
		public int getHeight() {
			return 1080;
		}
		
		@Override
		public OutputVideoOrientation getOrientation() {
			return OutputVideoOrientation.LANDSCAPE;
		}
		
	}, LANDSCAPE_HD {
		@Override
		public int getWidth() {
			return 1080;
		}

		@Override
		public int getHeight() {
			return 720;
		}
		
		@Override
		public OutputVideoOrientation getOrientation() {
			return OutputVideoOrientation.LANDSCAPE;
		}
	}, LANDSCAPE_SD {
		@Override
		public int getWidth() {
			return 720;
		}

		@Override
		public int getHeight() {
			return 480;
		}
		
		@Override
		public OutputVideoOrientation getOrientation() {
			return OutputVideoOrientation.LANDSCAPE;
		}
	}, SQUARE_FULL_HD {
		@Override
		public int getWidth() {
			return 1080;
		}

		@Override
		public int getHeight() {
			return 1080;
		}
		
		@Override
		public OutputVideoOrientation getOrientation() {
			return OutputVideoOrientation.SQUARE;
		}
	},SQUARE_HD {
		@Override
		public int getWidth() {
			return 720;
		}

		@Override
		public int getHeight() {
			return 720;
		}
		
		@Override
		public OutputVideoOrientation getOrientation() {
			return OutputVideoOrientation.SQUARE;
		}
	}, PORTRAIT_FULL_HD {
		@Override
		public int getWidth() {
			return 1080;
		}

		@Override
		public int getHeight() {
			return 1920;
		}
		
		@Override
		public OutputVideoOrientation getOrientation() {
			return OutputVideoOrientation.PORTRAIT;
		}
	}, PORTRAIT_HD {
		@Override
		public int getWidth() {
			return 720;
		}

		@Override
		public int getHeight() {
			return 1080;
		}
		
		@Override
		public OutputVideoOrientation getOrientation() {
			return OutputVideoOrientation.PORTRAIT;
		}
	};
	
	public abstract int getWidth();
	
	public abstract int getHeight();
	
	public abstract OutputVideoOrientation getOrientation();
	
	public double getAspectRatio() {
		return getWidth() * 1.0 / getHeight();
	}
}
