package com.autovideo.utils;

public enum Language {
	EN {
		@Override
		public String getName() {
			return "english";
		}
	}, 
	
	IT {
		@Override
		public String getName() {
			return "italian";
		}
	}, 
	
	FR {
		@Override
		public String getName() {
			return "french";
		}
	}, 
	
	ES {
		@Override
		public String getName() {
			return "spanish";
		}
	}, 
	
	DE {
		@Override
		public String getName() {
			return "german";
		}
	}, 
	
	ZH {
		@Override
		public String getName() {
			return "chinese";
		}
	},
	
	SIMPLE {
		@Override
		public String getName() {
			return "simple english";
		}
	},
	
	NL {
		@Override
		public String getName() {
			return "dutch";
		}
	},
	
	AGNOSTIC {
		@Override
		public String getName() {
			return "agnostic";
		}
	};
	
	public static Language fromString(String lang) {
		try {
			return valueOf(lang.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	public abstract String getName();
}
