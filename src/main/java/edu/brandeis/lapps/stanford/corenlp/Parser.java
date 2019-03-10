package edu.brandeis.lapps.stanford.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import java.util.*;

import static org.lappsgrid.discriminator.Discriminators.Uri;
import static org.lappsgrid.vocabulary.Features.PhraseStructure;
import static org.lappsgrid.vocabulary.Features.Token;

public class Parser extends AbstractStanfordCoreNLPWebService {

    private static String TOOL_DESCRIPTION = "This service is a wrapper around Stanford CoreNLP 3.3.1 providing a phrase structure parser service" +
                    "\nInternally it uses CoreNLP default \"tokenize\", \"ssplit\", \"parse\" annotators as one pipeline.";

    public Parser() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_PARSE);
    }

    @Override
    protected String execute(Container container) {

        String text = container.getText();
        View view = container.newView();
        setUpContainsMetadata(view, "stanford");

        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);

        int sid = 1;
        for (CoreMap sent : sents) {

            Map<String, String> childToParent = new HashMap<>();

            // first, populate tokens
            Map<String, String> tokenIndex = new HashMap<>();
            int tid = 1; // from 1 because Tree.indexLeaves() (in later) will start from 1
            List<CoreLabel> tokens = sent.get(TokensAnnotation.class);
            for (CoreLabel token : tokens) {
                String tokenId = String.format("%s%d_%d", TOKEN_ID, sid, tid++);
                tokenIndex.put(token.word(), tokenId);
                Annotation ann = view.newAnnotation(tokenId,
                        Uri.TOKEN, token.beginPosition(), token.endPosition());
                ann.addFeature(Token.POS, token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                ann.addFeature("word", token.value());
            }

            // then populate constituents.
            // constituents indexed left-right breadth-first
            // leaves are indexed by stanford
            int cid = 1;
            int nextNonTerminal = cid + 1; // because the first non terminal is always the root.
            Annotation ps = view.newAnnotation(PS_ID + sid, Uri.PHRASE_STRUCTURE,
                    sent.get(CharacterOffsetBeginAnnotation.class),
                    sent.get(CharacterOffsetEndAnnotation.class));
            Tree root = sent.get(TreeAnnotation.class);
            root.indexLeaves(true); // index all tokens, starting from 1
            root.setSpans(); // token offsets for all nodes (it starts from 0 and inclusive on both sides)
            Queue<Tree> queue = new LinkedList<>();
            queue.add(root);
            // as the ROOT always enqued first, it will get the first cid.
            ps.addFeature(PhraseStructure.ROOT, String.format("%s%d_%d", CONSTITUENT_ID, sid, cid));
            ps.addFeature("sentence", sent.toString());
            ps.addFeature("penntree", root.pennString());
            List<String> allConstituents = new LinkedList<>();
            while (!queue.isEmpty()) {
                Tree cur = queue.remove();
                if (!cur.isLeaf()) {
                    String curID = String.format("%s%d_%d", CONSTITUENT_ID, sid, cid++);
                    allConstituents.add(curID);
                    IntPair curSpan = cur.getSpan(); // this is token offsets, not characters
                    Annotation constituent = view.newAnnotation( curID, Uri.CONSTITUENT,
                            tokens.get(curSpan.getSource()).beginPosition(),
                            tokens.get(curSpan.getTarget()).endPosition());
                    // TODO: 2/22/2018 top-level "label" will go away after LIF JSON scheme 1.1.0
                    String constituentLabel = cur.label().value();
                    constituent.setLabel(constituentLabel);
                    ArrayList<String> childrenIDs = new ArrayList<>();

                    for (Tree child : cur.getChildrenAsList()) {
                        queue.add(child);
                        String childID = child.isLeaf() ?
                                String.format("%s%d_%d", TOKEN_ID, sid, ((CoreLabel) child.label()).index())
                                : String.format("%s%d_%d", CONSTITUENT_ID, sid, nextNonTerminal++);
                        childToParent.put(childID, curID);
                        childrenIDs.add(childID);
                    }
                    constituent.addFeature(Features.Constituent.CHILDREN, childrenIDs);
                    constituent.addFeature(Features.Constituent.PARENT, childToParent.get(curID));
                    // as of LIF JSON scheme 1.1.0, all the "label"-ish go into the features map
                    constituent.addFeature(Features.Constituent.LABEL, constituentLabel);
                }
            }
            ps.addFeature(PhraseStructure.CONSTITUENTS, allConstituents);
            sid++;
        }

        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    @Override
    protected ServiceMetadata loadMetadata() {
        ServiceMetadata metadata = setCommonMetadata();
        metadata.setDescription(TOOL_DESCRIPTION);
        metadata.getProduces().addAnnotations(Uri.CONSTITUENT, Uri.POS, Uri.PHRASE_STRUCTURE);
        metadata.getProduces().addTagSet(Uri.POS, Uri.TAGS_POS_PENNTB);
        metadata.getProduces().addTagSet(Uri.PHRASE_STRUCTURE, Uri.TAGS_CAT_PENNTB);

        return metadata;
    }
}
