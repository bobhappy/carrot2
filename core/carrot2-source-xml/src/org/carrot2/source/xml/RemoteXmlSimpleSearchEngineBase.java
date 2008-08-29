package org.carrot2.source.xml;

import java.util.List;

import javax.xml.transform.Templates;

import org.carrot2.core.*;
import org.carrot2.source.SearchEngineResponse;
import org.carrot2.source.SimpleSearchEngine;
import org.carrot2.util.resource.ClassResource;

/**
 * A base class for implementing data sources based on XML/XSLT. The XSLT stylesheet will
 * be loaded once during component initialization and cached for all further requests.
 */
public abstract class RemoteXmlSimpleSearchEngineBase extends SimpleSearchEngine
{
    /** A helper class that groups common functionality for XML/XSLT based data sources. */
    private final XmlDocumentSourceHelper xmlDocumentSourceHelper = new XmlDocumentSourceHelper();

    /** XSLT transformation to Carrot2 DTD */
    private Templates toCarrot2Xslt;

    @Override
    public void init()
    {
        super.init();
        toCarrot2Xslt = xmlDocumentSourceHelper.loadXslt(getXsltResource());
    }

    @Override
    public void beforeProcessing() throws ProcessingException
    {
        super.beforeProcessing();
        if (toCarrot2Xslt == null)
        {
            throw new ProcessingException("XSLT stylesheet must not be null");
        }
    }

    @Override
    protected SearchEngineResponse fetchSearchResponse() throws Exception
    {
        final String serviceURL = buildServiceUrl();
        final SearchEngineResponse response = new SearchEngineResponse();

        final ProcessingResult processingResult = xmlDocumentSourceHelper
            .loadProcessingResult(serviceURL, toCarrot2Xslt, null, response.metadata);

        final List<Document> documents = processingResult.getDocuments();
        if (documents != null)
        {
            response.results.addAll(documents);
            response.metadata.put(SearchEngineResponse.RESULTS_TOTAL_KEY, response
                .getResultsTotal() > 0 ? response.getResultsTotal() : (long) documents
                .size());
        }
        else
        {
            response.metadata.put(SearchEngineResponse.RESULTS_TOTAL_KEY, 0L);
        }

        afterFetch(response);
        
        return response;
    }

    /**
     * Returns the XSLT stylesheet that transforms the custom XML into Carrot2 compliant
     * XML. This method will be called once during component initialization.
     * Initialization time attributes will have been bound before the call to this method.
     */
    protected abstract ClassResource getXsltResource();

    /**
     * Builds the URL from which XML stream will be fetched. This method will be called
     * once per request processing cycle. Processing-time attributes will have been bound
     * before this method the call to this method.
     */
    protected abstract String buildServiceUrl();
}
