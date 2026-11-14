<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="/WEB-INF/jsp/fragments/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jsp" %>
<main class="wrap">
    <h1><c:out value="${title}"/></h1>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <c:if test="${empty visit}">
        <form class="filters" method="get" action="/admin/appointments/new">
            <label>Doctor
                <select name="doctorId">
                    <c:forEach var="item" items="${doctors}">
                        <option value="${item.id()}" ${item.id() eq doctor.id() ? 'selected' : ''}><c:out value="${item.name()}"/> · <c:out value="${item.specialization()}"/></option>
                    </c:forEach>
                </select>
            </label>
            <button class="solid" type="submit">Show hours</button>
        </form>
    </c:if>
    <c:if test="${not empty visit}">
        <p class="lede"><c:out value="${visit.title()}"/> · currently <c:out value="${visit.when()}"/></p>
    </c:if>
    <div class="days">
        <c:forEach var="chip" items="${days}">
            <c:set var="boardPath" value="/admin/appointments/new"/>
            <c:if test="${not empty visit}"><c:set var="boardPath" value="/admin/appointments/${visit.id()}"/></c:if>
            <c:url var="dayLink" value="${boardPath}">
                <c:param name="day" value="${chip.value()}"/>
                <c:param name="doctorId" value="${doctor.id()}"/>
            </c:url>
            <a class="${chip.selected() ? 'on' : ''}" href="${dayLink}"><c:out value="${chip.label()}"/></a>
        </c:forEach>
    </div>
    <c:set var="saveAction" value="/admin/appointments"/>
    <c:if test="${not empty visit}"><c:set var="saveAction" value="/admin/appointments/${visit.id()}"/></c:if>
    <form class="panel" method="post" action="${saveAction}">
        <input type="hidden" name="doctorId" value="${doctor.id()}">
        <c:choose>
            <c:when test="${empty visit}">
                <label>Patient
                    <select name="patientId" required>
                        <option value="">Choose</option>
                        <c:forEach var="patient" items="${patients}">
                            <option value="${patient.id()}" ${patient.id() eq patientId ? 'selected' : ''}><c:out value="${patient.fullName()}"/></option>
                        </c:forEach>
                    </select>
                </label>
            </c:when>
            <c:otherwise>
                <input type="hidden" name="patientId" value="${patientId}">
            </c:otherwise>
        </c:choose>
        <label>Reason
            <textarea name="reason" minlength="3" maxlength="200" required><c:out value="${reason}"/></textarea>
        </label>
        <h2><c:out value="${doctor.name()}"/> · <c:out value="${dayLabel}"/></h2>
        <div class="slots">
            <c:forEach var="slot" items="${slots}">
                <c:choose>
                    <c:when test="${slot.state() eq 'open'}">
                        <button type="submit" name="startsAt" value="${slot.value()}"><c:out value="${slot.label()}"/></button>
                    </c:when>
                    <c:otherwise>
                        <span class="${slot.state()}"><c:out value="${slot.label()}"/></span>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
    </form>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
