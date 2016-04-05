package org.silverpeas.core.importexport.ical.ical4j;

import net.fortuna.ical4j.model.Escapable;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.ValidationException;

public class Html extends Property implements Escapable {

	private static final long serialVersionUID = 7287564228220558361L;
	private static final String HTML = "X-ALT-DESC;FMTTYPE=text/html";

	private String value;

	/**
	 * Default constructor.
	 */
	public Html() {
		super(HTML, PropertyFactoryImpl.getInstance());
	}

	/**
	 * @param aValue
	 *            a value string for this component
	 */
	public Html(final String aValue) {
		super(HTML, PropertyFactoryImpl.getInstance());
    setValue(aValue);
	}

	/**
	 * @param aList
	 *            a list of parameters for this component
	 * @param aValue
	 *            a value string for this component
	 */
	public Html(final ParameterList aList, final String aValue) {
		super(HTML, aList, PropertyFactoryImpl.getInstance());
		setValue(aValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void validate() throws ValidationException {
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setValue(final String aValue) {
		this.value = aValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getValue() {
		return value;
	}
}
