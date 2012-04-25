import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

public class Dataset {
	private String name;
	private String text;
	private String protagonist;
	
	/*
	 * Note that these should be stored in lowercase and the protagonist's primary name should be included...
	 */
	private Set<String> protagonistNames;
	
	public Dataset(String name, String text, String protagonist) {
		this.name = name;
		this.text = text;
		this.protagonist = protagonist;
		this.protagonistNames = new HashSet<String>();
		this.protagonistNames.add(protagonist.toLowerCase());
	}
	
	public Dataset(String name) throws IOException {
		this.name = name;
		this.protagonistNames = new HashSet<String>();
		
		//Code copied from: http://www.javapractices.com/topic/TopicAction.do?Id=42
		BufferedReader file = new BufferedReader(new FileReader("../data/"+name+".txt"));
		this.protagonist = file.readLine().trim();
		this.protagonistNames.add(protagonist.toLowerCase());
		for(String psuedonym : file.readLine().toLowerCase().trim().split(",")){
			psuedonym = psuedonym.trim();
			if(psuedonym.length()>0)
				this.protagonistNames.add(psuedonym);
		}
		
		StringBuilder contents = new StringBuilder();
		String line = null;
		while (( line = file.readLine()) != null){
	          contents.append(line);
	          contents.append("\n");
	    }
		this.text = contents.toString();
	}
	
	public boolean isProtagonist(String name) {
		return protagonistNames.contains(name.toLowerCase());
	}
	public void addProtagonistName(String name) {
		protagonistNames.add(name);
	}

	
	public String getName() {
		return name;
	}
	public String getText() {
		return text;
	}
	public String getProtagonist() {
		return protagonist;
	}
	
	/*
	 * protagonistNames is mutable, but that's fine
	 */
	public Set<String> getProtagonistNames() {
		return protagonistNames;
	}
}
