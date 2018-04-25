import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class SAX {


	public static void main(String[] args) {
		
		System.out.println("**** SAX PARSER ****");
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);

		// set timer
		long parsingTime = System.currentTimeMillis();
		
		SAXParser saxParser;
		try {
			
			saxParser = factory.newSAXParser();
			File file = new File("../size2mb.xml");
			saxParser.parse(file, new PrintElementsHandler());
			
			parsingTime = System.currentTimeMillis() - parsingTime;
			System.out.println("Parsing is done and took " + parsingTime + " miliseconds. Output file is above.");

		} catch (ParserConfigurationException e1) {
			System.out.println("ParserConfigurationException: "+e1.getMessage());
		} catch (SAXException e1) {
			System.out.println("SAXException: "+e1.getMessage());
		} catch (IOException e) {
			System.out.println("IOException: "+e.getMessage());
		}
	}
}


class PrintElementsHandler extends DefaultHandler {
	
	StringBuffer blanks = new StringBuffer();
	StringBuffer elem = new StringBuffer();
	
	int depth = 0;
	int bookCount = 0;
	int bookCountXML = 0;
	int countPages = 0;
	int countEmpty = 0;
	
	boolean isDollar = false;
	boolean isPrice = false;
	boolean isPage = false;
	
	double avgPrice = (double) 0;
	
	public void startDocument() throws SAXException {
		System.out.println("start document -------------------------------");
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		
		switch (qName) {
		case "Buch":
			processBuch(qName, attributes);
			break;
			
		case "Titel":
			processTitel(qName, attributes);
			break;
			
		case "Preis":
			processPreis(qName, attributes);
			break;
			
		case "Seiten":
			processSeiten(qName, attributes);
			break;
			
		default:
			processDefault(qName, attributes);
			break;
		}
	}
	
	public void processBuch(String qName, Attributes attributes) {
//		System.out.println();
		elem.append("\n");
		processDefault(qName, attributes);
	}
	
	public void processTitel(String qName, Attributes attributes) {
//		System.out.println();
		elem.append("\n");
		processDefault(qName, attributes);
	}
	
	public void processPreis(String qName, Attributes attributes) {
		String check;
		for (int i = 0; i < attributes.getLength(); i++) {
			check = attributes.getValue(i);
			if (check.contains("Dollar")) {	
				isDollar = true;
			}		
		}
		isPrice = true;
		processDefault(qName, attributes);
	}	
	
	public void processSeiten(String qName, Attributes attributes) {
		isPage = true;
		processDefault(qName, attributes);
	}	
	
	public void processDefault(String qName, Attributes attributes) {
		blanks.append("  "); // add 2 blanks
		depth++;
		
		elem.append(blanks.toString() + "<" + qName);
		for (int i = 0; i < attributes.getLength(); i++) {
			elem.append(" " + attributes.getQName(i) + "=\"");
			if (isDollar) {
				elem.append("Euro");
			} else {
				elem.append(attributes.getValue(i));
			}
			elem.append("\"");
		}	
		elem.append(">");
	}	
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		String s = new String(ch, start, length);
		
		if(s.startsWith("ISBN: "))
			s = s.replace("ISBN: ", "");
		
		if(isDollar) {
			double value = Double.parseDouble(s);
			value = value / 1.06;
			avgPrice += value;
			s = Double.toString(value);
			isDollar = false;
			isPrice = false;
			bookCount++;
		} else if (isPrice) {
			avgPrice += (double) Double.parseDouble(s);
			isPrice = false;
			bookCount++;
		}
		
		if (isPage) {
//			s = s.replace(" ", "");
			Integer val = Integer.parseInt(s);
			if (val.intValue() > 500) {
				countPages++;
			}
			isPage = false;
		}
		

		
		if(s.startsWith("Professional XML") && s.endsWith("Professional XML"))
			bookCountXML++;
			
		elem.append(s);
//		System.out.print(s);
			
	}
	
	
	public void endElement(String uri, String localname, String qName) throws SAXException {
		
		if(qName == "Rechnungen")
			addStatistics();
		
		// don't print elements when empty (ie. <Verlag>
		if (elem.toString().endsWith(">")) {
			countEmpty++;
			elem.append("\n");
		} else {
			if (elem.toString().contains("<Buch>"))
				elem.append(blanks.toString());
			elem.append(blanks.toString() +  "</" + qName + ">\n");
			System.out.print(elem.toString());
		}
		
		// reset StringBuffer
		elem.setLength(0);
			
		blanks.delete(0, 2); // delete 2 blanks
		depth--;
	}
	
	public void endDocument() throws SAXException {
		System.out.println("end document -------------------------------");
	}
	
	public void addStatistics() {
		System.out.println(blanks.toString() + "<Statistik>");
		System.out.println(blanks.toString() + "  <leereElemente>" + countEmpty + "</leereElemente>");
		System.out.println(blanks.toString() + "  <Verkaufszahlen buch=\"XML Professional\">" + bookCountXML + "</Verkaufszahlen>");
		System.out.println(blanks.toString() + "  <Durchschnittspreis gruppierung=\"Rechnung\">" + (double) avgPrice / bookCount + "</Durchschnittspreis>");
		System.out.println(blanks.toString() + "  <Seiten mehrAls=\"500\">" + countPages + "</Seiten>");
		System.out.println(blanks.toString() + "</Statistik>");
		depth = 0;
		bookCount = 0;
		bookCountXML = 0;
		countPages = 0;
		
		isDollar = false;
		isPrice = false;
		isPage = false;
		
		avgPrice = (double) 0;
	}

	@Override
	public void warning(SAXParseException ex) throws SAXException {
		printError("Warning", ex);
	}
	
	@Override
	public void error(SAXParseException ex) throws SAXException {
		printError("Error", ex);
	}
	
	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		printError("Fatal Error", ex);
	}
	
	//
	// Protected methods
	//

	/** Prints the error message. */
	protected void printError(String type, SAXParseException ex) {

		System.err.print("[");
		System.err.print(type);
		System.err.print("] ");
		if (ex == null) {
			System.err.println("!!!");
		}
		String systemId = ex.getSystemId();
		if (systemId != null) {
			int index = systemId.lastIndexOf('/');
			if (index != -1)
				systemId = systemId.substring(index + 1);
			System.err.print(systemId);
		}
		System.err.print(':');
		System.err.print(ex.getLineNumber());
		System.err.print(':');
		System.err.print(ex.getColumnNumber());
		System.err.print(": ");
		System.err.print(ex.getMessage());
		System.err.println();
		System.err.flush();

	} // printError(String,SAXParseException)

} // PrintElementsHandler

