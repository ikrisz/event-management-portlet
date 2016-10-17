Configuration required:

1) In the server's file 'portal-ext.properties' configure EventBrite settings. These are company based.

	#Flag to enable/disable eventbrie integration
	eventbrite.enabled = false
	
	#Eventbrite Application key
	eventbrite.appkey = 
	
	#Eventbrite default user api key. Can be overridden in portlet prefences
	eventbrite.api.userkey = 
	
	#List the role names that will be notified if errors occur during the scheduled sync of eventbrite events
	event.import.error.emails.admin.to.roles=Administrator


2) Personalize the error page web content articles.
	These are found under GLOBAL and have articleId:
	INTELLIGUS-EVENTS-NOTFOUND-ERROR-PAGE --> When an event that is listed can't be found. 
				Search index and liferay database are out of sync. Need to reindex
	INTELLIGUS-EVENTS-PERMISSION-ERROR-PAGE --> User does not have permission to execute action.
	INTELLIGUS-EVENTS-EVENTBRITE-ERROR-PAGE  --> for errors that occur with eventbrite sync
	INTELLIGUS-EVENTS-INTERNAL-ERROR-PAGE --> for system errors. 
	
