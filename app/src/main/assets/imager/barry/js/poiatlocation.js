var lchangeCount = 0;

// implementation of AR-Experience (aka "World")
var World = {
	// true once data was fetched
	initiallyLoadedData: false,

	// POI-Marker asset
	markerDrawable_idle: null,
	
	markerFilename : null,
	
	markerImageDrawable_idle : null,
	markerImageDrawable_idle2 : null,
	
	ghostAnimGroup : null,
	ghostAnimGroup2 : null,
	isAnimStart : false,
	isWithinFrame: false,

	markerObject : null,
	markerObject2 : null,
	
	markerLocation :null,
	markerLocation2 : null,

	init: function initFn(){
	
	},
	
	// called to inject new POI data, happens every second when location change is updated
	loadPoisFromJsonData: function loadPoisFromJsonDataFn(poiData, poiData2) {
		World.markerDrawable_idle = new AR.ImageResource(markerFilename);
		
		// create the marker
		World.markerLocation = new AR.GeoLocation(poiData.latitude, poiData.longitude, poiData.altitude);
		
		World.markerImageDrawable_idle = new AR.ImageDrawable(World.markerDrawable_idle, 2.5, {
			zOrder: 0,
			opacity: 1,
			scale: 0,
		});
		
		World.markerLocation2 = new AR.GeoLocation(poiData2.latitude, poiData2.longitude, poiData2.altitude);
		
		World.markerImageDrawable_idle2 = new AR.ImageDrawable(World.markerDrawable_idle, 2.5, {
			zOrder: 0,
			opacity: 0,
			scale: 0,
		});
		

		ghostAnimGroup = this.createGhostAnimation(World.markerImageDrawable_idle, 9, 0);
		ghostAnimGroup2 = this.createGhostAnimation(World.markerImageDrawable_idle2, 7, 0.8);
		

		// create GeoObject
		World.markerObject = new AR.GeoObject(World.markerLocation, {
			drawables: {
				cam: [World.markerImageDrawable_idle]
			},
			onEnterFieldOfVision: this.appear,
			onExitFieldOfVision : this.disappear
		});
		
		World.markerObject2 = new AR.GeoObject(World.markerLocation2, {
			drawables: {
				cam: [World.markerImageDrawable_idle2]
				
			}
		});
	
	},

	// updates status message shon in small "i"-button aligned bottom center
	updateStatusMessage: function updateStatusMessageFn(message, isWarning) {

		var themeToUse = isWarning ? "e" : "c";
		var iconToUse = isWarning ? "alert" : "info";

		$("#status-message").html(message);
		$("#popupInfoButton").buttonMarkup({
			theme: themeToUse
		});
		$("#popupInfoButton").buttonMarkup({
			icon: iconToUse
		});
	},
	

	createGhostAnimation: function createAppearingAnimationFn(model, scale, opac) {
		var sx = new AR.PropertyAnimation(model, "scale", 5, scale, 15000, 
			{type: AR.CONST.EASING_CURVE_TYPE.EASE_IN_OUT_BOUNCE} );
		var sy = new AR.PropertyAnimation(model, "opacity", 0.2, opac, 15000, 
			{type: AR.CONST.EASING_CURVE_TYPE.EASE_OUT_SINE},
			{onFinish:World.handleFinish} );
		
		var sz = new AR.PropertyAnimation(model, "heading", 0, 25, 25000, 
			{type: AR.CONST.EASING_CURVE_TYPE.EASE_IN_EXPO} );
				
		return new AR.AnimationGroup(AR.CONST.ANIMATION_GROUP_TYPE.PARALLEL, [sx, sy,sz]);
	
	},
	
	
	
	handleFinish: function handleFinishFn( ) {
		AR.logger.debug("Animation Completed!");
	},
	
	
	setGhostMarker: function setGhostMarkerFn(ghostNum) {
		switch(ghostNum){
			case 1:
				markerFilename = "assets/Ghost1.png";
			break;
			case 2:
				markerFilename = "assets/Ghost2.png";
			break;
			case 3:
				markerFilename = "assets/Ghost3.png";
			break;
		}
	},


	locationChanged: function locationChangedFn(lat, lon, alt, acc) {
		lchangeCount++;
		// request data if not already present
		if (!World.initiallyLoadedData) {
			var poiData = {
				"id": 1,
				"longitude": (lon + (Math.random() / 5 - 0.1)),
				"latitude": (lat + (Math.random() / 5 - 0.1)),
				"altitude": 100.0
			};
			
			AR.logger.debug("poidata, lonlat " + poiData.longitude.toString());
			
			
			var poiData2 = {
				"id": 1,
				"longitude": (lon + (Math.random() / 5 - 0.1)),
				"latitude": (lat + (Math.random() / 5 - 0.1)),
				"altitude": 100.0
			};
			
			AR.logger.debug("poidata2, lonlat " + poiData2.longitude.toString());


			//use same POI for now, create shadowy effect
			World.loadPoisFromJsonData(poiData, poiData);
			World.initiallyLoadedData = true;
		}
	},
	
	// reload places from content source
	captureScreen: function captureScreenFn() {
		document.location = "architectsdk://button?visible=" + World.isWithinFrame;
	},
	
	// screen was clicked but no geo-object was hit
	onScreenClick: function onScreenClickFn() {
		// you may handle clicks on empty AR space too
	},
	
	disappear: function disappearFn() {
	    World.isWithinFrame = false;
	    document.location = "architectsdk://exit";
    },

	appear: function appearFn() {
	    document.location = "architectsdk://enter";
	    World.isWithinFrame = true;
		if(World.isAnimStart == false){
			ghostAnimGroup.start();
			ghostAnimGroup2.start();
			World.isAnimStart = true;
		}
	}
	
	
};

/* forward clicks in empty area to World */
AR.context.onScreenClick = World.onScreenClick;



/* forward locationChanges to custom function */
AR.context.onLocationChanged = World.locationChanged;
setTimeout(function(){World.init()}, 10000);