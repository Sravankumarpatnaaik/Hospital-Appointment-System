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
    <c:set var="saveAction" value="/admin/doctors"/>
    <c:if test="${not empty doctorId}"><c:set var="saveAction" value="/admin/doctors/${doctorId}"/></c:if>
    <form class="panel" method="post" action="${saveAction}">
        <label>Full name
            <input name="fullName" value="${form.fullName}" maxlength="80" required>
        </label>
        <label>Email
            <input type="email" name="email" value="${form.email}" maxlength="120" required>
        </label>
        <label>Specialization
            <select name="specialization" required>
                <c:forEach var="name" items="${specializations}">
                    <option value="${name}" ${name eq form.specialization ? 'selected' : ''}><c:out value="${name}"/></option>
                </c:forEach>
            </select>
        </label>
        <label>Room
            <input name="room" value="${form.room}" maxlength="16" required>
        </label>
        <label>Password
            <input type="password" name="password" minlength="8" ${empty doctorId ? 'required' : ''} placeholder="${empty doctorId ? '' : 'Leave blank to keep the current password'}">
        </label>
        <label><input type="checkbox" name="active" value="true" ${form.active ? 'checked' : ''} style="width:auto; display:inline-block; margin-right:0.4rem;"> Taking appointments</label>
        <button class="solid" type="submit">${empty doctorId ? 'Add doctor' : 'Save changes'}</button>
    </form>
    <c:if test="${canDelete}">
        <form method="post" action="/admin/doctors/${doctorId}/delete" data-confirm="Delete this doctor record?">
            <button class="linkish" type="submit">Delete this doctor</button>
        </form>
    </c:if>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
