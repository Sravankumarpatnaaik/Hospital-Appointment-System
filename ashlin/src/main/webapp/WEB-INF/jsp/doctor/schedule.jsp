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
    <h1>Your clinic</h1>
    <p class="lede"><c:out value="${doctor.specialization()}"/> · Room <c:out value="${doctor.room()}"/>. Patients book these hours themselves. A taken hour cannot be given to a second person.</p>
    <%@ include file="/WEB-INF/jsp/fragments/banners.jsp" %>
    <c:forEach var="day" items="${days}">
        <section class="panel">
            <h2><c:out value="${day.label()}"/></h2>
            <c:if test="${empty day.visits()}"><p class="empty">No one booked.</p></c:if>
            <c:forEach var="visit" items="${day.visits()}">
                <article class="visit">
                    <div>
                        <strong><c:out value="${visit.when()}"/></strong>
                        <div><c:out value="${visit.title()}"/></div>
                        <div class="meta"><c:out value="${visit.meta()}"/></div>
                        <p><c:out value="${visit.reason()}"/></p>
                    </div>
                </article>
            </c:forEach>
        </section>
    </c:forEach>
</main>
<%@ include file="/WEB-INF/jsp/fragments/foot.jsp" %>
</body>
</html>
