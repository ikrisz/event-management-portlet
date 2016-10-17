Date.prototype.stdTimezoneOffset = function() {
	var jan = new Date(2015, 0, 1);
	var jul = new Date(2015, 6, 1);
	return Math.max(jan.getTimezoneOffset(), jul.getTimezoneOffset());
	}

Date.prototype.dst = function() {
	return this.getTimezoneOffset() < this.stdTimezoneOffset();
	}

function getDateTimeNoOffset(thisVar)
{
	var thisDate = new Date(thisVar);
	if (thisDate.dst())
	{
		return (thisDate + (-1*(thisDate.stdTimezoneOffset())));  //we use a negative here as the offsets are -60 when 60 minutes behind etc
	}
	else
	{
		return thisDate; //do nothing 
	} 
}

function loadCalendar(namespace, eventSearchURL, eventDetailsURL){  
	//getter
	var firstDay = $('.selector').datepicker('option', 'firstDay');
	//setter
	$('.selector').datepicker('option', 'firstDay', 1);
	$('#calendar').fullCalendar({
		header: {
			left: 'today prevYear,prev,next,nextYear',
			center: 'title',
			right: 'month,agendaWeek,agendaDay'
		},
		selectable: false,
		weekends:true,
		weekMode:'variable',
		defaultView:'month',
		timeFormat: 'H:mm',
		
		titleFormat:{
		    month: 'MMMM yyyy',                            
		    week: "d[ yyyy]{ '&#8212;'[ MMM] d MMMM yyyy}",  
		    day: 'dddd, d MMMM yyyy' 
		},
		
		columnFormat:{
		    month: 'dddd',    
		    week: 'ddd, d MMM',
		    day: '' 
		},
		
		dayClick: function(date, allDay, jsEvent, view) {
			$('#calendar').fullCalendar('changeView', 'agendaDay')
	                .fullCalendar('gotoDate', date);
			 
		},

		eventClick: function(calEvent, jsEvent, view) {
			var eventDetailsURLToUse = eventDetailsURL+
				'&'+namespace+'eventId='+calEvent.id +
				'&'+namespace+'eventUid='+calEvent.eventUid;
			
			openModalDialog(eventDetailsURLToUse, jsEvent.pageY);
		},
		
		events: function(start, end, callback) {
			var startTime = $.fullCalendar.formatDate(start, "yyyy-MM-dd");
			var endTime = $.fullCalendar.formatDate(end, "yyyy-MM-dd");
			var startDateFilterParam = '&'+namespace+'startDateFilter='+startTime;
			var endDateFilterParam = '&'+namespace+'endDateFilter='+endTime;
			var urlToCall = eventSearchURL + startDateFilterParam + endDateFilterParam;
			$.ajax({
	            url: urlToCall,
	            dataType: 'json',
	            success: function(doc) {
	                var events = [];
	                for(var i in doc){
	                	var curr = doc[i];
	                	events.push({
	                		id: curr.id,
	                		eventUid: curr.uid,
	                        title: curr.title,
	                        start: getDateTimeNoOffset(curr.dates.start),
	                        end: getDateTimeNoOffset(curr.dates.end),
	                        allDay: curr.dates.allDay
	                    });
	                }
	                callback(events);
	            }
	        });
	    }
	});
}