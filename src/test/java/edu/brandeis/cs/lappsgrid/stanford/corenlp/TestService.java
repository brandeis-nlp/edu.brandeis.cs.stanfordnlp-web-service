package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * Created by Chunqi SHI (shicq@cs.brandeis.edu) on 3/5/14.
 */
public class TestService {

    AbstractStanfordCoreNLPWebService service;

    protected Container wrapContainer(String plainText) {
        Container container = new Container();
        container.setText(plainText);
        container.setLanguage("en");
        // return empty metadata for process result (for now)
//        Data metadata = Serializer.parse(service.getMetadata(), Data.class);
//        container.setMetadata((Map) metadata.getPayload());
        return container;
    }

    protected Container reconstructPayload(String json) {
        Container cont = new Container(
                (Map) Serializer.parse(json, Data.class).getPayload());
        // TODO 151022 this will be redundant when @context stuff sorted out
        cont.setContext(Container.REMOTE_CONTEXT);
        return cont;
    }

    public ServiceMetadata testCommonMetadata() {
        System.out.println(service.getMetadata());
        String json = service.getMetadata();
        assertNotNull(service.getClass().getName() + ".getMetadata() returned null", json);

        Data data = Serializer.parse(json, Data.class);
        assertNotNull("Unable to parse metadata json.", data);
        assertNotSame(data.getPayload().toString(), Uri.ERROR, data.getDiscriminator());

        ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());

        assertEquals("Vendor is not correct", "http://www.cs.brandeis.edu/", metadata.getVendor());
        assertEquals("Name is not correct", service.getClass().getName(), metadata.getName());
        assertEquals("Version is not correct.", Version.getVersion(), metadata.getVersion());
        assertEquals("License is not correct", Uri.APACHE2, metadata.getLicense());

        IOSpecification produces = metadata.getProduces();
        assertEquals("Produces encoding is not correct", "UTF-8", produces.getEncoding());
        assertEquals("Too many output formats", 1, produces.getFormat().size());
        assertEquals("LIF not produces", Uri.LAPPS, produces.getFormat().get(0));

        IOSpecification requires = metadata.getRequires();
        assertEquals("Requires encoding is not correct", "UTF-8", requires.getEncoding());
        List<String> list = requires.getFormat();
        assertTrue("LIF format not accepted.", list.contains(Uri.LAPPS));
        assertTrue("Text not accepted", list.contains(Uri.TEXT));
        list = requires.getAnnotations();
        assertEquals("Required annotations should be empty", 0, list.size());

        // return json for additional tests
        return metadata;
    }
}
