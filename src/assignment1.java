import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;


public class assignment1 {
	final static int TEXT_CHUNK_SIZE = 10000;
	
	public static void main(String[] args) throws IOException {
		LinkedList<Dataset> datasets = getDatasets();
		
		for(Dataset dataset : datasets) {
			LinkedList<String> textChunks = chunkText(dataset.getText());
			
			for(String textChunk : textChunks){
				Annotation document = getAnnotation(textChunk);
				LinkedList<SemanticGraph> dependencyGraphs = getDependencies(document);
				Map<Integer, CorefChain> corefChains = getCorefs(document);
				
				LinkedList<Event> events = getEvents(dependencyGraphs, corefChains, dataset);
				
				writeEvents(dataset.getName(), events);
			}
		}
	}

	private static LinkedList<Dataset> getDatasets() throws IOException {
		LinkedList<Dataset> datasets = new LinkedList<Dataset>();
		
		Dataset jackAndJillDataset = new Dataset(
				"jack and jill",
				"Jack and Jill went up the hill to fetch a pail of water. Jack fell down and broke his crown, and Jill came tumbling after. Up he got, and home did trot, as fast as he could caper; to old Dame Dob, who patched his nob with vinegar and brown paper.",
				"Jack");
		
		datasets.add(jackAndJillDataset);
		datasets.add(new Dataset("news"));
		datasets.add(new Dataset("salinger_wiki_full"));
		datasets.add(new Dataset("indiana_jones_full"));
		datasets.add(new Dataset("forums_bush"));
		datasets.add(new Dataset("forums_kerry"));
		
		return datasets;
	}

	private static Annotation getAnnotation(String text) {
		/*TODO:
		 * Write your code here
		 * Hint: copy example from CoreNLP page..
		 *         For memory and time reasons, limit the length of sentences parsed by adding:
		 *           props.put("parser.maxlen", "115"); 
		 * */
		
	    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    props.put("parser.maxlen", "115");
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    // read some text in the text variable
	    //String text1 = "jack and jill"; // Add your text here!
	    
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);

