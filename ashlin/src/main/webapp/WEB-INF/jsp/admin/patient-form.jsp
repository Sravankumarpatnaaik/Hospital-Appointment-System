<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="/WEB-INF/jsp/fragments/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jsp" %>
<main class="wrap narrow">
    <h1><c:out value="${title}"/></h1>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <c:set var="saveAction" value="/admin/patients"/>
    <c:if test="${not empty patientId}"><c:set var="saveAction" value="/admin/patients/${patientId}"/></c:if>
    <form class="panel" method="post" action="${saveAction}">
        <label>Full name
            <input name="fullName" value="${form.fullName}" maxlength="80" required>
        </label>
        <label>Email
            <input type="email" name="email" value="${form.email}" maxlength="120" required>
        </label>
        <label>Phone
            <input name="phone" value="${form.phone}" maxlength="20" required>
        </label>
        <label>Date of birth
            <input type="date" name="dateOfBirth" value="${form.dateOfBirth}" required>
        </label>
        <label>Password
            <input type="password" name="password" minlength="8" ${empty patientId ? 'required' : ''} placeholder="${empty patientId ? '' : 'Leave blank to keep the current password'}">
        </label>
        <button class="solid" type="submit">${empty patientId ? 'Add patient' : 'Save changes'}</button>
    </form>
    <c:if test="${canDelete}">
        <form method="post" action="/admin/patients/${patientId}/delete" data-confirm="Delete this patient record?">
            <button class="linkish" type="submit">Delete this patient</button>
        </form>
    </c:if>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
