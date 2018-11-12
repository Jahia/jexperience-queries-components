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
<%@ taglib prefix="pqc" uri="http://www.jahia.org/tags/pqc" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="ml/vendor/tf.js"/>
<template:addResources type="javascript" resources="ml/machine-learning-poc.js"/>

<c:set var="result" value="${pqc:getViewedPage(renderContext, renderContext.site.siteKey)}"/>

<script type="text/javascript">
    let items = [];
    <c:forEach items="${result}" var="r">
        <jcr:node var="node" uuid="${r.id}"/>

        items.push(
            <json:object>
                <json:property name="id" value="${r.id}"/>
                <json:property name="nView" value="${r.nView}"/>
                <json:property name="nTags" value="${r.nTags}"/>
                <json:property name="nCategories" value="${r.nCategories}"/>
                <json:property name="nTotal" value="${r.nTotal}"/>
                <json:property name="url" value="${node.url}"/>
                <json:property name="name" value="${node.name}"/>
            </json:object>
        );
    </c:forEach>

    ml.init(items);
</script>

<h2>ML - ${currentNode.properties['jcr:title'].string}</h2>

<div id="loading" style="display: block;">
    <h4>Waiting for ML...</h4>
</div>

<div id="result" style="display: none;">
</div>