		return document;
	}

	private static LinkedList<SemanticGraph> getDependencies(Annotation document) {
		LinkedList<SemanticGraph> dependencyGraphs = new LinkedList<SemanticGraph>();
		/*TODO:
		 * Write your code here
		 * Hint: copy example from CoreNLP page.. & populate dependencyGraphs list
		 * */
		
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	      // traversing the words in the current sentence
	      // a CoreLabel is a CoreMap with additional token-specific methods
//	      for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
//	        // this is the text of the token
//	        String word = token.get(TextAnnotation.class);
//	        // this is the POS tag of the token
//	        String pos = token.get(PartOfSpeechAnnotation.class);
//	        // this is the NER label of the token
//	        String ne = token.get(NamedEntityTagAnnotation.class);       
//	      }

	      // this is the parse tree of the current sentence
	      //Tree tree = sentence.get(TreeAnnotation.class);

	      // this is the Stanford dependency graph of the current sentence
	      SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
	      dependencyGraphs.add(dependencies);
	    }
	    
	    return dependencyGraphs;
	}
	
	private static Map<Integer, CorefChain> getCorefs(Annotation document) {
		/*TODO:
		 * Write your code here
		 * Hint: copy example from CoreNLP page..
		 *         it is one line, though you could do processing there if you wanted
		 *         
		 *         
		 * */
		
	    // This is the coreference link graph
	    // Each chain stores a set of mentions that link to each other,
	    // along with a method for getting the most representative mention
	    // Both sentence and token offsets start at 1!
	    Map<Integer, CorefChain> graph = 
	      document.get(CorefChainAnnotation.class);
	    
		return graph;
	}

	private static LinkedList<Event> getEvents(
			LinkedList<SemanticGraph> dependencyGraphs,
			Map<Integer, CorefChain> corefChains, 
			Dataset dataset) {
		
		LinkedList<Event> events = new LinkedList<Event>();
		List<Integer> sentencesIndex = new ArrayList<Integer>();	//contains the sentences that mention the protagonist
		List<String> subjectIndex = new ArrayList<String>();
		Object[] protagonists = dataset.getProtagonistNames().toArray();
		int sentenceNum = 0;
		
		Iterator<Entry<Integer, CorefChain>> entries = corefChains.entrySet().iterator();
		Boolean leaveLoop = false;
		
		while (entries.hasNext()) {
			Entry<Integer, CorefChain> entry = entries.next();

			//System.out.println(entry.getKey() + "/" + entry.getValue());
			for(int a = 0; a < protagonists.length; a++)
			{
				if ((entry.getValue().getRepresentativeMention().toString().toLowerCase()).contains(protagonists[a].toString()))
				{
					leaveLoop = true;
					String[] tokens = entry.getValue().getCorefMentions().toString().split(" ");
					for (int i = 0; i < tokens.length; i++){
						if (tokens[i].contains("sentence")){
							String sentenceNumber = tokens[i+1].substring(0, tokens[i+1].length()-1);
							sentencesIndex.add(Integer.parseInt(sentenceNumber));
						}
					}
					tokens = entry.getValue().getCorefMentions().toString().split("\"");
					for (int i = 0; i < tokens.length; i++)
					{

						if (i == 0){
							//String sentenceI = tokens[i].substring(2, tokens[i].length()-1);
							//subjectIndex.add(sentenceI);
						}
						else if (i == tokens.length - 1)
						{
							//String sentenceI = tokens[i].substring(1, tokens[i].length()-2);
							//subjectIndex.add(sentenceI);
						}
						else
						{
							String sentenceI = tokens[i].substring(0, tokens[i].length());
							subjectIndex.add(sentenceI);
						}
							
						
					}
				}
			}
			if (leaveLoop)
				break;
		}
		
		
		//This is an example loop to get you started, it is but one way to do things and adding coref will require more effort
		for(SemanticGraph sentenceDeps : dependencyGraphs)
		{
			sentenceNum++;
			//System.out.println(sentenceDeps);	//uncomment to see the dependency graph!
			
			//IndexedWord protagonistIW = sentenceDeps.getNodeByWordPattern(protagonist);
			
			/*			
			 * try {			
				for(IndexedWord dependency: sentenceDeps.getParentList(protagonistIW))
				{
					if (dependency.tag().contains("VB"))
					{
						Event event = new Event(dependency.word(), protagonist, true, true, false);
						events.add(event);
					}
				}
			}
			catch(Exception e){	}	*/


			
			try {			
				for (int i = 0; i < sentenceDeps.size(); i++)
				{
					IndexedWord dependency = sentenceDeps.getNodeByIndexSafe(i);
					if (dependency != null && dependency.tag().contains("VB")){
						for(IndexedWord possible: sentenceDeps.getChildList(dependency))
						{
							if (possible.tag().toString().contains("PR"))
							{
								for (int a = 0; a < sentencesIndex.size(); a++)
								{
									if (sentenceNum == sentencesIndex.get(a) && subjectIndex.contains(possible.word()))
									{
										Event event = new Event(possible.word(), dependency.word(), false, false, true);
										events.add(event);
										break;
									}
								}
							}
							else{
								for (int b = 0; b < protagonists.length; b++)
								{
									if (possible.tag().toString().contains("N") && protagonists[b].toString().equals(possible.word().toLowerCase()))
									{
										for (int a = 0; a < sentencesIndex.size(); a++)
										{
											if (sentenceNum == sentencesIndex.get(a))
											{
												Event event = new Event(dependency.word(), possible.word(), true, true, false);
												events.add(event);
												break;
											}
										}
									}//end if
								}//end for
							}//end else
						}
					}
				}
			}
			catch(Exception e){
			}		
						
	
			/*			
			String protagonistCoreference = dataset.getProtagonist();
			protagonistIW = sentenceDeps.getNodeByWordPattern("he");
			
			try {
				
				for(IndexedWord dependency: sentenceDeps.getParentList(protagonistIW)){
					Event event = new Event(dependency.word(), protagonist, false, false, true);
					events.add(event);
				}
			}
			catch(Exception e){
			}	
			System.out.println("");
			
			*/
		}	
		
		return events;
	}

	private static void writeEvents(String name, LinkedList<Event> events) {
		System.err.flush();
		System.out.flush();
		System.out.println();
		System.out.println("Name: " + name);
		System.out.println("Events: ");
		for(Event event : events){
			System.out.println(event);
		}
		System.out.println();
		System.out.flush();
		System.err.flush();
	}

	private static  LinkedList<String> chunkText(String text) {
		LinkedList<String> textChunks = new LinkedList<String>();
		if(text.length() < TEXT_CHUNK_SIZE){
			textChunks.add(text);
		} else {
			StringBuilder tempStr = new StringBuilder();
			for(String line : text.split("\n")){
				tempStr.append(line);
				tempStr.append("\n");
				if(tempStr.length() > TEXT_CHUNK_SIZE){
					textChunks.add(tempStr.toString());
					tempStr = new StringBuilder();
				}
			}
			textChunks.add(tempStr.toString());
		}
		return textChunks;
	}
}
