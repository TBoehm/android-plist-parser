/**
 * Licensed under Creative Commons Attribution 3.0 Unported license.
 * http://creativecommons.org/licenses/by/3.0/
 * You are free to copy, distribute and transmit the work, and 
 * to adapt the work.  You must attribute android-plist-parser 
 * to Free Beachler (http://www.freebeachler.com).
 * 
 * The Android PList parser (android-plist-parser) is distributed in 
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.
 */
package com.longevitysoft.android.xml.plist;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import com.longevitysoft.android.util.Stringer;
import com.longevitysoft.android.xml.plist.domain.PList;
import com.longevitysoft.android.xml.plist.domain.PListObject;

/**
 * <p>
 * Parse the a PList. Documentation on PLists can be found at: <a href=
 * "http://developer.apple.com/library/mac/#documentation/Cocoa/Conceptual/PropertyLists/AboutPropertyLists/AboutPropertyLists.html#//apple_ref/doc/uid/10000048i-CH3-SW2"
 * >The Mac OS X Reference</a>
 * </p>
 * 
 * 
 * @author fbeachler
 * 
 */
public class PListXMLHandler extends DefaultHandler2 {

	public static final java.lang.String TAG = "PListXMLHandler";

	/**
	 * Defines the modes the parser reports to registered listeners.
	 * 
	 * @author fbeachler
	 * 
	 */
	public enum ParseMode {
		START_TAG, END_TAG
	};

	/**
	 * Implementors can listen for events defined by {@link ParseMode}.
	 * 
	 * @author fbeachler
	 * 
	 */
	public static interface PListParserListener {
		public void onPListParseDone(PList pList, ParseMode mode);
	}

	/**
	 * Listener for this parser.
	 */
	private PListParserListener parseListener;

	/**
	 * The value of parsed characters from elements and attributes.
	 */
	private Stringer tempVal;

	/**
	 * The parsed {@link PList}.
	 */
	private PList pList;

	// Registers to hold state of parsing the workflow as Dict
	protected java.lang.String key;

	
	public PList getPlist() {
		return pList;
	}
	public void setPlist(PList plist) {
		this.pList = plist;
	}

	public PListParserListener getParseListener() {
		return parseListener;
	}

	public void setParseListener(PListParserListener parseListener) {
		this.parseListener = parseListener;
	}

	public Stringer getTempVal() {
		return tempVal;
	}

	public void setTempVal(Stringer tempVal) {
		this.tempVal = tempVal;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		tempVal = new Stringer();
		pList = null;
		key = null;
	}

	@Override
	public void startElement(java.lang.String uri, java.lang.String localName,
			java.lang.String qName, Attributes attributes) throws SAXException {
		tempVal.newBuilder();
		if (localName.equalsIgnoreCase(Constants.TAG_PLIST)) {
			if (null != pList) {
				// there should only be one PList element in the root
				throw new SAXException(
						"there should only be one PList element in PList XML");
			}
			pList = new PList();
		} else {
			if (null == pList) {
				throw new SAXException(
						"invalid PList - please see http://www.apple.com/DTDs/PropertyList-1.0.dtd");
			}
			if (localName.equalsIgnoreCase(Constants.TAG_DICT) || 
					localName.equalsIgnoreCase(Constants.TAG_PLIST_ARRAY)) {
				try {
					PListObject objToAdd = pList.buildObject(localName, tempVal
							.getBuilder().toString());
					pList.stackObject(objToAdd, key);
				} catch (Exception e) {
					throw new SAXException(e);
				}
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		tempVal.getBuilder().append(new java.lang.String(ch, start, length));
	}

	@Override
	public void endElement(java.lang.String uri, java.lang.String localName,
			java.lang.String qName) throws SAXException {
		
		if (localName.equalsIgnoreCase(Constants.TAG_KEY)) {
			key = tempVal.getBuilder().toString().trim();
		} else if (localName.equalsIgnoreCase(Constants.TAG_DICT) || 
				localName.equalsIgnoreCase(Constants.TAG_PLIST_ARRAY)) {
			pList.popStack();
		} else if (!localName.equalsIgnoreCase(Constants.TAG_PLIST)) {
			try {
				PListObject objToAdd = pList.buildObject(localName, tempVal
						.getBuilder().toString());
				pList.stackObject(objToAdd, key);
			} catch (Exception e) {
				throw new SAXException(e);
			}
			key = null;
		} else if (localName.equalsIgnoreCase(Constants.TAG_PLIST)) {
			if (null != parseListener) {
				parseListener.onPListParseDone(pList, ParseMode.END_TAG);
			}
		}
		tempVal.newBuilder();

	}

}
