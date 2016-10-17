package com.pfiks.intelligus.events.model;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.liferay.portal.kernel.cal.Recurrence;

public enum RecurrenceTypes {

    NONE("none", Recurrence.NO_RECURRENCE), //
    DAILY("daily", Recurrence.DAILY), //
    WEEKLY("weekly", Recurrence.WEEKLY), //
    MONTHLY("monthly", Recurrence.MONTHLY);

    private RecurrenceTypes(final String recurrenceLabel, final int recurrenceType) {
	label = recurrenceLabel;
	type = recurrenceType;
    }

    private String label;
    private int type;

    public String getLabel() {
	return label;
    }

    public int getType() {
	return type;
    }

    public static RecurrenceTypes withType(final int typeToRetrieve) {
	final Optional<RecurrenceTypes> tryFind = Iterables.tryFind(Lists.newArrayList(RecurrenceTypes.values()), new Predicate<RecurrenceTypes>() {

	    @Override
	    public boolean apply(final RecurrenceTypes arg0) {
		return arg0.getType() == typeToRetrieve;
	    }
	});
	if (tryFind.isPresent()) {
	    return tryFind.get();
	} else {
	    return RecurrenceTypes.NONE;
	}
    }
}
