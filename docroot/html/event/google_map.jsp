<script type="text/javascript" src="//maps.google.com/maps/api/js?sensor=false"></script>

<script type="text/javascript">

	var geocoder;
	var eventMap;
	var google;

	function initialize() {

	   	var latlng = new google.maps.LatLng(51.50, -0.12);
	   	var myOptions = {
	     	zoom: 15,
	     	center: latlng,
	     	mapTypeId: google.maps.MapTypeId.ROADMAP,
	     	mapTypeControl: false,
	     	streetViewControl: false
	   	};
	   	eventMap = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	}

	function codeAddress(address) {
		if (google) {
			geocoder = new google.maps.Geocoder();
	
		  	geocoder.geocode( { 'address': address}, function(results, status) {
	
			  	if (status == google.maps.GeocoderStatus.OK) {
			  		initialize();
			  		eventMap.setCenter(results[0].geometry.location);
	
			  		var marker = new google.maps.Marker({
			          	map: eventMap,
			          	position: results[0].geometry.location
			   		});
	
			  		jQuery('#mapview').show();
			    }
			});
	  	
		}
	}

</script>