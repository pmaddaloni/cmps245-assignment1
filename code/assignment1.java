import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
		
//		TODO: Once you have "jack and jill" running, add the following:
		//datasets.add(new Dataset("news"));
		//datasets.add(new Dataset("salinger_wiki_full"));
		//datasets.add(new Dataset("indiana_jones_full"));
		//datasets.add(new Dataset("forums_bush"));
		//datasets.add(new Dataset("forums_kerry"));
		
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
		
		/*TODO:
		 * Write your code here
		 * This is where the magic happens
		 * Hint: see loop. 
		 *          Use code completion (ctrl+space in eclipse)
		 *          Use a debugger!
		 *          Print things out using System.out.println(foo)
		 *          Use sentence and token indices to uniquely identify words.
		 *          Check out the javadocs online.
		 * */
		
		
		//This is an example loop to get you started, it is but one way to do things and adding coref will require more effort
		for(SemanticGraph sentenceDeps : dependencyGraphs){
			System.out.println(sentenceDeps);
			//System.out.println("Node 3: "+sentenceDeps.getNodeByIndex(3)); //Has all sorts of info about this "word"
			
			String protagonist = dataset.getProtagonist();
			
			for(IndexedWord dependency : sentenceDeps.getAllNodesByWordPattern(protagonist)){
				
				Event event = new Event(dependency.word(), dependency.docID(), true, true, false);
				events.add(event);
			}
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
