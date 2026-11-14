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
    <h1><c:out value="${doctor.name()}"/></h1>
    <p class="lede"><c:out value="${doctor.specialization()}"/> · Room <c:out value="${doctor.room()}"/>. Write the reason, then choose an open hour. Taken hours stay on the grid so you can see the book.</p>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <div class="days">
        <c:forEach var="chip" items="${days}">
            <a class="${chip.selected() ? 'on' : ''}" href="/patient/doctors/${doctor.id()}?day=${chip.value()}"><c:out value="${chip.label()}"/></a>
        </c:forEach>
    </div>
    <form class="panel" method="post" action="/patient/doctors/${doctor.id()}">
        <label>Reason for the visit
            <textarea name="reason" minlength="3" maxlength="200" required><c:out value="${reason}"/></textarea>
        </label>
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
        <p class="legend"><span><i class="open"></i>Open</span><span><i class="taken"></i>Taken</span><span><i class="mine"></i>Yours</span></p>
    </form>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
