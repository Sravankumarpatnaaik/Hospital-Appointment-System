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
    <h1>Move this visit</h1>
    <p class="lede"><c:out value="${visit.title()}"/> · <c:out value="${visit.meta()}"/>. Currently <c:out value="${visit.when()}"/>. <c:out value="${visit.reason()}"/></p>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <div class="days">
        <c:forEach var="chip" items="${days}">
            <a class="${chip.selected() ? 'on' : ''}" href="/patient/appointments/${visit.id()}?day=${chip.value()}"><c:out value="${chip.label()}"/></a>
        </c:forEach>
    </div>
    <form class="panel" method="post" action="/patient/appointments/${visit.id()}">
        <h2><c:out value="${dayLabel}"/></h2>
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
