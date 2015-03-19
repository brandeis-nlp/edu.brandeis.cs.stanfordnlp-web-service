package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.ICoreference;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

public class Coreference extends AbstractStanfordCoreNLPWebService implements
		ICoreference {

	public Coreference() {
		this.init(PROP_TOKENIZE,PROP_SENTENCE_SPLIT,PROP_POS_TAG,PROP_CORERENCE);
	}

    @Override
    public String execute(LIFJsonSerialization json) throws StanfordWebServiceException {
        return null;
    }

    @Override
    public String getMetadata() {
        return null;
    }
//
//
//	@Override
//	public Data execute(Data input) {
//
//		Data ret = null;
//		long inputType = input.getDiscriminator();
//		if (inputType == Types.ERROR)
//		{
//			return input;
//		}
//	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//	    // Requires Text?
//
//	    Annotation document = new Annotation(input.getPayload());
//	    pipeline.annotate(document);
//	    Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
//	    ProcessingStep step = new ProcessingStep();
//        putFeature(step.getMetadata(), Metadata.PRODUCED_BY, "Stanford NER");
////	    pipeline.prettyPrint(d, os);
//
//
//
////	    pipeline.
////	    return data;
//	    return null;
//	}

	// public static void main(String[]args) {
	// // creates a StanfordCoreNLP object, with POS tagging, lemmatization,
	// NER, parsing, and coreference resolution
	// Properties props = new Properties();
	// props.put("annotators",
	// "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	// StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	//
	// // read some text in the text variable
	// String text = ""; // Add your text here!
	//
	// // create an empty Annotation just with the given text
	// Annotation document = new Annotation(text);
	//
	// // run all Annotators on this text
	// pipeline.annotate(document);
	//
	// // these are all the sentences in this document
	// // a CoreMap is essentially a Map that uses class objects as keys and has
	// values with custom types
	// List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	//
	// for(CoreMap sentence: sentences) {
	// // traversing the words in the current sentence
	// // a CoreLabel is a CoreMap with additional token-specific methods
	// for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	// // this is the text of the token
	// String word = token.get(TextAnnotation.class);
	// // this is the POS tag of the token
	// String pos = token.get(PartOfSpeechAnnotation.class);
	// // this is the NER label of the token
	// String ne = token.get(NamedEntityTagAnnotation.class);
	// }
	//
	// // this is the parse tree of the current sentence
	// Tree tree = sentence.get(TreeAnnotation.class);
	//
	// // this is the Stanford dependency graph of the current sentence
	// SemanticGraph dependencies =
	// sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
	// }
	//
	// // This is the coreference link graph
	// // Each chain stores a set of mentions that link to each other,
	// // along with a method for getting the most representative mention
	// // Both sentence and token offsets start at 1!
	// Map<Integer, CorefChain> graph =
	// document.get(CorefChainAnnotation.class);
	// }

}
