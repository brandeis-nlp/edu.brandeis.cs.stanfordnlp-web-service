package edu.brandeis.lapps.stanford.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;

import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 *
 * @author Chunqi SHI (shicq@cs.brandeis.edu)
 * @author Keigh Rim (krim@brandeis.edu)
 * @since 2014-01-31
 *
 */
public class NamedEntityRecognizer extends AbstractStanfordCoreNLPWebService {

    private static String TOOL_DESCRIPTION = "This service is a wrapper around Stanford CoreNLP 3.3.1 providing a named entity recognition service" +
            "\nInternally it uses CoreNLP default \"tokenize\", \"ssplit\", \"pos\", \"lemma\", \"ner\" annotators as one pipeline.";

    public NamedEntityRecognizer() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT,
                PROP_POS_TAG, PROP_LEMMA, PROP_NER);
    }

    @Override
    public String execute(Container container) {

        String text = container.getText();
        View view = container.newView();
        setUpContainsMetadata(view);

        int id = -1;
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : sents) {
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String label = token.ner();
                if(label != null && !label.equalsIgnoreCase("O")) {
                    label = label.toLowerCase();
                    String type = Uri.NE;
                    Annotation ann = new Annotation(NE_ID + (++id), type, label,
                            token.beginPosition(), token.endPosition());
                    ann.addFeature("category", label);
                    ann.addFeature("word", token.value());
                    view.addAnnotation(ann);
                }
            }
        }
        // set discriminator to LIF
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    @Override
    ServiceMetadata loadMetadata() {
        ServiceMetadata metadata = this.setCommonMetadata();
        metadata.setDescription(TOOL_DESCRIPTION);
        metadata.getProduces().addAnnotations(Uri.NE);
        metadata.getProduces().addTagSet(Uri.NE, Uri.TAGS_NER_STANFORD);

        return metadata;
    }
}
