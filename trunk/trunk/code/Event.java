/*
 * Feel free to add to or even replace this if it suits you..
 */
public class Event {
	private String verb;
	private String entity ;
	private boolean printEntityFirst;
	private boolean entityIsSubject;
	private boolean usedCoref;
	private String extra;
	
	public Event(String verb, String entity, boolean entityIsSubject, boolean printEntityFirst, boolean usedCoref) {
		this(verb, entity, entityIsSubject, printEntityFirst, usedCoref, "");
	}
	
	public Event(String verb, String entity, boolean entityIsSubject, boolean printEntityFirst, boolean usedCoref, String extra) {
		this.verb = verb;
		this.entity = entity;
		this.printEntityFirst = printEntityFirst;
		this.entityIsSubject = entityIsSubject;
		this.usedCoref = usedCoref;
		this.extra = extra;
	}

	public String toString(){
		String type = (entityIsSubject) ? "Subject " : "Object  "; //ternary operator, google it if you care
		String coref = (usedCoref) ? "   Coref" : "No Coref "; 
		
		String result = "("+type+", "+coref+") ";
		if(printEntityFirst)
			result = result.concat(entity+" "+verb);
		else
			result = result.concat(verb+" "+entity);
		if(extra.length() > 0)
			result = result.concat(" \t\t :  Extra info: "+extra);
		
		return result;
	}
}
