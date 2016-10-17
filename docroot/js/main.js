function updateResultsPage(formAction, namespace, pageToView){
	var updatedUrl = formAction + "&"+namespace+"currentPage=" + pageToView;
	var form = document.createElement("form");
	form.setAttribute("method", "post");
	form.setAttribute("action", updatedUrl);
	document.body.appendChild(form);
	form.submit();
	return false;
};

function toggleErrorContentDiv(){
	jQuery('#errorContent').toggleClass('hide');
}

function submitInvitePeopleForm(namespace, eventLoadingDivId){
	jQuery('#'+eventLoadingDivId).modal({backdrop:'static'});
	jQuery('#'+namespace+'invitePeopleForm').submit();
}

function submitSendMessageToUserForm(namespace, alertMessage){
	var emailBody = jQuery('textarea[name='+namespace+'emailBody]').val();
	
	if(jQuery.trim(emailBody) == '' ){
			alert(alertMessage);
	}else{
		jQuery('#sendMessagePanelFooder').hide();
		jQuery('#loadingSendMessagePanelFooder').show();
		jQuery('#'+namespace+'sendMessageToUserForm').submit();
		return false;
	}
}

function submitUpdateEventForm(namespace, alertMessage, eventLoadingDivId){
	var selectedSecurityLevel = jQuery('select[name='+namespace+'securityLevel]').find('option:selected').text();
	var isPublicEvent = selectedSecurityLevel == 'PUBLIC';
	if(isPublicEvent){
		if(!confirm(alertMessage)){
			return false;
		}
	}
	jQuery('#'+eventLoadingDivId).modal({backdrop:'static'});
	jQuery('#'+namespace+'updateEventForm').submit();
}

function openModalDialogWithPos(urlToUse, modalTitle, topPosDiv){
	var topValRow = jQuery('#'+topPosDiv).position().top;
	var topVal = 200;
	if(topValRow >500){
		topVal = topValRow - 200;
	}
	
	Liferay.Intelligus.displayPopup(urlToUse, modalTitle);
	
	window.scrollTo(0,topVal-100);
};

function openModalDialog(urlToUse, yCoordinates){
	var topVal = 200;
	if(yCoordinates >700){
		topVal = yCoordinates - 400;
	}
	Liferay.Intelligus.displayPopup(urlToUse, 'Event Details');
	
	window.scrollTo(0,topVal-100);
};


function importEventbriteEvent(urlToUse, namespace, eventId){
	jQuery('#eventImport_'+eventId).addClass('hide');
	jQuery('#eventImport_importing_'+eventId).removeClass('hide');
	var ajaxUrl = urlToUse+'&'+namespace+'eventbriteId='+eventId;
	$.ajax({ 
		  url: ajaxUrl,
		  context: document.body,
		  dataType: 'json', 
		  success: function(result) {
			  var callResult = result.success;
			  jQuery('#eventImport_importing_'+eventId).addClass('hide');
			  if(callResult == 'true'){
				jQuery('#eventImport_'+eventId).addClass('hide');
				jQuery('#eventImported_'+eventId).removeClass('hide');
			  } else if(callResult == 'false'){
				jQuery('#eventImported_error_'+eventId).removeClass('hide');
			  }
		  }
	  });
};

function addTicketDiv(namespace){
	var ticketsIndex = jQuery('#'+namespace+'newTicketIndex');
	var currentVal = parseInt(ticketsIndex.val());
	var newIndex = currentVal + 1;
	jQuery(ticketsIndex).val(newIndex);
	
	var $html = $('.eventTicketsTemplate').clone();
	var htmlString = $html.html();
	var replacementString = '['+newIndex+']';
	var updatedHtmlString =  htmlString.replace(/\[0\]/g, replacementString);
    $('<div/>', {
        'class': 'row-fluid separator-row',
        'id': 'ticketIndex['+newIndex+']',
        html: updatedHtmlString
    }).hide().appendTo('#eventTicketsContainer').slideDown('slow');
}

function removeTicketDiv(namespace, index){
	var parent = document.getElementById('eventTicketsContainer');
	var child = document.getElementById('ticketIndex'+index);
	if(parent){
		parent.removeChild(child);
	}else{
		jQuery(child).addClass('hide');
	}
	var ticketsToRemove = jQuery('#'+namespace+'ticketsToRemove');
	var currentVal = ticketsToRemove.val();
	var updatedVal = currentVal + ','+index;
	jQuery(ticketsToRemove).val(updatedVal);
}


function changeTicketType(val, namespace, index){
	var optionSelected = val.value;
	var priceContainer = document.getElementById(namespace+'_price_'+index);
	var priceDiv = jQuery(priceContainer).find('input');
	var priceErrorDiv = document.getElementById(namespace+'_priceError_'+index);
	if(optionSelected && optionSelected == 'paid'){
		jQuery(priceDiv).prop('disabled', false).removeClass('disabled-field');
		jQuery(priceErrorDiv).removeClass('hide');
	}else{
		jQuery(priceDiv).prop('disabled', 'disabled').addClass('disabled-field');
		jQuery(priceErrorDiv).addClass('hide');
	}
}


