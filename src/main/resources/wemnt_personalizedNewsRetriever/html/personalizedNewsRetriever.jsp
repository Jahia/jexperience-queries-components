<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="wem" uri="http://www.jahia.org/tags/wem" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:set value="${not (renderContext.editModeConfigName eq 'studiomode' or renderContext.editModeConfigName eq 'studiovisualmode')}" var="isNotStudio"/>

<c:if test="${isNotStudio}">
    <c:set var="maxNews" value="${currentNode.properties['maxNews'].long}"/>
    <c:set var="newsDateLastDays" value="${currentNode.properties['newsDateLastDays'].long}"/>
    <c:set var="lastNewsIds" value="${wem:retrieveLastContents(renderContext, renderContext.site.siteKey, newsDateLastDays, 'jnt:news', 'date')}"/>

    <h2>${currentNode.properties['jcr:title'].string}</h2>

    <c:choose>
        <c:when test="${functions:length(lastNewsIds) != 0}">
            <c:forEach items="${lastNewsIds}" var="lastNewsId" end="${maxNews - 1}">
                <jcr:node var="lastNewsNode" uuid="${lastNewsId}"/>
                <template:module node="${lastNewsNode}"/>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <span><fmt:message key="personalizedNewsRetriever.upToDate.label"/></span>
        </c:otherwise>
    </c:choose>
</c:if>
