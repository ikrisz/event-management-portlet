if (intelligusEvent === undefined) {
	var intelligusEvent = {};
}

(function() {
	intelligusEvent.Update = function(options) {
		jQuery.extend(this, this.prototype, options);
		this.initializeVariables();
		this.initializeFieldAvailable();
		this.bindCheckOnlineEvent();
		this.bindChangeEventCountry();
		this.bindCheckAllDayEvent();
		this.bindCheckMultiDayEvent();
		this.bindChangePublicEvent();
		
		if(this.eventbriteVenueEnabled && this.eventbriteVenueEnabled=="true"){
			this.bindChangeVenue();
		}
		if(this.eventbriteOrganizerEnabled && this.eventbriteOrganizerEnabled=="true"){
			this.bindChangeOrganizer();
		}
		if(this.recurrencyDateEnabled && this.recurrencyDateEnabled=="true"){
			this.bindChangeRecurrenceType();
		}
		if(this.eventbriteTicketUpdate && this.eventbriteTicketUpdate=="true"){
			this.bindChangeEventUpdateCheckbox();
			this.bindChangeEventStartDateForTicketsUpdate();
		}
	};

	intelligusEvent.Update.prototype = {
			portletBoundary: '',
			namespace: '',
			
			publicEventAlertMessage: '',
			
			securityLevelSelect: null,
			
			recurrencyDateEnabled: false,
			eventbriteVenueEnabled: false,
			eventbriteOrganizerEnabled: false,
			eventbriteTicketUpdate: false,
			
			eventCountryCode: '',
			eventCountrySelect: null,
			eventStateCode: '',
			
			isAllDayEvent : false,
			allDayCheckbox : null,
			
			isMultiDayEvent: false,
			multiDayEnabled: false,
			multiDayCheckbox: null,
			
			organizerId: '',
			organizerSelect: null,
			
			venueId: '',
			venueSelect: null,
			
			recurrentEvent: 'none',
			recurrenceLabelSelect: null,

			isOnlineEvent: false,
			onlineCheckbox: null,
			
			startHour: null,
			startMinute: null,
			startDay: null,
			startMonth: null,
			startYear: null,
			
			endMinute: null,
			endHour: null,
			
			recurrenceEndDay: null,
			recurrenceEndMonth: null,
			recurrenceEndYear: null,
			
			initializeVariables: function() {
				var self = this;
				
				//Security level
				self.securityLevelSelect = jQuery(self.portletBoundary).find('select[name='+self.namespace+'securityLevel]');
				
				//Dates
				self.allDayCheckbox = jQuery(self.portletBoundary).find('#'+self.namespace+'datesAllDayCheckbox');
				self.multiDayCheckbox = jQuery(self.portletBoundary).find('#'+self.namespace+'datesMultiDayCheckbox');
				
				self.startMinute = jQuery(self.portletBoundary).find('#'+self.namespace+'datesStartMinute');
				self.startHour = jQuery(self.portletBoundary).find('#'+self.namespace+'datesStartHour');
				self.startDay = jQuery(self.portletBoundary).find('select[name='+self.namespace+'startDay]');
				self.startMonth = jQuery(self.portletBoundary).find('select[name='+self.namespace+'startMonth]');
				self.startYear = jQuery(self.portletBoundary).find('select[name='+self.namespace+'startYear]');
				self.endMinute = jQuery(self.portletBoundary).find('#'+self.namespace+'datesEndMinute');
				self.endHour = jQuery(self.portletBoundary).find('#'+self.namespace+'datesEndHour');
				
				//Venue
				self.onlineCheckbox = jQuery(self.portletBoundary).find('#'+self.namespace+'venueOnlineCheckbox');
				self.eventCountrySelect = jQuery(self.portletBoundary).find('#'+self.namespace+'venueCountry');
				if(self.eventbriteVenueEnabled && self.eventbriteVenueEnabled=="true"){
					self.venueSelect = jQuery(self.portletBoundary).find('#'+self.namespace+'venueId');
				}
				
				//Organizer
				if(self.eventbriteOrganizerEnabled && self.eventbriteOrganizerEnabled=="true"){
					self.organizerSelect = jQuery(self.portletBoundary).find('#'+self.namespace+'organizerId');
				}
				
				//Recurrent dates
				if(self.recurrencyDateEnabled && self.recurrencyDateEnabled=="true"){
					self.recurrenceLabelSelect = jQuery(self.portletBoundary).find('#'+self.namespace+'recurrenceLabel');
					self.recurrenceEndDay = jQuery(self.portletBoundary).find('select[name='+self.namespace+'recurrenceEndDay]');
					self.recurrenceEndMonth = jQuery(self.portletBoundary).find('select[name='+self.namespace+'recurrenceEndMonth]');
					self.recurrenceEndYear = jQuery(self.portletBoundary).find('select[name='+self.namespace+'recurrenceEndYear]');
					
					if(self.recurrentEvent=="" || self.recurrentEvent=="none"){
						jQuery(self.recurrenceLabelSelect).val('none');
						self.recurrentEvent= 'none';
					}
				}
			},

			//On page load
			initializeFieldAvailable: function() {
				var self = this;
				
				//All day
				if(self.isAllDayEvent && self.isAllDayEvent == "true"){
					self.disableEndTimeFields();
					self.disableStartTimeFields();
				}
				
				//Country code for state selection
				if(self.eventCountryCode == "" || self.eventCountryCode != "US"){
					self.switchEventStateFields("text");
				} else if(self.eventCountryCode && self.eventCountryCode == "US"){
					self.switchEventStateFields("select");
					jQuery(self.portletBoundary).find('#'+self.namespace+'venueRegionState').val(self.eventStateCode);
				}
				
				//Organizer
				if(self.eventbriteOrganizerEnabled && self.eventbriteOrganizerEnabled=="true"){
					jQuery(self.organizerSelect).val(self.organizerId);
					self.disableOrganizerFieldBasedOnOrganizerId(self.organizerId);
				}
				
				//Recurrent dates
				if(self.recurrencyDateEnabled && self.recurrencyDateEnabled=="true"){
					if(self.recurrentEvent == "" || self.recurrentEvent == "none"){
						self.disableRecurrenceEndDateFields();
					} else if(self.recurrentEvent && self.recurrentEvent != "none"){
						self.enableRecurrenceEndDateTimeFields();
						self.switchRecurrenceIntervals(self.recurrentEvent);
						self.disableMultidaySelect();
					}
				}
				
				//Multiday event
				if(self.multiDayEnabled && self.multiDayEnabled=="true" && self.isMultiDayEvent && self.isMultiDayEvent == "true"){
					self.enableMultidayFields();
					self.disableRecurrenceTypeSelect();
				} else {
					self.disableMultidayFields();
				}
				
				//Venue
				if(self.eventbriteVenueEnabled && self.eventbriteVenueEnabled=="true"){
					jQuery(self.venueSelect).val(self.venueId);
					self.disableLocationFieldsBasedOnVenueId(self.venueId);
				}
				
				//Online event
				if(self.isOnlineEvent && self.isOnlineEvent == "true"){
					self.disableAllLocationFields(true);
					self.disableVenueSelect(true);
				}
			},
			
			bindChangePublicEvent: function(){
				var self = this;
				jQuery(self.securityLevelSelect).change(function(){
					var selectedValue = jQuery(self.securityLevelSelect).val();
					if(selectedValue == "PUBLIC"){
						alert(self.publicEventAlertMessage); 
					}
				});
			},
			
			bindChangeEventUpdateCheckbox: function(){
				var self = this;
				var updateTicketCheckbox = jQuery(self.portletBoundary).find('#'+self.namespace+'updateTicketsCheckbox');
				jQuery(updateTicketCheckbox).change(function(){
					if(jQuery(updateTicketCheckbox).is(':checked')){
						 jQuery('#ticketsUpdateDiv').removeClass('hide');
					} else {
						jQuery('#ticketsUpdateDiv').addClass('hide');
					}
				});
			},
			
			bindChangeEventStartDateForTicketsUpdate:function(){				
				//getter
				var firstDay = $('.selector').datepicker('option', 'firstDay');
				//setter
				$('.selector').datepicker('option', 'firstDay', 1);
				var self = this;
				jQuery(self.startDay).change(function(){
					var selectedValue = jQuery(self.startDay).val();
					jQuery('#'+self.namespace+'ticketsendday').val(selectedValue);
				});
				
				jQuery(self.startMonth).change(function(){
					var selectedValue = jQuery(self.startMonth).val();
					jQuery('#'+self.namespace+'ticketsendmonth').val(selectedValue);
				});
				
				jQuery(self.startYear).change(function(){
					var selectedValue = jQuery(self.startYear).val();
					jQuery('#'+self.namespace+'ticketsendyear').val(selectedValue);
				});
				
				jQuery(self.startHour).change(function(){
					var selectedValue = jQuery(self.startHour).val();
					jQuery('#'+self.namespace+'eventbriteEndHour').val(selectedValue);
					
				});
				
				jQuery(self.startMinute).change(function(){
					var selectedValue = jQuery(self.startMinute).val();
					jQuery('#'+self.namespace+'eventbriteEndMinute').val(selectedValue);
				});
			},
			
			bindCheckAllDayEvent: function() {
				var self = this;
				jQuery(self.allDayCheckbox).change(function(){
					if(jQuery(self.allDayCheckbox).is(':checked')){
						self.disableEndTimeFields();
						self.disableStartTimeFields();
					} else {
						self.enableEndTimeFields();
						self.enableStartTimeFields();
					}
				});
			},
			
			bindCheckMultiDayEvent: function() {
				var self = this;
				jQuery(self.multiDayCheckbox).change(function(){
					if(jQuery(self.multiDayCheckbox).is(':checked')){
						self.enableMultidayFields();
						self.disableRecurrenceTypeSelect();
					} else {
						self.disableMultidayFields();
						self.enableRecurrenceTypeSelect();
					}
				});
			},
			
			bindChangeRecurrenceType: function(){
				var self = this;
				jQuery(self.recurrenceLabelSelect).change(function(){
					var selectedValue = jQuery(self.recurrenceLabelSelect).val();
					if(selectedValue == "none"){
						self.disableRecurrenceEndDateFields();
						self.enableMultidaySelect();
					} else {
						self.enableRecurrenceEndDateTimeFields();
						self.switchRecurrenceIntervals(selectedValue);
						self.disableMultidaySelect();
					}
				});
			},
			
			bindCheckOnlineEvent: function() {
				var self = this;
				jQuery(self.onlineCheckbox).change(function(){
					if(jQuery(self.onlineCheckbox).is(':checked')){
						self.disableAllLocationFields(true);
						self.disableVenueSelect(true);
					} else {
						self.disableAllLocationFields(false);
						self.disableVenueSelect(false);
					}
				});
			},
			
			bindChangeOrganizer: function(){
				var self = this;
				jQuery(self.organizerSelect).change(function(){
					var selectedOrganizerId = jQuery(self.organizerSelect).val();
					self.disableOrganizerFieldBasedOnOrganizerId(selectedOrganizerId);
					jQuery('#'+self.namespace+'organizerName').val('');
				});
			}, 
			
			bindChangeVenue: function(){
				var self = this;
				jQuery(self.venueSelect).change(function(){
					var selectedVenueId = jQuery(self.venueSelect).val();
					self.disableLocationFieldsBasedOnVenueId(selectedVenueId);
				});
			},
			
			enableMultidayFields: function(){
				var self = this;
				if(self.multiDayEnabled && self.multiDayEnabled=="true"){
					jQuery('#'+self.namespace+'multidaySelectionDiv').removeClass('hide');
					jQuery('#'+self.namespace+'multidayEmptyDiv').addClass('hide');
				}
			},
			
			disableMultidayFields: function(){
				var self = this;
				jQuery('#'+self.namespace+'multidaySelectionDiv').addClass('hide');
				jQuery('#'+self.namespace+'multidayEmptyDiv').removeClass('hide');
			},
			
			enableMultidaySelect: function(){
				var self = this;
				//If not allday and not recurrence selected
				var recurrenceValue = jQuery(self.recurrenceLabelSelect).val();
				if(recurrenceValue == "none" && !jQuery(self.multiDayCheckbox).is(':checked')){
					jQuery(self.multiDayCheckbox).prop('disabled', false).removeClass('disabled-field');
				}
			},
			
			disableMultidaySelect: function(){
				var self = this;
				jQuery(self.multiDayCheckbox).prop('disabled', 'disabled').addClass('disabled-field');
				self.disableMultidayFields();
			},
			
			
			disableAllDaySelect: function(){
				var self = this;
				jQuery(self.allDayCheckbox).prop('disabled', 'disabled').addClass('disabled-field');
			}, 
			
			disableRecurrenceTypeSelect: function(){
				var self = this;
				jQuery(self.recurrenceLabelSelect).val('none');
				jQuery(self.recurrenceLabelSelect).prop('disabled', 'disabled').addClass('disabled-field');
				self.disableRecurrenceEndDateFields();
			},
			
			enableAllDaySelect: function(){
				var self = this;
				jQuery(self.allDayCheckbox).prop('disabled', false).removeClass('disabled-field');
			},
			
			enableRecurrenceTypeSelect: function(){
				var self = this;
				jQuery(self.recurrenceLabelSelect).prop('disabled', false).removeClass('disabled-field');
			},
			
			disableLocationFieldsBasedOnVenueId: function(venueIdVal){
				var self = this;
				if(venueIdVal && venueIdVal != ""){
					self.disableAllLocationFields(true);
					jQuery('#'+self.namespace+'venueName').val('');
					jQuery('#'+self.namespace+'venueAddressLineOne').val('');
					jQuery('#'+self.namespace+'venueAddressLineTwo').val('');
					jQuery('#'+self.namespace+'venueCity').val('');
					jQuery('#'+self.namespace+'venueCountry').val('');
					jQuery('#'+self.namespace+'venueZip').val('');
					jQuery('#'+self.namespace+'venueRegionStateText').val('');
					jQuery('#'+self.namespace+'venueRegionStateSelect').val('');
				} else {
					self.disableAllLocationFields(false);
				}
			},
			
			disableOrganizerFieldBasedOnOrganizerId: function(selectedOrganizerIdValue){
				var self = this;
				if(selectedOrganizerIdValue && selectedOrganizerIdValue != ""){
					var selectedOrganizerName = jQuery(self.organizerSelect).find("option:selected").text();
					jQuery('#'+self.namespace+'organizerName').val(selectedOrganizerName);
					jQuery('#'+self.namespace+'organizerName').prop('disabled', 'disabled').addClass('disabled-field');
				} else {
					jQuery('#'+self.namespace+'organizerName').prop('disabled', false).removeClass('disabled-field');
				}
			},
			
			disableVenueSelect: function(isDisabled){
				var self = this;
				if(self.eventbriteVenueEnabled && self.eventbriteVenueEnabled=="true"){
					if(isDisabled){
						jQuery(self.venueSelect).val("");
						jQuery(self.venueSelect).prop('disabled', 'disabled').addClass('disabled-field');
					}else{
						jQuery(self.venueSelect).prop('disabled', false).removeClass('disabled-field');
					}
				}
			},
			
			disableAllLocationFields: function(isDisabled){
				var self = this;
				if(isDisabled){
					jQuery('#'+self.namespace+'venueName').prop('disabled', 'disabled').addClass('disabled-field');
					jQuery('#'+self.namespace+'venueAddressLineOne').prop('disabled', 'disabled').addClass('disabled-field');
					jQuery('#'+self.namespace+'venueAddressLineTwo').prop('disabled', 'disabled').addClass('disabled-field');
					jQuery('#'+self.namespace+'venueCity').prop('disabled', 'disabled').addClass('disabled-field');
					jQuery('#'+self.namespace+'venueCountry').prop('disabled', 'disabled').addClass('disabled-field');
					jQuery('#'+self.namespace+'venueZip').prop('disabled', 'disabled').addClass('disabled-field');
					jQuery('#'+self.namespace+'venueRegionStateText').prop('disabled', 'disabled').addClass('disabled-field');
					jQuery('#'+self.namespace+'venueRegionStateSelect').prop('disabled', 'disabled').addClass('disabled-field');
				} else {
					jQuery('#'+self.namespace+'venueName').prop('disabled', false).removeClass('disabled-field');
					jQuery('#'+self.namespace+'venueAddressLineOne').prop('disabled', false).removeClass('disabled-field');
					jQuery('#'+self.namespace+'venueAddressLineTwo').prop('disabled', false).removeClass('disabled-field');
					jQuery('#'+self.namespace+'venueCity').prop('disabled', false).removeClass('disabled-field');
					jQuery('#'+self.namespace+'venueCountry').prop('disabled', false).removeClass('disabled-field');
					jQuery('#'+self.namespace+'venueZip').prop('disabled', false).removeClass('disabled-field');
					jQuery('#'+self.namespace+'venueRegionStateText').prop('disabled', false).removeClass('disabled-field');
					jQuery('#'+self.namespace+'venueRegionStateSelect').prop('disabled', false).removeClass('disabled-field');
				}
			},
			
			switchRecurrenceIntervals: function(valueSelected){
				var self = this;
				if(valueSelected == "daily"){
					jQuery('#'+self.namespace+'dayIntervalDiv').removeClass('hide');
					jQuery('#'+self.namespace+'dayIntervalLabelPostDiv').html('&nbsp; days');
					jQuery('#'+self.namespace+'weekIntervalDiv').addClass('hide');
					jQuery('#'+self.namespace+'monthIntervalDiv').addClass('hide');
					jQuery('#'+self.namespace+'dayMultiIntervalDiv').addClass('hide');
				} else if(valueSelected =="weekly"){	
					jQuery('#'+self.namespace+'dayIntervalDiv').addClass('hide');
					jQuery('#'+self.namespace+'weekIntervalDiv').removeClass('hide');
					jQuery('#'+self.namespace+'monthIntervalDiv').addClass('hide');
					jQuery('#'+self.namespace+'dayMultiIntervalDiv').removeClass('hide');
				} else if(valueSelected =="monthly"){
					jQuery('#'+self.namespace+'dayIntervalDiv').removeClass('hide');
					jQuery('#'+self.namespace+'dayIntervalLabelPostDiv').html('&nbsp; day of the month');
					jQuery('#'+self.namespace+'weekIntervalDiv').addClass('hide');
					jQuery('#'+self.namespace+'monthIntervalDiv').removeClass('hide');
					jQuery('#'+self.namespace+'dayMultiIntervalDiv').addClass('hide');
				}
			},
			
			disableRecurrenceEndDateFields: function(){
				var self = this;
				jQuery(self.recurrenceEndDay).prop('disabled', 'disabled').addClass('disabled-field').val(jQuery(self.startDay).val());
				jQuery(self.recurrenceEndMonth).prop('disabled', 'disabled').addClass('disabled-field').val(jQuery(self.startMonth).val());
				jQuery(self.recurrenceEndYear).prop('disabled', 'disabled').addClass('disabled-field').val(jQuery(self.startYear).val());
				jQuery('#'+self.namespace+'endDateTimeRecurrenceValuesDiv').addClass('hide');
				jQuery('#'+self.namespace+'recurrenceIntervalValuesDiv').addClass('hide');
			},
			
			enableRecurrenceEndDateTimeFields: function(){
				var self = this;
				self.disableField(self.recurrenceEndDay, false);
				self.disableField(self.recurrenceEndMonth, false);
				self.disableField(self.recurrenceEndYear, false);
				jQuery('#'+self.namespace+'endDateTimeRecurrenceValuesDiv').removeClass('hide');
				jQuery('#'+self.namespace+'recurrenceIntervalValuesDiv').removeClass('hide');
			},
			
			disableEndTimeFields: function(){
				var self = this;
				jQuery(self.endMinute).prop('disabled', 'disabled').addClass('disabled-field').val(59);
				jQuery(self.endHour).prop('disabled', 'disabled').addClass('disabled-field').val(23);
			},
			
			enableEndTimeFields: function(){
				var self = this;
				self.disableField(self.endMinute, false);
				self.disableField(self.endHour, false);
			},
			
			disableStartTimeFields: function(){
				var self = this;
				jQuery(self.startMinute).prop('disabled', 'disabled').addClass('disabled-field').val(0);
				jQuery(self.startHour).prop('disabled', 'disabled').addClass('disabled-field').val(0);
			},
			
			enableStartTimeFields: function(){
				var self = this;
				self.disableField(self.startMinute, false);
				self.disableField(self.startHour, false);
			},
			
			disableField: function(fieldToUpdate, isDisabled){
				if(isDisabled){
					jQuery(fieldToUpdate).prop('disabled', 'disabled').addClass('disabled-field');
				} else {
					jQuery(fieldToUpdate).prop('disabled', false).removeClass('disabled-field');
				}
			},
			
			switchEventStateFields: function(fieldType){
				var self = this;
				if(fieldType == "select"){
					jQuery('#'+self.namespace+'stateTextDiv').addClass('hide');
					jQuery('#'+self.namespace+'stateSelectUSADiv').removeClass('hide');
				} else {
					jQuery('#'+self.namespace+'stateSelectUSADiv').addClass('hide');
					jQuery('#'+self.namespace+'stateTextDiv').removeClass('hide');
				}
			},
			
			bindChangeEventCountry: function(){
				var self = this;
				jQuery(self.eventCountrySelect).change(function(){
					var selectedValue = jQuery(self.eventCountrySelect).val();
					if(selectedValue == "US"){
						self.switchEventStateFields("select");
					} else {
						self.switchEventStateFields("text");
					}
				});
			}
	};
	
	
	intelligusEvent.InitializeEventbriteEventUpdate = function(portletNamespace, publicAlertMessage, allDayEvent, eventOrganizerId, eventVenueId, onlineEventVal, eventCountryVal, eventStateVal) {
		var _portletBoundary = document.getElementById('p_p_id'+portletNamespace);
		new intelligusEvent.Update({
			portletBoundary: _portletBoundary,
			namespace: portletNamespace,
			publicEventAlertMessage: publicAlertMessage,
			recurrencyDateEnabled: 'false',
			eventbriteVenueEnabled: 'true',
			eventbriteOrganizerEnabled: 'true',
			eventbriteTicketUpdate: 'true',
			isAllDayEvent: allDayEvent,
			isMultiDayEvent: 'false',
			multiDayEnabled: 'false',
			recurrentEvent: '',
			organizerId: eventOrganizerId,
			venueId: eventVenueId,
			isOnlineEvent: onlineEventVal,
			eventCountryCode: eventCountryVal,
			eventStateCode: eventStateVal
		});
	};
	
	intelligusEvent.InitializeEventUpdate = function(portletNamespace, publicAlertMessage, allDayEvent, multiDayEvent, eventRecurrency, onlineEventVal, eventCountryVal, eventStateVal) {
		var _portletBoundary = document.getElementById('p_p_id'+portletNamespace);
		new intelligusEvent.Update({
			portletBoundary: _portletBoundary,
			namespace: portletNamespace,
			publicEventAlertMessage: publicAlertMessage,
			recurrencyDateEnabled: 'true',
			eventbriteVenueEnabled: 'false',
			eventbriteOrganizerEnabled: 'false',
			eventbriteTicketUpdate: 'false',
			isAllDayEvent: allDayEvent,
			isMultiDayEvent: multiDayEvent,
			multiDayEnabled: 'true',
			recurrentEvent: eventRecurrency,
			organizerId: '',
			venueId: '',
			isOnlineEvent: onlineEventVal,
			eventCountryCode: eventCountryVal,
			eventStateCode: eventStateVal
		});
	};

})();
