/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.personalizednewsretriever.tag;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.modules.marketingfactory.admin.ContextServerService;
import org.jahia.modules.marketingfactory.tag.WemFunctions;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.Hit;
import org.jahia.services.search.JCRNodeHit;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.SearchResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class PnrFunctions {
    private static Logger logger = LoggerFactory.getLogger(PnrFunctions.class);

    private static ContextServerService contextServerService;
    private static String ipForwardingHeaderName;

    public static Map<String, Object> retrieveLastContents(RenderContext renderContext, String siteKey, long numberOfPastDays, String nodeType, String datePropertyName, boolean hideViewedContents) throws RepositoryException, IOException, JSONException {
        Set<String> viewedContents = new HashSet<>();
        if (hideViewedContents) {
            viewedContents = getViewedContents(renderContext.getRequest(), siteKey, numberOfPastDays, nodeType);
        }

        List<Hit<?>> hits = getDXContents(renderContext, siteKey, numberOfPastDays, nodeType, datePropertyName);
        Set<String> lastContents = getLastContents(viewedContents, hits);

        JCRNodeWrapper currentNode = renderContext.getMainResource().getNode();
        if (currentNode.isNodeType(nodeType)) {
            lastContents.remove(currentNode.getIdentifier());
        }

        Set<String> contents = new LinkedHashSet<>();
        Map<String, Set<String>> tags = new HashMap<>();

        Map<String, Long> tagsAggregation = WemFunctions.getTagsAggregation(renderContext.getRequest(), siteKey, numberOfPastDays);

        for (String tag : tagsAggregation.keySet()) {
            for (String contentId : lastContents) {
                JCRNodeWrapper content = renderContext.getSite().getSession().getNodeByUUID(contentId);

                if (content.hasProperty(Constants.TAG_LIST)) {
                    JCRValueWrapper[] tagList = content.getProperty(Constants.TAG_LIST).getValues();

                    boolean tagExist = Arrays.stream(tagList).anyMatch(x -> {
                        try {
                            return x.getString().equals(tag);
                        } catch (RepositoryException e) {
                            logger.error("Unable to read the value of JCR object", e);
                        }

                        return false;
                    });

                    if (tagExist) {
                        Set<String> t = tags.get(contentId) != null ? tags.get(contentId) : new LinkedHashSet<>();
                        t.add(tag);
                        tags.put(contentId, t);

                        contents.add(contentId);
                    }
                }
            }
        }

        contents.addAll(lastContents);

        Map<String, Object> result = new HashMap<>();
        result.put("contents", contents);
        result.put("tags", tags);

        return result;
    }

    private static Set<String> getViewedContents(HttpServletRequest httpServletRequest, String siteKey, long numberOfPastDays, String nodeType) throws JSONException, IOException {
        String url = "/cxs/query/event/target.itemId?optimizedQuery=true";
        String profileId = contextServerService.getProfileId(httpServletRequest, siteKey);

        if (numberOfPastDays <= 0) {
            numberOfPastDays = 30;
        }

        JSONObject timeStampPropertyConditionParameters = new JSONObject();
        timeStampPropertyConditionParameters.put("propertyName", "timeStamp");
        timeStampPropertyConditionParameters.put("propertyValueDateExpr", "now-" + numberOfPastDays + "d");
        timeStampPropertyConditionParameters.put("comparisonOperator", "greaterThanOrEqualTo");

        JSONObject timeStampPropertyCondition = new JSONObject();
        timeStampPropertyCondition.put("type", "eventPropertyCondition");
        timeStampPropertyCondition.put("parameterValues", timeStampPropertyConditionParameters);

        JSONObject profileIdPropertyConditionParameters = new JSONObject();
        profileIdPropertyConditionParameters.put("propertyName", "profileId");
        profileIdPropertyConditionParameters.put("propertyValue", profileId != null ? profileId : "");
        profileIdPropertyConditionParameters.put("comparisonOperator", "equals");

        JSONObject profileIdPropertyCondition = new JSONObject();
        profileIdPropertyCondition.put("type", "eventPropertyCondition");
        profileIdPropertyCondition.put("parameterValues", profileIdPropertyConditionParameters);

        JSONObject nodeTypePropertyConditionParameters = new JSONObject();
        nodeTypePropertyConditionParameters.put("propertyName", "target.properties.pageInfo.nodeType");
        nodeTypePropertyConditionParameters.put("propertyValue", nodeType);
        nodeTypePropertyConditionParameters.put("comparisonOperator", "equals");

        JSONObject nodeTypePropertyCondition = new JSONObject();
        nodeTypePropertyCondition.put("type", "eventPropertyCondition");
        nodeTypePropertyCondition.put("parameterValues", nodeTypePropertyConditionParameters);

        JSONObject isContentTemplatePropertyConditionParameters = new JSONObject();
        isContentTemplatePropertyConditionParameters.put("propertyName", "target.properties.pageInfo.isContentTemplate");
        isContentTemplatePropertyConditionParameters.put("propertyValue", "true");
        isContentTemplatePropertyConditionParameters.put("comparisonOperator", "equals");

        JSONObject isContentTemplatePropertyCondition = new JSONObject();
        isContentTemplatePropertyCondition.put("type", "eventPropertyCondition");
        isContentTemplatePropertyCondition.put("parameterValues", isContentTemplatePropertyConditionParameters);

        JSONObject booleanConditionParameterValues = new JSONObject();
        booleanConditionParameterValues.put("operator", "and");
        booleanConditionParameterValues.put("subConditions", new JSONArray(Arrays.asList(profileIdPropertyCondition, nodeTypePropertyCondition, timeStampPropertyCondition, isContentTemplatePropertyCondition)));

        JSONObject booleanCondition = new JSONObject();
        booleanCondition.put("type", "booleanCondition");
        booleanCondition.put("parameterValues", booleanConditionParameterValues);

        JSONObject aggregateQuery = new JSONObject();
        aggregateQuery.put("condition", booleanCondition);

        Map<String, Long> result = contextServerService.executePostRequest(siteKey, url, aggregateQuery.toString(), null, getHeaders(httpServletRequest), new HashMap<>().getClass());

        result.remove("_filtered");
        result.remove("_missing");
        result.remove("_all");

        return result.keySet();
    }

    private static List<Hit<?>> getDXContents(RenderContext renderContext, String siteKey, long numberOfPastDays, String nodeType, String datePropertyName) {
        Calendar calendar = Calendar.getInstance();
        SearchCriteria.DateValue dateValue = new SearchCriteria.DateValue();
        dateValue.setType(SearchCriteria.DateValue.Type.RANGE);
        dateValue.setToAsDate(calendar.getTime());
        calendar.add(Calendar.DATE, (int) -numberOfPastDays);
        dateValue.setFromAsDate(calendar.getTime());

        SearchCriteria.NodeProperty dateProperty = new SearchCriteria.NodeProperty();
        dateProperty.setNodeType(nodeType);
        dateProperty.setName(datePropertyName);
        dateProperty.setType(SearchCriteria.NodeProperty.Type.DATE);
        dateProperty.setDateValue(dateValue);

        Map<String, SearchCriteria.NodeProperty> mapProperties = new HashMap<>();
        mapProperties.put(datePropertyName, dateProperty);

        Map<String, Map<String, SearchCriteria.NodeProperty>> properties = new HashMap<>();
        properties.put(nodeType, mapProperties);

        List<SearchCriteria.Ordering> orderings = new ArrayList<>();
        SearchCriteria.Ordering ordering = new SearchCriteria.Ordering();
        ordering.setPropertyName(datePropertyName);
        ordering.setOperand(SearchCriteria.Ordering.Operand.PROPERTY);
        ordering.setOrder(SearchCriteria.Ordering.Order.DESCENDING);
        orderings.add(ordering);

        SearchCriteria.CommaSeparatedMultipleValue sites = new SearchCriteria.CommaSeparatedMultipleValue();
        sites.setValue(siteKey);

        SearchCriteria criteria = new SearchCriteria();
        criteria.setOriginSiteKey(siteKey);
        criteria.setNodeType(nodeType);
        criteria.setProperties(properties);
        criteria.setOrderings(orderings);
        criteria.setSites(sites);

        SearchResponse searchResponse = ServicesRegistry.getInstance().getSearchService().search(criteria, renderContext);
        return searchResponse.getResults();
    }

    private static Set<String> getLastContents(Set<String> viewedContents, List<Hit<?>> hits) throws RepositoryException {
        Set<String> lastContents = new LinkedHashSet<>();

        for (Hit<?> hit : hits) {
            String nodeId = null;

            if (hit instanceof JCRNodeHit) {
                nodeId = ((JCRNodeHit) hit).getDisplayableNode().getIdentifier();
            } else if (hit.getClass().getName().equals("org.jahia.modules.elasticsearch.search.ESHit")) {
                // As we would not add a dependency to the module elasticsearch-search-provider and
                // we don't have any way to get the hit identifier, so we use the java reflection to
                // get access to the id Field
                //
                // Improvement ticket was created to fix the issue of retrieve hit id:
                // https://jira.jahia.org/browse/QA-10986

                try {
                    Field id = hit.getRawHit().getClass().getDeclaredField("id");
                    id.setAccessible(true);

                    nodeId = id.get(hit.getRawHit()).toString();
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            lastContents.add(nodeId);
        }
        lastContents.removeAll(viewedContents);

        return lastContents;
    }

    private static Map<String, String> getHeaders(HttpServletRequest httpServletRequest) {
        Map<String, String> headers = new HashMap<>();
        String xff = httpServletRequest.getHeader(ipForwardingHeaderName);
        if (StringUtils.isNotBlank(xff)) {
            headers.put("X-Forwarded-For", xff);
            logger.debug("X-Forwarded-For header value set to " + xff);
        }
        headers.put("User-Agent", httpServletRequest.getHeader("User-Agent"));
        return headers;
    }

    public void setContextServerService(ContextServerService contextServerService) {
        PnrFunctions.contextServerService = contextServerService;
    }

    public void setIpForwardingHeaderName(String ipForwardingHeaderName) {
        PnrFunctions.ipForwardingHeaderName = ipForwardingHeaderName;
    }
}
